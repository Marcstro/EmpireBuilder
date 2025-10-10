package buildings;

import LandTypes.LandType;
import empirebuilder.Point;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


public class Town extends VillageOwningBuilding {

    Set<TownArea> townAreaPoints;
    City city = null;
    
    static final int INITIAL_FOOD_NEEDED_TO_GROW = 50;
    final double TOWN_TAXATION_RATE = 0.6;
    
    public Town(Point point) {
        super(point, INITIAL_FOOD_NEEDED_TO_GROW, LandType.getBaseColor(LandType.TOWN));
        farms = new LinkedList();
        food = 0;
        townAreaPoints = new HashSet();
    }

    public Town(){
        super();
    }

    @Override
    public String getImagePath() {
        return "/resources/images/TownImage.png";
    }

    @Override
    void processTaxation(double foodIncome) {
        if (city != null){
            city.processTaxation(((foodIncome*TOWN_TAXATION_RATE)));
            addToCurrentFoodTaxIncome((foodIncome*(1-TOWN_TAXATION_RATE)));
            food += (foodIncome*(1-TOWN_TAXATION_RATE));
        }
        else {
            addToCurrentFoodTaxIncome(foodIncome);
            food += foodIncome;
        }
    }

    @Override
    Building getTopOwner() {
        if (hasCity()){
            return city.getTopOwner();
        }
        else return this;
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
        return "{Town: " + getId() +
                ", point=" + getPoint().getPositionString() +
                ", food = " + String.format("%.2f", getFood()) +
                ", people=" + getPeople() +
                ", gold=" + String.format("%.2f", getGold()) +
                ", wealth=" + String.format("%.2f", getWealth()) +
                ", villages=" + getVillages().size() +
                ", currentTaxIncome=" + String.format("%.2f", getLastIterationFoodTaxIncome()) +
                (getCity()!=null ? " (" + String.format("%.2f",(TOWN_TAXATION_RATE*getLastIterationFoodTaxIncome())) + " taxed)" : "") +
                (this.hasCity() ? ", City: " + getCity().getPoint().getPositionString() : ", Has city: None")
                + ".} ";
    }
}