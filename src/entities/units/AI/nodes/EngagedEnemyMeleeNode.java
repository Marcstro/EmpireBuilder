package entities.units.AI.nodes;

import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.Unit;

public class EngagedEnemyMeleeNode implements Node {
    @Override
    public GoalStatus tick(Unit unit, Game game) {


        if (unit.getCombatTarget() == null) {
            return GoalStatus.REJECTED; // cannot attack because there is no target
        }
        else if (!unit.getCombatTarget().isAlive()) {
            unit.setCombatTarget(null);
            unit.resetSearchCooldown();
            return GoalStatus.SUCCESS;
        }

        if (unit.isTargetInMeleeRange(unit, unit.getCombatTarget())) {
            unit.attemptMeleeAttack(unit.getCombatTarget(), game);
            if (unit .getCombatTarget() != null && unit.getCombatTarget().isAlive()){
                unit.clearPathB();
            }
            else {
                return GoalStatus.SUCCESS;
            }
            return GoalStatus.RUNNING;
        } else {
            //unit is walking towards target
            //no need to set point target here as the pathfinding system will look at CombatTarget for walking destination
            return GoalStatus.RUNNING;
        }
    }
}
