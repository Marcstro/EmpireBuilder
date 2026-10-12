package buildings.subBuildings.subBuildingFunctions;

import buildings.Building;
import buildings.UnitOwner;
import empirebuilder.Game;
import entities.units.Unit;

import java.util.List;

public abstract class SubBuilding {
    protected final AreaBuildingSlot areaBuilding;
    public Building mainOwner;
    protected final List<SubBuildingRequirement> subBuildingRequirements;
    protected double value = 0;
    protected boolean isPermanent = true;
    protected double lifetime = -1;
    protected double upfrontCost = 0;

    public SubBuilding(AreaBuildingSlot areaBuilding, List<SubBuildingRequirement> subBuildingRequirements) {
        this.areaBuilding = areaBuilding;
        this.mainOwner = areaBuilding.getMainOwner();
        this.subBuildingRequirements = List.copyOf(subBuildingRequirements);
    }

    public SubBuilding(AreaBuildingSlot areaBuilding) {
        this(areaBuilding, List.of());
    }

    public abstract void tick(Game game);

    public abstract double getMaintenanceCost();

    public double getBuildingValue() {
        return value;
    }

    protected void setValue(double value) {
        this.value = value;
    }

    // Some buildings, particularly evil ones, are not permanent but disappear afte X ticks
    public boolean isPermanent() {
        return isPermanent;
    }

    protected void setPermanent(boolean permanent) {
        this.isPermanent = permanent;
    }

    public double getLifetime() {
        return lifetime;
    }

    protected void setLifetime(double ticks) {
        this.lifetime = ticks;
    }

    public double getUpfrontCost() {
        return upfrontCost;
    }

    protected void setUpfrontCost(double cost) {
        this.upfrontCost = cost;
    }

    public boolean hasExpired() {
        return !isPermanent && lifetime <= 0;
    }

    public List<SubBuildingRequirement> getRequirements() {
        return subBuildingRequirements;
    }

    public boolean canBeBuild() {
        return subBuildingRequirements.stream().allMatch(req -> req.isMet(mainOwner));
    }

    public void sendUnitToOwner(Unit unit, Game game) {
        if (mainOwner instanceof UnitOwner owner) {
            owner.getUnitManagerComponent().receiveUnit(unit, game);
        }
    }

    public AreaBuildingSlot getAreaBuilding() {
        return areaBuilding;
    }

    public Building getMainOwner() {
        return mainOwner;
    }

    public void setMainOwner(Building owner) {
        this.mainOwner = owner;
    }
}
