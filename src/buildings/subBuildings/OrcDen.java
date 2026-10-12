package buildings.subBuildings;

import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import empirebuilder.Game;
import entities.units.Orc;

public class OrcDen extends UnitProducingSubBuilding {
    private static final int MAX_ORC_SOLDIERS = 100;
    private static final int SPAWN_INTERVAL_BUILDING_TICKS = 30; // ~5s
    private static final double LIFETIME_BUILDING_TICKS = 360; // 1 minute
    private static final double UPFRONT_COST = 50000;
    public static final java.util.List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = java.util.List.of(
        SubBuildingRequirement.anyOf(
            SubBuildingRequirement.atLeastOneOf(WolfDen.class),
            SubBuildingRequirement.atLeastOneOf(GoblinArcheryRange.class),
            SubBuildingRequirement.atLeastOneOf(OrcFortress.class)
        )
    );

    public OrcDen(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        setValue(5500.0);
        setPermanent(false);
        setLifetime(LIFETIME_BUILDING_TICKS);
        setUpfrontCost(UPFRONT_COST);
    }

    @Override
    protected int getMaxCapacity() {
        return MAX_ORC_SOLDIERS;
    }

    @Override
    protected int getSpawnInterval() {
        return SPAWN_INTERVAL_BUILDING_TICKS;
    }

    @Override
    protected void spawnUnit(Game game) {
        Orc orc = new Orc(mainOwner.getPoint().getX(), mainOwner.getPoint().getY());
        orc.setSubBuildingOwner(this);
        sendUnitToOwner(orc, game);
        currentUnitCount++;
    }

    @Override
    public double getMaintenanceCost() {
        return 0;
    }
}
