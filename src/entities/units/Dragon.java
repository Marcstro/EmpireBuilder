package entities.units;


import empirebuilder.Point;
import entities.Entity;

public class Dragon extends Unit{

    static String imageName = "dragonUnit";
    static double dragonDamage = 25;
    static double dragonSpeed = 1.5;
    static double dragonHealth = 300;
    static double size = 0.9;
    static int dragonFaction = 3;

    public Dragon(double x, double y) {
        super(x, y, dragonSpeed, dragonHealth, dragonDamage, dragonFaction, size);
        setPriorityTier(3);
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

