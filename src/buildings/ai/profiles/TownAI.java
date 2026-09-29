package buildings.ai.profiles;

import buildings.Building;
import buildings.DefensiveTroopBuilding;
import buildings.UnitOwner;
import buildings.ai.BuildingAIProfile;
import buildings.ai.BuildingAIState;
import empirebuilder.Game;

/**
 * AI for fortresses: single-purpose.
 * If danger: spawn defenders and manage them.
 * Else: idle and conserve resources.
 */

public class TownAI implements BuildingAIProfile {
    
    private static final int MAX_DEFENDERS = 20;
    
    @Override
    public BuildingAIState evaluate(Building building, Game game) {
        if (!(building instanceof DefensiveTroopBuilding defBuilding)) {
            return BuildingAIState.IDLE;
        }
        
        // Priority queue: DEFENDING > IDLE
        if (defBuilding.getDefensiveTroopComponent().hasDanger()) {
            return BuildingAIState.DEFENDING;
        }
        
        return BuildingAIState.IDLE;
    }
    
    @Override
    public void execute(Building building, Game game, BuildingAIState currentState) {
        if (!(building instanceof UnitOwner defBuilding)) {
            return;
        }
        
        switch (currentState) {
            case DEFENDING:
                // Spawn defenders if under limit and we have resources
                int activeDefenders = defBuilding.getUnitManagerComponent().getUnits().size();
                if (activeDefenders < MAX_DEFENDERS && ((Building)(defBuilding)).getGold() > 50) {
                    //defBuilding.spawnUnit(); or something
                }
                break;
                
            case IDLE:
                // Could regenerate resources, rest, etc.
                break;
                
            default:
                break;
        }
    }
    
    @Override
    public void onStateEnter(Building building, BuildingAIState enteringState) {
        if (enteringState == BuildingAIState.DEFENDING) {
            // Could trigger an alert sound, visual effect, etc.
        }
    }
}
