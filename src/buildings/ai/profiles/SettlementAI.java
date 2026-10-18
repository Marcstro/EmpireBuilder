package buildings.ai.profiles;

import buildings.Building;
import buildings.AttackCapableBuilding;
import buildings.City;
import buildings.DefensiveTroopBuilding;
import buildings.DefensiveTroopComponent;
import buildings.Town;
import buildings.UnitOwner;
import buildings.VillageOwningBuilding;
import buildings.ai.BuildingAI;
import buildings.ai.BuildingState;
import buildings.ai.GoodSideBuildingStates;
import buildings.ai.GoodSideThreatAssessment;
import buildings.ai.ThreatFact;
import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import buildings.subBuildings.subBuildingFunctions.SubBuildingFactory;
import empirebuilder.Game;

/** Shared AI for Towns and Cities — covers their overlap */
public class SettlementAI extends SubBuildingAIProfile {

    private static final int LOSSES_BEFORE_ALARM = 3;
    private static final double MILITIA_COST = 1000.0;

    protected final GoodSideThreatAssessment assessment = new GoodSideThreatAssessment();

    @Override
    public void evaluate(Building building, Game game) {

        boolean raided = building instanceof DefensiveTroopBuilding def
                && def.getDefensiveTroopComponent().hasDanger();
        assessment.setFact(ThreatFact.OWNED_AREA_RAIDED, raided);

        boolean enemiesWithinShootingDistance = building instanceof AttackCapableBuilding attackCapableBuilding
                && attackCapableBuilding.getAttackCapableBuildingComponent().hasAttackTarget();
        assessment.setFact(ThreatFact.ENEMIES_WITHIN_SHOOTING_DISTANCE,
                enemiesWithinShootingDistance);

        boolean underDirectAttack = building instanceof DefensiveTroopBuilding def
                && def.getDefensiveTroopComponent().wasRecentlyDamaged();
        assessment.setFact(ThreatFact.UNDER_DIRECT_ATTACK, underDirectAttack);

        if (building instanceof UnitOwner owner) {
            owner.getUnitManagerComponent().decayThreatMemory();

            assessment.setFact(ThreatFact.ENEMY_SCOUT_DETECTED,
                    owner.getUnitManagerComponent().hasRecentlySightedEnemy());

            assessment.setFact(ThreatFact.LOSING_UNITS,
                    owner.getUnitManagerComponent().getRecentUnitLosses() >= LOSSES_BEFORE_ALARM);
        }

        assessment.setFact(ThreatFact.ALLY_NEEDS_HELP, detectAllyNeedsHelp(building, game));

        GoodSideBuildingStates previousBand = assessment.getBand();
        GoodSideBuildingStates newBand = assessment.update();

        if (newBand != previousBand) {
            onStateExit(building, game, previousBand);
            onStateEnter(building, game, newBand);
        }

        determineWhatToBuildNext(building, game, building.getBuildingAI());
    }

    protected boolean detectAllyNeedsHelp(Building building, Game game) {
        return false;
    }

    @Override
    public void determineWhatToBuildNext(Building building, Game game, BuildingAI ai) {
    }

    @Override
    public void execute(Building building, Game game) {
        assessment.tick();

        if (assessment.shouldDeployGarrisonedUnits()){
            if (building instanceof UnitOwner owner) {
                owner.getUnitManagerComponent().spawnUnitsFromGarrison(game);
            }
        }
        if (assessment.has(ThreatFact.UNDER_DIRECT_ATTACK)) {
            spawnMilitiaWave(building, game);
        }

        buildFocusedBuilding(building, game);
    }

    private void spawnMilitiaWave(Building building, Game game) {
        if (!(building instanceof DefensiveTroopBuilding def)) {
            return;
        }

        DefensiveTroopComponent defensiveComponent = def.getDefensiveTroopComponent();
        int availableMilitiaSlots = defensiveComponent.getMaxAvailableMilitia();

        if (availableMilitiaSlots <= 0) {
            return;
        }

        int militiaToSpawn = Math.min(availableMilitiaSlots, 10);
        int cost = (int)(militiaToSpawn * MILITIA_COST);

        if (building.getGold() < cost) {
            return;
        }

        for (int i = 0; i < militiaToSpawn; i++) {
            if (building.getGold() < MILITIA_COST) {
                break;
            }

            // TODO: Actually spawn the militia unit (requires Point finding logic)
            // For now, reserve the slot and deduct gold
            defensiveComponent.reserveMilitiaSlots(1);
            building.addGold(-MILITIA_COST);
        }
    }

    private void buildFocusedBuilding(Building building, Game game) {
        BuildingAI ai = building.getBuildingAI();
        Class<? extends SubBuilding> nextBuilding = ai.getSubBuildingToBuildNext();

        if (nextBuilding == null) {
            return;
        }

        AreaBuildingSlot emptySlot = findEmptyAreaSlot(building);
        if (emptySlot == null) {
            // TODO here, adjust what buildings exist and replace the least useful
            return;
        }

        SubBuilding newBuilding = SubBuildingFactory.create(nextBuilding, emptySlot);
        if (newBuilding == null) {
            return;
        }
        double upfrontCost = newBuilding.getUpfrontCost();
        if (building.getGold() < upfrontCost) {
            return;
        }

        emptySlot.setSubBuilding(newBuilding, game);
        building.addGold(-upfrontCost);

        ai.clearSubBuildingToBuildNext();
        ai.ticksSinceLastEvaluation = ai.evaluationInterval;
    }

    private AreaBuildingSlot findEmptyAreaSlot(Building building) {
        if (building instanceof Town town) {
            for (var area : town.getTownAreaPoints()) {
                if (area.isAlive() && area.getSubBuilding() == null) {
                    return area;
                }
            }
        } else if (building instanceof City city) {
            for (var area : city.getCityAreaPoints()) {
                if (area.isAlive() && area.getSubBuilding() == null) {
                    return area;
                }
            }
        }
        return null;
    }

    protected void sendAidToAlly(Building building, Game game) {
    }

    @Override
    public void onStateEnter(Building building, Game game, BuildingState enteringState) {
    }

    @Override
    public void onStateExit(Building building, Game game, BuildingState exitingState) {
    }

    public GoodSideThreatAssessment getAssessment() {
        return assessment;
    }
}
