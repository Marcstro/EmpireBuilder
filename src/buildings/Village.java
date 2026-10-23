package buildings;

import LandTypes.LandType;
import buildingsTools.AnimalType;
import buildings.subBuildings.AnimalHusbandry;
import buildings.subBuildings.Market;
import empirebuilder.Game;
import empirebuilder.Point;
import entities.units.*;

public class Village extends FarmOwningBuilding implements UnitOwner {
    
    Point villageCenter;
    private int ticksUntilNextSearch;
    private double communalFoodForNewFarms;
    private double currentCommunalFoodIncome;
    private double lastIterationCommunalFoodIncome;
    
    final static int INITIAL_FOOD_NEEDED_TO_CREATE_FARM = 30;
    final double TAXATION_VILLAGE_RATE = 0.6;

    final static int FOOD_PER_FARM_ANIMAL = 75;
    final static int MAX_ANIMAL_POPULATION = 1;
    private static final int ANIMAL_PURCHASE_COOLDOWN = 900;
    private static final int MIN_GOLD_FOR_FERTILISER = 15000;

    private int animalPopulation = 0;
    private int animalPurchaseCooldown;
    private FarmAnimal farmAnimal;
    private AnimalHusbandry animalSupplier;
    AnimalType animalType;

    private final UnitManagerComponent unitManager = new UnitManagerComponent(this);

    public Village(Point point) {
        super(point, INITIAL_FOOD_NEEDED_TO_CREATE_FARM, LandType.getBaseColor(LandType.VILLAGE), DEFAULT_BUILDING_HEALTH);
        this.villageCenter = point;
        food = 0;
        ticksUntilNextSearch = 0;
        communalFoodForNewFarms = 0;
        currentCommunalFoodIncome = 0;
        lastIterationCommunalFoodIncome=0;
    }

    @Override
    public void tick(){
        resetCurrentCommunalFoodIncome();
        ticksUntilNextSearch--;
        if (animalPurchaseCooldown > 0) {
            animalPurchaseCooldown--;
        }
        super.tick();
    }

    public void convertCommunalFoodToTaxes(){
        payFoodTax(getCommunalFoodForNewFarms());
        clearCommunalFoodStorage();
    }

    public void spawnAnimal(Game game){
        if (!hasRoomForAdditionalAnimals()) {
            return;
        }
        int random = (int)(Math.random()*AnimalType.values().length);
        FarmAnimal animal;
        if (random==0){
            animal = new Capybara(villageCenter.getX(), villageCenter.getY());
        }
        else if (random==1) {
            animal = new Donkey(villageCenter.getX(), villageCenter.getY());
        }
        else {
            animal = new Alpaca(villageCenter.getX(), villageCenter.getY());
        }
        game.spawnUnitAt(animal, villageCenter);
        animal.setIdleTarget(villageCenter);
        animal.setIdleBasePoint(villageCenter);
        animal.setUnitOwner(this);
        unitManager.addUnit(animal);
        farmAnimal = animal;
        animalPopulation = 1;
    }

    public void addAnimals(Game game){
        spawnAnimal(game);
    }

    public void animalDied(){
        animalPopulation = 0;
        farmAnimal = null;
        animalSupplier = null;
    }

    public void returnAnimal(Game game) {
        if (farmAnimal == null) {
            return;
        }
        FarmAnimal animal = farmAnimal;
        game.despawnUnit(animal);
        unitManager.removeUnit(animal);
        animal.setIsAlive(false);
        animal.setUnitOwner(null);
    }

    public int getAnimalPopulation() {
        return animalPopulation;
    }

    public int getAnimalPurchaseCooldown() {
        return animalPurchaseCooldown;
    }

    public AnimalType getAnimalType() {
        return animalType;
    }

    public void setAnimalType(AnimalType animalType) {
        this.animalType = animalType;
    }

    @Override
    public UnitManagerComponent getUnitManagerComponent() {
        return unitManager;
    }

