# EmpireBuilder — Pathfinding Architecture

This document describes the movement and collision-avoidance system used in EmpireBuilder. It covers the path-caching strategy, ORCA collision avoidance, terrain collision resolution, stuck detection, and all supporting data structures.

---

## Table of Contents

1. [Background & design rationale](#1-background--design-rationale)
2. [Key vocabulary](#2-key-vocabulary)
3. [High-level architecture](#3-high-level-architecture)
4. [Path caching — PathCacheB](#4-path-caching--pathcacheb)
   - [Primary key](#41-primary-key)
   - [Sub-segment caching](#42-sub-segment-caching)
   - [Cell reverse-index & invalidation](#43-cell-reverse-index--invalidation)
   - [TTL eviction](#44-ttl-eviction)
5. [The PathB object](#5-the-pathb-object)
6. [ORCA collision avoidance](#6-orca-collision-avoidance)
   - [Priority tiers](#61-priority-tiers)
   - [Neighbour gathering (gatherNeighbors)](#62-neighbour-gathering-gatherneighbors)
   - [The ORCA LP (computeOrcaVelocity)](#63-the-orca-lp-computeorcavelocity)
   - [Emergency overlap pre-pass](#64-emergency-overlap-pre-pass)
   - [Physical separation (computePhysicalSeparation)](#65-physical-separation-computephysicalseparation)
   - [Non-combat ORCA throttling](#66-non-combat-orca-throttling)
7. [Terrain collision — applyOrcaAndTerrain](#7-terrain-collision--applyorcaandterrain)
8. [Full per-tick movement walkthrough](#8-full-per-tick-movement-walkthrough)
9. [Stuck detection & recovery](#9-stuck-detection--recovery)
10. [Lifecycle methods](#10-lifecycle-methods)
11. [Performance design decisions](#11-performance-design-decisions)
12. [Constants quick-reference](#12-constants-quick-reference)
13. [Edge cases and recovery table](#13-edge-cases-and-recovery-table)
14. [Class map](#14-class-map)

---

## 1. Background & Design Rationale

Running A\* on the full 300×200 tile grid every time a unit needs a new route means searching up to 60 000 nodes per search, per unit, potentially every few ticks. With thousands of units this is prohibitively expensive.

The system separates movement into three independent concerns:

| Concern | Tool | Cost per unit per tick |
|---|---|---|
| *Where do I go across the map?* | Cached A\* path, shared by cell pair | O(1) array index |
| *How do I avoid the crowd around me?* | ORCA velocity adjustment | O(k), k = nearby unit count |
| *How do I avoid terrain?* | Axis-slide fallbacks after ORCA | O(1) per axis |

**Key insight — per-cell-pair path caching:**

Two units travelling between the *same pair of MapCells* will almost always want the same tile-level route. By running A\* once for each `(originCellIndex, destCellIndex)` pair and sharing the result across all units with the same source/dest cells, the system pays the A\* cost once and amortises it over the entire lifetime of that route.

The first unit to request a given pair triggers one full-map A\* call. Every subsequent unit that starts in the same origin cell and heads to the same destination cell gets an O(1) cache hit. In a battle with 500 units marching on the same city, only a handful of A\* calls are ever made — one per unique origin cell along the route — regardless of unit count.

---

## 2. Key Vocabulary

| Term | Meaning |
|---|---|
| **Point** | A single tile on the map; integer coordinates `(x, y)`. May be walkable (grass, dirt) or unwalkable (mountain, water). |
| **MapCell** | A rectangular block of `MAP_CELL_SIZE × MAP_CELL_SIZE` tiles (currently 5×5). The map is divided into a grid of these cells. Also acts as a spatial bucket for nearby-unit lookup. |
| **Cell index** | Flat integer uniquely identifying a cell: `cellX * cellGridHeight + cellY`. |
| **PathB** | An immutable cached tile path from one cell to another, stored as a `Point[]`. |
| **PathCacheB** | The shared cache mapping `(originCellIdx, destCellIdx)` → `PathB`. |
| **Waypoint index** | The index into `PathB.points[]` that a unit is currently targeting. Each unit stores its own index so multiple units can share one `PathB` while walking it at different speeds. |
| **ORCA** | Optimal Reciprocal Collision Avoidance — a velocity-space algorithm that adjusts each unit's preferred velocity to avoid collision with nearby units. |
| **Priority tier** | Integer 0–3 attached to each unit type, controlling how much of the ORCA avoidance load each unit absorbs when interacting with a unit of a different tier. |
| **Tick** | One game update cycle. `beginTick()` must be called once per cycle. |
| **TTL** | Time-To-Live. Cached paths unused for `TTL_TICKS` (6 000 ticks ≈ 5 min at 20 TPS) are evicted. |

---

## 3. High-level Architecture

```
Game tick
│
├── PathfindingSystemB.beginTick()          ← tick counter advances; periodic eviction
│
└── for each Unit:
    PathfindingSystemB.computeNextPosition(unit)
    │
    ├── resolveTarget()                     ← combat target > point target
    ├── attack-range check                  ← stop if already in range; sep if overlap
    ├── checkStuck()                        ← periodic position sample; local re-route
    ├── destination-reached check
    ├── same-cell check → direct steer
    │
    ├── getOrComputePath()                  ← PathCacheB hit (O(1)) or A* miss
    │       └── AStarPathfinder.getPathBetween()
    │           └── PathCacheB.put()        ← stores full path + sub-segments
    │
    ├── findJoinIndex()                     ← find best waypoint for THIS unit's position
    ├── advance waypoint index              ← if within ADVANCE_THRESHOLD tiles
    ├── preferred velocity ← waypoint center
    │
    └── applyOrcaAndTerrain()
            ├── gatherNeighbors()           ← dynamic-radius filter + partial sort
            ├── computeOrcaVelocity()       ← emergency overlap pre-pass + 2D LP
            └── terrain axis-slide fallbacks
```

---

## 4. Path Caching — PathCacheB

`PathCacheB` is the central data structure of the system. It holds a `HashMap<Long, PathB>` keyed by a 64-bit encoding of `(originCellIdx, destCellIdx)`.

### 4.1 Primary key

```java
long key = ((long) originCellIdx << 32) | (destCellIdx & 0xFFFFFFFFL);
```

Cell indices fit comfortably in 32 bits for any realistic map size, making this encoding collision-free and much faster than a two-integer tuple.

### 4.2 Sub-segment caching

When A\* returns a path that crosses cells **A → B → C → D**, `PathCacheB.put()` stores not just `(A, D)` but also:

```
(A, D) → full path
(B, D) → sub-path starting at the first tile inside B
(C, D) → sub-path starting at the first tile inside C
```

This means any unit that happens to start in cell B or C **already has a cache hit** the next time it requests a path to D, without requiring another A\* call. The sub-segments are stored as `subList` views of the original `List<Point>` (backed by the same array) so there is no extra memory allocation.

### 4.3 Cell reverse-index & invalidation

Every `PathB` knows which cells it passes through (`cellIndices`). `PathCacheB` maintains a reverse-index:

```
cellToPathsIndex: cellIdx → List<PathB>
```

When a building is placed or destroyed in a cell, `invalidateCell(cellIdx)` removes every cached path that passes through that cell — in O(affected paths) time. The next unit to request a path through that cell will trigger a fresh A\* call.

### 4.4 TTL eviction

`maybeEvict(currentTick)` is called by `beginTick()` every `EVICT_INTERVAL = 200` ticks. Any path whose `lastUsedTick` is more than `TTL_TICKS = 6 000` ticks in the past is removed from both the primary cache and the reverse-index. This prevents unbounded cache growth in long sessions with changing unit goals.

**Important:** `lastUsedTick` is updated on every cache *read* (not just write), so active routes keep their TTL rolling forward.

---

## 5. The PathB Object

```java
public class PathB {
    public final int      id;              // unique ID for debugging
    public final int      originCellIdx;
    public final int      destCellIdx;
    public final Point[]  points;          // O(1) indexed access
    public final Set<Integer> cellIndices; // for cache invalidation
    public volatile long  lastUsedTick;    // TTL timer
}
```

`points` is a `Point[]` (array, not list) for O(1) random access during per-tick waypoint steering. Multiple units can hold a reference to the same `PathB` simultaneously — each unit stores its own `waypointIndexB` so they advance independently through the shared array.

`PathB` objects are package-private to construct: only `PathCacheB` creates them. The rest of the codebase only reads them.

---

## 6. ORCA Collision Avoidance

ORCA (Optimal Reciprocal Collision Avoidance) answers: *"given that I want to walk toward my waypoint, but there are other units nearby, what is the closest velocity to my preferred one that will not cause a collision within the next τ ticks?"*

It works entirely in **velocity space**, not position space. Instead of steering around units, it shifts velocities just enough that no two units are on a collision course.

The LP solver lives directly in `computeOrcaVelocity` inside `PathfindingSystemB`, using instance-level work arrays to avoid all per-tick allocation.

### 6.1 Priority tiers

Each unit type has an integer **priority tier** (0–3), set in the unit's constructor via `setPriorityTier()`:

| Tier | Unit types | Avoidance behaviour |
|---|---|---|
| 0 | Archers | Yield most — absorb 75% of the correction |
| 1 | Infantry | Yield moderately |
| 2 | Fast cavalry | Yield somewhat |
| 3 | Huge beasts | Yield least — absorb only 25% of the correction |

When two units of **different tiers** interact:
- The lower-tier unit absorbs **75%** of the avoidance (`resp = 0.75`)
- The higher-tier unit absorbs **25%** (`resp = 0.25`)
- Same-tier units split evenly (**50%** each)

**Friendly higher-tier units are completely invisible to ORCA in their own gather pass:** when a knight encounters a friendly archer, the knight skips the archer entirely in `gatherNeighbors`. The archer still sees the knight and steps aside. Physical overlap between them is handled by `computePhysicalSeparation` in the archer's own update. This prevents knights from slowing down while marching through friendly formations.

### 6.2 Neighbour gathering (gatherNeighbors)

```
For each cell in the ORCA_CELL_RADIUS window (default: own cell only):
  For each Unit in that cell:
    Skip self, skip combat target
    Skip friendly higher-tier units (see above)
    dynRadius = unitSize + ORCA_TAU * unitSpeed + neighborSize + ORCA_TAU * neighborSpeed
    If dist <= dynRadius:
      Add to orcaPool[], record distSq, compute resp

Partial selection sort → take MAX_ORCA_NEIGHBORS (7) closest
```

The **dynamic radius filter** (`dynRadius`) is the maximum distance at which two moving units could collide within the time horizon `τ = 10` ticks. Pairs outside this distance are provably safe and are skipped before the LP, keeping the constraint count small.

A pre-allocated pool of `OrcaAgentB` objects (`orcaPool[ORCA_POOL_SIZE]`) is reused every tick — zero heap allocation after construction.

**Why `ORCA_CELL_RADIUS = 0` (own cell only)?** At default density a unit's own cell contains all the units it can plausibly collide with in 10 ticks. Expanding to a 3×3 window would be more thorough at cell boundaries but roughly 9× the gather cost. Benchmark testing showed the quality improvement was negligible for typical battle densities.

### 6.3 The ORCA LP (computeOrcaVelocity)

For each nearby unit B, the LP constraint is:

1. Compute the **relative position** `p = posB − posA` and **relative velocity** `rv = velA − velB`.
2. Determine whether the relative velocity is inside the velocity obstacle (VO) cone or not.
3. Project the relative velocity to the nearest point on the VO boundary to get `u` — the minimum velocity change needed to exit the VO.
4. The **ORCA half-plane** for this pair:
   - Point on line: `velA + resp * u`
   - Normal direction: outward from VO boundary

After building one half-plane per neighbour, a 2D incremental LP finds the velocity closest to `preferredVelocity` that satisfies all constraints and lies inside the speed disc `|v| ≤ maxSpeed`.

**ORCA_RADIUS_SCALE = 0.7:** the combined radius inside the LP constraints is scaled to 70% of the actual combined size. This lets units stand ~30% closer together before ORCA starts pushing them apart, producing denser formations. The emergency overlap pre-pass (Section 6.4) always uses the **full unscaled radius** so actual physical overlap is still corrected.

### 6.4 Emergency overlap pre-pass

The standard ORCA LP breaks down when two units are **already physically overlapping** (dist < combined radii). In this case the safe-boundary halfplane falls outside the speed disc and the LP returns `(0, 0)` — the unit freezes in place and the overlap persists forever.

**Fix:** before running the LP, scan all neighbours for physical overlap. If any are found:
- Skip the LP entirely
- Return `max-speed * direction_away_from_closest_overlapper`

This resolves the overlap within 1–3 ticks. Normal ORCA LP resumes once the units are separated.

### 6.5 Physical separation (computePhysicalSeparation)

This method runs when a unit is *already inside its attack range* and should therefore be stationary. In that scenario ORCA is not called at all (the unit cleared its path). But if *another unit is standing on top of this unit*, it would be frozen in place indefinitely.

`computePhysicalSeparation` scans the cell window for units overlapping `dist < sizeA + sizeB`. If any are found, it computes a weighted average separation direction and blends it with the unit's previous velocity at rate `PHYS_SEP_BLEND = 0.4`:

```
blendedVel = prevVel * 0.6 + separationDir * 0.4
```

This blend rate prevents rapid direction flips ("spazzing") when an archer is surrounded by multiple units moving in different directions — it produces a smooth, gradual drift away from the overlap.

### 6.6 Non-combat ORCA throttling

Running a full ORCA gather + LP for every unit every tick is expensive. Units that are simply **marching** (no active combat target) don't need per-tick precision — brief momentary overlaps during a long march are acceptable.

```
runOrca = (unit.getCombatTarget() != null)
        || ((tick + ux + uy * 7) % NON_COMBAT_ORCA_INTERVAL == 0)
```

`NON_COMBAT_ORCA_INTERVAL` controls how often non-combat units run full ORCA:
- `1` = every tick for all units (current default — no throttling)
- `4` = non-combat units run ORCA every 4th tick (~75% cost reduction for marching armies)

The stagger term `(ux + uy * 7)` ensures units at different positions run on different ticks — spreading the load evenly rather than all non-combat units bursting together. Combat units **always** run ORCA every tick.

---

## 7. Terrain Collision — applyOrcaAndTerrain

After computing the ORCA-adjusted velocity `(avX, avY)`, the method tries four fallbacks in order:

```
1. avX, avY          → full ORCA-adjusted move
2. prefX, prefY      → preferred (A*) velocity (ORCA may have pushed into a wall)
3. prefX, 0          → slide along X axis only
4. 0, prefY          → slide along Y axis only
5. (none)            → stay put this tick (fully surrounded by terrain)
```

The guard on steps 3 and 4 — `Math.abs(prefX) > 1e-9` — is important: without it, a zero component trivially passes `isValidAndWalkable` at the current position and the unit snaps back to `(ox, oy)` every tick, appearing stuck.

Step 2 (fall back to preferred velocity when ORCA pushes into terrain) is reliable because the preferred velocity always points toward a walkable A\* waypoint, so this recovery is guaranteed to succeed unless all four neighbours of the unit are unwalkable.

---

## 8. Full Per-Tick Movement Walkthrough

What happens for one unit heading toward a distant city each tick:

```
beginTick()   ← called once per game tick by Game.tickUnits(), NOT per unit
│  tick++
│  if tick % 200 == 0: PathCacheB.maybeEvict(tick)
│  tPath = tGather = tSolve = tPhysSep = 0

computeNextPosition(unit)
│
├── 1. resolveTarget()
│       combatTarget != null → use combatTarget's tile as Point
│       else                 → use unit.getPointTarget()
│       null target          → return null (no movement)
│
├── 1a. Guard: target tile unwalkable?
│       → clearPointTarget(), setPointTarget(newRandom), return null
│
├── 1b. Attack-range check
│       If combatTarget within (attackRange + combatTarget.size):
│         computePhysicalSeparation → any unit literally standing on this one?
│           Yes → applyOrcaAndTerrain with separation velocity
│           No  → return null (stand still, already in attack range)
│
├── 1c. checkStuck()  (see Section 9)
│
├── 2. Already at destination?
│       dist <= unit.speed → snap to tile center, return
│
├── 3. Determine cells
│       unitCellIdx = (int)ux / mapCellSize * cellGridH + (int)uy / mapCellSize
│       destCellIdx = target.x / mapCellSize * cellGridH + target.y / mapCellSize
│
├── 4a. Same cell?
│       → clear path, steer directly to tile center
│
├── 4b. Ensure valid cached path
│       path = unit.getPathB()
│       if null or path.destCellIdx != destCellIdx:
│           getOrComputePath():
│               PathCacheB.get(originCellIdx, destCellIdx, tick)   ← O(1) hit?
│               MISS → AStarPathfinder.getPathBetween(startPoint, target)
│                      PathCacheB.put(...)  ← stores full path + sub-segments
│               null result → destination unreachable → new random target, return null
│           unit.setPathB(path, findJoinIndex(unit, path))
│
├── 5. Advance waypoint index
│       wp = path.points[idx]
│       dist(unit, wp+0.5) <= ADVANCE_THRESHOLD (0.8 tiles)?
│           → idx++, unit.setWaypointIndexB(idx)
│
├── 6. Preferred velocity
│       idx >= path.points.length:
│           path exhausted → steer directly to final target center
│       else:
│           normalize(wpCenter − unitPos) * unit.speed
│
└── 7. applyOrcaAndTerrain(unit, prefX, prefY, ux, uy, map)
```

**Why tile centers (+0.5)?**
A `Point` at integer `(x, y)` maps to the *top-left corner* of its tile. Steering toward a corner creates velocity components pointing up and left, which frequently intersects the adjacent tile above or to the left when those tiles are water/mountain. Steering toward the *center* `(x+0.5, y+0.5)` is equidistant from all four neighbouring tiles, so it never preferentially pokes into any of them. This one change eliminated a large class of "unit walking into walls" bugs.

---

## 9. Stuck Detection & Recovery

Units can get genuinely stuck — wedged in a concave terrain feature, or in a dense crowd that ORCA cannot resolve in time. The system uses a **single periodic position sampler**:

```
Every STUCK_SAMPLE_INTERVAL (100) ticks:
  Compute distance from saved sample position to current position.
  If moved < STUCK_MIN_DISTANCE (2.0 tiles):
    → unit is stuck
```

**Recovery procedure (stuck detected):**
1. Look `STUCK_LOOKAHEAD = 10` waypoints ahead in the unit's current `PathB`. Use that point as an *escape sub-goal*.
2. Run a **bounded local A\*** (`getLocalPath`) from the unit's current tile to the escape sub-goal, confined to a `STUCK_LOCAL_RADIUS = 20` tile bounding box.
3. If a local escape path is found: store it in the cache (keyed from the unit's current cell to the original destination cell) and assign it to the unit. This gives a fresh short detour around the obstacle while preserving the rest of the original route.
4. If the local A\* also fails (unit is fully enclosed): clear `pointTarget`, request a new random target.

**Why not just re-run the full A\*?** The full long-range A\* might return the *same* path that the unit is already stuck on, because the obstacle causing the stuck is a *dynamic* one (a crowd of units, not terrain). The local escape A\* focuses only on the next few tiles and finds a way around the immediate blockage without discarding the rest of the route.

**Skipped when in combat:** if `unit.getCombatTarget() != null && unit.getCombatTarget().isAlive()`, stuck detection is skipped. A combat unit near its target may legitimately not be moving — it is fighting.

**Staggered sampling:** when a unit spawns, `stuckSampleTick` is initialised to a random value in `[0, STUCK_SAMPLE_INTERVAL)` so that units spawned in the same batch do not all trigger their stuck check simultaneously — which would cause a burst of A\* calls and a frame spike.

---

## 10. Lifecycle Methods

| Method | When to call | What it does |
|---|---|---|
| `beginTick()` | **Once per game tick**, before processing any units | Advances `tick`, runs periodic TTL eviction, resets timing counters |
| `computeNextPosition(unit)` | Once per unit per tick | Full per-unit movement computation; returns new `Coordinates` or `null` (no movement) |
| `reset()` | When the world is recreated (new game / map change) | Clears all cached paths, resets `tick = 0` |
| `invalidateCell(cellIdx)` | When a building is placed or destroyed in a cell | Evicts all cached paths passing through that cell |
| `cachedPathCount()` | Diagnostics only | Returns current size of the path cache |

**Critical:** `beginTick()` must be called **exactly once per game tick**, not once per unit. If it were called inside `computeNextPosition`, it would advance `tick` once per unit per tick — with 7 000 units that would advance `tick` by 7 000 per game tick, collapsing the 6 000-tick TTL to less than one real game tick and causing every cached path to expire immediately.

---

## 11. Performance Design Decisions

### Zero heap allocation per tick (after warmup)

All ORCA work arrays are allocated once at construction and reused every tick:

```java
private final OrcaAgentB[] orcaPool       = new OrcaAgentB[ORCA_POOL_SIZE];
private final double[]     orcaCandDistSq = new double[ORCA_POOL_SIZE];
private final double[]     agentRespB     = new double[ORCA_POOL_SIZE];
private final List<OrcaAgentB> agentList  = new ArrayList<>(MAX_ORCA_NEIGHBORS);
private final double[]     agentRespFinal = new double[MAX_ORCA_NEIGHBORS];
private final double[]     lpPtX/Y, lpDrX/Y = ...;
private final double[]     orcaResult     = new double[2];
private final double[]     sepResult      = new double[2];
```

`OrcaAgentB.update()` overwrites the pool objects in-place. `agentList.clear()` + `add()` reuses the list's existing backing array. No `new` is called on the hot path.

### Partial selection sort instead of full sort

`gatherNeighbors` collects all qualifying candidates in a pool array, then uses a **partial selection sort** to find the `MAX_ORCA_NEIGHBORS` closest without fully sorting the pool. For a pool of N candidates taking K of them, this is O(N·K) instead of O(N log N) — and since K = 7 is a constant, it is effectively O(N).

### Cache sub-segments to minimise A\* calls

With sub-segment caching (Section 4.2), a single A\* call seeds the cache for every intermediate cell along the route. In a battle where many units share the same destination, A\* is typically called only once per unique destination cell, regardless of unit count.

### Per-tick timing counters

`PathfindingSystemB` exposes public `long` fields (`tPath`, `tGather`, `tSolve`, `tPhysSep`) that accumulate nanosecond timings for each sub-phase within a tick. `beginTick()` resets them to zero. `Game.tickUnits()` logs these when a tick exceeds 20 ms, allowing performance regressions to be diagnosed without a profiler.

---

## 12. Constants Quick-Reference

| Constant | Location | Value | Purpose |
|---|---|---|---|
| `ADVANCE_THRESHOLD` | PathfindingSystemB | 0.8 tiles | Distance at which a unit advances to the next waypoint |
| `ORCA_TAU` | PathfindingSystemB | 10.0 ticks | ORCA time horizon — how far ahead to look for collisions |
| `ORCA_CELL_RADIUS` | PathfindingSystemB | 0 | Cell window for ORCA neighbour search (0 = own cell only) |
| `MAX_ORCA_NEIGHBORS` | PathfindingSystemB | 7 | Maximum neighbours fed to the ORCA LP per unit per tick |
| `NON_COMBAT_ORCA_INTERVAL` | PathfindingSystemB | 1 | 1 = ORCA every tick; raise to 4 to throttle non-combat units |
| `ORCA_RADIUS_SCALE` | PathfindingSystemB | 0.7 | Scales combined radius in LP constraints — units stand ~30% closer |
| `PHYS_SEP_BLEND` | PathfindingSystemB | 0.4 | Blend factor for physical-separation velocity (0 = old vel, 1 = new dir) |
| `ORCA_POOL_SIZE` | PathfindingSystemB | 128 | Pre-allocated ORCA candidate pool size |
| `JOIN_LOOKAHEAD` | PathfindingSystemB | 20 | Waypoints searched when a unit joins a cached path |
| `EVICT_INTERVAL` | PathfindingSystemB | 200 | Ticks between TTL eviction passes |
| `STUCK_SAMPLE_INTERVAL` | PathfindingSystemB | 100 | Ticks between stuck-detection position samples |
| `STUCK_MIN_DISTANCE` | PathfindingSystemB | 2.0 tiles | Minimum movement in one interval before "stuck" fires |
| `STUCK_LOOKAHEAD` | PathfindingSystemB | 10 | Waypoints ahead used as escape sub-goal during stuck recovery |
| `STUCK_LOCAL_RADIUS` | PathfindingSystemB | 20 | Bounding-box half-size (tiles) for the local escape A\* |
| `TTL_TICKS` | PathCacheB | 6 000 | Ticks of disuse before a cached path is evicted |
| `MAP_CELL_SIZE` | Game | 5 | Tiles per cell side; changing this rebuilds cell indices everywhere |

---

## 13. Edge Cases and Recovery Table

| Situation | What happens |
|---|---|
| Target tile is unwalkable | `clearPointTarget()`, request new random target, return null this tick |
| Path cache miss | A\* computed from unit's current tile to target; result stored in cache with all sub-segments |
| A\* returns null (destination unreachable) | `clearPointTarget()`, request new random target |
| Unit already in attack range | Stand still; if physically overlapped by another unit, one separation step via `computePhysicalSeparation` |
| Unit stuck (< 2 tiles in 100 ticks) | Local bounded A\* toward a waypoint 10 steps ahead; if that also fails, abandon target |
| ORCA LP constraint infeasible (all blocked) | LP returns `(0, 0)` → terrain fallback tries preferred velocity and axis slides |
| Two units physically overlapping (dist < combined radii) | Emergency overlap pre-pass fires; unit moves at full speed away from closest overlapper for 1–3 ticks |
| ORCA pushes unit into terrain | `applyOrcaAndTerrain` step 2: fall back to preferred (A\*) velocity |
| Preferred velocity also into terrain | Axis-slide fallbacks (step 3, 4); finally stand still (step 5) |
| Building placed/removed | `invalidateCell(cellIdx)` evicts all cached paths through that cell |
| World recreated | `reset()` clears entire cache and resets tick counter |
| Unit cached path has wrong destination cell | Detected by `path.destCellIdx != destCellIdx`; new path computed |
| Path waypoints exhausted mid-route | Unit switches to direct steering toward final target center |

---

## 14. Class Map

```
pathfinding/
│
├── PathfindingSystemB.java     — main system class; one instance per game
│       computeNextPosition()   — per-unit per-tick entry point
│       beginTick()             — must be called once per game tick
│       applyOrcaAndTerrain()   — ORCA + terrain collision resolution
│       gatherNeighbors()       — dynamic-radius filter + partial sort
│       computeOrcaVelocity()   — emergency overlap pre-pass + 2D LP
│       computePhysicalSeparation() — overlap separation for in-range units
│       getOrComputePath()      — cache lookup or A* trigger
│       findJoinIndex()         — finds best waypoint join point for a unit
│       checkStuck()            — periodic stuck detection + local recovery
│       reset() / invalidateCell() — lifecycle / invalidation
│
├── PathCacheB.java             — shared path cache
│       get()                   — O(1) cache lookup; updates TTL
│       put()                   — stores full path + sub-segments + reverse index
│       invalidateCell()        — terrain change propagation
│       maybeEvict()            — TTL sweep
│
├── PathB.java                  — immutable cached path
│       points[]                — tile array for O(1) waypoint access
│       cellIndices             — set of cells this path passes through
│       lastUsedTick            — TTL timer
│
├── OrcaAgentB.java             — poolable data carrier for ORCA neighbour state
│       update()                — overwrites fields in-place (zero allocation)
│
├── AStarPathfinder.java        — A* / Theta* implementation
│       getPathBetween()        — full-map Theta* A* (used on cache miss)
│       getLocalPath()          — bounded Theta* A* (used for stuck recovery)
│
└── PathNode.java               — internal A* node (priority queue element)
```

**External dependencies:**

| Class | Used for |
|---|---|
| `empirebuilder.GameManager` | Map access, cell grid access, game reference |
| `empirebuilder.Game` | `getMapCellGrid()`, `getNewPointTarget()`, `isUnitInDestinedPoint()` |
| `empirebuilder.Map` | `isValidAndWalkable()`, `getPoint()`, `hasLineOfSight()` |
| `empirebuilder.MapCell` | `getUnits()` for ORCA neighbour scan |
| `entities.units.Unit` | All unit state: position, velocity, path, target, tier, etc. |
| `empirebuilder.Coordinates` | Return type for new position |
| `empirebuilder.Point` | Tile representation |

---

*Document written for EmpireBuilder, June 2026.*
