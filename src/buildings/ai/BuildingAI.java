package buildings.ai;

import buildings.Building;
import empirebuilder.Game;

/**
 * Component that manages a building's AI state and execution.
 * Ticks every N game ticks (e.g., 10), not every tick.
 */
public class BuildingAI {
    
    private BuildingAIProfile profile;
    private BuildingAIState currentState;
    private int ticksSinceLastEvaluation;
    private final int evaluationInterval; // e.g., 10 ticks = ~167ms at 60 TPS
    
    public BuildingAI(BuildingAIProfile profile, int evaluationInterval) {
        this.profile = profile;
        this.currentState = BuildingAIState.IDLE;
        this.ticksSinceLastEvaluation = 0;
        this.evaluationInterval = evaluationInterval;
    }
    
    /**
     * Called every game tick. Only evaluates/executes every N ticks.
     */
    public void tick(Building building, Game game) {
        ticksSinceLastEvaluation++;
        
        if (ticksSinceLastEvaluation >= evaluationInterval) {
            ticksSinceLastEvaluation = 0;
            
            // Evaluate: what state should we be in?
            BuildingAIState newState = profile.evaluate(building, game);
            
            // State transition hooks
            if (newState != currentState) {
                profile.onStateExit(building, currentState);
                profile.onStateEnter(building, newState);
                currentState = newState;
            }
        }
        
        // Execute: perform actions for current state every tick
        // (or you could gate this too if actions are expensive)
        profile.execute(building, game, currentState);
    }
    
    public BuildingAIState getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(BuildingAIState state) {
        this.currentState = state;
    }
}