    @Override
    public Point getInstructions(Unit unit, Game game) {
        return getPoint();
    }

    public boolean timeToRedoNearbySearch(){
        return ticksUntilNextSearch <= 0;
    }

    public void applySearchCoolDown(){
        ticksUntilNextSearch = 100;
    }

    public void resetCooldown() {
        this.ticksUntilNextSearch = 0;
    }

    public int getTicksUntilNextSearch() {
        return ticksUntilNextSearch;
    }

    public void setTicksUntilNextSearch(int ticksUntilNextSearch) {
        this.ticksUntilNextSearch = ticksUntilNextSearch;
    }

    public double getCurrentCommunalFoodIncome() {
        return currentCommunalFoodIncome;
    }

    public void addToCurrentCommunalFoodIncome(double currentCommunalFoodIncome) {
        this.currentCommunalFoodIncome += currentCommunalFoodIncome;
    }

    public void setCurrentCommunalFoodIncome(double income){
        currentCommunalFoodIncome=income;
    }

    public void resetCurrentCommunalFoodIncome(){
        setLastIterationCommunalFoodIncome(currentCommunalFoodIncome);
        currentCommunalFoodIncome=0;
    }

    public double getLastIterationCommunalFoodIncome() {
        return lastIterationCommunalFoodIncome;
    }

    public void setLastIterationCommunalFoodIncome(double lastIterationCommunalFoodIncome) {
        this.lastIterationCommunalFoodIncome = lastIterationCommunalFoodIncome;
    }

    public boolean hasCommunalFoodToCreateNewFarm(){
        return getCommunalFoodForNewFarms() >= calculateFoodToCreateNewFarm();
    }

    public void removeCostOfNewFarm(){
        removeCommunalFood(calculateFoodToCreateNewFarm());
    }

    public int calculateFoodToCreateNewFarm(){
        return FOOD_NEEDED_FOR_NEW_FARM + getFarms().size()*2; // TODO Administrative cost. Adjust?
    }

    public double getCommunalFoodForNewFarms() {
        return communalFoodForNewFarms;
    }

    public void clearCommunalFoodStorage(){
        communalFoodForNewFarms=0;
    }

    public void addCommunalFood(double addedFood) {
        addToCurrentCommunalFoodIncome(addedFood);
        communalFoodForNewFarms += addedFood;
    }

    public void removeCommunalFood(double removedFood){
        communalFoodForNewFarms -= removedFood;
    }

    @Override
    public String getImagePath() {
        return "/resources/images/VillageImage.png";
    }

    @Override
    public void tick(Game game) {
        if (!isAlive() || !hasOwner()) {
            returnAnimal(game);
            return;
        }
        purchaseFarmTools();
        payAnimalRent(game);
        if (animalPurchaseCooldown == 0 && hasRoomForAdditionalAnimals()
                && canAffordAnimal()) {
            AnimalHusbandry husbandry = findActiveSupplier(AnimalHusbandry.class);
            if (husbandry != null) {
                spawnAnimal(game);
                paySupplier(husbandry, AnimalHusbandry.ANIMAL_PRICE);
                animalSupplier = husbandry;
                animalPurchaseCooldown = ANIMAL_PURCHASE_COOLDOWN;
            }
        }
        purchaseFarmFertiliser();
    }

    public boolean canAffordAnimal() {
        double toolCostPerTick = getFarms().size()
                * ((double) Market.TOOL_PRICE / Farm.TOOL_BASE_DURATION);
        double incomeAfterTools = getPeople() - toolCostPerTick;
        double rentShortfall = Math.max(0, AnimalHusbandry.ANIMAL_RENT_PER_TICK - incomeAfterTools);
        double requiredSavings = rentShortfall * FarmAnimal.LIFESPAN_BUILDING_TICKS;
        return getGold() >= AnimalHusbandry.ANIMAL_PRICE + requiredSavings;
    }

