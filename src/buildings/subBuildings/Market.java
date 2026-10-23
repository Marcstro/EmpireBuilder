package buildings.subBuildings;

import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import empirebuilder.Game;

import java.util.List;

/** One essential supplier per settlement; sales require paid maintenance each building tick. */
public class Market extends SubBuilding {
    public static final int TOOL_PRICE = 100;
    public static final int FERTILISER_PRICE = 1000;
    private static final double MAINTENANCE_COST_PER_TICK = 80;
    public static final List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = List.of(
            SubBuildingRequirement.exclude(SubBuildingRequirement.atLeastOneOf(Market.class)));

    public Market(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        setValue(3500);
        setPermanent(true);
        setUpfrontCost(20000);
    }

    @Override
    public void tick(Game game) {
    }

    @Override
    public double getMaintenanceCost() {
        return MAINTENANCE_COST_PER_TICK;
    }
}
