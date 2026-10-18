package buildings.subBuildings;

import buildings.UnitOwner;
import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import empirebuilder.Game;
import entities.units.ElfArcher;

public class ElvenArcheryRange extends UnitProducingSubBuilding {
    private static final int MAX_ARCHERS = 10;
    private static final int SPAWN_INTERVAL_BUILDING_TICKS = 90;
    private static final double MAINTENANCE_COST_PER_TICK = 600;
    private static final double UPFRONT_COST = 30000;
    public static final java.util.List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = java.util.List.of(SubBuildingRequirement.atLeastOneOf(Barracks.class));

    public ElvenArcheryRange(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        setValue(5500.0);
        setPermanent(true);
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
        if (!(mainOwner instanceof UnitOwner unitOwner)) {
            return;
        }

        ElfArcher archer = new ElfArcher(mainOwner.getPoint().getX(), mainOwner.getPoint().getY());
        archer.setSubBuildingOwner(this);
        unitOwner.getUnitManagerComponent().receiveUnit(archer, game);
        currentUnitCount++;
    }

    @Override
    public double getMaintenanceCost() {
        return MAINTENANCE_COST_PER_TICK;
    }
}
