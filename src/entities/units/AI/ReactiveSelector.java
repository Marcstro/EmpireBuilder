package entities.units.AI;

import empirebuilder.Game;
import entities.units.Unit;

import java.util.List;

// a reactiveSelector is necessary for critical assessment tasks, like "detect nearby enemies", which must be run every tick
// as such it does not remember the previous tick's instructions

// SELECTOR (OR gate): Tries children left-to-right.
// Stops and returns SUCCESS/RUNNING on the first child that doesn't fail.
public class ReactiveSelector implements Node {
    private final List<Node> children;

    public ReactiveSelector(Node... children) {
        this.children = List.of(children);
    }

    @Override
    public GoalStatus tick(Unit unit, Game game) {
        for (Node child : this.children) {
            GoalStatus goalStatus = child.tick(unit, game);
            if (goalStatus != GoalStatus.REJECTED) {
                return goalStatus; // SUCCESS or RUNNING
            }
        }
        return GoalStatus.REJECTED; // All children failed
    }
}
