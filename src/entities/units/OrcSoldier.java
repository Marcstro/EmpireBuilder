package entities.units;

import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;

public class OrcSoldier extends Unit {

    static String imageName = "orcSoldierUnit";
    static double orcSoldierDamage = 6;
    static double orcSoldierSpeed = 0.11;
    static double orcSoldierHealth = 60;
    static double size = DEFAULT_UNIT_SIZE;
    static int orcSoldierFactionId = 2;
    static int orcSoldierAttackCooldown = 5;

    public OrcSoldier(double x, double y) {
        super(x, y, orcSoldierSpeed, orcSoldierHealth, orcSoldierDamage,
                orcSoldierFactionId, size, orcSoldierAttackCooldown);
        setPriorityTier(1);
    }

    @Override
    public Node createAISystem() {
        unitorder = UnitOrder.RAIDING;
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
