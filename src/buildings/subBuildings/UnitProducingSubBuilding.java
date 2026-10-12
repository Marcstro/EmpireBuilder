package buildings.subBuildings;

import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Game;
import entities.units.Unit;

import java.util.List;

/** Base class for all unit-producing sub-buildings. Handles capacity tracking and unit lifecycle. */
public abstract class UnitProducingSubBuilding extends SubBuilding {

    protected int currentUnitCount = 0;
    protected int spawnTimer = 0;

    public UnitProducingSubBuilding(AreaBuildingSlot areaBuilding, List<SubBuildingRequirement> subBuildingRequirements) {
        super(areaBuilding, subBuildingRequirements);
    }

    /**
     * Called when a unit produced by this building dies.
     * Immediately frees up a capacity slot.
     */
    public void releaseSpot(Unit unit) {
        currentUnitCount = Math.max(0, currentUnitCount - 1);
    }

    protected abstract int getMaxCapacity();

    protected abstract int getSpawnInterval();

    protected abstract void spawnUnit(Game game);

    @Override
    public void tick(Game game) {
        if (currentUnitCount < getMaxCapacity()) {
            spawnTimer++;
            if (spawnTimer >= getSpawnInterval()) {
                spawnTimer = 0;
                spawnUnit(game);
            }
        }

        if (!isPermanent) {
            lifetime--;
        }
    }

    public int getCurrentUnitCount() {
        return currentUnitCount;
    }

    public int getMaximumUnitCount() {
        return getMaxCapacity();
    }

    public int getTicksUntilNextSpawn() {
        if (currentUnitCount >= getMaxCapacity()) {
            return -1;
        }
        return Math.max(0, getSpawnInterval() - spawnTimer);
    }
}
