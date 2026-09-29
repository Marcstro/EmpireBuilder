package entities.units;

import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;

public class MountedKnight extends Unit {

    static String imageName = "mountedKnightUnit";
    static double mountedKnightDamage = 5;
    static double mountedKnightSpeed = 0.15;
    static double mountedKnightHealth = 80;
    static double size = DEFAULT_UNIT_SIZE;
    static int mountedKnightFactionId = 1;
    static int mountedKnightAttackCooldown = 6;

    public MountedKnight(double x, double y) {
        super(x, y, mountedKnightSpeed, mountedKnightHealth, mountedKnightDamage,
                mountedKnightFactionId, size, mountedKnightAttackCooldown);
        setPriorityTier(1);
    }

    @Override
    public Node createAISystem() {
        return AIProfileFactory.createMeleeDefenderAI();
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
