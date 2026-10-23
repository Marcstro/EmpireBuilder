package buildings;

import buildings.subBuildings.Market;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Point;

import java.awt.*;
import java.util.*;

public abstract class FarmOwningBuilding extends LandOwningBuilding{
    
    LinkedList<Farm> farms;
    LinkedList<Point> emptyLand;
    double food;
    int foodNeededToCreateNewFarm;
    private double foodIncomeThisRound;
    private final Set<Farm> farmsNeedingTools = new LinkedHashSet<>();


    public FarmOwningBuilding(Point point, int foodNeededToCreateNewFarm, Color color, double health) {
        super(point, color, health);
        farms = new LinkedList();
        emptyLand = new LinkedList();
        this.foodNeededToCreateNewFarm = foodNeededToCreateNewFarm;
        foodIncomeThisRound = 0;
    }

    public void tick(){

        resetCurrentFoodTaxIncome();
        int foodCost = getPeople();
        if (foodCost > food){
            int deaths = (int)((food-foodCost)/ FOOD_NEEDED_FOR_NEW_CITIZEN); //this will be a negative number
            setPeople(getPeople() + deaths);
            food = 0;
        }
        else {
            food -= foodCost;
            int births = (int)(food/ FOOD_NEEDED_FOR_NEW_CITIZEN);
            setPeople(getPeople() + births);
        }

        // TODO implement destruction of Buildings when they lose all people
        // current issue: how to deal with building having 0 people at the beginning of its lifespan
        /*if (people <= 0) {
            people = 0;
        }*/

        int goldIncome = getPeople();

        setWealth(getPeople());
        // TODO shall wealth increase gradually? if so change an uncomment below
        /*
        if (goldIncome > getWealth()) {
            increaseWealth();
        } else if (goldIncome < getWealth()) {
            decreaseWealth();
        }*/
        addGold(goldIncome);
    }

    void payFoodTax(double foodIncome){
        foodIncomeThisRound += foodIncome;
    }

    protected double collectTotalFoodAcquired(){
        double income = foodIncomeThisRound;
        foodIncomeThisRound = 0;
        return income;
    }

    public abstract void processTotalFoodIncome();

    @Override
    public void setOwner(Building owner) {
        super.setOwner(owner);
        updateOwnedFarmsTechLevel();
    }

    public void updateOwnedFarmsTechLevel() {
        for (Farm farm : farms) {
            farm.checkAndUpdateTechLevel();
        }
    }

    public void updateToolRequest(Farm farm) {
        if (farm.getFarmOwningBuilding() == this && farms.contains(farm) && farm.needsTools()) {
            farmsNeedingTools.add(farm);
        } else {
            farmsNeedingTools.remove(farm);
        }
    }

    public void removeToolRequest(Farm farm) {
        farmsNeedingTools.remove(farm);
    }

    public int getFarmsNeedingToolsCount() {
        return farmsNeedingTools.size();
    }

    protected <T extends SubBuilding> T findActiveSupplier(Class<T> type) {
        for (Building owner = this; owner != null; owner = owner.getOwner()) {
            if (!owner.isAlive()) {
                return null;
            }
            for (SubBuilding supplier : owner.getSubBuildings()) {
                if (type.isInstance(supplier) && supplier.isActive()) {
                    return type.cast(supplier);
                }
            }
        }
        return null;
    }

    protected void paySupplier(SubBuilding supplier, int price) {
        Building seller = supplier.getMainOwner();
        if (seller == this) {
            return;
        }
        addGold(-price);
        seller.addGold(price);
    }

    protected void purchaseFarmTools() {
        boolean freeTools = this instanceof VillageOwningBuilding;
        if (farmsNeedingTools.isEmpty() || (!freeTools && getGold() < Market.TOOL_PRICE)) {
            return;
        }
        Market market = freeTools ? null : findActiveSupplier(Market.class);
        if (!freeTools && market == null) {
            return;
        }
        while (!farmsNeedingTools.isEmpty() && (freeTools || getGold() >= Market.TOOL_PRICE)) {
            Farm farm = farmsNeedingTools.iterator().next();
            if (farm.getFarmOwningBuilding() != this || !farm.needsTools()) {
                farmsNeedingTools.remove(farm);
                continue;
            }
            farm.equipTools();
            if (market != null) {
                paySupplier(market, Market.TOOL_PRICE);
            }
        }
    }

    protected boolean canPurchaseFertiliser() {
        return this instanceof Village && getTopOwner() instanceof City;
    }

    protected void purchaseFarmFertiliser() {
        if (!canPurchaseFertiliser() || getGold() < Market.FERTILISER_PRICE) {
            return;
        }
        Market market = findActiveSupplier(Market.class);
        if (market == null) {
            return;
        }
        for (Farm farm : farms) {
            if (farm.getFarmOwningBuilding() == this && farm.canUseFertiliser()) {
                farm.applyFertiliser();
                paySupplier(market, Market.FERTILISER_PRICE);
                return;
            }
        }
    }

    /** Towns and cities supply their direct farms with free tools, never fertiliser. */
    public void purchaseFarmSuppliesForDirectlyLargeOwnedBuildingsFarms() {
        if (isAlive()) {
            purchaseFarmTools();
        }
    }
    
    public Farm getRandomFarm(){
        return farms.peekLast();
    }
        
    public void removeFromFarmList(Farm farm){
        farms.remove(farm);
        removeToolRequest(farm);
        addEmptyPoint(farm.getPoint());
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
        if (point.getPointOwner() == this && !emptyLand.contains(point)) {
            emptyLand.add(point);
            Collections.shuffle(emptyLand);
        }
    }

    public LinkedList<Farm> getFarms() {
        return farms;
    }

    public void setFarms(LinkedList<Farm> farms) {
        this.farms = farms;
        farmsNeedingTools.clear();
        updateOwnedFarmsTechLevel();
    }
    
    public void addFarm(Farm farm){
        if (!farms.contains(farm)) {
            farms.add(farm);
        }
        emptyLand.remove(farm.getPoint());
        farm.checkAndUpdateTechLevel();
    }

    public void increasePeopleByOne(){
        increasePeople();
    }

    @Override
    public void removeFromControlledLand(Point point){
        super.removeFromControlledLand(point);
        removePointFromEmptyPointList(point);
    }

    public LinkedList<Point> getEmptyLand() {
        return emptyLand;
    }

    public void setEmptyLand(LinkedList<Point> emptyLand) {
        this.emptyLand = emptyLand;
    }
    
    public Point getRandomEmptySpotWithinDomain(){
        while (!emptyLand.isEmpty()) {
            Point point = emptyLand.pollFirst();
            if (point.getPointOwner() == this && point.canSpawnFarm()) {
                return point;
            }
        }
        return null;
    }

    public void removePointFromEmptyPointList(Point point){
        emptyLand.removeIf(candidate -> candidate == point);
    }
}