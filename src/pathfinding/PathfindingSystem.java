package pathfinding;

import empirebuilder.*;
import entities.Entity;
import entities.units.Unit;
import java.util.ArrayList;
import java.util.List;

/**
 * PathfindingSystem — clean-slate pathfinding using per-cell-pair path caching.
 *
 * CORE DESIGN
 *   • A* (getPathBetween) computes a full point-resolution Theta* path the first
 *     time a (originCell, destCell) pair is requested.  The path is stored in
 *     PathCache, including every sub-segment so future units on the same route
 *     always get a cache hit.
 *   • Each unit follows its assigned Path by advancing a waypoint index.
 *     Advance threshold is 0.8 points — generous enough for a group to walk
 *     side-by-side without all bunching on the exact path line.
 *   • ORCA handles unit-unit collision avoidance with priority-tier weighting:
 *     higher-tier units yield less, lower-tier units yield more.
 *
 * PRIORITY TIERS (via Unit.getPriorityTier())
 *   0 = archers (lowest — yield most)
 *   1 = infantry
 *   2 = fast cavalry
 *   3 = huge beasts (highest — yield least)
 *   priority only makes units avoid those of higher prio, higher prio units cannot push units (to be changed?)
 *
 * THREAD SAFETY
 *   All public methods must be called from the game thread only
 */
public class PathfindingSystem {


    // first variables are used to performance variation. current settings works fine for 10.000 units

    /** Distance (points) at which a unit advances to the next cached waypoint. */
    private static final double ADVANCE_THRESHOLD = 0.8;

    /** ORCA time horizon (ticks). */
    private static final double ORCA_TAU = 10.0;

    /**
     * MapCell radius for ORCA neighbour collection.
     *   0 = own cell only  (cheapest — unit count scanned ≈ local cell size)
     *   1 = 3×3 cell window (original — better coverage at cell boundaries)
     * Edge-case quality loss at 0 is negligible for typical unit densities.
     * Also used by computePhysicalSeparation.
     */
    private static final int ORCA_CELL_RADIUS = 0;

    /** Maximum neighbours passed to ORCA per unit per tick. */
    private static final int MAX_ORCA_NEIGHBORS = 7;


    /**
     * Scales the combined collision radius used inside the ORCA LP constraints.
     * Values < 1.0 let units stand closer together before ORCA starts pushing them apart.
     * Does NOT affect the emergency-overlap pre-pass (which always uses full radius).
     *   1.0 = original behaviour
     *   0.7 = ~30 % smaller repulsion bubble (units closer together)
     */
    private static final double ORCA_RADIUS_SCALE = 0.7;

    /**
     * Blend factor for physical-separation velocity (0 = pure previous velocity,
     * 1 = pure new separation direction).  Lower values smooth out rapid direction
     * changes when an archer is surrounded by many knights moving in different directions
     * ("spazzing").  0.4 gives a gradual, natural-looking drift.
     */
    private static final double PHYS_SEP_BLEND = 0.4;

    /**
     * Pre-allocated candidate pool for ORCA neighbour collection.
     * Must be large enough to hold ALL units that pass the dynamic-radius filter
     * in the 3×3 cell window — the selection sort needs to see every qualifying
     * candidate to guarantee it picks the true MAX_ORCA_NEIGHBORS closest ones.
     */
    private static final int ORCA_POOL_SIZE = 128;

    /** How many waypoints ahead to search when joining a cached path.
     * TODO adjust to lower number? This seems excessive. maybe just like 4-5?*/
    private static final int JOIN_LOOKAHEAD = 20;

    /** Ticks between TTL eviction passes on the path cache. */
    private static final int EVICT_INTERVAL = 200;

