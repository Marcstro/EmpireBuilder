package entities.units;

import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;

public class WolfRider extends Unit {

    static String imageName = "wolfRiderUnit";
    static double wolfRiderDamage = 3.5;
    static double wolfRiderSpeed = 0.15;
    static double wolfRiderHealth = 40;
    static double size = DEFAULT_UNIT_SIZE;
    static int wolfRiderFactionId = 2;
    static int wolfRiderAttackCooldown = 7;

    public WolfRider(double x, double y) {
        super(x, y, wolfRiderSpeed, wolfRiderHealth, wolfRiderDamage, wolfRiderFactionId, size, wolfRiderAttackCooldown);
        setPriorityTier(1);
    }

    @Override
    public Node createAISystem() {
        return AIProfileFactory.createMeleeAttackerAI();
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
