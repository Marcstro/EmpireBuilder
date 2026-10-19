package buildings;

import empirebuilder.Point;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class LandOwningBuilding extends Building {

    private final Set<Point> controlledLand = new HashSet<>();

    protected LandOwningBuilding(Point point, Color color, double health) {
        super(point, color, health);
    }

    public Set<Point> getControlledLand() {
        return Collections.unmodifiableSet(controlledLand);
    }

    public void setControlledLand(Set<Point> points) {
        Set<Point> newLand = new HashSet<>(points);
        for (Point point : new HashSet<>(controlledLand)) {
            if (!newLand.contains(point)) {
                removeFromControlledLand(point);
            }
        }
        for (Point point : newLand) {
            addToControlledLand(point);
        }
    }

    public void removeFromControlledLand(Point point) {
        controlledLand.remove(point);
        if (point.getPointOwner() == this) {
            point.setOwnerBuilding(null);
        }
    }

    public void addToControlledLand(Point point) {
        controlledLand.add(point);
        if (point.getPointOwner() != this) {
            point.setOwnerBuilding(this);
        }
    }
}