    private void payAnimalRent(Game game) {
        if (animalSupplier == null) {
            return;
        }
        if (!isAnimalSupplierValid() || getGold() < AnimalHusbandry.ANIMAL_RENT_PER_TICK) {
            returnAnimal(game);
            return;
        }
        paySupplier(animalSupplier, AnimalHusbandry.ANIMAL_RENT_PER_TICK);
    }

    private boolean isAnimalSupplierValid() {
        if (!animalSupplier.isActive()) {
            return false;
        }
        for (Building owner = getOwner(); owner != null; owner = owner.getOwner()) {
            if (!owner.isAlive()) {
                return false;
            }
            if (owner == animalSupplier.getMainOwner()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean canPurchaseFertiliser() {
        return animalPurchaseCooldown > 0
                && getGold() >= MIN_GOLD_FOR_FERTILISER
                && super.canPurchaseFertiliser();
    }

    public String getImageName(){
        return "village";
    }

    @Override
    public void processTotalFoodIncome() {
        double totalIncome = collectTotalFoodAcquired()
                + (animalPopulation * FOOD_PER_FARM_ANIMAL);
        if (hasOwner()){
            getVillageOwningBuilding().payFoodTax(totalIncome*TAXATION_VILLAGE_RATE);
            food += totalIncome*(1-TAXATION_VILLAGE_RATE);
            addToCurrentFoodTaxIncome(totalIncome*(1-TAXATION_VILLAGE_RATE));
        }
        else {
            food += totalIncome;
            addToCurrentFoodTaxIncome(totalIncome);
        }
    }

    public boolean hasRoomForAdditionalAnimals(){
        return animalPopulation < MAX_ANIMAL_POPULATION;
    }
    
    public void markCenter(){
        setColor(LandType.getBaseColor(LandType.TOWN));
    }

    public void unmarkCenter(){
        setColor(LandType.getBaseColor(LandType.VILLAGE));
    }

    public boolean hasOwner(){
        return getOwner() != null;
    }

    public VillageOwningBuilding getVillageOwningBuilding() {
        return (VillageOwningBuilding) getOwner();
    }

    public void setVillageOwningBuilding(VillageOwningBuilding villageOwningBuilding) {
        setOwner(villageOwningBuilding);
    }

    public Point getVillageCenter() {
        return villageCenter;
    }

    public void setVillageCenter(Point villageCenter) {
        this.villageCenter = villageCenter;
    }

    @Override
    public String getInfo(){
        return "Village{" +
                "id=" + getId() +
                ", health: " + getHealth() + "/" + DEFAULT_BUILDING_HEALTH +
                ", isAlive=" + isAlive() +
                ", people=" + getPeople() +
                ", food=" + String.format("%.2f", getFood()) +
                ", gold=" + String.format("%.2f", getGold()) +
                ", animalPopulation=" + animalPopulation +
                ", animalPurchaseCooldown=" + animalPurchaseCooldown +
                ", farmsNeedingTools=" + getFarmsNeedingToolsCount() +
                ", " + unitManager.getInfo() +
                ", communalFood=" + String.format("%.2f", getCommunalFoodForNewFarms()) +
                "/" + calculateFoodToCreateNewFarm() +
                ", amount of farms controlled: " + getFarms().size() +
                "/" + (getControlledLand().size()-1) + // excluding center point
                ", communalFoodIncome="+ String.format("%.2f", getLastIterationCommunalFoodIncome()) +
                ", currentFoodTaxIncome=" + String.format("%.2f", getLastIterationFoodTaxIncome()) +
                (hasOwner() ? " (" + String.format("%.2f",(TAXATION_VILLAGE_RATE*getLastIterationFoodTaxIncome())) + " taxed)" : "") +
                ", owner=" + (hasOwner() ? (getVillageOwningBuilding().getPoint().getPositionString() + ", class: " + getVillageOwningBuilding().getClass()) : "NONE") +
                ", gold=" + String.format("%.2f", getGold()) +
                ", wealth=" + String.format("%.2f", getWealth()) +
                "}";
    }
}