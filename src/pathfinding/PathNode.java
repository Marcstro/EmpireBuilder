package pathfinding;

import empirebuilder.Point;

/**
 * A single node in an A* / Theta* search.
 * Implements Comparable so it can be used directly in a PriorityQueue (min-heap on f = g + h).
 */
public class PathNode implements Comparable<PathNode> {

    private final Point    point;
    private final PathNode parent;
    private final double   gCost;
    private final double   fCost;

    public PathNode(Point point, PathNode parent, double gCost, double hCost) {
        this.point  = point;
        this.parent = parent;
        this.gCost  = gCost;
        this.fCost  = gCost + hCost;
    }

    public Point    getPoint()  { return point;  }
    public PathNode getParent() { return parent; }
    public double   getGCost()  { return gCost;  }

    @Override
    public int compareTo(PathNode other) {
        return Double.compare(this.fCost, other.fCost);
    }
}

