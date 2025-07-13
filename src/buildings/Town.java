package buildings;

import LandTypes.LandType;
import buildingsTools.FarmFertilityColors;
import empirebuilder.Point;

import java.awt.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


public class Town extends FarmOwningBuilding{

    LinkedList<Village> villages;
    Set<TownArea> townAreaPoints;
    City city = null;
    
    static final int INITIAL_FOOD_NEEDED_TO_GROW = 50;
    
    public Town(Point point) {
        super(point, INITIAL_FOOD_NEEDED_TO_GROW, LandType.getBaseColor(LandType.TOWN));
        villages = new LinkedList<>();
        farms = new LinkedList();
        food = 0;
        townAreaPoints = new HashSet();
    }

    public void setCity(City city){
        this.city = city;
    }

    public City getCity(){
        return city;
    }

    public boolean hasCity(){
        return city != null;
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

    @Override
    public String getInfo() {
        return "Town: " + getId() +
                ", point="+getPoint().getPositionString() +
                ", villages=" + getVillages().size()
                + (this.hasCity() ? "City: " + getCity().getPoint().getPositionString() : "Has city: false")
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