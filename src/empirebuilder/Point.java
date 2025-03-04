package empirebuilder;

import buildings.Building;
import LandTypes.Land;
import LandTypes.LandFactory;
import LandTypes.LandType;
import java.awt.*;

public class Point {
    private int x, y;
    private Land land;
    private Building building;

    public Point(int x, int y, LandType landType) {
        this.x = x;
        this.y = y;
        this.land = LandFactory.createLand(landType);
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

    public void setLandType(LandType landType) {
        this.land = LandFactory.createLand(landType);
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
        String buildingInfo = "";
        if (getBuilding() != null){
            buildingInfo = getBuilding().getInfo();
        }
               return "Point{" + "x=" + x 
                + ", y=" + y 
                + ", land=" + land
                + ", " + buildingInfo;
    }


    
    
    
}
