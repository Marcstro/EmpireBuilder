package entities.units;

import empirebuilder.Point;
import entities.Entity;
import entities.units.AI.AIProfileFactory;
import entities.units.AI.Node;

public class Capybara extends FarmAnimal {

    static String imageName = "capybaraUnit";
    static double capybaraDamage = 0;
    static double capybaraSpeed = 0.1;
    static double capybaraHealth = 8;
    static double size = DEFAULT_UNIT_SIZE;
    static int capybaraFactionId = 1;
    static int attackCooldownBase = 50;

    public Capybara(double x, double y) {
        super(x, y, capybaraSpeed, capybaraHealth, capybaraDamage, capybaraFactionId, size, attackCooldownBase);
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
