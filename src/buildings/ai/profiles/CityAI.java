package buildings.ai.profiles;

import buildings.Building;
import buildings.City;
import buildings.ai.BuildingAI;
import buildings.ai.GoodSideBuildingStates;
import buildings.ai.ThreatFact;
import buildings.subBuildings.AnimalHusbandry;
import buildings.subBuildings.Barracks;
import buildings.subBuildings.ElvenArcheryRange;
import buildings.subBuildings.MountedKnightSchool;
import buildings.subBuildings.ShootingTower;
import buildings.subBuildings.Stable;
import empirebuilder.Game;

public class CityAI extends SettlementAI {

    @Override
    public void determineWhatToBuildNext(Building building, Game game, BuildingAI ai) {
        if (!(building instanceof City)) {
            return;
        }

        if (assessment.getBand() == GoodSideBuildingStates.SAFE &&
            !assessment.has(ThreatFact.ALLY_NEEDS_HELP) &&
            !assessment.has(ThreatFact.ENEMY_SCOUT_DETECTED)) {
            int animalCount = countSubBuildingType(building, AnimalHusbandry.class);
            if (animalCount < 2) {
                if (canBuildSubBuilding(building, AnimalHusbandry.class)) {
                    ai.setSubBuildingToBuildNext(AnimalHusbandry.class);
                    return;
                }
            }
        }

        if (assessment.has(ThreatFact.ENEMY_SCOUT_DETECTED)) {
            int towerCount = countSubBuildingType(building, ShootingTower.class);
            if (towerCount < 2) {
                if (canBuildSubBuilding(building, ShootingTower.class)) {
                    ai.setSubBuildingToBuildNext(ShootingTower.class);
                    return;
                }
            }
        }

        if (assessment.has(ThreatFact.OWNED_AREA_RAIDED)) {
            int towerCount = countSubBuildingType(building, ShootingTower.class);
            if (towerCount < 1) {
                if (canBuildSubBuilding(building, ShootingTower.class)) {
                    ai.setSubBuildingToBuildNext(ShootingTower.class);
                    return;
                }
            }
        }

        if (assessment.has(ThreatFact.ENEMIES_WITHIN_SHOOTING_DISTANCE)
                || assessment.has(ThreatFact.UNDER_DIRECT_ATTACK)
                || assessment.has(ThreatFact.LOSING_UNITS)) {
            int towerCount = countSubBuildingType(building, ShootingTower.class);
            if (towerCount < 4) {
                if (canBuildSubBuilding(building, ShootingTower.class)) {
                    ai.setSubBuildingToBuildNext(ShootingTower.class);
                    return;
                }
            }
        }

        if (canBuildSubBuilding(building, MountedKnightSchool.class)) {
            int archerCount = countSubBuildingType(building, ElvenArcheryRange.class);
            int mountedKnightCount = countSubBuildingType(building, MountedKnightSchool.class);
            if (mountedKnightCount < archerCount) {
                ai.setSubBuildingToBuildNext(MountedKnightSchool.class);
                return;
            }
        }

        int barracksCount = countSubBuildingType(building, Barracks.class);
        int archerCount = countSubBuildingType(building, ElvenArcheryRange.class);
        if (archerCount < 3 && barracksCount < 3) {
            if (barracksCount <= archerCount) {
                if (canBuildSubBuilding(building, Barracks.class)) {
                    ai.setSubBuildingToBuildNext(Barracks.class);
                    return;
                }
            } else {
                if (canBuildSubBuilding(building, ElvenArcheryRange.class)) {
                    ai.setSubBuildingToBuildNext(ElvenArcheryRange.class);
                    return;
                }
            }
        } else if (archerCount < 3) {
            if (canBuildSubBuilding(building, ElvenArcheryRange.class)) {
                ai.setSubBuildingToBuildNext(ElvenArcheryRange.class);
                return;
            }
        } else if (barracksCount < 3) {
            if (canBuildSubBuilding(building, Barracks.class)) {
                ai.setSubBuildingToBuildNext(Barracks.class);
                return;
            }
        }

        if (assessment.has(ThreatFact.ENEMY_SCOUT_DETECTED) ||
            assessment.has(ThreatFact.ALLY_NEEDS_HELP)) {
            if (countSubBuildingType(building, Stable.class) == 0) {
                if (canBuildSubBuilding(building, Stable.class)) {
                    ai.setSubBuildingToBuildNext(Stable.class);
                    return;
                }
            }
        }

        // TODO ADD TRADER AND BUILDER BUILDING HERE
        // This is the default. build ElvenArcheryRange
        if (canBuildSubBuilding(building, ElvenArcheryRange.class)) {
            ai.setSubBuildingToBuildNext(ElvenArcheryRange.class);
        }
    }

}
