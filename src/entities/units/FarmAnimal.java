package entities.units;

import buildings.Village;
import empirebuilder.Game;
import empirebuilder.Point;

public abstract class FarmAnimal extends Unit{

    Point idleBasePoint;
    int lifeRemaining;
    final static int lifeSpan = 20000;

    public FarmAnimal(double x, double y, double speed, double health, double damage, int factionId, double size, int attackCooldownBase) {
        super(x, y, speed, health, damage, factionId, size, attackCooldownBase);
        lifeRemaining = lifeSpan;
    }

    public Point getIdleBasePoint() {
        return idleBasePoint;
    }

    public void setIdleBasePoint(Point idleBasePoint) {
        this.idleBasePoint = idleBasePoint;
    }

    @Override
    public void tick(Game game) {
        super.tick(game);
        lifeRemaining--;
        if (lifeRemaining <= 0) {
            ((Village)getUnitOwner()).animalDied();
            setIsAlive(false);
        }
    }
}