    // ---- Stuck detection ----
    /** Ticks between stuck-detection position samples.
     * note: this is the unit tick, not simulation tick, might be different values
     * */
    public static final int    STUCK_SAMPLE_INTERVAL = 100;
    /** Minimum distance (points) a unit must travel in STUCK_SAMPLE_INTERVAL ticks. */
    private static final double STUCK_MIN_DISTANCE    = 2.0;
    /** How many waypoints ahead in pathB to use as the escape sub-goal. */
    private static final int    STUCK_LOOKAHEAD       = 10;
    /** Bounding-box radius (points) for the local escape A*. */
    private static final int    STUCK_LOCAL_RADIUS    = 20;

    /** Arrival radius (points) for isUnitInDestinedPoint checks. */
    public static final double POINT_TARGET_ARRIVAL_RADIUS = 2.0;

    private static final double ORCA_EPSILON = 1e-6;

    private final GameManager gm;
    private final PathCache pathCache;

    // better to store this locally, taken from gameManager
    private final int mapCellSize;

    /**
     * Number of cell columns in the Y direction (= map.getHeight() / mapCellSize).
     * Used for cell-index encoding: cellIndex = cellX * cellGridH + cellY.
     */
    private final int cellGridH;

    /** Game unit tick counter, do note this is NOT necessarily simulation tick counter */
    private long tick = 0;

    // ORCA candidate pool — zero allocations after construction
    private final OrcaAgent[] orcaPool          = new OrcaAgent[ORCA_POOL_SIZE];
    private final double[]    orcaCandDistSq    = new double[ORCA_POOL_SIZE];
    private final double[]    agentRespB        = new double[ORCA_POOL_SIZE];
    private final List<OrcaAgent> agentList     = new ArrayList<>(MAX_ORCA_NEIGHBORS);
    private final double[]    agentRespFinal    = new double[MAX_ORCA_NEIGHBORS];

    // LP solver work arrays (inlined ORCA LP)
    private final double[] lpPtX      = new double[MAX_ORCA_NEIGHBORS];
    private final double[] lpPtY      = new double[MAX_ORCA_NEIGHBORS];
    private final double[] lpDrX      = new double[MAX_ORCA_NEIGHBORS];
    private final double[] lpDrY      = new double[MAX_ORCA_NEIGHBORS];
    private final double[] orcaResult  = new double[2];
    /** Reused by computePhysicalSeparation — avoids per-tick allocation. */
    private final double[] sepResult   = new double[2];

    public PathfindingSystem(GameManager gm) {
        this.gm          = gm;
        this.pathCache   = new PathCache();
        this.mapCellSize = gm.getGame().getMAP_CELL_SIZE();
        this.cellGridH   = gm.getMap().getHeight() / mapCellSize;
        for (int i = 0; i < ORCA_POOL_SIZE; i++) orcaPool[i] = new OrcaAgent();
    }

    public void beginTick() {
        tick++;
        if (tick % EVICT_INTERVAL == 0) pathCache.maybeEvict(tick);
    }

