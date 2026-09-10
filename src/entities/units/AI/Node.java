package entities.units.AI;

import empirebuilder.Game;
import entities.units.Unit;

public interface Node {
    GoalStatus tick(Unit unit, Game game);
}
