package buildings;

import LandTypes.LandType;
import buildingsTools.FarmFertilityColors;
import empirebuilder.Game;
import empirebuilder.Point;

import java.util.concurrent.ThreadLocalRandom;

public class Farm extends Building {
    
    double food;
    int timeUntilNextDeath;
    private FarmTechLevel techLevel;
    private FarmTechLevel technologyPotential = FarmTechLevel.LEVEL_1;
    private boolean haveTools;
    private int toolDuration;
    private boolean haveFertiliser;
    private int fertiliserDuration;
    public static final int TOOL_BASE_DURATION = 100;
    public static final int TOOL_REORDER_THRESHOLD = 50;
    public static final int FERTILISER_BASE_DURATION = 100;
    boolean partOfVillageCenter = false;
    public double successLevel;
    public double successLevelChange;
    public boolean active;
    int searchForNewOwnerCoolDown;

    final static int STARTING_FERTILITY_LEVEL = 0;
    final int EXPAND_THRESHOLD = 5;
    final int MAXIMUM_TIME_BEFORE_DEATH = 25;
    final int FOOD_COST_TO_MULTIPLY = 10;
    final int BASE_FARM_CAPACITY = 4;
    final int PEOPLE_REQUIRED_FOR_TECHNOLOGY_LEVEL_2 = 4;
    final double FOOD_TAX_RATE_VILLAGE = 0.3;
    final double FOOD_TAX_RATE_TOWN = 0.4;
    final double FOOD_TAX_RATE_CITY = 0.5;

    public Farm(int people, Point point) {
        super(point, FarmFertilityColors.getColor(STARTING_FERTILITY_LEVEL), DEFAULT_BUILDING_HEALTH); // change this
        setPeople(people);
        food=0;
        timeUntilNextDeath = ThreadLocalRandom.current().nextInt(MAXIMUM_TIME_BEFORE_DEATH);
        techLevel = FarmTechLevel.LEVEL_1;
        successLevel = 1;
        successLevelChange = 0;
        searchForNewOwnerCoolDown = 0;
    }

    public Farm(Point point) {
        this(1, point);
    }

    @Override
    public String getImagePath() {
        return "/resources/images/FarmImageLowResolution.png"; // alternative, prettier but not suitable for low resolution // "/resources/images/farmImage.png";
    }

    @Override
    public void tick(Game game) {

    }

    public String getImageName(){
        return "farm";
    }

    // TODO fix, farmowningBuilding can be null
    public void donateAllFoodToOwner(){
        getFarmOwningBuilding().payFoodTax(getFood());
        setFood(0);
    }

    public FarmTechLevel getTechLevel() {
        return techLevel;
    }

    public FarmTechLevel getTechnologyPotential() {
        return technologyPotential;
    }

    public boolean hasTools() {
        return haveTools;
    }

    public int getToolDuration() {
        return toolDuration;
    }

    public boolean hasFertiliser() {
        return haveFertiliser;
    }

    public int getFertiliserDuration() {
        return fertiliserDuration;
    }

    public boolean needsTools() {
        return isAlive() && getPeople() > 0 && toolDuration <= TOOL_REORDER_THRESHOLD
                && technologyPotential.ordinal() >= FarmTechLevel.LEVEL_4.ordinal();
    }

    public boolean canUseFertiliser() {
        return isAlive() && getPeople() > 0 && haveTools && !haveFertiliser
                && techLevel == FarmTechLevel.LEVEL_5;
    }

    public void equipTools() {
        if (!needsTools()) {
            throw new IllegalStateException("Farm is not eligible for tools: " + getId());
        }
        haveTools = true;
        toolDuration += TOOL_BASE_DURATION;
        checkAndUpdateTechLevel();
    }

    public void applyFertiliser() {
        if (!canUseFertiliser()) {
            throw new IllegalStateException("Farm is not eligible for fertiliser: " + getId());
        }
        haveFertiliser = true;
        fertiliserDuration = FERTILISER_BASE_DURATION;
        checkAndUpdateTechLevel();
    }

