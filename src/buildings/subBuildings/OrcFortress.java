package buildings.subBuildings;

import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import empirebuilder.Game;
import entities.units.OrcSoldier;

public class OrcFortress extends UnitProducingSubBuilding {
    private static final int MAX_ORC_SOLDIERS = 100;
    private static final int SPAWN_INTERVAL_BUILDING_TICKS = 30; // ~5s
    private static final double LIFETIME_BUILDING_TICKS = 360; // 1 minute
    private static final double UPFRONT_COST = 80000;
    public static final java.util.List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = java.util.List.of(
        SubBuildingRequirement.allOf(
            SubBuildingRequirement.atLeastOneOf(OrcDen.class),
            SubBuildingRequirement.anyOf(
                SubBuildingRequirement.atLeastOneOf(GoblinArcheryRange.class),
                SubBuildingRequirement.atLeastOneOf(WolfDen.class)
            )
        )
    );

    public OrcFortress(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        setValue(6500.0);
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
        OrcSoldier orc = new OrcSoldier(mainOwner.getPoint().getX(), mainOwner.getPoint().getY());
        orc.setSubBuildingOwner(this);
        sendUnitToOwner(orc, game);
        currentUnitCount++;
    }

    @Override
    public double getMaintenanceCost() {
        return 0;
    }
}
