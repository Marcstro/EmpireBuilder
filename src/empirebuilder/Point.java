package empirebuilder;

import buildings.Building;
import LandTypes.Land;
import LandTypes.LandFactory;
import LandTypes.LandType;
import buildings.Farm;
import buildings.FarmOwningBuilding;

import java.awt.*;

public class Point {
    private int x, y;
    private Land land;
    private Building building;
    private FarmOwningBuilding ownedByBuilding;
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

    public FarmOwningBuilding getPointOwner() {
        return ownedByBuilding;
    }

    public void setOwnerBuilding(FarmOwningBuilding ownedByBuilding) {
        this.ownedByBuilding = ownedByBuilding;
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
    
    public void setLand(Land land){
        this.land = land;
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
                + ", land=" + land 
                + ", building=" + ((getBuilding() != null) ? getBuilding().getInfo() : "")
                + ", elevation: " + elevation  + '}';
    }
    
    public String getInfo(){
        String buildingInfo = " no buildinginfo";
        if (getBuilding() != null){
            buildingInfo = getBuilding().getInfo();
        }
               return "Point{" + "x=" + x 
                + ", y=" + y 
                + ", land=" + land
                + ", " + buildingInfo
                + ", belongs to building: "+ (isOwnedByBuilding() ? getPointOwner().getInfo() : " ");
    }
    
    public String getPositionString(){
        return getX() + "," + getY();
    }
}
