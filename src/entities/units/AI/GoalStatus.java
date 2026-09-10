package entities.units.AI;

// 1. Every node returns one of three statuses
public enum GoalStatus {
    SUCCESS, // Task completed
    REJECTED, // Task failed - i.e. could not be completed or was not the right choice right now
    RUNNING  // Task in progress, walking to destination or attacking
}

