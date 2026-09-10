package entities.units;

import empirebuilder.Point;

public abstract class FarmAnimal extends Unit{

    Point idleBasePoint;

    public FarmAnimal(double x, double y, double speed, double health, double damage, int factionId, double size, int attackCooldownBase) {
        super(x, y, speed, health, damage, factionId, size, attackCooldownBase);
    }

    public Point getIdleBasePoint() {
        return idleBasePoint;
    }

    public void setIdleBasePoint(Point idleBasePoint) {
        this.idleBasePoint = idleBasePoint;
    }
}
