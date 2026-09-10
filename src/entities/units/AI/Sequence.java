package entities.units.AI;

import empirebuilder.Game;
import entities.units.Unit;

import java.util.List;


//the purpose of sequence is multi-action steps, that need to happend in a specific order
//etc "getWood", "create timber from wood", "build building from timber"

//
// SEQUENCE (AND gate): Runs children left-to-right.
// Returns FAILURE immediately if any child fails.
public class Sequence implements Node {
    private final List<Node> children;
    private int runningChildIndex = 0; // current, if any, RUNNING node to instantly jump to next tick

    public Sequence(Node... children) {
        this.children = List.of(children);
    }

    @Override
    public GoalStatus tick(Unit unit, Game game) {
        for (int i = runningChildIndex; i < this.children.size(); i++) {
            Node child = this.children.get(i);
            GoalStatus goalStatus = child.tick(unit, game);
            if (goalStatus != GoalStatus.SUCCESS) {
                runningChildIndex = (goalStatus == GoalStatus.RUNNING) ? i : 0;
                return goalStatus; // FAILURE or RUNNING
            }
        }
        runningChildIndex = 0;
        return GoalStatus.SUCCESS; // All children succeeded
    }
}
