package pathfinding;

import empirebuilder.Point;

public class PathNode implements Comparable<PathNode> {
    private Point point;
    private PathNode parent;
    private double gCost; // Cost from start to current node
    private double hCost; // Heuristic cost to goal

    public PathNode(Point point, PathNode parent, double gCost, double hCost) {
        this.point = point;
        this.parent = parent;
        this.gCost = gCost;
        this.hCost = hCost;
    }

    public Point getPoint() {
        return point;
    }

    public PathNode getParent() {
        return parent;
    }

    public double getFCost() {
        return gCost + hCost;
    }

    public double getGCost() {
        return gCost;
    }

    public double getHCost() {
        return hCost;
    }

    @Override
    public int compareTo(PathNode other) {
        return Double.compare(this.getFCost(), other.getFCost());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PathNode)) return false;
        return ((PathNode) obj).point.equals(this.point);
    }

    @Override
    public int hashCode() {
        return point.hashCode();
    }
}
