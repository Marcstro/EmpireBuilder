package entities.units;

import buildings.Village;
import empirebuilder.Game;
import empirebuilder.Point;

public abstract class FarmAnimal extends Unit{

    Point idleBasePoint;
    int lifeRemaining;
    private boolean expired = false;
    final static int lifeSpan = 3000;
    private static final int VALUE = 200;

    public FarmAnimal(double x, double y, double speed, double health, double damage, int factionId, double size, int attackCooldownBase) {
        super(x, y, speed, health, damage, factionId, size, attackCooldownBase);
        value = VALUE;
        lifeRemaining = lifeSpan;
    }

    // TODO change how farm animals get their idle point, currently they fetch their owners point instead of this
    public Point getIdleBasePoint() {
        return idleBasePoint;
    }

    public void setIdleBasePoint(Point idleBasePoint) {
        this.idleBasePoint = idleBasePoint;
    }

    public boolean hasExpired() {
        return expired;
    }

    @Override
    public void setIsAlive(boolean alive) {
        boolean wasAlive = isAlive();
        super.setIsAlive(alive);
        if (wasAlive && !alive && getUnitOwner() instanceof Village village) {
            village.animalDied();
        }
    }

    @Override
    public void setAlive(boolean alive) {
        setIsAlive(alive);
    }

    @Override
    public void tick(Game game) {
        super.tick(game);
        if (!isAlive()) {
            return;
        }
        lifeRemaining--;
        if (lifeRemaining <= 0) {
            expired = true;
            setIsAlive(false);
        }
    }
}
