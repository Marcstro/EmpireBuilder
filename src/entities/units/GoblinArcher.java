package entities.units;


import empirebuilder.Point;
import entities.Entity;

public class GoblinArcher extends Unit{

    static String imageName = "goblinArcher";
    static double goblinArcherDamage = 1;
    static double goblinArcherSpeed = 0.1;
    static double goblinArcherHealth = 15;
    static double size = DEFAULT_UNIT_SIZE;
    static int goblinArcherFactionId = 2;
    static double attackRange = 2.0;

    public GoblinArcher(double x, double y) {
        super(x, y, goblinArcherSpeed, goblinArcherHealth, goblinArcherDamage, goblinArcherFactionId, size);
    }

    @Override
    public String getImageName() {
        return imageName;
    }

    @Override
    public double getAttackRange() {
        return attackRange;
    }

    @Override
    public CombatStyle getCombatStyle() {
        return CombatStyle.RANGED;
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


