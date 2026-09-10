package entities.units;


import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;

public class Dragon extends Unit{

    static String imageName = "dragonUnit";
    static double dragonDamage = 50;
    static double dragonSpeed = 0.7;
    static double dragonHealth = 300;
    static double size = 0.9;
    static int dragonFaction = 2;
    static int attackCooldownBase = 50;

    public Dragon(double x, double y) {
        super(x, y, dragonSpeed, dragonHealth, dragonDamage, dragonFaction, size, attackCooldownBase);
        setPriorityTier(3);
    }

    @Override
    public Node createAISystem() {
        unitorder= UnitOrder.RAIDING;
        return AIProfileFactory.createDestructiveAttackerAI();
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

