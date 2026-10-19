package buildings.ai.profiles;

import buildings.Building;
import buildings.EvilBase;
import buildings.EvilBaseArea;
import buildings.ai.BuildingAI;
import buildings.ai.BuildingAIProfile;
import buildings.ai.EvilSideBuildingState;
import buildings.subBuildings.*;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import buildings.subBuildings.subBuildingFunctions.SubBuildingFactory;
import empirebuilder.Game;
import empirebuilder.Point;
import entities.units.AI.UnitOrder;
import entities.units.Unit;

/** AI for EvilBase (raider bases). Owns state and building tech tree independently. */
public class EvilBaseAI extends SubBuildingAIProfile {

    private EvilSideBuildingState state = EvilSideBuildingState.SCOUTING;
    private empirebuilder.Point currentRaidingTarget = null;

    @Override
    public void evaluate(Building building, Game game) {
        if (!(building instanceof EvilBase base)) {
            return;
        }
        updateRaidingTarget(base, game);
        determineWhatToBuildNext(building, game, building.getBuildingAI());
    }

    @Override
    public void determineWhatToBuildNext(Building building, Game game, BuildingAI ai) {
        if (!(building instanceof EvilBase base)) {
            return;
        }

        if (countSubBuildingType(base, WolfDen.class) == 0) {
            if (canBuildSubBuilding(base, WolfDen.class)) {
                ai.setSubBuildingToBuildNext(WolfDen.class);
                return;
            }
        }

        int archerCount = countSubBuildingType(base, GoblinArcheryRange.class);
        int fortressCount = countSubBuildingType(base, OrcFortress.class);

        if (fortressCount <= archerCount) {
            if (canBuildSubBuilding(base, OrcFortress.class)) {
                ai.setSubBuildingToBuildNext(OrcFortress.class);
                return;
            }
        }

        if (archerCount < fortressCount) {
            if (canBuildSubBuilding(base, GoblinArcheryRange.class)) {
                ai.setSubBuildingToBuildNext(GoblinArcheryRange.class);
                return;
            }
        }

        if (canBuildSubBuilding(base, OrcDen.class)) {
            ai.setSubBuildingToBuildNext(OrcDen.class);
            return;
        }

        if (canBuildSubBuilding(base, GoblinHut.class)) {
            ai.setSubBuildingToBuildNext(GoblinHut.class);
        }
    }

    @Override
    public void execute(Building building, Game game) {
        if (!(building instanceof EvilBase base)) {
            return;
        }

        BuildingAI ai = building.getBuildingAI();
        Class<? extends SubBuilding> nextBuilding = ai.getSubBuildingToBuildNext();

        if (nextBuilding == null) {
            return;
        }

        EvilBaseArea emptySlot = null;
        for (EvilBaseArea area : base.getEvilBaseAreas()) {
            if (area.isAlive() && area.getSubBuilding() == null) {
                emptySlot = area;
                break;
            }
        }

        if (emptySlot == null) {
            return;
        }

        SubBuilding newBuilding = SubBuildingFactory.create(nextBuilding, emptySlot);
        if (newBuilding == null) {
            return;
        }
        double upfrontCost = newBuilding.getUpfrontCost();
        if (base.getGold() < upfrontCost) {
            return;
        }

        emptySlot.setSubBuilding(newBuilding, game);
        base.addGold(-upfrontCost);

        ai.clearSubBuildingToBuildNext();
        ai.ticksSinceLastEvaluation = ai.evaluationInterval;
    }

    private void updateRaidingTarget(EvilBase base, Game game) {
        Point previousTarget = currentRaidingTarget;
        currentRaidingTarget = null;

        if (base.getUnitManagerComponent() == null || 
            base.getUnitManagerComponent().getKnownHostileBuildings().isEmpty()) {
            return;
        }

        Building richestTarget = null;
        double maxValue = -1;

        for (Building hostile : base.getUnitManagerComponent().getKnownHostileBuildings()) {
            double value = hostile.getGold();

            if (hostile instanceof buildings.FarmOwningBuilding) {
                value += ((buildings.FarmOwningBuilding) hostile).getFood();
            }

            if (value > maxValue && game.canReachPoint(base.getPoint(), hostile.getPoint())) {
                maxValue = value;
                richestTarget = hostile;
            }
        }

        currentRaidingTarget = richestTarget != null ? richestTarget.getPoint() : null;
        if (previousTarget == null && currentRaidingTarget != null) {
            awakenWaitingUnits(base);
        }
    }

    private void awakenWaitingUnits(EvilBase base) {
        for (Unit unit : base.getUnitManagerComponent().getUnits()) {
            if (!unit.isAlive()) {
                continue;
            }
            if (unit.getUnitOrder() == UnitOrder.NONE || unit.getUnitOrder() == UnitOrder.IDLING) {
                unit.clearPointTarget();
                unit.setIdleTarget(null);
                unit.setUnitOrder(UnitOrder.AWAITING_INSTUCTIONS);
            }
        }
    }

    public Point getCurrentRaidingTarget() {
        return currentRaidingTarget;
    }

    public Point getUnitInstructions(EvilBase base, Unit unit) {
        if (currentRaidingTarget != null) {
            unit.setUnitOrder(UnitOrder.RAIDING);
            return currentRaidingTarget;
        }

        unit.setUnitOrder(UnitOrder.IDLING);
        return base.getPoint();
    }

    public EvilSideBuildingState getState() {
        return state;
    }

    public void setState(EvilSideBuildingState newState) {
        this.state = newState;
    }
}
