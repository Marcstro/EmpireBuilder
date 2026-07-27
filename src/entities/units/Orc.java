package entities.units;

import empirebuilder.Point;
import entities.Entity;

public class Orc extends Unit{

    static String imageName = "orcUnit";
    static double orcDamage = 3;
    static double orcSpeed = 0.1;
    static double orcHealth = 50;
    static double size = DEFAULT_UNIT_SIZE;
    static int orcFactionId = 2;

    public Orc(double x, double y) {
        super(x, y, orcSpeed, orcHealth, orcDamage, orcFactionId, size);
        setPriorityTier(1);
    }

    @Override
    public String getImageName() {
        return imageName;
    }

    @Override
    public double getAttackRange() {
        return getMeleeRange();
    }

    @Override
    public CombatStyle getCombatStyle() {
        return CombatStyle.MELEE;
    }

    public Entity getCombatTarget() {
        return combatTarget;
    }

    public void setCombatTarget(Entity combatTarget) {
        this.combatTarget = combatTarget;
    }

    public Point getPointTarget() {
        return pointTarget;
    }

    public void setPointTarget(Point pointTarget) {
        this.pointTarget = pointTarget;
    }
}
