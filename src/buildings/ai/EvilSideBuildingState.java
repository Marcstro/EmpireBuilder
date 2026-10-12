package buildings.ai;

/** Extremely simple, consider entirely removing? */
public enum EvilSideBuildingState implements BuildingState {

    /** Scouting for targets, no active threat engaged. */
    SCOUTING,

    /** Found a target, actively raiding it. */
    RAIDING;
}
