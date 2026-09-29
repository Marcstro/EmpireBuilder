package buildings.ai;

import buildings.Building;
import empirebuilder.Game;

/**
 * Interface for building AI profiles. Each building type can have its own strategy.
 * Runs infrequently (every ~10 ticks), allowing heavier logic than unit AI.
 */
public interface BuildingAIProfile {
    
    /**
     * Evaluate current situation and decide what the building should do.
     * Returns the next state and any immediate actions to take.
     * Called every N ticks (e.g., 10), not every tick.
     */
    BuildingAIState evaluate(Building building, Game game);
    
    /**
     * Execute actions for the current state.
     * e.g., if DEFENDING: spawn units, direct them to danger
     * e.g., if EXPANDING: attempt to build a farm
     */
    void execute(Building building, Game game, BuildingAIState currentState);
    
    /**
     * Optional: cleanup when transitioning out of a state.
     */
    default void onStateExit(Building building, BuildingAIState exitingState) {}
    
    /**
     * Optional: setup when entering a state.
     */
    default void onStateEnter(Building building, BuildingAIState enteringState) {}
}
