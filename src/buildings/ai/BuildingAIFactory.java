package buildings.ai;

import buildings.Building;
import buildings.City;
import buildings.EvilSideBase;
import buildings.Town;
import buildings.ai.profiles.CityAI;
import buildings.ai.profiles.EvilSideBaseAI;
import buildings.ai.profiles.TownAI;

public class BuildingAIFactory {

    private static final int DEFAULT_BUILDINGAI_INTERVAL = 20;

    public static BuildingAI createAI(Building building) {
        if (building instanceof Town) {
            return new BuildingAI(new TownAI(), DEFAULT_BUILDINGAI_INTERVAL);
        }
        if (building instanceof City) {
            return new BuildingAI(new CityAI(), DEFAULT_BUILDINGAI_INTERVAL);
        }
        if (building instanceof EvilSideBase) {
            return new BuildingAI(new EvilSideBaseAI(), DEFAULT_BUILDINGAI_INTERVAL);
        }
        System.out.println("this shouldnt happen, building: " + building.getClass().getSimpleName() + " has no AI");
        // Villages, Farms and area-buildings have no AI
        return null;
    }
}
