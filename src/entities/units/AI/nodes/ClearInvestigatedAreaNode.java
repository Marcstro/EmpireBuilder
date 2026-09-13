package entities.units.AI.nodes;

import buildings.DefensiveTroopBuilding;
import empirebuilder.Game;
import empirebuilder.MapCell;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;
import entities.units.Unit;

public class ClearInvestigatedAreaNode implements Node {

    @Override
    public GoalStatus tick(Unit unit, Game game) {

        boolean buildingHasDanger =
                unit.getUnitOwner() instanceof DefensiveTroopBuilding def
                        && def.getDefensiveTroopComponent().hasDanger();

        if (!buildingHasDanger) {
            return GoalStatus.REJECTED;
        }
        if (unit.getPointTarget() == null){
            if (unit.getUnitOwner() instanceof DefensiveTroopBuilding def){
                unit.setPointTarget(def.getInstructions(unit, game));
                return GoalStatus.SUCCESS;
            }
            else {
                return GoalStatus.REJECTED;
            }
        }
        else if (!(game.isUnitInDestinedMapCell(unit))){
            return GoalStatus.RUNNING;
        }
        else {
            if (unit.getUnitOwner() instanceof DefensiveTroopBuilding def){
                MapCell mapcell = game.getMapCellByPoint(game.getPoint(unit.getX(), unit.getY()));
                def.getDefensiveTroopComponent().dangerIsOver(mapcell);
                mapcell.localDangerIsOver();
                unit.setPointTarget(def.getInstructions(unit, game));
                return GoalStatus.SUCCESS;
            }
            else {
                return GoalStatus.REJECTED;
            }
        }
    }
}
