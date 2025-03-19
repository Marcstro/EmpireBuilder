package empirebuilder;

import buildings.Building;
import LandTypes.Land;
import LandTypes.LandFactory;
import LandTypes.LandType;
import buildings.FarmOwningBuilding;

import java.awt.*;

public class Point {
    private int x, y;
    private Land land;
    private Building building;
    private FarmOwningBuilding ownedByBuilding;

    public Point(int x, int y, LandType landType) {
        this.x = x;
        this.y = y;
        this.land = LandFactory.createLand(landType);
        ownedByBuilding = null;
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
        return building == null;
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

    @Override
    public String toString() {
        return "Point{" + "x=" + x 
                + ", y=" + y 
                + ", land=" + land 
                + ", building=" + ((getBuilding() != null) ? getBuilding().getInfo() : "") + '}';
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
