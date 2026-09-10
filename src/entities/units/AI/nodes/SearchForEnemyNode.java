package entities.units.AI.nodes;

import buildings.Building;
import empirebuilder.Game;
import entities.Entity;
import entities.units.AI.GoalStatus;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;
import entities.units.Unit;

public class SearchForEnemyNode implements Node {

    @Override
    public GoalStatus tick(Unit unit, Game game) {

        if (unit.isSeachCooldownReady()){
            unit.resetSearchCooldown();
            Entity target = game.getNearestUnit(unit.getX(), unit.getY(), unit, unit.getSearchForUnitsDistance(), (neighbor) ->
                    neighbor.isAlive() && unit.isHostileTo(neighbor.getFactionId())
            );
            if (target == null){
                // TODO combine the two searches below so we don't have to search twice
                target = (Building)game.getNearestAttackCapableBuilding(unit, unit.getSearchForBuildingsDistance());
                if (target == null){
                    target = game.getNearestLargeBuilding(unit, unit.getSearchForBuildingsDistance());
                }
            }
            if (target != null){
                unit.setCombatTarget(target);
                // TODO check that the below line isnt needed
                //unit.setUnitOrder(UnitOrder.DEFEND_EXTERNAL_AREA);
                return GoalStatus.SUCCESS;
            }
            else {
                return GoalStatus.REJECTED;
            }
        }
        else {
            return GoalStatus.REJECTED;
        }
    }
}
