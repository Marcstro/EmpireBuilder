package entities.units;


import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;

public class TradingCart extends Unit{

    static String imageName = "tradingCartUnit";
    static double tradingCartDamage = 1;
    static double tradingCartSpeed = 0.7;
    static double tradingCartHealth = 12;
    static double size = DEFAULT_UNIT_SIZE;
    static int tradingCartFactionId = 1;
    static int tradingCartAttackCooldown = 6;

    public TradingCart(double x, double y) {
        super(x, y, tradingCartSpeed, tradingCartHealth, tradingCartDamage, tradingCartFactionId, size, tradingCartAttackCooldown);
        setPriorityTier(1);
    }

    @Override
    public Node createAISystem() {
        return AIProfileFactory.createTraderAI();
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