    public boolean isPartOfVillageCenter(){
        return partOfVillageCenter;
    }

    public void setIsPartOfVillageCenter(boolean val){
        partOfVillageCenter = val;
        if(partOfVillageCenter){
            setColor(LandType.getBaseColor(LandType.VILLAGE));
        }
    }

    public void tick(){
        if(belongsToFarmOwningBuilding()){
            belongsToFarmOwningBuildingTick();
        }
        else {
            independentTick();
        }
        tickSupplies();
    }

    private void tickSupplies() {
        boolean expired = false;
        if (haveTools && --toolDuration == 0) {
            haveTools = false;
            expired = true;
        }
        if (haveFertiliser && --fertiliserDuration == 0) {
            haveFertiliser = false;
            expired = true;
        }
        if (expired) {
            checkAndUpdateTechLevel();
        } else if (haveTools && toolDuration == TOOL_REORDER_THRESHOLD
                && getFarmOwningBuilding() != null) {
            getFarmOwningBuilding().updateToolRequest(this);
        }
    }

    private void resetSupplies() {
        haveTools = false;
        toolDuration = 0;
        haveFertiliser = false;
        fertiliserDuration = 0;
        technologyPotential = FarmTechLevel.LEVEL_1;
    }

    public void independentTick(){
        // TODO determine if to use success level or not
        // successlevel ranges between 0.0 and 2.0 and slowly changes a farms output over time

        //changeSuccessLevel(getSuccessLevelChange());
        //double increasedFood = getSuccessLevel() * (getTechLevel().getLevel() * (getPoint().getLand().getFertilityLevel()));
        double fertility = getPoint().getLand().getFertilityLevel();
        double increasedFood = getTechLevel().getLevel() + fertility - 1;

        if (increasedFood < 1 && fertility > 0) {
            increasedFood = 1;
        }

        setCurrentFoodTaxIncome(increasedFood);
        food += increasedFood;
        timeUntilNextDeath--;
        if (farmHasRoomForMorePeople() && getFood() >= FOOD_COST_TO_MULTIPLY){
            increasePeople();
            food -= FOOD_COST_TO_MULTIPLY;
            checkAndUpdateTechLevel();
        }
        searchForNewOwnerCoolDown--;
    }

    public void belongsToFarmOwningBuildingTick(){
        // TODO determine if to use success level or not
        //changeSuccessLevel(getSuccessLevelChange());
        //double increasedFood = getSuccessLevel() * (getTechLevel().getLevel() * getPoint().getLand().getFertilityLevel());
        double fertility = getPoint().getLand().getFertilityLevel();
        double increasedFood = getTechLevel().getLevel() + fertility - 1;

        if (increasedFood < 1 && fertility > 0) {
            increasedFood = 1;
        }
        setCurrentFoodTaxIncome(increasedFood);
        timeUntilNextDeath--;
        if (increasedFood > 0){
            if (farmHasRoomForMorePeople() || getFood() > FOOD_COST_TO_MULTIPLY){
                double foodToPay = increasedFood * calculateTaxRate();
                getFarmOwningBuilding().payFoodTax(foodToPay);
                food += increasedFood-foodToPay;
                if (farmHasRoomForMorePeople() && getFood() > FOOD_COST_TO_MULTIPLY){
                    increasePeople();
                    food -= FOOD_COST_TO_MULTIPLY;
                    checkAndUpdateTechLevel();
                }
            }
            else if (getFarmOwningBuilding() instanceof Village village){
                double foodToPay = increasedFood * calculateTaxRate();
                village.payFoodTax(foodToPay);
                village.addCommunalFood(increasedFood-foodToPay);
            }
            else {
                getFarmOwningBuilding().payFoodTax(increasedFood);
            }
        }
    }

