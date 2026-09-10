package entities.units.AI.nodes;

import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.Unit;

public class WalkTowardsPointTargetNode implements Node {
    @Override
    public GoalStatus tick(Unit unit, Game game) {
        if (unit.getPointTarget() != null) {
            if(game.isUnitInDestinedMapCell(unit)){
                unit.clearPointTarget();
                return GoalStatus.SUCCESS;
            }
            return GoalStatus.RUNNING; // Move toward target
        }
        return GoalStatus.REJECTED;
    }
}