package buildings.ai;

public enum ThreatFact {

    ENEMY_SCOUT_DETECTED(15, 5000),

    /** farms have been plundered or worse */
    OWNED_AREA_RAIDED(35, 200),

    LOSING_UNITS(25, 300),

    ENEMIES_WITHIN_SHOOTING_DISTANCE(60, 200),

    UNDER_DIRECT_ATTACK(85, 50),

    ALLY_NEEDS_HELP(10, 100);

    private final int weight;
    private final int cooldownTicks;

    ThreatFact(int weight, int cooldownTicks) {
        this.weight = weight;
        this.cooldownTicks = cooldownTicks;
    }

    public int getWeight() {
        return weight;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }
}
