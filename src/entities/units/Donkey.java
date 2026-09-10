package entities.units;

import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;

public class Donkey extends FarmAnimal {

    static String imageName = "donkeyUnit";
    static double donkeyDamage = 0;
    static double donkeySpeed = 0.1;
    static double donkeyHealth = 8;
    static double size = DEFAULT_UNIT_SIZE;
    static int donkeyFactionId = 1;
    static double donkeyAttackCooldown = 50;

    public Donkey(double x, double y) {
        super(x, y, donkeySpeed, donkeyHealth, donkeyDamage, donkeyFactionId, size, (int)donkeyAttackCooldown);
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
