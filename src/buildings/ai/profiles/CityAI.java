package buildings.ai.profiles;

import buildings.Building;
import buildings.DefensiveTroopBuilding;
import buildings.FarmOwningBuilding;
import buildings.ai.BuildingAIProfile;
import buildings.ai.BuildingAIState;
import empirebuilder.Game;

/**
 * AI for cities: multi-faceted.
 * Priority queue: DEFENDING > SUSTAINING > EXPANDING > DEVELOPING
 *
 * More complex than fortress because cities need to balance defense, economy, and growth.
 */
public class CityAI implements BuildingAIProfile {

    int maxFood = 10000;
    private static final int MAX_DEFENDERS = 30;
    private static final float FOOD_CRISIS_THRESHOLD = 0.2f;  // 20% of capacity
    private static final float GOLD_EXPAND_THRESHOLD = 200f;
    
    @Override
    public BuildingAIState evaluate(Building building, Game game) {
        if (!(building instanceof DefensiveTroopBuilding)) {
            return BuildingAIState.IDLE;
        }
        
        DefensiveTroopBuilding city = (DefensiveTroopBuilding) building;
        
        // Priority 1: Danger
        if (city.getDefensiveTroopComponent().hasDanger()) {
            return BuildingAIState.DEFENDING;
        }
        
        // Priority 2: Survival (food crisis)
        if (city instanceof FarmOwningBuilding farmOwner) {
            float foodRatio = (float) farmOwner.getFood() / maxFood;
            if (foodRatio < FOOD_CRISIS_THRESHOLD) {
                return BuildingAIState.SUSTAINING;
            }
        }
        
        // Priority 3: Growth (expansion)
        if (building.getGold() > GOLD_EXPAND_THRESHOLD && 
            building instanceof FarmOwningBuilding farmOwner && 
            farmOwner.getFood() > maxFood * 0.7f) {
            return BuildingAIState.EXPANDING;
        }
        
        // Priority 4: Development (economy, infrastructure)
        return BuildingAIState.DEVELOPING;
    }
    
    @Override
    public void execute(Building building, Game game, BuildingAIState currentState) {
        if (!(building instanceof DefensiveTroopBuilding defBuilding)) {
            return;
        }
        
        switch (currentState) {
            case DEFENDING:
                int activeDefenders = 10;
                /*if (activeDefenders < MAX_DEFENDERS && (Building(defBuilding)) > 50) {
                    defBuilding.spawnUnit();
                }*/
                break;
                
            case SUSTAINING:
                // Request food from owner, halt expansion
                if (building.getOwner() != null) {
                    // Could send a message or set a flag for owner to send resources
                    // For now, just stop expansion-related activities
                }
                break;
                
            case EXPANDING:
                // Try to build new farms/settlements
                if (building instanceof FarmOwningBuilding farmOwner) {
                    // This would call into the existing farm-creation logic
                    // farmOwner.attemptToCreateNewFarm(game);
                }
                break;
                
            case DEVELOPING:
                // Long-term economy: could train population, build infrastructure
                // For now, just a placeholder for future economy systems
                break;
                
            default:
                break;
        }
    }
    
    @Override
    public void onStateEnter(Building building, BuildingAIState enteringState) {
        switch (enteringState) {
            case DEFENDING:
                // Alert: city under threat
                break;
            case SUSTAINING:
                // Alert: food crisis
                break;
            default:
                break;
        }
    }
}
