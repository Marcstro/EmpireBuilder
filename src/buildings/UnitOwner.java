package buildings;

import empirebuilder.Game;
import empirebuilder.MapCell;
import empirebuilder.Point;
import entities.units.Unit;

public interface UnitOwner {

    UnitManagerComponent getUnitManagerComponent();

    Point getInstructions(Unit unit, Game game);
}
