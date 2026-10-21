package buildings.subBuildings;

import buildings.VillageOwningBuilding;
import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Game;

public class AnimalHusbandry extends SubBuilding {
    private static final double UPFRONT_COST = 20000;
    public static final java.util.List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = java.util.List.of();
    int animalSpawnCooldownBase = 73;
    int animalSpawnCooldown = 0;

    public AnimalHusbandry(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        setValue(3500.0);
        setPermanent(true);
        setUpfrontCost(UPFRONT_COST);
    }

    @Override
    public void tick(Game game) {

        animalSpawnCooldown--;
        if (animalSpawnCooldown <= 0) {
            animalSpawnCooldown = animalSpawnCooldownBase;
            if (mainOwner instanceof VillageOwningBuilding){
                ((VillageOwningBuilding) mainOwner).attemptToSpawnAnimal(game);
            }
        }
    }

    @Override
    public double getMaintenanceCost() {
        return 0;
    }
}
