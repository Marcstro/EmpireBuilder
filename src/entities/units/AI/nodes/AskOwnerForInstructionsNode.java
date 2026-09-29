package entities.units.AI.nodes;

import buildings.UnitOwner;
import empirebuilder.Game;
import empirebuilder.Point;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.Unit;

public class AskOwnerForInstructionsNode implements Node {

    private static final int ASK_COOLDOWN = 60;

    private int askCooldown = 0;

    @Override
    public GoalStatus tick(Unit unit, Game game) {
        UnitOwner owner = unit.getUnitOwner();
        if (owner == null) {
            return GoalStatus.REJECTED;
        }

        if (askCooldown > 0) {
            askCooldown--;
            return GoalStatus.REJECTED;
        }
        askCooldown = ASK_COOLDOWN;

        Point destination = owner.getInstructions(unit, game);
        if (destination == null) {
            return GoalStatus.REJECTED;
        }

        unit.setPointTarget(destination);
        return GoalStatus.SUCCESS;
    }
}
