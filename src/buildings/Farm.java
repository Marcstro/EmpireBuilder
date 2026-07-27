package buildings;

import LandTypes.LandType;
import buildingsTools.FarmFertilityColors;
import empirebuilder.Game;
import empirebuilder.Point;

import java.util.concurrent.ThreadLocalRandom;

public class Farm extends Building {
    
    int people;
    double food;
    FarmOwningBuilding farmOwningBuilding;
    int timeUntilNextDeath;
    private FarmTechLevel techLevel;
    boolean partOfVillageCenter = false;
    public double successLevel;
    public double successLevelChange;
    public boolean active;
    int searchForNewOwnerCoolDown;

    final static int STARTING_FERTILITY_LEVEL = 1;
    final int EXPAND_THRESHOLD = 5;
    final int MAXIMUM_TIME_BEFORE_DEATH = 25;
    final int FOOD_COST_TO_MULTIPLY = 10;
    final int BASE_FARM_CAPACITY = 5;
    final int PEOPLE_REQUIRED_FOR_TECHNOLOGY_LEVEL_2 = 4;
    final double FOOD_TAX_RATE_VILLAGE = 0.3;
    final double FOOD_TAX_RATE_TOWN = 0.4;
    final double FOOD_TAX_RATE_CITY = 0.5;

    public Farm(int people, Point point) {
        super(point, FarmFertilityColors.getColor(STARTING_FERTILITY_LEVEL), DEFAULT_BUILDING_HEALTH); // change this
        this.people = people;
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
        getFarmOwningBuilding().processTaxation(getFood());
        setFood(0);
    }

    public FarmTechLevel getTechLevel() {
        return techLevel;
    }

    public void increaseTechLevel(){
        techLevel = this.techLevel.increaseLevel();
    }

    public void decreaseTechLevel(){
        techLevel = this.techLevel.decreaseLevel();
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
    }

    public void independentTick(){
        // TODO determine if to use success level or not
        // successlevel ranges between 0.0 and 2.0 and slowly changes a farms output over time

        //changeSuccessLevel(getSuccessLevelChange());
        //double increasedFood = getSuccessLevel() * (getTechLevel().getLevel() * (getPoint().getLand().getFertilityLevel()));
        double increasedFood = getTechLevel().getLevel() * (getPoint().getLand().getFertilityLevel());
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
        double increasedFood = getTechLevel().getLevel() * getPoint().getLand().getFertilityLevel();
        setCurrentFoodTaxIncome(increasedFood);
        timeUntilNextDeath--;
        if (increasedFood > 0){
            if (farmHasRoomForMorePeople() || getFood() > FOOD_COST_TO_MULTIPLY){
                double foodToPay = increasedFood * calculateTaxRate();
                getFarmOwningBuilding().processTaxation(foodToPay);
                food += increasedFood-foodToPay;
                if (getFood() > FOOD_COST_TO_MULTIPLY && farmHasRoomForMorePeople()){
                    increasePeople();
                    food -= FOOD_COST_TO_MULTIPLY;
                    checkAndUpdateTechLevel();
                }
            }
            else if (getFarmOwningBuilding() instanceof Village village){
                double foodToPay = increasedFood * calculateTaxRate();
                village.processTaxation(foodToPay);
                village.addCommunalFood(increasedFood-foodToPay);
            }
            else {
                getFarmOwningBuilding().processTaxation(increasedFood);
            }
        }
    }

    public void resetState(){
        people = 0;
        food = 0;
        farmOwningBuilding = null;
        techLevel = FarmTechLevel.LEVEL_1;
        partOfVillageCenter = false;
        successLevel = 1;
        successLevelChange = 0.0;
        active = false;
        timeUntilNextDeath = ThreadLocalRandom.current().nextInt(MAXIMUM_TIME_BEFORE_DEATH);
    }

