package buildings.ai.profiles;

import buildings.Building;
import buildings.ai.BuildingAIProfile;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirementsRegistry;

public abstract class SubBuildingAIProfile implements BuildingAIProfile {

    protected int countSubBuildingType(Building building, Class<? extends SubBuilding> type) {
        return (int) building.getSubBuildings().stream()
                .filter(type::isInstance)
                .count();
    }

    protected boolean canBuildSubBuilding(Building building, Class<? extends SubBuilding> type) {
        return SubBuildingRequirementsRegistry.canBuildType(type, building);
    }
}
