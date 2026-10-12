package buildings.subBuildings;

import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import empirebuilder.Game;
import entities.units.WolfRider;

public class WolfDen extends UnitProducingSubBuilding {
    private static final int MAX_WOLF_RIDERS = 100;
    private static final int SPAWN_INTERVAL_BUILDING_TICKS = 30;
    private static final double LIFETIME_BUILDING_TICKS = 360;
    private static final double UPFRONT_COST = 30000;
    public static final java.util.List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = java.util.List.of(
        SubBuildingRequirement.anyOf(
            SubBuildingRequirement.countOf(GoblinHut.class, 3),
            SubBuildingRequirement.anyOf(
                SubBuildingRequirement.atLeastOneOf(OrcDen.class),
                SubBuildingRequirement.atLeastOneOf(GoblinArcheryRange.class),
                SubBuildingRequirement.atLeastOneOf(OrcFortress.class)
            )
        )
    );

    public WolfDen(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        setValue(4500.0);
        setPermanent(false);
        setLifetime(LIFETIME_BUILDING_TICKS);
        setUpfrontCost(UPFRONT_COST);
    }

    @Override
    protected int getMaxCapacity() {
        return MAX_WOLF_RIDERS;
    }

    @Override
    protected int getSpawnInterval() {
        return SPAWN_INTERVAL_BUILDING_TICKS;
    }

    @Override
    protected void spawnUnit(Game game) {
        WolfRider rider = new WolfRider(mainOwner.getPoint().getX(), mainOwner.getPoint().getY());
        rider.setSubBuildingOwner(this);
        sendUnitToOwner(rider, game);
        currentUnitCount++;
    }

    @Override
    public double getMaintenanceCost() {
        return 0;
    }
}
