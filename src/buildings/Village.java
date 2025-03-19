package buildings;

import LandTypes.LandType;
import empirebuilder.Point;

import java.awt.*;

public class Village extends FarmOwningBuilding{
    
    Point villageCenter;
    Town town;
    
    final static int INITIAL_FOOD_NEEDED_TO_CREATE_FARM = 30;

    public Village(Point point, Point villageCenter) {
        super(point, INITIAL_FOOD_NEEDED_TO_CREATE_FARM, LandType.getBaseColor(LandType.VILLAGE));
        this.villageCenter = villageCenter;
        food = 0;
    }
    
    public void tick(){
        food -=1;
    }
    
    public void markArea(){
        for(Point point: controlledLand){
            point.createNewLandForPoint(LandType.TOWN);
        }
    }
    
    public void markCenter(){
        villageCenter.createNewLandForPoint(LandType.TOWN);
        setColor(LandType.getBaseColor(LandType.TOWN));
    }
    
    public boolean hasTown(){
        return town != null;
    }

    public Town getTown() {
        return town;
    }

    public void setTown(Town town) {
        this.town = town;
    }

    public Point getVillageCenter() {
        return villageCenter;
    }

    public void setVillageCenter(Point villageCenter) {
        this.villageCenter = villageCenter;
    }
    
    @Override
    public String getInfo(){
                return "Village{" +
                "id=" + getId() + 
                ", point=" + getPoint().getPositionString() +
                ", amount of farms controlled: " + getFarms().size() +
                ", emptyLand remaining: " + emptyLand.size() +
                //", villageCenter=" + villageCenter.getX() + "," + villageCenter.getY() +

                ", town=" + (hasTown() ? (town.getPoint().getPositionString()) : "NONE") +
                ", food=" + food +
                ", foodNeededToCreateNewFarm=" + foodNeededToCreateNewFarm + "}";

    }
    
}