package entities.units.AI.nodes;

import empirebuilder.Game;
import empirebuilder.Point;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.Unit;

public class FleeNode implements Node {

    private static final double ARRIVAL_DISTANCE = 2.0;

    @Override
    public GoalStatus tick(Unit unit, Game game) {
        Point fleeTarget = unit.getFleeTarget();
        if (fleeTarget == null) {
            return GoalStatus.REJECTED;
        }

        double dx = (fleeTarget.getX() + 0.5) - unit.getX();
        double dy = (fleeTarget.getY() + 0.5) - unit.getY();
        if (Math.sqrt(dx * dx + dy * dy) <= ARRIVAL_DISTANCE) {
            unit.clearFleeTarget();
            return GoalStatus.REJECTED;
        }

        unit.setCombatTarget(null);
        return GoalStatus.RUNNING;
    }
}
