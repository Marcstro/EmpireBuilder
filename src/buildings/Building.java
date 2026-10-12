package buildings;

import buildings.ai.BuildingAI;
import buildings.ai.BuildingAIFactory;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Point;
import entities.Entity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Building extends Entity {
    
    private static int idCounter=1;
    private final int id;
    private Point point;
    private Color color;
    public double gold;
    private double wealth;
    private double currentFoodTaxIncome;
    private double lastIterationFoodTaxIncome;
    public final int FOOD_NEEDED_FOR_NEW_CITIZEN = 100;
    public final int FOOD_NEEDED_FOR_NEW_FARM = 100;
    public static final int SIZE_OF_BUILDINGS = 1;
    public static final int FACTION_OF_BUILDINGS = 1;
    public static final double DEFAULT_BUILDING_HEALTH = 100;

    Building owner = null;
    private int people = 0;
    private BuildingAI buildingAI;
    private List<SubBuilding> subBuildings = new ArrayList<>();

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

    protected void initBuildingAI() {
        this.buildingAI = BuildingAIFactory.createAI(this);
    }

    /** null for simple building */
    public BuildingAI getBuildingAI() {
        return buildingAI;
    }

    public void tickBuildingAI(empirebuilder.Game game) {
        if (buildingAI != null) {
            buildingAI.tick(this, game);
        }
    }

    // Default, TODO create default image to displaying missing images
    public String getImagePath() {
         return "/resources/images/farmImage.png";
    }

    @Override
    protected void reactOnDamage() {
        if (this instanceof DefensiveTroopBuilding def) {
            def.getDefensiveTroopComponent().recordDamageTaken();
        }
    }

    public void addSubBuilding(SubBuilding subBuilding) {
        subBuildings.add(subBuilding);
    }

    public void removeSubBuilding(SubBuilding subBuilding) {
        subBuildings.remove(subBuilding);
    }

    public List<SubBuilding> getSubBuildings() {
        return subBuildings;
    }

    protected double calculateTotalSubBuildingMaintenance() {
        double total = 0;
        for (SubBuilding subBuilding : subBuildings) {
            if (subBuilding != null) {
                total += subBuilding.getMaintenanceCost();
            }
        }
        return total;
    }

    protected void tickAreaBuildingsWithMaintenance(java.util.Collection<? extends buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot> areas, empirebuilder.Game game) {
        for (buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot area : areas) {
            buildings.subBuildings.subBuildingFunctions.SubBuilding subBuilding = area.getSubBuilding();
            if (subBuilding != null) {
                double maintenanceCost = subBuilding.getMaintenanceCost();
                if (this.getGold() < maintenanceCost) {
                    break;
                }
                subBuilding.tick(game);
                this.addGold(-maintenanceCost);
                if (subBuilding.hasExpired()) {
                    game.destroySubBuilding(subBuilding);
                }
            }
        }
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

    public void addGold(double newGold){
        gold += newGold;
    }

    public Building getOwner() {
        return owner;
    }

    public void setOwner(Building owner) {
        this.owner = owner;
    }

    public boolean hasOwner(){
        return owner != null;
    }

    public Building getTopOwner() {
        if (owner == null) return this;
        return owner.getTopOwner();
    }

    public List<Building> getAllHierarchicalOwners() {
        List<Building> owners = new ArrayList<>();
        Building current = owner;
        while (current != null) {
            owners.add(current);
            current = current.owner;
        }
        return owners;
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

    public void decreaseWealth(){
        wealth--;
    }

    public int getPeople() {
        return people;
    }

    public void setPeople(int people) {
        this.people = people;
    }

    public void increasePeople() {
        people++;
    }

    public abstract String getInfo();
}