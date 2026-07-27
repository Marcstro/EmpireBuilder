package buildings;

import empirebuilder.Game;
import entities.units.Unit;

public interface AttackCapableBuilding {
    double getAttackRange();
    void setTarget(Unit unit);
    void tickAttack(Game game);
    void setAttackReady(boolean state);
    int getAttackCooldown();
    void resetAttackCoolDown();
    boolean isAttackReady();
    double getX();
    double getY();
    int getFactionId();


    void resetTimeSinceLastShot();
    boolean hasSufficientTimePassedSinceLastShot();
    void setShootCooldown(int cooldown);
}
