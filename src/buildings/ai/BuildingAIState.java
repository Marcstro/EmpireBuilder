package buildings.ai;

/**
 * Building state indicates what it focuses on
 * The evaluate function determines the buildings state
 * and the execute function adjusts the buildings behavior accordingly.
 *
 * A changed behavior state triggers onStateExit and onStateEnter hooks for cleanup/setup
 */
public enum BuildingAIState {
    IDLE,               // No immediate threats or needs
    DEFENDING,          // Danger detected, spawn/manage defensive units
    SUSTAINING,         // Food/resource shortage, request help from owner
    EXPANDING,          // Surplus resources, try to build/expand
    DEVELOPING          // Growth phase, invest in infrastructure/economy
}
