package buildings.subBuildings;

import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Game;
import entities.units.Goblin;

public class GoblinHut extends UnitProducingSubBuilding {
    private static final int MAX_GOBLINS = 100;
    private static final int SPAWN_INTERVAL_BUILDING_TICKS = 30;
    private static final double LIFETIME_BUILDING_TICKS = 360;
    private static final double BASE_UPFRONT_COST = 10000;
    public static final java.util.List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = java.util.List.of();

    public GoblinHut(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        setValue(3000.0);
        setPermanent(false);
        setLifetime(LIFETIME_BUILDING_TICKS);
    }

    @Override
    public double getUpfrontCost() {
        int existingCount = countExistingGoblinHuts();
        return BASE_UPFRONT_COST * (existingCount);
    }

    private int countExistingGoblinHuts() {
        int count = 0;
        if (mainOwner instanceof buildings.Town town) {
            for (var area : town.getTownAreaPoints()) {
                SubBuilding sub = area.getSubBuilding();
                if (sub instanceof GoblinHut) {
                    count++;
                }
            }
        } else if (mainOwner instanceof buildings.City city) {
            for (var area : city.getCityAreaPoints()) {
                SubBuilding sub = area.getSubBuilding();
                if (sub instanceof GoblinHut) {
                    count++;
                }
            }
        } else if (mainOwner instanceof buildings.EvilSideBase base) {
            for (var area : base.getEvilSideBaseAreas()) {
                SubBuilding sub = area.getSubBuilding();
                if (sub instanceof GoblinHut) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    protected int getMaxCapacity() {
        return MAX_GOBLINS;
    }

    @Override
    protected int getSpawnInterval() {
        return SPAWN_INTERVAL_BUILDING_TICKS;
    }

    @Override
    protected void spawnUnit(Game game) {
        Goblin goblin = new Goblin(mainOwner.getPoint().getX(), mainOwner.getPoint().getY());
        goblin.setSubBuildingOwner(this);
        sendUnitToOwner(goblin, game);
        currentUnitCount++;
    }

    @Override
    public double getMaintenanceCost() {
        return 0;
    }
}
