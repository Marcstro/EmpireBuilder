package buildings.subBuildings.subBuildingFunctions;

import java.lang.reflect.Field;
import java.util.List;

public class SubBuildingRequirementsRegistry {

    public static List<SubBuildingRequirement> getRequirements(Class<? extends SubBuilding> clazz) {
        try {
            Field field = clazz.getDeclaredField("REQUIREMENTS");
            return (List<SubBuildingRequirement>) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // If the class doesn't have REQUIREMENTS, assume no requirements
            return List.of();
        }
    }

    public static boolean canBuildType(Class<? extends SubBuilding> clazz, buildings.Building owner) {
        List<SubBuildingRequirement> reqs = getRequirements(clazz);
        return reqs.stream().allMatch(req -> req.isMet(owner));
    }
}
