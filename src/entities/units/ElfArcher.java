package entities.units;


import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;

public class ElfArcher extends Unit{

    static String imageName = "elfArcher";
    static double elfArcherDamage = 1;
    static double elfArcherSpeed = 0.1;
    static double elfArcherHealth = 15;
    static double size = DEFAULT_UNIT_SIZE;
    static int elfArcherFactionId = 1;
    static double attackRange = 2.4;
    static int elfArcherAttackCooldown = 12;

    public ElfArcher(double x, double y) {
        super(x, y, elfArcherSpeed, elfArcherHealth, elfArcherDamage, elfArcherFactionId, size, elfArcherAttackCooldown);
    }

    @Override
    public Node createAISystem() {
        return AIProfileFactory.createRangedDefenderAI();
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