    /**
     * Computes the new position for a unit this tick.
     * Returns null if the unit should not move (no target, already at destination).
     */
    public Coordinates computeNextPosition(Unit unit) {

        Point target = resolveTarget(unit);
        if (target == null) return null;

        empirebuilder.Map map = gm.getMap();

        if (!map.isValidAndWalkable(target.getX(), target.getY())) {
            unit.clearPointTarget();
            unit.setPointTarget(gm.getGame().getNewPointTarget());
            return null;
        }

        double ux = unit.getX(), uy = unit.getY();

        // ---- 1b. Attack-range check ----
        // Stop moving once the combat target is within attack range.
        // Exception: if another unit is physically overlapping this unit (dist < combined
        // radii), take one separation step via ORCA so we don't freeze while being
        // stood on.  This is the only trigger — proximity alone does not cause movement.

        // TODO change this for future ai logic handling. how to determine if ranged units
        // are within shooting distsance, backwarding distance etc
        Entity combatTarget = unit.getCombatTarget();
        if (combatTarget != null) {
            double cdx  = combatTarget.getX() - ux;
            double cdy  = combatTarget.getY() - uy;
            double cdist = Math.sqrt(cdx * cdx + cdy * cdy);
            if (cdist <= unit.getAttackRange() + combatTarget.getSize()) {
                double[] sepVel = computePhysicalSeparation(unit, ux, uy);
                if (sepVel != null) {
                    unit.clearPathB();
                    return applyOrcaAndTerrain(unit, sepVel[0], sepVel[1], ux, uy, map);
                }
                unit.clearPathB();
                return null; // In attack range, nothing overlapping — stand still
            }
        }

        // ---- 1c. Stuck detection ----
        checkStuck(unit, map);

        // ---- 2. Already at destination? ----
        double targetCX = target.getX() + 0.5;
        double targetCY = target.getY() + 0.5;
        double dxFinal = targetCX - ux;
        double dyFinal = targetCY - uy;
        double distFinal = Math.sqrt(dxFinal * dxFinal + dyFinal * dyFinal);
        if (distFinal <= unit.getSpeed()) {
            unit.clearPathB();
            unit.setLastVelX(dxFinal);
            unit.setLastVelY(dyFinal);
            return new Coordinates(targetCX, targetCY);
        }

        // ---- 3. Determine cells ----
        int unitCellX   = (int) ux / mapCellSize;
        int unitCellY   = (int) uy / mapCellSize;
        int destCellX   = target.getX() / mapCellSize;
        int destCellY   = target.getY() / mapCellSize;
        int unitCellIdx = cellIndex(unitCellX, unitCellY);
        int destCellIdx = cellIndex(destCellX, destCellY);

        double prefX, prefY;

        if (unitCellIdx == destCellIdx) {
            // ---- 4a. Same cell → steer directly toward point center ----
            unit.clearPathB();
            prefX = (dxFinal / distFinal) * unit.getSpeed();
            prefY = (dyFinal / distFinal) * unit.getSpeed();

        } else {
            // ---- 4b. Ensure unit has a valid cached path ----
            Path path = unit.getPathB();
            boolean needNewPath = (path == null || path.destCellIdx != destCellIdx);

            if (needNewPath) {
                path = getOrComputePath(unit, unitCellIdx, destCellIdx, target, map, mapCellSize);
                if (path == null) {
                    unit.clearPointTarget();
                    unit.setPointTarget(gm.getGame().getNewPointTarget());
                    return null;
                }
                unit.setPathB(path, findJoinIndex(unit, path));
                path.lastUsedTick = tick;
            }

            // ---- 5. Advance waypoint index if close enough ----
            int idx = unit.getWaypointIndexB();
            if (idx < path.points.length) {
                Point wp = path.points[idx];
                double wpCX = wp.getX() + 0.5, wpCY = wp.getY() + 0.5;
                double dwx = wpCX - ux, dwy = wpCY - uy;
                if (Math.sqrt(dwx * dwx + dwy * dwy) <= ADVANCE_THRESHOLD) {
                    idx++;
                    unit.setWaypointIndexB(idx);
                }
            }

            // ---- 6. Preferred velocity toward current waypoint CENTER ----
            if (idx >= path.points.length) {
                unit.clearPathB();
                prefX = (dxFinal / distFinal) * unit.getSpeed();
                prefY = (dyFinal / distFinal) * unit.getSpeed();
            } else {
                Point wp = path.points[idx];
                double wpCX = wp.getX() + 0.5, wpCY = wp.getY() + 0.5;
                double dwx = wpCX - ux, dwy = wpCY - uy;
                double dist = Math.sqrt(dwx * dwx + dwy * dwy);
                if (dist < 1e-6) {
                    prefX = (dxFinal / distFinal) * unit.getSpeed();
                    prefY = (dyFinal / distFinal) * unit.getSpeed();
                } else {
                    prefX = (dwx / dist) * unit.getSpeed();
                    prefY = (dwy / dist) * unit.getSpeed();
                }
            }
        }


        // ---- 7. ORCA + terrain ----
        return applyOrcaAndTerrain(unit, prefX, prefY, ux, uy, map);
    }

    // -------------------------------------------------------------------------
    // Path resolution
    // -------------------------------------------------------------------------

