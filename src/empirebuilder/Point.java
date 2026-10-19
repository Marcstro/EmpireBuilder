package empirebuilder;

import buildings.Building;
import LandTypes.Land;
import LandTypes.LandFactory;
import LandTypes.LandType;
import buildings.Farm;
import buildings.FarmOwningBuilding;
import buildings.LandOwningBuilding;

import java.awt.*;

public class Point {
    private int x, y;
    private Land land;
    private Building building;
    private LandOwningBuilding ownedByBuilding;
    private int elevation;  // 0–255, more is higher

    public Point(int x, int y, LandType landType) {
        this.x = x;
        this.y = y;
        this.land = LandFactory.createLand(landType);
        ownedByBuilding = null;
    }

    public double getWalkingCost(){
        return land.getTerrainWalkingCost();
    }

    public boolean isTerrainWalkable(){
        return land.isWalkable();
    }

    public boolean isWalkable(){
        return (isTerrainWalkable() && (building != null && building instanceof Farm));
    }
    
    public boolean isOwnedByBuilding(){
        return ownedByBuilding != null;
    }

    public LandOwningBuilding getPointOwner() {
        return ownedByBuilding;
    }

    public void setOwnerBuilding(LandOwningBuilding ownedByBuilding) {
        if (this.ownedByBuilding == ownedByBuilding) {
            return;
        }
        LandOwningBuilding previousOwner = this.ownedByBuilding;
        this.ownedByBuilding = ownedByBuilding;
        if (previousOwner != null) {
            previousOwner.removeFromControlledLand(this);
        }
        if (ownedByBuilding != null) {
            ownedByBuilding.addToControlledLand(this);
        }
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }
    
    public boolean isEmpty(){
        return building == null && isTerrainWalkable();
    }

    public boolean canSpawnFarm() {
        return isEmpty() && (ownedByBuilding == null || ownedByBuilding instanceof FarmOwningBuilding);
    }

    public boolean isEmptyAndUnowned(){
        return building == null && !isOwnedByBuilding();
    }
    
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public LandType getLandType(){
        return land.getLandType();
    }

    public void createNewLandForPoint(LandType landType) {
        this.land = LandFactory.createLand(landType);
    }
    
    public Land setLand(Land land){
        this.land = land;
        return land;
    }

    public Color getColor() {
        if (getBuilding() != null){
            return getBuilding().getColor();
        }
        return land.getColor();
    }
    
    public Land getLand(){
        return land;
    }

    public int getElevation() {
        return elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    @Override
    public String toString() {
        return "Point{" + "x=" + x 
                + ", y=" + y 
                + ", land=" + this.getClass().getSimpleName()
                + "fertility=" + this.getLand().getFertilityLevel()
                + ", building=" + ((getBuilding() != null) ? getBuilding().getInfo() : "")
                + ", elevation: " + elevation  + '}';
    }
    
    public String getInfo(){
        String ownerInfo = "NONE";
        if (isOwnedByBuilding()) {
            Building owner = getPointOwner();
            ownerInfo = "{" + owner.getClass().getSimpleName() + ": " + owner.getId()
                    + ", X=" + owner.getPoint().getX() + ", Y=" + owner.getPoint().getY() + "}";
        }
        return "Point{X=" + x + ", Y=" + y + ", land=" + land.getLandType() + "}"
                + "\nBuilding: " + (getBuilding() != null ? getBuilding().getInfo() : "NONE")
                + "\npoint belongs to building: " + ownerInfo;
    }
    
    public String getPositionString(){
        return getX() + "," + getY();
    }
}
