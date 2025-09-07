package buildings;

import empirebuilder.Point;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class FarmOwningBuilding extends Building{
    
    LinkedList<Farm> farms;
    List<Point> controlledLand;
    LinkedList<Point> emptyLand;
    int food;
    int foodNeededToCreateNewFarm;

    public FarmOwningBuilding(Point point, int foodNeededToCreateNewFarm, Color color) {
        super(point, color);
        farms = new LinkedList();
        controlledLand = new ArrayList();
        emptyLand = new LinkedList();
        food = 0;
        this.foodNeededToCreateNewFarm = foodNeededToCreateNewFarm;
    }

    public FarmOwningBuilding(){
        super();
    }
    
    public Farm getRandomFarm(){
        return farms.peekLast();
    }
        
    public void destroyFarm(Farm farm){
        farms.remove(farm);
        emptyLand.add(farm.getPoint());
    }
    
    public void addFood(int foodAdd){
        food+=foodAdd;
    }
    
    public int getFood(){
        return food;
    }
    
    public void setFood(int food){
        this.food = food;
    }
    
    public void addEmptyPoint(Point point){
        emptyLand.add(point);
        Collections.shuffle(emptyLand);
    }
    
    public void deductNewFarmCost(){
        addFood((-foodNeededToCreateNewFarm));
        if (!getEmptyLand().isEmpty()){
            foodNeededToCreateNewFarm += 15;
        }
    }

    public void dedustNewDistantFarmCost(){
        addFood((-foodNeededToCreateNewFarm*10));
        if (!getEmptyLand().isEmpty()){
            foodNeededToCreateNewFarm += 100;
        }
    }
    
    public boolean hasFoodToCreateNewFarm(){
        return getFood() > foodNeededToCreateNewFarm;
    }

    public boolean hasFoodToCreateNewDistantFarm(){
        return getFood() > foodNeededToCreateNewFarm*10;
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

    public List<Point> getControlledLand() {
        return controlledLand;
    }

    public void setControlledLand(List<Point> controlledLand) {
        this.controlledLand = controlledLand;
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

    public void occupyPoint(Point point){
        emptyLand.remove(point);
    }
}