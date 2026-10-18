package entities.units.AI.nodes;

import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;
import entities.units.Unit;

public class IdleNode implements Node {
    int wanderCooldown = 0;
    int wanderCooldownBase = 80;

    @Override
    public GoalStatus tick(Unit unit, Game game) {
        wanderCooldown--;

        if (unit.getUnitOwner() == null){
            return GoalStatus.REJECTED;
        }
        if (unit.getCombatTarget() != null || unit.getPointTarget() != null || unit.isFleeing()) {
            return GoalStatus.REJECTED;
        }
        unit.setUnitOrder(UnitOrder.IDLING);
        if (wanderCooldown <= 0) {
            unit.setIdleTarget(game.getIdleWalkTarget(unit.getUnitOwner().getUnitManagerComponent().getOwnerBuilding().getPoint()));
            wanderCooldown = wanderCooldownBase;
            return GoalStatus.SUCCESS;
        }
        return GoalStatus.RUNNING;
    }
}