    private Path getOrComputePath(Unit unit, int originCellIdx, int destCellIdx,
                                  Point target, empirebuilder.Map map, int mapCellSize) {
        Path cached = pathCache.get(originCellIdx, destCellIdx, tick);
        if (cached != null) return cached;

        int startTileX = (int) unit.getX();
        int startTileY = (int) unit.getY();

        if (!map.isValidAndWalkable(startTileX, startTileY)) {
            int[] nearest = nearestWalkable(map, startTileX, startTileY, mapCellSize / 2);
            startTileX = nearest[0];
            startTileY = nearest[1];
        }

        Point startPoint = map.getPoint(startTileX, startTileY);
        if (startPoint == null) return null;

        List<Point> fullPath = gm.getPathfinder().getPathBetween(startPoint, target);
        if (fullPath == null || fullPath.isEmpty()) return null;

        pathCache.put(originCellIdx, destCellIdx, fullPath, mapCellSize, cellGridH, tick);

        return pathCache.get(originCellIdx, destCellIdx, tick);
    }

    private int findJoinIndex(Unit unit, Path path) {
        double ux = unit.getX(), uy = unit.getY();
        double best = Double.MAX_VALUE;
        int bestIdx = 0;
        int limit = Math.min(path.points.length, JOIN_LOOKAHEAD);
        for (int i = 0; i < limit; i++) {
            Point p = path.points[i];
            double dx = p.getX() - ux, dy = p.getY() - uy;
            double d = dx * dx + dy * dy;
            if (d < best) { best = d; bestIdx = i; }
        }
        return bestIdx;
    }

