package entities.units.AI.nodes;

import buildings.Building;
import buildings.FarmOwningBuilding;
import buildings.UnitOwner;
import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;
import entities.units.Unit;

public class LookForEnemyBuildingNode implements Node {

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
        Building hostileBuilding = game.getNearestLargeBuilding(unit, 3);
        if (hostileBuilding == null || unit.getScoutedBuildings().contains(hostileBuilding)){
            return GoalStatus.REJECTED;
        }

        alarmOwnerChain(hostileBuilding);

        // owner already knows about this one — nothing new to report, don't trigger a trip home for it
        if (unit.getUnitOwner().getUnitManagerComponent().getKnownHostileBuildings().contains(hostileBuilding)){
            return GoalStatus.REJECTED;
        }

        boolean firstSighting = unit.getScoutedBuildings().isEmpty();
        unit.addScoutedBuilding(hostileBuilding);

        if (firstSighting) {
            unit.setUnitOrder(UnitOrder.RETURN_REPORT);
            unit.setPointTarget(unit.getUnitOwner().getUnitManagerComponent().getOwnerBuilding().getPoint());
        }

        return GoalStatus.SUCCESS;
    }

    /** Alarms the spotted building and its whole owner chain (not just the top owner). */
    private void alarmOwnerChain(Building spotted) {
        if (spotted instanceof UnitOwner unitOwner) {
            unitOwner.getUnitManagerComponent().reportHostileUnit();
        }
        for (Building owner : spotted.getAllHierarchicalOwners()) {
            if (owner instanceof UnitOwner unitOwner) {
                unitOwner.getUnitManagerComponent().reportHostileUnit();
            }
        }
    }
}

