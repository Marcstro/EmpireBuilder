package pathfinding;

import empirebuilder.Point;

import java.util.*;

/**
 * Shared path cache for PathfindingSystemB.
 *
 * PRIMARY CACHE
 *   (originCellIdx, destCellIdx) → PathB
 *   Encoded as a single long: (originCellIdx << 32) | destCellIdx
 *
 * SUB-SEGMENT CACHING
 *   When A* produces a path through cells A → B → C → D the cache stores:
 *     (A,D)  full path
 *     (B,D)  path from first tile in B onward
 *     (C,D)  path from first tile in C onward
 *   Units that start in B or C get an instant cache hit next time.
 *
 * CELL REVERSE-INDEX
 *   cellToPathsIndex: cellIdx → List<PathB>
 *   Used for instant invalidation when a cell's terrain changes.
 *
 * TTL EVICTION
 *   Call maybeEvict(currentTick) periodically (PathfindingSystemB calls it
 *   every EVICT_INTERVAL ticks). Paths unused for more than TTL_TICKS ticks
 *   are removed from both the primary cache and the cell reverse-index.
 */
public class PathCache {

    /** Ticks of disuse before a path is evicted (~5 min at 20 tps).
     TODO set this better
     */
    private static final long TTL_TICKS = 6_000;

    /** Primary key → path. */
    private final Map<Long, Path> cache = new HashMap<>();

    /** Cell → all paths that pass through it (for fast invalidation). */
    private final Map<Integer, List<Path>> cellToPathsIndex = new HashMap<>();

    private static long key(int origin, int dest) {
        return ((long) origin << 32) | (dest & 0xFFFFFFFFL);
    }

    /**
     * Returns the cached path for (originCellIdx → destCellIdx), or null on miss.
     * Updates lastUsedTick on a hit so the TTL timer resets.
     */
    public Path get(int originCellIdx, int destCellIdx, long currentTick) {
        Path p = cache.get(key(originCellIdx, destCellIdx));
        if (p != null) p.lastUsedTick = currentTick;
        return p;
    }

    /**
     * Stores the full path and every sub-segment that begins in a new MapCell.
     */
    public void put(int originCellIdx, int destCellIdx,
                    List<Point> fullPath, int mapCellSize, int cellGridH, long currentTick) {
        if (fullPath == null || fullPath.isEmpty()) return;

        int n = fullPath.size();

        // Map each tile to its cell index
        int[] pointCell = new int[n];
        for (int i = 0; i < n; i++) {
            Point p = fullPath.get(i);
            int cx = p.getX() / mapCellSize;
            int cy = p.getY() / mapCellSize;
            pointCell[i] = cx * cellGridH + cy;
        }

        // Store the full path
        storeSegment(originCellIdx, destCellIdx, fullPath, pointCell, 0, n, currentTick);

        // Store sub-segments — one per cell boundary crossed
        int prevCell = originCellIdx;
        for (int i = 1; i < n; i++) {
            int c = pointCell[i];
            if (c != prevCell) {
                long subKey = key(c, destCellIdx);
                // Only store if not already cached (preserve the earlier canonical path)
                if (!cache.containsKey(subKey)) {
                    storeSegment(c, destCellIdx, fullPath, pointCell, i, n, currentTick);
                }
                prevCell = c;
            }
        }
    }

    private void storeSegment(int originCellIdx, int destCellIdx,
                               List<Point> fullPath, int[] pointCell,
                               int from, int to, long currentTick) {
        List<Point> sub = fullPath.subList(from, to);

        // Collect distinct cell indices for this segment
        Set<Integer> cells = new LinkedHashSet<>();
        for (int i = from; i < to; i++) cells.add(pointCell[i]);

        Path path = new Path(originCellIdx, destCellIdx, sub, cells, currentTick);
        long k = key(originCellIdx, destCellIdx);

        // Remove the previous entry from the reverse-index (if any) before replacing
        Path old = cache.get(k);
        if (old != null) removeCellIndex(old);

        cache.put(k, path);

        // Register in reverse-index
        for (int c : cells) {
            cellToPathsIndex.computeIfAbsent(c, x -> new ArrayList<>()).add(path);
        }
    }

    /**
     * Removes all cached paths that pass through the given MapCell.
     * Call when a building is placed or destroyed in that cell.
     */
    public void invalidateCell(int cellIdx) {
        List<Path> affected = cellToPathsIndex.remove(cellIdx);
        if (affected == null) return;
        for (Path p : new ArrayList<>(affected)) {
            cache.remove(key(p.originCellIdx, p.destCellIdx));
            removeCellIndex(p);
        }
    }

    /**
     * Evicts paths that have not been used for TTL_TICKS ticks.
     * Currently called periodically
     */
    public void maybeEvict(long currentTick) {
        Iterator<Map.Entry<Long, Path>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            Path p = it.next().getValue();
            if (currentTick - p.lastUsedTick > TTL_TICKS) {
                it.remove();
                removeCellIndex(p);
            }
        }
    }

    /** Remove all entries — call on world recreate. */
    public void clear() {
        cache.clear();
        cellToPathsIndex.clear();
    }

    public int size() { return cache.size(); }

    private void removeCellIndex(Path p) {
        for (int c : p.cellIndices) {
            List<Path> list = cellToPathsIndex.get(c);
            if (list != null) {
                list.remove(p);
                if (list.isEmpty()) cellToPathsIndex.remove(c);
            }
        }
    }
}

