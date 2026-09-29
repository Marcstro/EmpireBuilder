package entities.units;

import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;

public class FarmerMilitia extends Unit {

    static String imageName = "farmerMilitiaUnit";
    static double farmerMilitiaDamage = 1;
    static double farmerMilitiaSpeed = 0.1;
    static double farmerMilitiaHealth = 15;
    static double size = DEFAULT_UNIT_SIZE;
    static int farmerMilitiaFactionId = 1;
    static int farmerMilitiaAttackCooldown = 8;

    public FarmerMilitia(double x, double y) {
        super(x, y, farmerMilitiaSpeed, farmerMilitiaHealth, farmerMilitiaDamage,
                farmerMilitiaFactionId, size, farmerMilitiaAttackCooldown);
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
