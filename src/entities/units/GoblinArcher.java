package entities.units;


import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;

public class GoblinArcher extends Unit{

    static String imageName = "goblinArcher";
    static double goblinArcherDamage = 1;
    static double goblinArcherSpeed = 0.1;
    static double goblinArcherHealth = 15;
    static double size = DEFAULT_UNIT_SIZE;
    static int goblinArcherFactionId = 2;
    static double attackRange = 2.0;
    static int goblinArcherAttackCooldown = 12;

    public GoblinArcher(double x, double y) {
        super(x, y, goblinArcherSpeed, goblinArcherHealth, goblinArcherDamage, goblinArcherFactionId, size, goblinArcherAttackCooldown);
    }

    @Override
    public Node createAISystem() {
        unitorder= UnitOrder.RAIDING;
        return AIProfileFactory.createRangedAttackerAI();
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


