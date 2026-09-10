package entities.units.AI;

import empirebuilder.Game;
import entities.units.Unit;

import java.util.List;

//selector is for simple direct decisions
// etc "attack"
// need complicated AI? use sequence

// a statefulSelector remembers its previous instructions, used for "low prio tasks" to avoid heavy computations
// it will, if enabled, still re-evaluate every x ticks (set on constructor)

// SELECTOR (OR gate): Tries children left-to-right.
// Stops and returns SUCCESS/RUNNING on the first child that doesn't fail.
public class StatefulSelector implements Node {
    private final List<Node> children;
    private int runningChildIndex = 0; // current, if any, RUNNING node to instantly jump to next tick
    private int ticksSinceReset = 0;
    private final int reEvaluateInterval;
    private boolean reEvaluateEnabled;

    public StatefulSelector(Node... children) {
        this.children = List.of(children);
        this.reEvaluateInterval = 0;
        this.reEvaluateEnabled = false;
    }

    public StatefulSelector(int reEvaluateInterval, Node... children) {
        this.children = List.of(children);
        this.reEvaluateInterval = reEvaluateInterval;
        this.reEvaluateEnabled = true;
    }

    @Override
    public GoalStatus tick(Unit unit, Game game) {
        if (reEvaluateEnabled && ticksSinceReset >= reEvaluateInterval) {
            runningChildIndex = 0;
            ticksSinceReset = 0;
        }
        ticksSinceReset++;

        for (int i = runningChildIndex; i < this.children.size(); i++) {
            Node child = this.children.get(i);
            GoalStatus goalStatus = child.tick(unit, game);
            if (goalStatus != GoalStatus.REJECTED) {
                runningChildIndex = (goalStatus == GoalStatus.RUNNING) ? i : 0;
                return goalStatus; // SUCCESS or RUNNING
            }
        }
        this.runningChildIndex = 0;
        return GoalStatus.REJECTED; // All children failed
    }
}


