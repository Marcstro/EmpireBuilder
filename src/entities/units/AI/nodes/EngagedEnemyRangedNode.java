package entities.units.AI.nodes;

import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.Unit;

public class EngagedEnemyRangedNode implements Node {
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

        if (unit.isTargetInRangedAttack(unit, unit.getCombatTarget())){
            if (unit.isAttackReady()){
                game.unitShootArrow(unit, unit.getCombatTarget(), unit.getFactionId());
                unit.resetAttackCooldown();
            }
            unit.clearPathB();
            return GoalStatus.RUNNING;
        }
        return GoalStatus.RUNNING;
    }
}
