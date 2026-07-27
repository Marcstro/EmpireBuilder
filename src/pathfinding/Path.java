package pathfinding;

import empirebuilder.Point;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An immutable cached point-level path from one MapCell to another.
 *
 * The path is stored as a Point[]. The first entry is
 * the point the computing unit was standing on when A* was requested (or the
 * cell centre for canonical paths), and the last entry is the actual target point.
 *
 * cellIndices holds every MapCell index this path passes through. PathCache
 * uses this set to evict paths whose cells have changed (building placed/removed).
 */
public class Path {

    private static final AtomicInteger ID_GEN = new AtomicInteger(0);

    //for debugging, remove?
    public final int id;

    public final int originCellIdx;

    public final int destCellIdx;

    // path
    public final Point[] points;

    /** Every MapCell index this path passes through. Used for invalidation. */
    public final Set<Integer> cellIndices;

    /** Game-tick at which this path was last handed to a unit. Updated on every cache hit.
     * TODO update, we need a better system for this
     */
    public volatile long lastUsedTick;

    Path(int originCellIdx, int destCellIdx,
         List<Point> pathList, Set<Integer> cellIndices, long currentTick) {
        this.id            = ID_GEN.incrementAndGet();
        this.originCellIdx = originCellIdx;
        this.destCellIdx   = destCellIdx;
        this.points        = pathList.toArray(new Point[0]);
        this.cellIndices   = Collections.unmodifiableSet(cellIndices);
        this.lastUsedTick  = currentTick;
    }

    // length is used for finding fastest path
    public int length() { return points.length; }
}

