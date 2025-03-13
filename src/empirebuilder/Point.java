package empirebuilder;

import buildings.Building;
import LandTypes.Land;
import LandTypes.LandFactory;
import LandTypes.LandType;
import buildings.Village;
import java.awt.*;

public class Point {
    private int x, y;
    private Land land;
    private Building building;
    private Village belongsToVillage;

    public Point(int x, int y, LandType landType) {
        this.x = x;
        this.y = y;
        this.land = LandFactory.createLand(landType);
        belongsToVillage = null;
    }
    
    public boolean hasVillage(){
        return belongsToVillage != null;
    }

    public Village getVillage() {
        return belongsToVillage;
    }

    public void setVillage(Village village) {
        this.belongsToVillage = village;
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
                + ", building=" + getBuilding().toString() + '}';
    }
    
    public String getInfo(){
        String buildingInfo = " no buildinginfo";
        if (getBuilding() != null){
            buildingInfo = getBuilding().getInfo();
        }
               return "Point{" + "x=" + x 
                + ", y=" + y 
                + ", land=" + land
                + ", " + buildingInfo;
    }
    
    public String getPositionString(){
        return getX() + "," + getY();
    }
}
