package buildings.subBuildings;

import buildings.UnitOwner;
import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import empirebuilder.Game;
import entities.units.MountedKnight;

public class MountedKnightSchool extends UnitProducingSubBuilding {
    private static final int MAX_MOUNTED_KNIGHTS = 10;
    private static final int SPAWN_INTERVAL_BUILDING_TICKS = 90;
    private static final double MAINTENANCE_COST_PER_TICK = 10000.0;
    private static final double UPFRONT_COST = 50000;
    public static final java.util.List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = java.util.List.of(SubBuildingRequirement.atLeastOneOf(Stable.class));

    public MountedKnightSchool(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        setValue(6000.0);
        setPermanent(true);
        setUpfrontCost(UPFRONT_COST);
    }

    @Override
    protected int getMaxCapacity() {
        return MAX_MOUNTED_KNIGHTS;
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

        MountedKnight knight = new MountedKnight(mainOwner.getPoint().getX(), mainOwner.getPoint().getY());
        knight.setUnitOwner(unitOwner);
        knight.setSubBuildingOwner(this);
        game.spawnUnitAt(knight, mainOwner.getPoint());
        unitOwner.getUnitManagerComponent().addUnit(knight);
        currentUnitCount++;
    }

    @Override
    public double getMaintenanceCost() {
        return MAINTENANCE_COST_PER_TICK;
    }
}
