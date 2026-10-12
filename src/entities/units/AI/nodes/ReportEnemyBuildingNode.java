package entities.units.AI.nodes;

import buildings.Building;
import buildings.UnitOwner;
import empirebuilder.Game;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;
import entities.units.Unit;

import java.util.List;

public class ReportEnemyBuildingNode implements Node {

    @Override
    public GoalStatus tick(Unit unit, Game game) {
        if (!unit.getUnitOrder().equals(UnitOrder.RETURN_REPORT)) {
            return GoalStatus.REJECTED;
        }

        UnitOwner owner = unit.getUnitOwner();
        List<Building> scouted = unit.getScoutedBuildings();
        if (owner == null || scouted.isEmpty()) {
            unit.setUnitOrder(UnitOrder.NONE);
            unit.clearScoutedBuildings();
            return GoalStatus.REJECTED;
        }

        if (unit.getPointTarget() == null) {
            // something else cleared our target before we arrived; re-aim for home
            unit.setPointTarget(owner.getUnitManagerComponent().getOwnerBuilding().getPoint());
            return GoalStatus.RUNNING;
        }

        if (!game.isUnitInDestinedMapCell(unit)) {
            return GoalStatus.RUNNING;
        }

        for (Building building : scouted) {
            owner.getUnitManagerComponent().reportHostileBuilding(building);
        }
        unit.clearScoutedBuildings();
        unit.clearPointTarget();
        unit.setUnitOrder(UnitOrder.NONE);
        return GoalStatus.SUCCESS;
    }
}

