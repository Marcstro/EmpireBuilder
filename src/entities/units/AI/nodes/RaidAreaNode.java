package entities.units.AI.nodes;

import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;
import entities.units.Unit;

public class RaidAreaNode implements Node {
    @Override
    public GoalStatus tick(Unit unit, Game game) {
        if (!unit.getUnitOrder().equals(UnitOrder.RAIDING)) {
            return GoalStatus.REJECTED;
        }
        if (unit.getPointTarget() == null){
            unit.setPointTarget(unit.getUnitOwner().getInstructions(unit, game));
            return GoalStatus.SUCCESS;
        }
        else {
            if (game.isUnitInDestinedMapCell(unit)) {
                unit.clearPointTarget();
                return GoalStatus.SUCCESS;
            }
            return GoalStatus.RUNNING; // Move toward target
        }
    }
}
