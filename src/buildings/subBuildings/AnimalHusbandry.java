package buildings.subBuildings;

import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Game;

public class AnimalHusbandry extends SubBuilding {
    private static final double UPFRONT_COST = 20000;
    public static final int ANIMAL_PRICE = 8000;
    public static final int ANIMAL_RENT_PER_TICK = 75;
    public static final java.util.List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = java.util.List.of();
    private static final double MAINTENANCE_COST_PER_TICK = 80;

    public AnimalHusbandry(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        setValue(3500.0);
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
