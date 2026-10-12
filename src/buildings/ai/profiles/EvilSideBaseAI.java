package buildings.ai.profiles;

import buildings.Building;
import buildings.EvilSideBase;
import buildings.EvilSideBaseArea;
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

/** AI for EvilSideBase (raider bases). Owns state and building tech tree independently. */
public class EvilSideBaseAI extends SubBuildingAIProfile {

    private EvilSideBuildingState state = EvilSideBuildingState.SCOUTING;
    private empirebuilder.Point currentRaidingTarget = null;

    @Override
    public void evaluate(Building building, Game game) {
        if (!(building instanceof EvilSideBase base)) {
            return;
        }
        updateRaidingTarget(base);
        determineWhatToBuildNext(building, game, building.getBuildingAI());
    }

    @Override
    public void determineWhatToBuildNext(Building building, Game game, BuildingAI ai) {
        if (!(building instanceof EvilSideBase base)) {
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
        if (!(building instanceof EvilSideBase base)) {
            return;
        }

        BuildingAI ai = building.getBuildingAI();
        Class<? extends SubBuilding> nextBuilding = ai.getSubBuildingToBuildNext();

        if (nextBuilding == null) {
            return;
        }

        EvilSideBaseArea emptySlot = null;
        for (EvilSideBaseArea area : base.getEvilSideBaseAreas()) {
            if (area.isAlive() && area.getSubBuilding() == null) {
                emptySlot = area;
                break;
            }
        }

        if (emptySlot == null) {
            return;
        }

        SubBuilding newBuilding = SubBuildingFactory.create(nextBuilding, emptySlot);
        if (newBuilding == null || base.getGold() < newBuilding.getUpfrontCost()) {
            return;
        }

        emptySlot.setSubBuilding(newBuilding, game);
        base.addGold(-newBuilding.getUpfrontCost());

        ai.clearSubBuildingToBuildNext();
        ai.ticksSinceLastEvaluation = ai.evaluationInterval;
    }

    private void updateRaidingTarget(EvilSideBase base) {
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

            if (value > maxValue) {
                maxValue = value;
                richestTarget = hostile;
            }
        }

        currentRaidingTarget = richestTarget != null ? richestTarget.getPoint() : null;
        if (previousTarget == null && currentRaidingTarget != null) {
            awakenWaitingUnits(base);
        }
    }

    private void awakenWaitingUnits(EvilSideBase base) {
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

    public Point getUnitInstructions(EvilSideBase base, Unit unit) {
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
