package entities.units.AI.nodes;

import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;
import entities.units.Unit;

public class DepositLootNode implements Node {
    @Override
    public GoalStatus tick(Unit unit, Game game) {
        if (!unit.getUnitOrder().equals(UnitOrder.RETURN_LOOT)){
            return GoalStatus.REJECTED;
        }
        if (game.isUnitInDestinedPoint(unit)){
            // TODO replace with function in either Unit or Game
            unit.getUnitOwner().getUnitManagerComponent().getOwnerBuilding().addGold((int)unit.getLoot());
            unit.clearLoot();
            unit.clearPointTarget();
            //below line should come from getInstructions()
            unit.setUnitOrder(UnitOrder.RAIDING);
            return GoalStatus.SUCCESS;
        }
        else {
            return GoalStatus.RUNNING;
        }
    }
}
