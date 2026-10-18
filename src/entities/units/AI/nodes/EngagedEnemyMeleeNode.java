package entities.units.AI.nodes;

import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;
import entities.units.Unit;

public class EngagedEnemyMeleeNode implements Node {
    private static final int DISTANCE_CHECK_INTERVAL = 100;
    private int ticksSinceLastDistanceCheck = 0;

    @Override
    public GoalStatus tick(Unit unit, Game game) {
        if (unit.getCombatTarget() == null) {
            return GoalStatus.REJECTED;
        }
        else if (!unit.getCombatTarget().isAlive()) {
            unit.setCombatTarget(null);
            unit.resetSearchCooldown();
            ticksSinceLastDistanceCheck = 0;
            return GoalStatus.REJECTED;
        }

        if (unit.getUnitOrder() == UnitOrder.IDLING) {
            unit.setUnitOrder(UnitOrder.NONE);
        }
        ticksSinceLastDistanceCheck++;
        if (ticksSinceLastDistanceCheck >= DISTANCE_CHECK_INTERVAL) {
            ticksSinceLastDistanceCheck = 0;
            double searchDistance = unit.getSearchForUnitsDistance() * game.getMAP_CELL_SIZE();
            double distanceToTarget = game.calculateDistance(unit.getX(), unit.getY(),
                    unit.getCombatTarget().getX(), unit.getCombatTarget().getY());
            if (distanceToTarget > searchDistance) {
                unit.setCombatTarget(null);
                unit.resetSearchCooldown();
                return GoalStatus.REJECTED;
            }
        }

        if (unit.isTargetInMeleeRange(unit, unit.getCombatTarget())) {
            unit.attemptMeleeAttack(unit.getCombatTarget(), game);
            if (unit .getCombatTarget() != null && unit.getCombatTarget().isAlive()){
                unit.clearPathB();
            }
            else {
                return GoalStatus.SUCCESS;
            }
        }
        return GoalStatus.RUNNING;
    }
}
