package buildings;

import LandTypes.LandType;
import buildingsTools.FarmFertilityColors;
import empirebuilder.Point;

public class Farm extends Building {
    
    int people;
    static final int EXPAND_TRESHHOLD = 5;
    final int MAXIMUM_TIME_BEFORE_DEATH = 25;
    final int FOOD_COST_TO_MULTIPLY = 10;
    final int FARM_CAPACITY = 6;
    int food;
    FarmOwningBuilding farmOwningBuilding;
    int timeUntilNextDeath;
    int fertilityLevel;
    final static int STARTING_FERTILITY_LEVEL = 1;
    boolean partOfVillageCenter = false;

    public Farm(int people, Point point) {
        super(point, FarmFertilityColors.getColor(STARTING_FERTILITY_LEVEL));
        this.people = people;
        fertilityLevel = 1;
        food=0;
        timeUntilNextDeath = (int)(Math.random()*MAXIMUM_TIME_BEFORE_DEATH);
    }

    public Farm(Point point) {
        this(1, point);
    }

    public Farm(){
        super();
    }

    @Override
    public String getImagePath() {
        return "/resources/images/FarmImageLowResolution.png";
        // alternative, prettier but not suitable for low resolution
        // "/resources/images/farmImage.png";
    }

    public void payTaxes(){

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
        //TODO check for fertilitylevel update here. or do it on rarer occasion?
        if(belongsToFarmOwningBuilding()){
            belongsToFarmOwningBuildingTick();
        }
        else {
            independentTick();
        }
    }

    public void independentTick(){
        food += getFertilityLevel();
        timeUntilNextDeath--;
        if (getFood() >= FOOD_COST_TO_MULTIPLY && people <= (FARM_CAPACITY+getFertilityLevel())){
            increasePeople();
            setFood(0);
            if (getFertilityLevel() == 2){
                improveFertility();
            }
        }
    }

    public void belongsToFarmOwningBuildingTick(){
        if (people <= FARM_CAPACITY + getFertilityLevel()){
            food += getFertilityLevel();
            if (getFood() >= FOOD_COST_TO_MULTIPLY) {
                increasePeople();
                setFood(0);
            }
        }
        else {
            farmOwningBuilding.addFood(getFertilityLevel());
        }
        if (getFertilityLevel() == 2){
            improveFertility();
        }
        else if (getFertilityLevel() == 3 && belongsToFarmOwningBuilding()){
            improveFertility();
        }
        // TODO fix this weird logic
        else if (getFertilityLevel() == 4 && belongsToFarmOwningBuilding() &&
                (getFarmOwningBuilding() instanceof Town || (getFarmOwningBuilding() instanceof Village village && village.hasOwner()))){
            improveFertility();
        }
    }

    // TODO maybe use or combine with other
    public void checkForFertilityLevel(){
        if (getFertilityLevel()==1 && getPeople() >= 3){
            improveFertility();
        }
        else if (getFertilityLevel() == 2 && belongsToFarmOwningBuilding()) {
            improveFertility();
        }
        else if (getFertilityLevel() == 3 && (getFarmOwningBuilding() instanceof Town || (getFarmOwningBuilding() instanceof Village village && village.hasOwner()))){
            improveFertility();
        }
    }
    
    public boolean belongsToFarmOwningBuilding(){
        return farmOwningBuilding != null;
    }
    
    public void improveFertility(){
        if(fertilityLevel != 5){
            fertilityLevel++;
            updateColor();
        }
    }

    public void updateColor(){
        if (!isPartOfVillageCenter()){
            setColor(FarmFertilityColors.getColor(getFertilityLevel()));
        }
    }

    public void setFertilityLevel(int fertilityLevel) {
        this.fertilityLevel = fertilityLevel;
    }
    
    public int getFertilityLevel(){
        return fertilityLevel;
    }
    
    public boolean lastPersonDied(){
        if (timeUntilNextDeath <= 0){
            people--;
            if (people == 1){
                setFertilityLevel(1);
            }
            else if (people <= 0){
                // Game file removes this farm
                return true;
            }
            timeUntilNextDeath = (int)(Math.random()*MAXIMUM_TIME_BEFORE_DEATH);

        }
        return false;
    }
    
    public void increasePeople(){
        people++;
        if (getFertilityLevel() ==1 && people >= 3){
            improveFertility();
        }
    }
    
    public boolean isTimeToCreateNewFarm(){
        if( people >= EXPAND_TRESHHOLD ){
            //food=0; // TODO uncomment if farms spread too fast
            return true;
        }
        return false;
    }
    
    public void halvePeopleAmount(){
        people=people/2;
    }
    
    public FarmOwningBuilding getFarmOwningBuilding() {
        return farmOwningBuilding;
    }

    public void setFarmOwningBuilding(FarmOwningBuilding farmOwningBuilding) {
        this.farmOwningBuilding = farmOwningBuilding;
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

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    @Override
    public String toString() {
        return "Farm{" + "people=" + people + ", MAXIMUM_TIME_BEFORE_DEATH=" + MAXIMUM_TIME_BEFORE_DEATH + ", FOOD_COST_TO_MULTIPLY=" + FOOD_COST_TO_MULTIPLY + ", FARM_CAPACITY=" + FARM_CAPACITY + ", food=" + food + ", FarmOwningBuilding=" + farmOwningBuilding + ", timeUntilNextDeath=" + timeUntilNextDeath + '}';
    }



    @Override
    public String getInfo(){
        return "Farm{" + "people=" + people 
                + ", fertility level: " + getFertilityLevel()
                + ", FARM_CAPACITY=" + FARM_CAPACITY 
                + ", food=" + food
                + ", timeUntilNextDeath=" + timeUntilNextDeath
                + ", Belongs to building = " +(belongsToFarmOwningBuilding() ? (farmOwningBuilding.getClass() + ", " + farmOwningBuilding.getPoint().getPositionString()) : " false ")
                + ", isPartOfVillageCenter=" + isPartOfVillageCenter()
                + '}';
    }
    
    
}