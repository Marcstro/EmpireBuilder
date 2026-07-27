package buildings;

import empirebuilder.Point;
import entities.Entity;

import java.awt.*;

public abstract class Building extends Entity {
    
    private static int idCounter=1;
    private final int id;
    private Point point;
    private Color color;
    public double gold;
    private double wealth;
    private double currentFoodTaxIncome;
    private double lastIterationFoodTaxIncome;
    public final int FOOD_NEEDED_FOR_NEW_PERSON = 10;
    public static final int SIZE_OF_BUILDINGS = 1;
    public static final int FACTION_OF_BUILDINGS = 1;
    public static final double DEFAULT_BUILDING_HEALTH = 100;

    public Building(Point point, Color color, double health){
        super(point.getX(), point.getY(), health, SIZE_OF_BUILDINGS, FACTION_OF_BUILDINGS);
        this.id = idCounter++;
        this.point = point;
        this.color = color;
        currentFoodTaxIncome = 0;
        lastIterationFoodTaxIncome = 0;
        gold = 0;
        wealth = 0;
    }

    public abstract String getImageName();

    // Default, TODO create default image to displaying missing images
    public String getImagePath() {
         return "/resources/images/farmImage.png";
    }

    public Point getPoint(){
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public int getId(){
        return id;
    }

    public Color getColor(){
        return color;
    };

    public void setColor(Color color){
        this.color = color;
    };

    public double getGold() {
        return gold;
    }

    public void setGold(double gold) {
        this.gold = gold;
    }

    public double getCurrentFoodTaxIncome() {
        return currentFoodTaxIncome;
    }

    public void addToCurrentFoodTaxIncome(double currentFoodIncome) {
        this.currentFoodTaxIncome += currentFoodIncome;
    }

    public void resetCurrentFoodTaxIncome(){
        setLastIterationFoodTaxIncome(getCurrentFoodTaxIncome());
        currentFoodTaxIncome=0;
    }

    public void setCurrentFoodTaxIncome(double income){
        currentFoodTaxIncome = income;
    }

    public double getLastIterationFoodTaxIncome() {
        return lastIterationFoodTaxIncome;
    }

    public void setLastIterationFoodTaxIncome(double lastIterationFoodTaxIncome) {
        this.lastIterationFoodTaxIncome = lastIterationFoodTaxIncome;
    }

    public double getWealth() {
        return wealth;
    }

    public void setWealth(double wealth) {
        this.wealth = wealth;
    }

    public void increaseWealth(){
        wealth++;
    }

    public void decrease(){
        wealth--;
    }

    public abstract String getInfo();

    // TODO replace with isAlive() ?
    public boolean isDestroyed(){
        return getHealth() <= 0;
    }
    
}