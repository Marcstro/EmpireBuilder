package pathfinding;

import empirebuilder.Point;
import empirebuilder.Map;

import java.util.*;

public class AStarPathfinder {
    private final Map map;

    public AStarPathfinder(Map map) {
        this.map = map;
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

            for (Point neighbor : map.getAllWalkableValidNeighbours(currentPoint)) {
                if (closedSet.contains(neighbor)) continue;

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
        double diagonalModifier = (dx == 1 && dy == 1) ? 1.41 : 1.0;
        return to.getWalkingCost() * diagonalModifier;
    }
}

