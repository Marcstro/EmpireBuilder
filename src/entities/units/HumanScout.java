package entities.units;

import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;

public class HumanScout extends Unit {

    static String imageName = "humanScoutUnit";
    static double humanScoutDamage = 1;
    static double humanScoutSpeed = 0.13;
    static double humanScoutHealth = 20;
    static double size = DEFAULT_UNIT_SIZE;
    static int humanScoutFactionId = 1;
    static int humanScoutAttackCooldown = 6;

    public HumanScout(double x, double y) {
        super(x, y, humanScoutSpeed, humanScoutHealth, humanScoutDamage,
                humanScoutFactionId, size, humanScoutAttackCooldown);
        setPriorityTier(1);
    }

    @Override
    public Node createAISystem() {
        return AIProfileFactory.createScoutAI();
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
