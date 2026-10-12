package entities.units.AI.nodes;

import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.Unit;

// used for scouts to wander the map.
// TODO Could be improved
public class WanderToRandomPointNode implements Node {
    @Override
    public GoalStatus tick(Unit unit, Game game) {
        if (unit.getPointTarget() != null) {
            return GoalStatus.REJECTED;
        }
        unit.setPointTarget(game.getNewCompletelyRandomPointTarget());
        return GoalStatus.SUCCESS;
    }
}
