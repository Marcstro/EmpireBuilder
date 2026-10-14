package buildings.subBuildings.subBuildingFunctions;

import buildings.Building;

import java.util.Arrays;
import java.util.function.Function;

public class SubBuildingRequirement {

    private final Function<Building, Boolean> check;

    private SubBuildingRequirement(Function<Building, Boolean> check) {
        this.check = check;
    }

    public boolean isMet(Building building) {
        return check.apply(building);
    }

    public static SubBuildingRequirement atLeastOneOf(Class<? extends SubBuilding> targetClass) {
        return new SubBuildingRequirement(building -> countSubBuildingType(building, targetClass) > 0);
    }

    public static SubBuildingRequirement countOf(Class<? extends SubBuilding> targetClass, int minimumCount) {
        return new SubBuildingRequirement(building -> countSubBuildingType(building, targetClass) >= minimumCount);
    }

    public static SubBuildingRequirement anyOf(SubBuildingRequirement... subBuildingRequirements) {
        return new SubBuildingRequirement(building ->
            Arrays.stream(subBuildingRequirements).anyMatch(req -> req.isMet(building))
        );
    }

    public static SubBuildingRequirement allOf(SubBuildingRequirement... subBuildingRequirements) {
        return new SubBuildingRequirement(building ->
            Arrays.stream(subBuildingRequirements).allMatch(req -> req.isMet(building))
        );
    }

    public static SubBuildingRequirement exclude(SubBuildingRequirement subBuildingRequirement) {
        return new SubBuildingRequirement(building -> !subBuildingRequirement.isMet(building));
    }

    private static int countSubBuildingType(Building building, Class<? extends SubBuilding> type) {
        int count = 0;

        if (building instanceof buildings.Town town) {
            for (var area : town.getTownAreaPoints()) {
                SubBuilding sub = area.getSubBuilding();
                if (sub != null && type.isInstance(sub)) {
                    count++;
                }
            }
        } else if (building instanceof buildings.City city) {
            for (var area : city.getCityAreaPoints()) {
                SubBuilding sub = area.getSubBuilding();
                if (sub != null && type.isInstance(sub)) {
                    count++;
                }
            }
        } else if (building instanceof buildings.EvilBase base) {
            for (var area : base.getEvilBaseAreas()) {
                SubBuilding sub = area.getSubBuilding();
                if (sub != null && type.isInstance(sub)) {
                    count++;
                }
            }
        }

        return count;
    }
}
