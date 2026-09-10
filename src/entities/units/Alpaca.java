package entities.units;

import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;

public class Alpaca extends FarmAnimal {

    static String imageName = "alpacaUnit";
    static double alpacaDamage = 0;
    static double alpacaSpeed = 0.1;
    static double alpacaHealth = 8;
    static double size = DEFAULT_UNIT_SIZE;
    static int alpacaFactionId = 1;
    static int attackCooldownBase = 50;

    public Alpaca(double x, double y) {
        super(x, y, alpacaSpeed, alpacaHealth, alpacaDamage, alpacaFactionId, size, attackCooldownBase);
        setPriorityTier(0);
    }

    @Override
    public Node createAISystem() {
        return AIProfileFactory.justIdleAI();
    }

    @Override
    public String getImageName() {
        return imageName;
    }

    @Override
    public double getAttackRange() {
        return 0;
    }

    @Override
    public CombatStyle getCombatStyle() {
        return CombatStyle.NONE;
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
