package buildings;

import empirebuilder.Point;

import java.awt.*;
import java.util.*;

public abstract class FarmOwningBuilding extends Building{
    
    LinkedList<Farm> farms;
    Set<Point> controlledLand;
    LinkedList<Point> emptyLand;
    double food;
    int foodNeededToCreateNewFarm;
    private int people;


    public FarmOwningBuilding(Point point, int foodNeededToCreateNewFarm, Color color, double health) {
        super(point, color, health);
        farms = new LinkedList();
        controlledLand = new HashSet<>();
        emptyLand = new LinkedList();
        this.foodNeededToCreateNewFarm = foodNeededToCreateNewFarm;
    }

    public void tick(){

        resetCurrentFoodTaxIncome();
        int foodCost = people;
        if (foodCost > food){
            int deaths = (int)((food-foodCost)/FOOD_NEEDED_FOR_NEW_PERSON); //this will be a negative number
            people += deaths;
            food = 0;
        }
        else {
            food -= foodCost;
            int births = (int)(food/FOOD_NEEDED_FOR_NEW_PERSON);
            people += births;
        }

        // TODO implement destruction of Buildings when they lose all people
        // current issue: how to deal with building having 0 people at the beginning of its lifespan
        /*if (people <= 0) {
            people = 0;
        }*/

        int goldIncome = people;

        if (goldIncome > getWealth()) {
            increaseWealth();
        } else if (goldIncome < getWealth()) {
            decrease();
        }
        gold += goldIncome;
    }

    abstract void processTaxation(double foodIncome);
    
    public Farm getRandomFarm(){
        return farms.peekLast();
    }
        
    public void removeFromFarmList(Farm farm){
        farms.remove(farm);
        emptyLand.add(farm.getPoint());
        Collections.shuffle(emptyLand);
    }
    
    public void addFood(int foodAdd){
        food+=foodAdd;
    }
    
    public double getFood(){
        return food;
    }
    
    public void setFood(double food){
        this.food = food;
    }
    
    public void addEmptyPoint(Point point){
        emptyLand.add(point);
        Collections.shuffle(emptyLand);
    }

    public LinkedList<Farm> getFarms() {
        return farms;
    }

    public void setFarms(LinkedList<Farm> farms) {
        this.farms = farms;
    }
    
    public void addFarm(Farm farm){
        farms.add(farm);
        emptyLand.remove(farm.getPoint());
    }

    public int getPeople() {
        return people;
    }

    public void setPeople(int people) {
        this.people = people;
    }

    public void increasePeopleByOne(){
        people++;
    }

    public Set<Point> getControlledLand() {
        return controlledLand;
    }

    public void setControlledLand(Set<Point> controlledLand) {
        this.controlledLand = controlledLand;
    }

    public void removeFromControlledLand(Point point){
        controlledLand.remove(point);
    }

    public void addToControlledLand(Point point){
        controlledLand.add(point);
    }

    public LinkedList<Point> getEmptyLand() {
        return emptyLand;
    }

    public void setEmptyLand(LinkedList<Point> emptyLand) {
        this.emptyLand = emptyLand;
    }
    
    public Point getRandomEmptySpotWithinDomain(){
        return emptyLand.pollFirst();
    }

    public void removePointFromEmptyPointList(Point point){
        emptyLand.remove(point);
    }
}