    public void resetState(){
        setPeople(0);
        food = 0;
        resetSupplies();
        partOfVillageCenter = false;
        setOwner(null);
        techLevel = FarmTechLevel.LEVEL_1;
        successLevel = 1;
        successLevelChange = 0.0;
        active = false;
        timeUntilNextDeath = ThreadLocalRandom.current().nextInt(MAXIMUM_TIME_BEFORE_DEATH);
    }

    public void activate(Point point, int people){
        resetSupplies();
        partOfVillageCenter = false;
        setOwner(null);
        setIsAlive(true);
        setHealth(DEFAULT_BUILDING_HEALTH);
        setCurrentFoodTaxIncome(0);
        setLastIterationFoodTaxIncome(0);
        food=0;
        timeUntilNextDeath = ThreadLocalRandom.current().nextInt(MAXIMUM_TIME_BEFORE_DEATH);
        techLevel = FarmTechLevel.LEVEL_1;
        successLevel = 1;
        successLevelChange = 0;
        setPoint(point);
        setPeople(people);
        active = true;
        checkAndUpdateTechLevel();
    }

    public boolean timeToSearchForNewOwner(){
        return searchForNewOwnerCoolDown <= 0;
    }

    public void resetSearchForNewOwnerCoolDown(){
        searchForNewOwnerCoolDown = 50;
    }

    public int foodRequiredForNewFarm(){
        if (getFarmOwningBuilding() != null){
            return FOOD_COST_TO_MULTIPLY * 12;
        }
        return FOOD_COST_TO_MULTIPLY;
    }

    public void uncommonFarmTick(){
        checkAndUpdateTechLevel();
    }

    public void checkAndUpdateTechLevel(){
        if (hasOwner()) {
            Building topOwner = getFarmOwningBuilding().getTopOwner();
            if (topOwner instanceof Village){
                technologyPotential = FarmTechLevel.LEVEL_3;
            }
            else if (topOwner instanceof Town) {
                technologyPotential = FarmTechLevel.LEVEL_4;
            }
            else if (topOwner instanceof City){
                technologyPotential = haveFertiliser ? FarmTechLevel.LEVEL_6 : FarmTechLevel.LEVEL_5;
            }
            techLevel = haveTools ? technologyPotential : FarmTechLevel.LEVEL_3;
        }
        else {
            if (getPeople() >= PEOPLE_REQUIRED_FOR_TECHNOLOGY_LEVEL_2) {
                technologyPotential = FarmTechLevel.LEVEL_2;
            }
            else {
                technologyPotential = FarmTechLevel.LEVEL_1;
            }
            techLevel = technologyPotential;
        }
        if (!isPartOfVillageCenter()){
            setColor(FarmFertilityColors.getColor(getTechLevel().getLevel()));
        }
        if (getFarmOwningBuilding() != null) {
            getFarmOwningBuilding().updateToolRequest(this);
        }
    }

    public boolean farmHasRoomForMorePeople(){
        return getPeople() < calculateMaxPopulation();
    }

    public int calculateMaxPopulation(){
        return BASE_FARM_CAPACITY + (getTechLevel().getLevel() + (int)getPoint().getLand().getFertilityLevel());
    }

    public double calculateTaxRate(){
        Building topOwner = getFarmOwningBuilding().getTopOwner();
        if (topOwner instanceof City){
            return FOOD_TAX_RATE_CITY;
        }
        else if (topOwner instanceof Town){
            return FOOD_TAX_RATE_TOWN;
        }
        else return FOOD_TAX_RATE_VILLAGE;
    }

    public boolean belongsToFarmOwningBuilding(){
        return hasOwner();
    }

    public void updateColor(){
        if (!isPartOfVillageCenter()){
            setColor(FarmFertilityColors.getColor(getTechLevel().getLevel()));
        }
    }
    
