package buildings;

import empirebuilder.Point;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


public class Town extends Building{

    LinkedList<Village> villages;
    LinkedList<Farm> farms;
    int food;
    Set<TownArea> townAreaPoints;
    
    public Town(Point point) {
        super(point);
        villages = new LinkedList<>();
        farms = new LinkedList();
        food = 0;
        townAreaPoints = new HashSet();
    }
    
    public void addTownArea(TownArea townArea){
        townAreaPoints.add(townArea);
    }

    public Set<TownArea> getTownAreaPoints() {
        return townAreaPoints;
    }

    public void setTownAreaPoints(Set<TownArea> townAreaPoints) {
        this.townAreaPoints = townAreaPoints;
    }
    
    public void addFarm(Farm farm){
        farms.add(farm);
    }
    
    public void addFood(int amount){
        food += amount;
    }

    public LinkedList<Farm> getFarms() {
        return farms;
    }

    public void setFarms(LinkedList<Farm> farms) {
        this.farms = farms;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }
    
    

    @Override
    public String getInfo() {
        return "Town: " + getId() +
                ", point="+getPoint().getPositionString() +
                ", villages=" + getVillages().size()
                + ".";
    }
    
    public void addVillage(Village village){
        villages.add(village);
    }

    public LinkedList<Village> getVillages() {
        return villages;
    }

    public void setVillages(LinkedList<Village> villages) {
        this.villages = villages;
    }
}