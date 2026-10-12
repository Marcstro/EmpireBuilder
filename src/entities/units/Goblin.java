package entities.units;

import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;

public class Goblin extends Unit {

    static String imageName = "goblinUnit";
    static double goblinDamage = 2.5;
    static double goblinSpeed = 0.12;
    static double goblinHealth = 30;
    static double size = DEFAULT_UNIT_SIZE;
    static int goblinFactionId = 2;
    static int goblinAttackCooldown = 5;

    public Goblin(double x, double y) {
        super(x, y, goblinSpeed, goblinHealth, goblinDamage, goblinFactionId, size, goblinAttackCooldown);
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
