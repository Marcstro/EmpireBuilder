package pathfinding;

/**
 * Represents one agent (unit or static obstacle) as seen by the ORCA solver.
 * Fields are non-final to allow pool reuse: pre-allocate a fixed array of
 * OrcaAgentB objects and call update() instead of constructing new ones each tick.
 */
public class OrcaAgent {
    public double posX, posY, velX, velY, radius;

    public OrcaAgent(double posX, double posY, double velX, double velY, double radius) {
        update(posX, posY, velX, velY, radius);
    }

    public OrcaAgent() {}

    public void update(double posX, double posY, double velX, double velY, double radius) {
        this.posX = posX; this.posY = posY;
        this.velX = velX; this.velY = velY;
        this.radius = radius;
    }

    /** Convenience factory for a zero-velocity static obstacle.
     * possible future use for walls and unwalkable buildings
     */
    public static OrcaAgent staticObstacle(double posX, double posY, double radius) {
        return new OrcaAgent(posX, posY, 0, 0, radius);
    }
}

