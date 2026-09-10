package buildings;

import empirebuilder.Game;
import empirebuilder.MapCell;
import empirebuilder.Point;
import entities.units.AI.UnitOrder;
import entities.units.Unit;

import java.util.LinkedList;

public class DefensiveTroopComponent {

    private final LinkedList<MapCell> dangerList = new LinkedList<>();

    public void addToDangerList(MapCell mapCell) {
        if (!dangerList.contains(mapCell)) {
            dangerList.add(mapCell);
        }
    }

    public void dangerIsOver(MapCell mapCell) {
        dangerList.remove(mapCell);
    }

    public boolean hasDanger() {
        return !dangerList.isEmpty();
    }

    public MapCell getNextDangerCell() {
        return dangerList.isEmpty() ? null : dangerList.getFirst();
    }

    public Point getDefensiveInstructions(Unit unit, Game game){
        if (hasDanger()) {
            unit.setUnitOrder(UnitOrder.DEFEND_EXTERNAL_AREA);
            return game.getPointByMapCell(getNextDangerCell());
        }
        unit.setUnitOrder(UnitOrder.NONE);
        return null;
    }
}
