package buildings.ai;

/**
Current not implemented fully. Will perhaps be used when comparing the hostility
 between different buildings
 */
public enum GoodSideBuildingStates implements BuildingState {

    // 0 danger measured
    SAFE(0, 0.0),

    // enemies detected, no hostile actions
    WATCHFUL(10, 0.2),

    // detected hostile actions
    DEFENDING(30, 0.6),

    // losing battles, requesting backup
    EMBATTLED(60, 0.95),

    // enemies are attacking the building, engaging militia, requesting ALL assistance
    FULL_ALERT(85, 1.0);

    private final int minMeter;
    private final double militaryShare;

    GoodSideBuildingStates(int minMeter, double militaryShare) {
        this.minMeter = minMeter;
        this.militaryShare = militaryShare;
    }

    public int getMinMeter() {
        return minMeter;
    }

    public double getMilitaryShare() {
        return militaryShare;
    }

    public double getEconomyShare() {
        return 1.0 - militaryShare;
    }

    /** Returns the highest band whose threshold the given meter value reaches. */
    public static GoodSideBuildingStates fromMeter(double meter) {
        GoodSideBuildingStates result = SAFE;
        for (GoodSideBuildingStates candidate : values()) {
            if (meter >= candidate.minMeter) {
                result = candidate;
            }
        }
        return result;
    }
}
