package buildings.ai;

import buildings.Building;
import empirebuilder.Game;

/** A AI for buildings, implemented by specific building's AI. */
public interface BuildingAIProfile {

    // this decides what the building should focus on right now
    void evaluate(Building building, Game game);

    /** decided from evaluate() what to build next, if anything. */
    void determineWhatToBuildNext(Building building, Game game, BuildingAI ai);

    // Executes what the evaluate decided
    void execute(Building building, Game game);

    /** Optional one-shot hook fired when the building leaves a state. */
    default void onStateExit(Building building, Game game, BuildingState exitingState) {}

    /** Optional one-shot hook fired when the building enters a state. */
    default void onStateEnter(Building building, Game game, BuildingState enteringState) {}
}