    public void activate(Point point, int people){
        food=0;
        timeUntilNextDeath = ThreadLocalRandom.current().nextInt(MAXIMUM_TIME_BEFORE_DEATH);
        techLevel = FarmTechLevel.LEVEL_1;
        successLevel = 1;
        successLevelChange = 0;
        setPoint(point);
        this.people = people;
        active = true;
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
        if (farmOwningBuilding != null) {
            Building topOwner = farmOwningBuilding.getTopOwner();
            if (topOwner instanceof Village){
                techLevel = FarmTechLevel.LEVEL_3;
            }
            else if (topOwner instanceof Town) {
                techLevel = FarmTechLevel.LEVEL_4;
            }
            else if (topOwner instanceof City){
                techLevel = FarmTechLevel.LEVEL_5;
            }
        }
        else {
            if (people >= PEOPLE_REQUIRED_FOR_TECHNOLOGY_LEVEL_2) {
                techLevel = FarmTechLevel.LEVEL_2;
            }
            else {
                techLevel = FarmTechLevel.LEVEL_1;
            }
        }
        if (!isPartOfVillageCenter()){
            setColor(FarmFertilityColors.getColor(getTechLevel().getLevel()));
        }
    }

    public boolean farmHasRoomForMorePeople(){
        return people < calculateMaxPopulation();
    }

    public int calculateMaxPopulation(){
        return BASE_FARM_CAPACITY + (getTechLevel().getLevel() * (int)getPoint().getLand().getFertilityLevel());
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
        return farmOwningBuilding != null;
    }

    public void updateColor(){
        if (!isPartOfVillageCenter()){
            setColor(FarmFertilityColors.getColor(getTechLevel().getLevel()));
        }
    }
    
    public boolean checkIfLastPersonOnFarmDies(){
        if (timeUntilNextDeath <= 0){
            people--;
            if (people <= 0){
                // Game class removes this farm
                return true;
            }
            if (people < PEOPLE_REQUIRED_FOR_TECHNOLOGY_LEVEL_2){
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

    public void increasePeople(){
        people++;
    }

    public boolean isTimeToCreateNewFarm(){
        return (getFood() >= foodRequiredForNewFarm()
        && people > EXPAND_THRESHOLD);
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
        people=people/2;
        checkAndUpdateTechLevel();
    }
    
    public FarmOwningBuilding getFarmOwningBuilding() {
        return farmOwningBuilding;
    }

    public void setFarmOwningBuilding(FarmOwningBuilding farmOwningBuilding) {
        this.farmOwningBuilding = farmOwningBuilding;
        checkAndUpdateTechLevel();
    }
    
    public void removeFarmingOwningBuilding(){
        this.farmOwningBuilding =null;
    }
    
    public void increaseFoodBy1(){
        food++;
    }

    public int getPeople() {
        return people;
    }

    public void setPeople(int people) {
        this.people = people;
    }

    public double getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    @Override
    public String toString() {
        return "Farm{" + "people=" + people + ", MAXIMUM_TIME_BEFORE_DEATH=" + MAXIMUM_TIME_BEFORE_DEATH + ", FOOD_COST_TO_MULTIPLY=" + FOOD_COST_TO_MULTIPLY + ", FARM_CAPACITY=" + BASE_FARM_CAPACITY + ", food=" + food + ", FarmOwningBuilding=" + farmOwningBuilding + ", timeUntilNextDeath=" + timeUntilNextDeath + '}';
    }

    @Override
    public String getInfo(){
        return "Farm(id:" + getId() + "){" + "people=" + people + "/" +calculateMaxPopulation()
                + ",point=" + getPoint().getX() + ","+getPoint().getY()
                + ", pos=" + getX() + "," + getY()
                + ", Tech=" + getTechLevel()
                + ", food=" + String.format("%.2f", food)
                + ", foodIncome=" + String.format("%.2f",getCurrentFoodTaxIncome())
                + (getFarmOwningBuilding()!=null ? " (" + String.format("%.2f",(calculateTaxRate()*getLastIterationFoodTaxIncome())) + " taxed)" : "")
                + ", farmSuccessLevel=" + String.format("%.2f", getSuccessLevel())
                + ", timeUntilNextDeath=" + timeUntilNextDeath
                + ", Owned by=" +(belongsToFarmOwningBuilding() ? (farmOwningBuilding.getClass().getSimpleName() + "(" + farmOwningBuilding.getPoint().getPositionString()) +")" : "None ")
                + ", isPartOfVillageCenter=" + isPartOfVillageCenter()
                + '}';
    }
}