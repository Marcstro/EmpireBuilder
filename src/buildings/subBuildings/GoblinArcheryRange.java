package buildings.subBuildings;

import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import empirebuilder.Game;
import entities.units.GoblinArcher;

public class GoblinArcheryRange extends UnitProducingSubBuilding {
    private static final int MAX_ARCHERS = 100;
    private static final int SPAWN_INTERVAL_BUILDING_TICKS = 30;
    private static final double LIFETIME_BUILDING_TICKS = 360;
    private static final double UPFRONT_COST = 40000;
    public static final java.util.List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = java.util.List.of(
        SubBuildingRequirement.anyOf(
            SubBuildingRequirement.atLeastOneOf(OrcDen.class),
            SubBuildingRequirement.atLeastOneOf(OrcFortress.class)
        )
    );

    public GoblinArcheryRange(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        setValue(4800.0);
        setPermanent(false);
        setLifetime(LIFETIME_BUILDING_TICKS);
        setUpfrontCost(UPFRONT_COST);
    }

    @Override
    protected int getMaxCapacity() {
        return MAX_ARCHERS;
    }

    @Override
    protected int getSpawnInterval() {
        return SPAWN_INTERVAL_BUILDING_TICKS;
    }

    @Override
    protected void spawnUnit(Game game) {
        GoblinArcher archer = new GoblinArcher(mainOwner.getPoint().getX(), mainOwner.getPoint().getY());
        archer.setSubBuildingOwner(this);
        sendUnitToOwner(archer, game);
        currentUnitCount++;
    }

    @Override
    public double getMaintenanceCost() {
        return 0;
    }
}
