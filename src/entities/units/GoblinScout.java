package entities.units;

import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;

public class GoblinScout extends Unit {

    static String imageName = "goblinScoutUnit";
    static double goblinScoutDamage = 1;
    static double goblinScoutSpeed = 0.14;
    static double goblinScoutHealth = 20;
    static double size = DEFAULT_UNIT_SIZE;
    static int goblinScoutFactionId = 2;
    static int goblinScoutAttackCooldown = 10;

    public GoblinScout(double x, double y) {
        super(x, y, goblinScoutSpeed, goblinScoutHealth, goblinScoutDamage,
                goblinScoutFactionId, size, goblinScoutAttackCooldown);
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
