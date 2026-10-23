package buildings.subBuildings.subBuildingFunctions;

import buildings.Building;
import buildings.subBuildings.AnimalHusbandry;
import buildings.subBuildings.Barracks;
import buildings.subBuildings.ElvenArcheryRange;
import buildings.subBuildings.GoblinArcheryRange;
import buildings.subBuildings.GoblinHut;
import buildings.subBuildings.MountedKnightSchool;
import buildings.subBuildings.Market;
import buildings.subBuildings.OrcDen;
import buildings.subBuildings.OrcFortress;
import buildings.subBuildings.ShootingTower;
import buildings.subBuildings.SimpleGoblinScoutHut;
import buildings.subBuildings.Stable;
import buildings.subBuildings.WolfDen;

import java.util.List;
import java.util.Map;

public class SubBuildingRequirementsRegistry {

    private static final Map<Class<? extends SubBuilding>, List<SubBuildingRequirement>> REQUIREMENTS_BY_TYPE =
            Map.ofEntries(
                    Map.entry(AnimalHusbandry.class, AnimalHusbandry.SUB_BUILDING_REQUIREMENTS),
                    Map.entry(Market.class, Market.SUB_BUILDING_REQUIREMENTS),
                    Map.entry(Barracks.class, Barracks.SUB_BUILDING_REQUIREMENTS),
                    Map.entry(ElvenArcheryRange.class, ElvenArcheryRange.SUB_BUILDING_REQUIREMENTS),
                    Map.entry(GoblinArcheryRange.class, GoblinArcheryRange.SUB_BUILDING_REQUIREMENTS),
                    Map.entry(GoblinHut.class, GoblinHut.SUB_BUILDING_REQUIREMENTS),
                    Map.entry(MountedKnightSchool.class, MountedKnightSchool.SUB_BUILDING_REQUIREMENTS),
                    Map.entry(OrcDen.class, OrcDen.SUB_BUILDING_REQUIREMENTS),
                    Map.entry(OrcFortress.class, OrcFortress.SUB_BUILDING_REQUIREMENTS),
                    Map.entry(ShootingTower.class, ShootingTower.SUB_BUILDING_REQUIREMENTS),
                    Map.entry(SimpleGoblinScoutHut.class, SimpleGoblinScoutHut.SUB_BUILDING_REQUIREMENTS),
                    Map.entry(Stable.class, Stable.SUB_BUILDING_REQUIREMENTS),
                    Map.entry(WolfDen.class, WolfDen.SUB_BUILDING_REQUIREMENTS)
            );

    public static List<SubBuildingRequirement> getRequirements(Class<? extends SubBuilding> type) {
        List<SubBuildingRequirement> requirements = REQUIREMENTS_BY_TYPE.get(type);
        if (requirements == null) {
            throw new IllegalArgumentException("No requirements registered for " + type.getSimpleName());
        }
        return requirements;
    }

    public static boolean canBuildType(Class<? extends SubBuilding> type, Building owner) {
        List<SubBuildingRequirement> reqs = getRequirements(type);
        return reqs.stream().allMatch(req -> req.isMet(owner));
    }
}
