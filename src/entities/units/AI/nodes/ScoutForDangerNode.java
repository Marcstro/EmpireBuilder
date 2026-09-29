package entities.units.AI.nodes;

import empirebuilder.Game;
import empirebuilder.Point;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.AI.ThreatScan;
import entities.units.Unit;

public class ScoutForDangerNode implements Node {

    private static final int DANGER_SCAN_COOLDOWN = 15;
    private static final double DANGER_RADIUS = 8.0;
    // if fleeing, flee slightly further than sight to avoid stuttering back and forth between fleeing and walking
    private static final double SAFE_RADIUS_FACTOR = 1.5;
    private static final double FLEE_DISTANCE = 12.0;
    private static final int FLEE_COMMITMENT_TICKS = 90;

    // new flee direction must be at least this big to change flee direction
    private static final double MIN_ESCAPE_ALIGNMENT = 0.3;

    // max distance to continue running away from danger
    private static final double MAX_DANGER_RADIUS = DANGER_RADIUS * SAFE_RADIUS_FACTOR;
    private static final int SCAN_CELL_DISTANCE = (int) Math.ceil(MAX_DANGER_RADIUS / Game.MAP_CELL_SIZE);

    // Escape directions tried in order
    private static final double DIAGONAL = Math.sqrt(0.5);
    private static final double[][] ESCAPE_ROTATIONS = {
            {        1,         0 },
            { DIAGONAL,  DIAGONAL },
            { DIAGONAL, -DIAGONAL },
            {        0,         1 },
            {        0,        -1 }
    };

    private final ThreatScan threatScan = new ThreatScan();
    private int scanCooldown = 0;

    @Override
    public GoalStatus tick(Unit unit, Game game) {

        if (scanCooldown > 0) {
            scanCooldown--;
            return GoalStatus.REJECTED;
        }
        scanCooldown = DANGER_SCAN_COOLDOWN;

        game.accumulateHostileRepulsion(unit.getX(), unit.getY(), unit, SCAN_CELL_DISTANCE,
                (neighbor) -> neighbor.isAlive() && unit.isHostileTo(neighbor.getFactionId()),
                threatScan);

        double triggerRadius = unit.isFleeing() ? MAX_DANGER_RADIUS : DANGER_RADIUS;
        if (!threatScan.isNearestThreatWithin(triggerRadius)) {
            unit.clearFleeTarget();
            return GoalStatus.REJECTED;
        }

        if (isCurrentEscapeStillValid(unit, threatScan.repulsionX, threatScan.repulsionY)) {
            return GoalStatus.REJECTED;
        }

        Point fleePoint = findFleePoint(unit, game, threatScan.repulsionX, threatScan.repulsionY);
        if (fleePoint == null) {
            return GoalStatus.REJECTED;
        }

        unit.setCombatTarget(null);
        unit.setFleeTarget(fleePoint, FLEE_COMMITMENT_TICKS);
        return GoalStatus.SUCCESS;
    }

    private boolean isCurrentEscapeStillValid(Unit unit, double repulsionX, double repulsionY) {
        Point fleeTarget = unit.getFleeTarget();
        if (fleeTarget == null) {
            return false;
        }

        double toTargetX = (fleeTarget.getX() + 0.5) - unit.getX();
        double toTargetY = (fleeTarget.getY() + 0.5) - unit.getY();

        double toTargetLength = Math.sqrt(toTargetX * toTargetX + toTargetY * toTargetY);
        double repulsionLength = Math.sqrt(repulsionX * repulsionX + repulsionY * repulsionY);
        if (toTargetLength < 1e-9 || repulsionLength < 1e-9) {
            return false;
        }

        double alignment = ((toTargetX * repulsionX) + (toTargetY * repulsionY))
                / (toTargetLength * repulsionLength);
        return alignment >= MIN_ESCAPE_ALIGNMENT;
    }

    private Point findFleePoint(Unit unit, Game game, double repulsionX, double repulsionY) {
        double length = Math.sqrt(repulsionX * repulsionX + repulsionY * repulsionY);

        if (length < 1e-9) {
            // Threats cancelled each other out exactly — keep running the way we already face.
            repulsionX = unit.getLastVelX();
            repulsionY = unit.getLastVelY();
            length = Math.sqrt(repulsionX * repulsionX + repulsionY * repulsionY);
            if (length < 1e-9) {
                repulsionX = 1;
                repulsionY = 0;
                length = 1;
            }
        }

        double directionX = repulsionX / length;
        double directionY = repulsionY / length;

        for (double[] rotation : ESCAPE_ROTATIONS) {
            double cos = rotation[0];
            double sin = rotation[1];
            double escapeX = directionX * cos - directionY * sin;
            double escapeY = directionX * sin + directionY * cos;

            int targetX = (int) (unit.getX() + escapeX * FLEE_DISTANCE);
            int targetY = (int) (unit.getY() + escapeY * FLEE_DISTANCE);

            Point candidate = game.getWalkablePointOrNull(targetX, targetY);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }
}
