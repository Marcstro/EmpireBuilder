package entities.units.AI.nodes;

import buildings.FarmOwningBuilding;
import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.Unit;

public class ScoutForEnemyBuildingNode implements Node {

    private int lastMapCellX = -1;
    private int lastMapCellY = -1;

    @Override
    public GoalStatus tick(Unit unit, Game game) {
        int currentMapCellX = unit.getMapCellX();
        int currentMapCellY = unit.getMapCellY();

        if (currentMapCellX != lastMapCellX || currentMapCellY != lastMapCellY) {
            lastMapCellX = currentMapCellX;
            lastMapCellY = currentMapCellY;
            unit.setSearchForBuildingCooldown(false);
        }

        if (unit.isSearchForBuildingCooldown() || unit.getUnitOwner() == null) {
            return GoalStatus.REJECTED;
        }

        unit.setSearchForBuildingCooldown(true);
        FarmOwningBuilding hostileBuilding = game.getNearestLargeBuilding(unit, 3);
        if (hostileBuilding != null){
            unit.getUnitOwner().getUnitManagerComponent().reportHostileBuilding(hostileBuilding);
            return GoalStatus.SUCCESS;
        }

        return GoalStatus.REJECTED;
    }
}