    private int[] nearestWalkable(empirebuilder.Map map, int x, int y, int maxRadius) {
        if (map.isValidAndWalkable(x, y)) return new int[]{x, y};
        for (int r = 1; r <= maxRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    if (Math.abs(dx) != r && Math.abs(dy) != r) continue;
                    if (map.isValidAndWalkable(x + dx, y + dy)) return new int[]{x + dx, y + dy};
                }
            }
        }
        return new int[]{x, y};
    }

    // -------------------------------------------------------------------------
    // Stuck detection
    // -------------------------------------------------------------------------

    private void checkStuck(Unit unit, empirebuilder.Map map) {
        if (unit.getCombatTarget() != null && unit.getCombatTarget().isAlive()) {
            return;
        }

        int remaining = unit.getStuckSampleTick();
        if (remaining > 0) {
            unit.setStuckSampleTick(remaining - 1);
            return;
        }
        unit.setStuckSampleTick(STUCK_SAMPLE_INTERVAL);

        double sx = unit.getStuckSampleX();
        double sy = unit.getStuckSampleY();
        double cx = unit.getX(), cy = unit.getY();

        if (sx != Double.MIN_VALUE) {
            double dx    = cx - sx;
            double dy    = cy - sy;
            double moved = Math.sqrt(dx * dx + dy * dy);

            if (moved < STUCK_MIN_DISTANCE) {
                Path currentPath = unit.getPathB();

                Point escapeGoal = null;
                if (currentPath != null && currentPath.points.length > 0) {
                    int aheadIdx = Math.min(
                            unit.getWaypointIndexB() + STUCK_LOOKAHEAD,
                            currentPath.points.length - 1);
                    escapeGoal = currentPath.points[aheadIdx];
                }

                if (escapeGoal != null) {
                    Point startPt = map.getPoint((int) cx, (int) cy);
                    if (startPt != null && !startPt.equals(escapeGoal)) {
                        List<Point> escapePath = gm.getPathfinder()
                                .getLocalPath(startPt, escapeGoal, STUCK_LOCAL_RADIUS);

                        if (escapePath != null && escapePath.size() > 1) {
                            pathCache.put(
                                    cellIndex((int) cx / mapCellSize, (int) cy / mapCellSize),
                                    currentPath.destCellIdx,
                                    escapePath, mapCellSize, cellGridH, tick);
                            Path escape = pathCache.get(
                                    cellIndex((int) cx / mapCellSize, (int) cy / mapCellSize),
                                    currentPath.destCellIdx, tick);
                            if (escape != null) {
                                unit.setPathB(escape, 0);
                            }
                            unit.setStuckSampleX(cx);
                            unit.setStuckSampleY(cy);
                            return;
                        }
                    }
                }

                unit.clearPathB();
                unit.clearPointTarget();
                unit.setPointTarget(gm.getGame().getNewPointTarget());
            }
        }

        unit.setStuckSampleX(cx);
        unit.setStuckSampleY(cy);
    }

    // -------------------------------------------------------------------------
    // ORCA with priority tiers
    // -------------------------------------------------------------------------

    private Coordinates applyOrcaAndTerrain(Unit unit, double prefX, double prefY,
                                             double ux, double uy,
                                             empirebuilder.Map map) {
        int count = gatherNeighbors(unit);

        double avX = prefX, avY = prefY;
        if (count > 0) {
            double[] adj = computeOrcaVelocity(
                    ux, uy,
                    unit.getLastVelX(), unit.getLastVelY(),
                    prefX, prefY,
                    unit.getSize(), unit.getSpeed(), ORCA_TAU,
                    count);
            avX = adj[0];
            avY = adj[1];
        }

        // ---- Step 1: try ORCA-adjusted position ----
        double nx = ux + avX, ny = uy + avY;
        if (map.isValidAndWalkable(nx, ny)) {
            unit.setLastVelX(avX);
            unit.setLastVelY(avY);
            return new Coordinates(nx, ny);
        }

        // ---- Step 2: ORCA pushed into terrain — fall back to preferred (A*) velocity ----
        double px = ux + prefX, py = uy + prefY;
        if (map.isValidAndWalkable(px, py)) {
            unit.setLastVelX(prefX);
            unit.setLastVelY(prefY);
            return new Coordinates(px, py);
        }

        // ---- Step 3: preferred direction also blocked — axis slides ----
        if (Math.abs(prefX) > 1e-9 && map.isValidAndWalkable(ux + prefX, uy)) {
            unit.setLastVelX(prefX);
            unit.setLastVelY(0);
            return new Coordinates(ux + prefX, uy);
        }
        if (Math.abs(prefY) > 1e-9 && map.isValidAndWalkable(ux, uy + prefY)) {
            unit.setLastVelX(0);
            unit.setLastVelY(prefY);
            return new Coordinates(ux, uy + prefY);
        }

        // ---- Step 4: truly surrounded — stay put this tick ----
        unit.setLastVelX(0);
        unit.setLastVelY(0);
        return null;
    }

    private int gatherNeighbors(Unit unit) {
        agentList.clear();

        final Entity combatTarget = unit.getCombatTarget();
        final double ux           = unit.getX();
        final double uy           = unit.getY();
        final double unitHalf     = unit.getSize() + ORCA_TAU * unit.getSpeed();

        MapCell[][] cellGrid = gm.getGame().getMapCellGrid();
        int startCX = Math.max(0, unit.getMapCellX() - ORCA_CELL_RADIUS);
        int endCX   = Math.min(cellGrid.length - 1, unit.getMapCellX() + ORCA_CELL_RADIUS);
        int pIdx    = 0;

        for (int cx = startCX; cx <= endCX; cx++) {
            int startCY = Math.max(0, unit.getMapCellY() - ORCA_CELL_RADIUS);
            int endCY   = Math.min(cellGrid[cx].length - 1, unit.getMapCellY() + ORCA_CELL_RADIUS);
            for (int cy = startCY; cy <= endCY; cy++) {
                for (Unit neighbor : cellGrid[cx][cy].getUnits()) {
                    if (neighbor == unit || neighbor == combatTarget) continue;
                    double ddx    = neighbor.getX() - ux;
                    double ddy    = neighbor.getY() - uy;
                    double dSq    = ddx * ddx + ddy * ddy;
                    double dynRad = unitHalf + neighbor.getSize() + ORCA_TAU * neighbor.getSpeed();
                    if (dSq <= dynRad * dynRad) {
                        if (pIdx < ORCA_POOL_SIZE) {
                            int tierDiff = unit.getPriorityTier() - neighbor.getPriorityTier();

                            if (tierDiff > 0 && neighbor.getFactionId() == unit.getFactionId()) {
                                continue;
                            }

                            if      (tierDiff > 0) agentRespB[pIdx] = 0.25;
                            else if (tierDiff < 0) agentRespB[pIdx] = 0.75;
                            else                   agentRespB[pIdx] = 0.50;

                            orcaPool[pIdx].update(
                                    neighbor.getX(), neighbor.getY(),
                                    neighbor.getLastVelX(), neighbor.getLastVelY(),
                                    neighbor.getSize());
                            orcaCandDistSq[pIdx] = dSq;
                            pIdx++;
                        }
                    }
                }
            }
        }

        int count = pIdx;
        int take  = Math.min(count, MAX_ORCA_NEIGHBORS);

        for (int i = 0; i < take; i++) {
            int minIdx = i;
            for (int j = i + 1; j < count; j++) {
                if (orcaCandDistSq[j] < orcaCandDistSq[minIdx]) minIdx = j;
            }
            if (minIdx != i) {
                double     tmpD = orcaCandDistSq[i];
                OrcaAgent tmpA = orcaPool[i];
                double     tmpR = agentRespB[i];
                orcaCandDistSq[i]    = orcaCandDistSq[minIdx];
                orcaPool[i]          = orcaPool[minIdx];
                agentRespB[i]        = agentRespB[minIdx];
                orcaCandDistSq[minIdx] = tmpD;
                orcaPool[minIdx]       = tmpA;
                agentRespB[minIdx]     = tmpR;
            }
            agentList.add(orcaPool[i]);
            agentRespFinal[i] = agentRespB[i];
        }

        return take;
    }

    /**
     * ORCA velocity computation with per-neighbour responsibility.
     *
     * OVERLAP PRE-PASS
     *   When two units are already physically overlapping (dist < r) the standard LP
     *   almost always becomes infeasible — the LP returns (0,0) and the unit freezes.
     *   The fix: before running the LP, scan for overlapping neighbours.  If any exist,
     *   skip the LP entirely and return max-speed separation from the closest overlapper.
     */
    private double[] computeOrcaVelocity(
            double posX, double posY,
            double velX, double velY,
            double prefX, double prefY,
            double radius, double maxSpeed, double tau,
            int neighborCount) {

        // ---- Emergency overlap pre-pass ----
        double emergX = 0, emergY = 0;
        double bestOverlapDistSq = Double.MAX_VALUE;

        for (int i = 0; i < neighborCount; i++) {
            OrcaAgent b = agentList.get(i);
            double px = b.posX - posX, py = b.posY - posY;
            double distSq = px * px + py * py;
            double r = radius + b.radius;
            if (distSq < r * r && distSq < bestOverlapDistSq) {
                bestOverlapDistSq = distSq;
                double dist = Math.sqrt(distSq);
                if (dist < ORCA_EPSILON) {
                    emergX = maxSpeed;
                    emergY = 0;
                } else {
                    emergX = -(px / dist) * maxSpeed;
                    emergY = -(py / dist) * maxSpeed;
                }
            }
        }

        if (bestOverlapDistSq < Double.MAX_VALUE) {
            orcaResult[0] = emergX;
            orcaResult[1] = emergY;
            return orcaResult;
        }

        // ---- Normal ORCA LP ----
        int count = 0;
        double invTau = 1.0 / tau;

        for (int i = 0; i < neighborCount; i++) {
            OrcaAgent b = agentList.get(i);
            double resp = agentRespFinal[i];
            double px = b.posX - posX, py = b.posY - posY;
            double rvX = velX - b.velX,  rvY = velY - b.velY;
            double distSq = px * px + py * py;
            double r = (radius + b.radius) * ORCA_RADIUS_SCALE, rSq = r * r;
            double dX, dY, uX, uY;

            if (distSq > rSq) {
                double wx = rvX - invTau * px, wy = rvY - invTau * py;
                double wLenSq = wx * wx + wy * wy;
                double dotWP  = wx * px + wy * py;
                if (dotWP < 0.0 && dotWP * dotWP > rSq * wLenSq) {
                    double wLen = Math.sqrt(wLenSq);
                    if (wLen < ORCA_EPSILON) continue;
                    dX =  wy / wLen; dY = -wx / wLen;
                    uX = (r * invTau - wLen) * (wx / wLen);
                    uY = (r * invTau - wLen) * (wy / wLen);
                } else {
                    double leg = Math.sqrt(Math.max(0.0, distSq - rSq));
                    if (px * wy - py * wx > 0.0) {
                        dX = ( px * leg - py * r) / distSq;
                        dY = ( px * r   + py * leg) / distSq;
                    } else {
                        dX = -(px * leg + py * r) / distSq;
                        dY =  (px * r   - py * leg) / distSq;
                    }
                    double dot = rvX * dX + rvY * dY;
                    uX = dot * dX - rvX;
                    uY = dot * dY - rvY;
                }
            } else {
                double wx = rvX - invTau * px, wy = rvY - invTau * py;
                double wLenSq = wx * wx + wy * wy;
                if (wLenSq < ORCA_EPSILON * ORCA_EPSILON) continue;
                double wLen = Math.sqrt(wLenSq);
                dX = wy / wLen; dY = -wx / wLen;
                uX = (r * invTau - wLen) * (wx / wLen);
                uY = (r * invTau - wLen) * (wy / wLen);
            }

            lpPtX[count] = velX + resp * uX;
            lpPtY[count] = velY + resp * uY;
            lpDrX[count] = dX;
            lpDrY[count] = dY;
            count++;
        }

        double vx = prefX, vy = prefY;
        double spd = Math.sqrt(vx * vx + vy * vy);
        if (spd > maxSpeed) { vx = vx / spd * maxSpeed; vy = vy / spd * maxSpeed; }

        for (int i = 0; i < count; i++) {
            if (lpDrX[i] * (vy - lpPtY[i]) - lpDrY[i] * (vx - lpPtX[i]) >= 0.0) continue;
            double tL = -Double.MAX_VALUE, tR = Double.MAX_VALUE;
            double b2   = lpPtX[i] * lpDrX[i] + lpPtY[i] * lpDrY[i];
            double c    = lpPtX[i] * lpPtX[i] + lpPtY[i] * lpPtY[i] - maxSpeed * maxSpeed;
            double disc = b2 * b2 - c;
            if (disc < 0.0) { orcaResult[0] = 0; orcaResult[1] = 0; return orcaResult; }
            double sq = Math.sqrt(disc);
            tL = Math.max(tL, -b2 - sq);
            tR = Math.min(tR, -b2 + sq);
            for (int j = 0; j < i; j++) {
                double den = lpDrX[j] * lpDrY[i] - lpDrY[j] * lpDrX[i];
                double num = lpDrX[j] * (lpPtY[i] - lpPtY[j]) - lpDrY[j] * (lpPtX[i] - lpPtX[j]);
                if (Math.abs(den) < ORCA_EPSILON) {
                    if (num < 0.0) { orcaResult[0] = 0; orcaResult[1] = 0; return orcaResult; }
                    continue;
                }
                double t = -num / den;
                if (den > 0.0) tL = Math.max(tL, t);
                else           tR = Math.min(tR, t);
                if (tL > tR) { orcaResult[0] = 0; orcaResult[1] = 0; return orcaResult; }
            }
            if (tL > tR) { orcaResult[0] = 0; orcaResult[1] = 0; return orcaResult; }
            double tPref = (prefX - lpPtX[i]) * lpDrX[i] + (prefY - lpPtY[i]) * lpDrY[i];
            double t = Math.max(tL, Math.min(tR, tPref));
            vx = lpPtX[i] + t * lpDrX[i];
            vy = lpPtY[i] + t * lpDrY[i];
        }

        orcaResult[0] = vx;
        orcaResult[1] = vy;
        return orcaResult;
    }


    // -------------------------------------------------------------------------
    // Target resolution
    // -------------------------------------------------------------------------

    private Point resolveTarget(Unit unit) {
        Entity combatTarget = unit.getCombatTarget();
        if (combatTarget != null) {
            return gm.getMap().getPoint((int) combatTarget.getX(), (int) combatTarget.getY());
        }
        return unit.getPointTarget();
    }


    // -------------------------------------------------------------------------
    // Physical-overlap separation
    // -------------------------------------------------------------------------

    private double[] computePhysicalSeparation(Unit unit, double ux, double uy) {
        MapCell[][] cellGrid = gm.getGame().getMapCellGrid();
        int startCX = Math.max(0, unit.getMapCellX() - ORCA_CELL_RADIUS);
        int endCX   = Math.min(cellGrid.length - 1, unit.getMapCellX() + ORCA_CELL_RADIUS);

        double sepX = 0, sepY = 0;
        boolean found = false;

        for (int cx = startCX; cx <= endCX; cx++) {
            int startCY = Math.max(0, unit.getMapCellY() - ORCA_CELL_RADIUS);
            int endCY   = Math.min(cellGrid[cx].length - 1, unit.getMapCellY() + ORCA_CELL_RADIUS);
            for (int cy = startCY; cy <= endCY; cy++) {
                for (Unit neighbor : cellGrid[cx][cy].getUnits()) {
                    if (neighbor == unit) continue;
                    double ddx = ux - neighbor.getX(), ddy = uy - neighbor.getY();
                    double dSq = ddx * ddx + ddy * ddy;
                    double minDist = unit.getSize() + neighbor.getSize();
                    if (dSq < minDist * minDist) {
                        double dist = Math.sqrt(dSq);
                        if (dist < ORCA_EPSILON) {
                            sepX += 1;
                        } else {
                            sepX += ddx / dist;
                            sepY += ddy / dist;
                        }
                        found = true;
                    }
                }
            }
        }

        if (!found) return null;

        double len = Math.sqrt(sepX * sepX + sepY * sepY);
        double targetVx, targetVy;
        if (len < ORCA_EPSILON) {
            targetVx = unit.getSpeed();
            targetVy = 0;
        } else {
            targetVx = (sepX / len) * unit.getSpeed();
            targetVy = (sepY / len) * unit.getSpeed();
        }

        double lx = unit.getLastVelX(), ly = unit.getLastVelY();
        double blendedX = lx * (1.0 - PHYS_SEP_BLEND) + targetVx * PHYS_SEP_BLEND;
        double blendedY = ly * (1.0 - PHYS_SEP_BLEND) + targetVy * PHYS_SEP_BLEND;

        double bLen = Math.sqrt(blendedX * blendedX + blendedY * blendedY);
        if (bLen > unit.getSpeed()) {
            blendedX = blendedX / bLen * unit.getSpeed();
            blendedY = blendedY / bLen * unit.getSpeed();
        }

        sepResult[0] = blendedX;
        sepResult[1] = blendedY;
        return sepResult;
    }

    // -------------------------------------------------------------------------
    // Cell-index helper
    // -------------------------------------------------------------------------

    private int cellIndex(int cx, int cy) {
        return cx * cellGridH + cy;
    }


    // -------------------------------------------------------------------------
    // Cache management — called by GameManager
    // -------------------------------------------------------------------------

    /** Clear all cached paths. Call when the world is recreated. */
    public void reset() {
        pathCache.clear();
        tick = 0;
    }

    /**
     * Invalidate all cached paths that pass through the given MapCell index.
     * Call when an unwalkable building is placed or destroyed in that cell.
     *
     * @param cellIdx  cell index (cellX * cellGridH + cellY)
     */
    public void invalidateCell(int cellIdx) {
        pathCache.invalidateCell(cellIdx);
    }

    /** Diagnostic: current number of cached paths. */
    public int cachedPathCount() { return pathCache.size(); }
}

