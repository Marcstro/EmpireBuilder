package buildings.subBuildings.subBuildingFunctions;

import buildings.Building;

/** An area-building slot that can hold one sub-building.
 * Exists on every area building */
public interface AreaBuildingSlot {
    SubBuilding getSubBuilding();
    void setSubBuilding(SubBuilding subBuilding, empirebuilder.Game game);
    Building getMainOwner();
    
    /** Removes all connections between subbuilding, areabuilding and main owner of areabuilding */
    void cleanupDestruction(empirebuilder.Game game);
}
