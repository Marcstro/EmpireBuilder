package pathfinding;

import empirebuilder.Point;
import empirebuilder.Map;

import java.util.*;

public class AStarPathfinder {
    private final Map map;

    public AStarPathfinder(Map map) {
        this.map = map;
    }

    /**
     * Bounded Theta* (any-angle A*), we first check whether there is
     * a direct line-of-sight from the *parent* of the current node to the neighbour.
     * If yes, we skip the current node as a hop and route grandparent → neighbour
     * directly, producing smooth any-angle paths instead of 45° grid steps.
     */
    public List<Point> getLocalPath(Point start, Point goal, int maxRadius) {
        int minX = Math.max(0,                Math.min(start.getX(), goal.getX()) - maxRadius);
        int maxX = Math.min(map.getWidth()  - 1, Math.max(start.getX(), goal.getX()) + maxRadius);
        int minY = Math.max(0,                Math.min(start.getY(), goal.getY()) - maxRadius);
        int maxY = Math.min(map.getHeight() - 1, Math.max(start.getY(), goal.getY()) + maxRadius);

        PriorityQueue<PathNode>         openSet  = new PriorityQueue<>();
        java.util.Map<Point, PathNode>  allNodes = new HashMap<>();
        Set<Point>                      closed   = new HashSet<>();

        PathNode startNode = new PathNode(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            PathNode current      = openSet.poll();
            Point    currentPoint = current.getPoint();

            if (currentPoint.equals(goal)) return reconstructPath(current);

            closed.add(currentPoint);

            int cx = currentPoint.getX(), cy = currentPoint.getY();
            for (int ddx = -1; ddx <= 1; ddx++) {
                for (int ddy = -1; ddy <= 1; ddy++) {
                    if (ddx == 0 && ddy == 0) continue;
                    int nx = cx + ddx, ny = cy + ddy;
                    if (nx < minX || nx > maxX || ny < minY || ny > maxY) continue;
                    Point neighbor = map.getPoint(nx, ny);
                    if (neighbor == null) continue;
                    if (!neighbor.equals(goal) && !map.isValidAndWalkable(nx, ny)) continue;
                    if (closed.contains(neighbor)) continue;

                    // Diagonal corner-clip prevention: a diagonal step (ddx≠0 AND ddy≠0)
                    // must not squeeze through a wall corner.  Both axis-aligned neighbors
                    // that share the corner must also be walkable, otherwise the unit's
                    // body would have to pass through unwalkable terrain at the corner.
                    if (ddx != 0 && ddy != 0) {
                        if (!map.isValidAndWalkable(cx + ddx, cy)) continue;
                        if (!map.isValidAndWalkable(cx, cy + ddy)) continue;
                    }

                    // --- Theta* line-of-sight shortcut ---
                    // Plain LOS: a cell is reachable if every cell on the line is walkable.
                    // Body-clearance is the responsibility of the steering/movement layer,
                    PathNode parent  = current.getParent();
                    PathNode tryFrom = (parent != null
                            && map.hasLineOfSight(
                                    parent.getPoint().getX(), parent.getPoint().getY(),
                                    nx, ny))
                            ? parent : current;

                    double tentativeG = tryFrom.getGCost()
                            + getMovementCost(tryFrom.getPoint(), neighbor);

                    PathNode neighborNode = allNodes.get(neighbor);
                    if (neighborNode == null || tentativeG < neighborNode.getGCost()) {
                        PathNode newNode = new PathNode(neighbor, tryFrom,
                                tentativeG, heuristic(neighbor, goal));
                        openSet.add(newNode);
                        allNodes.put(neighbor, newNode);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    public List<Point> getPathBetween(Point start, Point goal) {
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        java.util.Map<Point, PathNode> allNodes = new HashMap<>();

        PathNode startNode = new PathNode(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        Set<Point> closedSet = new HashSet<>();

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();
            Point currentPoint = current.getPoint();

            if (currentPoint.equals(goal)) {
                return reconstructPath(current);
            }

            closedSet.add(currentPoint);

            int cx = currentPoint.getX(), cy = currentPoint.getY();
            for (int ddx = -1; ddx <= 1; ddx++) {
                for (int ddy = -1; ddy <= 1; ddy++) {
                    if (ddx == 0 && ddy == 0) continue;
                    int nx = cx + ddx, ny = cy + ddy;
                    if (!map.isValidAndWalkable(nx, ny)) continue;
                    Point neighbor = map.getPoint(nx, ny);
                    if (neighbor == null || closedSet.contains(neighbor)) continue;

                    // Diagonal corner-clip prevention (same rule as getLocalPath)
                    if (ddx != 0 && ddy != 0) {
                        if (!map.isValidAndWalkable(cx + ddx, cy)) continue;
                        if (!map.isValidAndWalkable(cx, cy + ddy)) continue;
                    }

                    double tentativeG = current.getGCost() + getMovementCost(currentPoint, neighbor);
                    PathNode neighborNode = allNodes.get(neighbor);

                    if (neighborNode == null || tentativeG < neighborNode.getGCost()) {
                        double h = heuristic(neighbor, goal);
                        PathNode newNode = new PathNode(neighbor, current, tentativeG, h);
                        openSet.add(newNode);
                        allNodes.put(neighbor, newNode);
                    }
                }
            }
        }

        return Collections.emptyList(); // No path found
    }

    private List<Point> reconstructPath(PathNode endNode) {
        List<Point> path = new LinkedList<>();
        PathNode current = endNode;
        while (current != null) {
            path.add(0, current.getPoint());
            current = current.getParent();
        }
        return path;
    }

    private double heuristic(Point a, Point b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        return (dx + dy) + (1.41 - 2) * Math.min(dx, dy); // Octile distance
    }

    private double getMovementCost(Point from, Point to) {
        int dx = Math.abs(from.getX() - to.getX());
        int dy = Math.abs(from.getY() - to.getY());
        // True Euclidean distance — essential for Theta* which can skip multiple tiles in one hop.
        double dist = Math.sqrt((double) dx * dx + (double) dy * dy);
        return to.getWalkingCost() * dist;
    }
}

