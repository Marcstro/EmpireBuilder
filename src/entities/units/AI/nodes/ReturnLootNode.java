package entities.units.AI.nodes;

import buildings.Building;
import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;
import entities.units.Unit;

public class ReturnLootNode implements Node{

    int lootToReturnHome = 1000;
    @Override
    public GoalStatus tick(Unit unit, Game game) {
        if (unit.getUnitOrder().equals(UnitOrder.RETURN_LOOT)){
            return GoalStatus.RUNNING;
        }
        if (unit.getLoot() > lootToReturnHome){
            unit.setCombatTarget(null);
            unit.setUnitOrder(UnitOrder.RETURN_LOOT);
            unit.setPointTarget(
                    ((Building)unit.getUnitOwner()).getPoint());
            return GoalStatus.SUCCESS;
        }
        return GoalStatus.REJECTED;
    }
}
