package buildings;

import LandTypes.LandType;
import empirebuilder.Point;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class City extends VillageOwningBuilding{

    Set<CityArea> cityAreaPoints;
    LinkedList<Town> towns;

    static final int INITIAL_FOOD_NEEDED_TO_GROW = 50;

    public City(Point point) {
        super(point, INITIAL_FOOD_NEEDED_TO_GROW, LandType.getBaseColor(LandType.CITY));
        towns = new LinkedList<>();
        cityAreaPoints = new HashSet();
    }

    public void addCityArea(CityArea cityArea){
        cityAreaPoints.add(cityArea);
    }

    @Override
    public String getInfo() {
        return "{City: " + getId() +
                ", point="+getPoint().getPositionString() +
                ", food="+getFood() +
                ", towns=" + getTowns().size() +
                ", villages=" + getVillages().size()
                + ".} ";    }

    public void addTown(Town town){
        towns.add(town);
    }

    public void releaseTown(Town town){
        towns.remove(town);
    }

    public LinkedList<Town> getTowns() {
        return towns;
    }

    public void setTowns(LinkedList<Town> towns) {
        this.towns = towns;
    }

    public void releaseVillage(Village village){
        if (villages.contains(village)){
            // TODO add village.removeOwningBuilding()
            villages.remove(village);
        }
    }
}
