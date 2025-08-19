package buildings;

import LandTypes.LandType;
import empirebuilder.Point;

import java.awt.*;

public class Village extends FarmOwningBuilding{
    
    Point villageCenter;
    VillageOwningBuilding owner;
    
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

    public boolean hasOwner(){
        return owner != null;
    }

    public VillageOwningBuilding getOwner() {
        return owner;
    }

    public void setOwner(VillageOwningBuilding owner) {
        this.owner = owner;
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

                ", owner=" + (hasOwner() ? (owner.getPoint().getPositionString() + ", class: " + owner.getClass()) : "NONE") +
                ", food=" + food +
                ", foodNeededToCreateNewFarm=" + foodNeededToCreateNewFarm + "}";

    }
    
}