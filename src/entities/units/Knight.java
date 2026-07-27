package entities.units;


import empirebuilder.Point;
import entities.Entity;

public class Knight extends Unit{

    static String imageName = "knightUnit";
    static double knightDamage = 1;
    static double knightSpeed = 0.1;
    static double knightHealth = 30;
    static double size = DEFAULT_UNIT_SIZE;
    static int knightFactionId = 1;

    public Knight(double x, double y) {
        super(x, y, knightSpeed, knightHealth, knightDamage, knightFactionId, size);
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

