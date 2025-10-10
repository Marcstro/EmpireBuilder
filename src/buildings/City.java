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

    public City(){
        super();
    }

    @Override
    Building getTopOwner() {
        return this;
    }

    public void addCityArea(CityArea cityArea){
        cityAreaPoints.add(cityArea);
    }

    @Override
    public String getImagePath() {
        return "/resources/images/CityImage4.png";
    }

    @Override
    void processTaxation(double foodIncome) {
        addToCurrentFoodTaxIncome(foodIncome);
        food += foodIncome;
    }

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

    @Override
    public String getInfo() {
        return "{City: " + getId() +
                ", food=" + String.format("%.2f", getFood()) +
                ", people=" + getPeople() +
                ", gold=" + String.format("%.2f", getGold()) +
                ", wealth=" + String.format("%.2f", getWealth()) +
                ", towns=" + getTowns().size() +
                ", villages=" + getVillages().size() +
                ", currentTaxIncome=" + String.format("%.2f", getLastIterationFoodTaxIncome()) +
                ".} ";
    }
}
