package entities.units.AI.nodes;

import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.Unit;

public class EngagedEnemyRangedNode implements Node {
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
