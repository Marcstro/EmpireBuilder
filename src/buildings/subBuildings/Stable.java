package buildings.subBuildings;

import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Game;

/** Requirement for Mounted knights, does nothing by itself */
public class Stable extends SubBuilding {
    private static final double MAINTENANCE_COST_PER_TICK = 8000.0;
    private static final double UPFRONT_COST = 40000;
    public static final java.util.List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = java.util.List.of(SubBuildingRequirement.atLeastOneOf(Barracks.class));

    public Stable(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        setValue(20000);
        setPermanent(true);
        setUpfrontCost(UPFRONT_COST);
    }

    @Override
    public void tick(Game game) {
    }

    @Override
    public double getMaintenanceCost() {
        return MAINTENANCE_COST_PER_TICK;
    }
}