    public boolean checkIfLastPersonOnFarmDies(){
        if (timeUntilNextDeath <= 0){
            setPeople(getPeople() - 1);
            if (getPeople() <= 0){
                // Game class removes this farm
                return true;
            }
            if (getPeople() < PEOPLE_REQUIRED_FOR_TECHNOLOGY_LEVEL_2){
                checkAndUpdateTechLevel();
            }
            timeUntilNextDeath = ThreadLocalRandom.current().nextInt(MAXIMUM_TIME_BEFORE_DEATH);
        }
        return false;
    }

    public double getSuccessLevelChange() {
        return successLevelChange;
    }

    public void setSuccessLevelChange(double successLevelChange) {
        this.successLevelChange = successLevelChange;
    }

    public boolean isTimeToCreateNewFarm(){
        return (getFood() >= foodRequiredForNewFarm()
        && getPeople() > EXPAND_THRESHOLD);
    }

    public void consumeFoodForNewFarm(){
        food -= foodRequiredForNewFarm();
    }

    public double getSuccessLevel() {
        return successLevel;
    }

    public void setSuccessLevel(double successLevel) {
        this.successLevel = successLevel;
    }

    public void changeSuccessLevel(double successLevelChangeRate){
        successLevel = (Math.max(0.0, Math.min(2.0, (successLevelChangeRate+successLevel))));
    }

    public void halvePeopleAmount(){
        setPeople(getPeople() / 2);
        checkAndUpdateTechLevel();
    }
    
    public FarmOwningBuilding getFarmOwningBuilding() {
        return (FarmOwningBuilding) getOwner();
    }

    public void setFarmOwningBuilding(FarmOwningBuilding farmOwningBuilding) {
        setOwner(farmOwningBuilding);
    }

    @Override
    public void setOwner(Building owner) {
        if (owner != null && !(owner instanceof FarmOwningBuilding)) {
            throw new IllegalArgumentException("Farm owner must own farms");
        }
        FarmOwningBuilding previousOwner = getFarmOwningBuilding();
        if (previousOwner != null && previousOwner != owner) {
            previousOwner.removeToolRequest(this);
        }
        super.setOwner(owner);
        checkAndUpdateTechLevel();
    }
    
    public void removeFarmingOwningBuilding(){
        setOwner(null);
    }
    
    public void increaseFoodBy1(){
        food++;
    }

    public double getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    @Override
    public String toString() {
        return "Farm{" + "people=" + getPeople() + ", MAXIMUM_TIME_BEFORE_DEATH=" + MAXIMUM_TIME_BEFORE_DEATH + ", FOOD_COST_TO_MULTIPLY=" + FOOD_COST_TO_MULTIPLY + ", FARM_CAPACITY=" + calculateMaxPopulation() + ", food=" + food + ", FarmOwningBuilding=" + getFarmOwningBuilding() + ", timeUntilNextDeath=" + timeUntilNextDeath + '}';
    }

    @Override
    public String getInfo(){
        return "Farm(id:" + getId() + "){" + "people=" + getPeople() + "/" +calculateMaxPopulation()
                + ",point=" + getPoint().getX() + ","+getPoint().getY()
                + ", Tech=" + getTechLevel()
                + ", technologyPotential=" + technologyPotential
                + ", tools=" + toolDuration
                + ", fertiliser=" + fertiliserDuration
                + ", food=" + String.format("%.2f", food)
                + ", foodIncome=" + String.format("%.2f",getCurrentFoodTaxIncome())
                + (getFarmOwningBuilding()!=null ? " (" + String.format("%.2f",(calculateTaxRate()*getLastIterationFoodTaxIncome())) + " taxed)" : "")
                + ", farmSuccessLevel=" + String.format("%.2f", getSuccessLevel())
                + ", timeUntilNextDeath=" + timeUntilNextDeath
                + ", Owned by=" +(belongsToFarmOwningBuilding() ? (getFarmOwningBuilding().getClass().getSimpleName() + "(" + getFarmOwningBuilding().getPoint().getPositionString()) +")" : "None ")
                + ", isPartOfVillageCenter=" + isPartOfVillageCenter()
                + '}';
    }
}