package buildings.subBuildings;

import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Game;
import entities.units.GoblinScout;

public class SimpleGoblinScoutHut extends SubBuilding {
    private static final int MAX_SCOUTS = 2;
    private static final int SPAWN_INTERVAL_BUILDING_TICKS = 15; // ~20s
    private static final double MAINTENANCE_COST_PER_TICK = 0;
    public static final java.util.List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = java.util.List.of();

    private int scoutCount = 0;
    private int spawnTimer = 0;

    public SimpleGoblinScoutHut(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        setValue(2000.0);
        setPermanent(true);
        setUpfrontCost(10000);
    }

    @Override
    public void tick(Game game) {
        if (scoutCount < MAX_SCOUTS) {
            spawnTimer++;
            if (spawnTimer >= SPAWN_INTERVAL_BUILDING_TICKS) {
                spawnTimer = 0;
                spawnScout(game);
            }
        }
    }

    private void spawnScout(Game game) {
        GoblinScout scout = new GoblinScout(mainOwner.getPoint().getX(), mainOwner.getPoint().getY());
        sendUnitToOwner(scout, game);
        scoutCount++;
    }

    public void onScoutDeath() {
        scoutCount = Math.max(0, scoutCount - 1);
    }

    @Override
    public double getMaintenanceCost() {
        return MAINTENANCE_COST_PER_TICK;
    }

    public int getScoutCount() {
        return scoutCount;
    }

    public int getMaximumScoutCount() {
        return MAX_SCOUTS;
    }

    public int getTicksUntilNextSpawn() {
        if (scoutCount >= MAX_SCOUTS) {
            return -1;
        }
        return Math.max(0, SPAWN_INTERVAL_BUILDING_TICKS - spawnTimer);
    }
}
