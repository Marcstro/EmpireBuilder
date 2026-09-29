package buildings.ai;

import buildings.Building;
import buildings.ai.profiles.CityAI;
import buildings.ai.profiles.TownAI;

/**
 * Factory for creating building AI profiles based on building type.
 */

public class BuildingAIFactory {
    
    private static final int DEFAULT_EVAL_INTERVAL = 10; // Ticks between evaluations (60 TPS)
    
    public static BuildingAI createAI(Building building) {
        String buildingClass = building.getClass().getSimpleName();
        
        return switch (buildingClass) {
            case "Fortress" -> new BuildingAI(new TownAI(), DEFAULT_EVAL_INTERVAL);
            case "City" -> new BuildingAI(new CityAI(), DEFAULT_EVAL_INTERVAL);
            case "Town" -> new BuildingAI(new CityAI(), DEFAULT_EVAL_INTERVAL); // Towns use same logic as cities
            case "Village" -> new BuildingAI(new CityAI(), 15); // Villages evaluate less frequently
            default -> new BuildingAI(new TownAI(), DEFAULT_EVAL_INTERVAL); // Fallback
        };
    }
}
