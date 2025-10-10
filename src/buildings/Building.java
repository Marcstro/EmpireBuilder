package buildings;

import empirebuilder.Point;

import java.awt.*;

public abstract class Building{
    
    private static int idCounter=1;
    private final int id;
    private Point point;
    private Color color;
    public double gold;
    private double wealth;
    private double currentFoodTaxIncome;
    private double lastIterationFoodTaxIncome;
    public final int FOOD_NEEDED_FOR_NEW_PERSON = 10;

    // TODO this is necessary to initialise buildings in order to get getImage(), maybe find another workaround
    protected Building() {
        this.id = idCounter++;
    }

    public Building(Point point, Color color){
        this.id = idCounter++;
        this.point = point;
        this.color = color;
        currentFoodTaxIncome = 0;
        lastIterationFoodTaxIncome = 0;
        gold = 0;
        wealth = 0;
    }

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
    
}