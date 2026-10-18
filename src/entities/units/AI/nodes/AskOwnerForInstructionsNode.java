package entities.units.AI.nodes;

import buildings.UnitOwner;
import empirebuilder.Game;
import empirebuilder.Point;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;
import entities.units.Unit;

public class AskOwnerForInstructionsNode implements Node {

    private static final int ASK_COOLDOWN = 60;

    private int askCooldown = 0;

    @Override
    public GoalStatus tick(Unit unit, Game game) {
        UnitOrder order = unit.getUnitOrder();
        if (order != UnitOrder.NONE
                && order != UnitOrder.IDLING
                && order != UnitOrder.AWAITING_INSTUCTIONS) {
            askCooldown = 0;
            return GoalStatus.REJECTED;
        }

        UnitOwner owner = unit.getUnitOwner();
        if (owner == null) {
            return GoalStatus.REJECTED;
        }

        if (order == UnitOrder.AWAITING_INSTUCTIONS) {
            askCooldown = 0;
        } else if (askCooldown > 0) {
            askCooldown--;
            return GoalStatus.REJECTED;
        }
        askCooldown = ASK_COOLDOWN;

        Point destination = owner.getInstructions(unit, game);
        if (destination == null) {
            return GoalStatus.REJECTED;
        }

        if (unit.getUnitOrder() == UnitOrder.IDLING
                || unit.getUnitOrder() == UnitOrder.AWAITING_INSTUCTIONS) {
            unit.setUnitOrder(UnitOrder.NONE);
        }
        unit.setPointTarget(destination);
        return GoalStatus.SUCCESS;
    }
}
