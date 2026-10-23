package buildings.ai.profiles;

import buildings.Building;
import buildings.ai.BuildingAI;
import buildings.ai.GoodSideBuildingStates;
import buildings.ai.ThreatFact;
import buildings.subBuildings.AnimalHusbandry;
import buildings.subBuildings.Market;
import buildings.subBuildings.Barracks;
import buildings.subBuildings.ElvenArcheryRange;
import buildings.subBuildings.ShootingTower;
import empirebuilder.Game;

public class TownAI extends SettlementAI {

    @Override
    public void determineWhatToBuildNext(Building building, Game game, BuildingAI ai) {
        if (canBuildSubBuilding(building, Market.class)) {
            ai.setSubBuildingToBuildNext(Market.class);
            return;
        }

        if (assessment.getBand() == GoodSideBuildingStates.SAFE && 
            !assessment.has(ThreatFact.ALLY_NEEDS_HELP)) {
            if (countSubBuildingType(building, AnimalHusbandry.class) == 0) {
                ai.setSubBuildingToBuildNext(AnimalHusbandry.class);
                return;
            }
        }

        if (assessment.has(ThreatFact.ENEMY_SCOUT_DETECTED)) {
            int towerCount = countSubBuildingType(building, ShootingTower.class);
            if (towerCount < 1) {
                if (canBuildSubBuilding(building, ShootingTower.class)) {
                    ai.setSubBuildingToBuildNext(ShootingTower.class);
                    return;
                }
            }
        }

        if (assessment.has(ThreatFact.LOSING_UNITS)
                || assessment.has(ThreatFact.ENEMIES_WITHIN_SHOOTING_DISTANCE)
                || assessment.has(ThreatFact.UNDER_DIRECT_ATTACK)) {
            int towerCount = countSubBuildingType(building, ShootingTower.class);
            if (towerCount < 3) {
                if (canBuildSubBuilding(building, ShootingTower.class)) {
                    ai.setSubBuildingToBuildNext(ShootingTower.class);
                    return;
                }
            }
        }

        if (countSubBuildingType(building, Barracks.class) == 0) {
            if (canBuildSubBuilding(building, Barracks.class)) {
                ai.setSubBuildingToBuildNext(Barracks.class);
                return;
            }
        }

        if (countSubBuildingType(building, ElvenArcheryRange.class) == 0) {
            if (canBuildSubBuilding(building, ElvenArcheryRange.class)) {
                ai.setSubBuildingToBuildNext(ElvenArcheryRange.class);
                return;
            }
        }

        // default
        // Build more ElvenArcheryRanges if there's gold
        if (canBuildSubBuilding(building, ElvenArcheryRange.class)) {
            ai.setSubBuildingToBuildNext(ElvenArcheryRange.class);
        }
    }
}
