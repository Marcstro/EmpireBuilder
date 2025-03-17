package buildings;

import LandTypes.Grassland;
import empirebuilder.Point;

public class Farm extends Building{
    
    int people;
    static final int EXPAND_TRESHHOLD = 5;
    final int MAXIMUM_TIME_BEFORE_DEATH = 25;
    final int FOOD_COST_TO_MULTIPLY = 10;
    final int FARM_CAPACITY = 6;
    int food;
    FarmOwningBuilding farmOwningBuilding;
    int timeUntilNextDeath;
    int fertilityLevel;

        
    public Farm(int people, Point point) {
        super(point);
        this.people = people;
        fertilityLevel = 1;
        food=0;
        timeUntilNextDeath = (int)(Math.random()*MAXIMUM_TIME_BEFORE_DEATH);
    }
    
    public Farm(Point point) {
        this(1, point);
    }
    
    public void tick(){
        if(hasFarmOwningBuilding()){
            hasFarmOwningBuildingTick();
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
            if (hasFarmOwningBuilding() && getFertilityLevel() == 2){
                improveFertility();
            }
        }
    }

    public void hasFarmOwningBuildingTick(){
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
    }

    public void checkForFertilityLevel(){
        if (getFertilityLevel()==1 && getPeople() >= 3){
            improveFertility();
        }
        else if (getFertilityLevel()==2 && hasFarmOwningBuilding()) {
            improveFertility();
        }
        else if (getFertilityLevel() == 3 && getFarmOwningBuilding() instanceof Town){
            improveFertility();
        }
    }
    
    public boolean hasFarmOwningBuilding(){
        return farmOwningBuilding != null;
    }
    
    public void improveFertility(){
        if(fertilityLevel != 5){
            fertilityLevel++;
            if (getPoint().getLand() instanceof Grassland grassland){
                grassland.updateColor(fertilityLevel);
            }
           
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
            //System.out.println("Farm " + getId() + " person died, " + people + " people left");
            if (people == 1){
                setFertilityLevel(1);
            }
            else if (people <= 0){
                        //System.out.println("Farm " + getId() + " lost its last person and should be deleted");

                return true;
            }
            timeUntilNextDeath = (int)(Math.random()*MAXIMUM_TIME_BEFORE_DEATH);
            
        }
        return false;
    }
    
    // TODO maybe a farm should be limited to grasslands and not have any type of land
    public void increaseFood(){
        food+=getFertilityLevel();
    }
    
    public void increasePeople(){
        people++;
        if (getFertilityLevel() ==1 && people >= 3){
            improveFertility();
        }
        //System.out.println("Farm " + getId() + " increased to " + people + " people");
    }
    
    public boolean isTimeToCreateNewFarm(){
        if( people >= EXPAND_TRESHHOLD 
                //&& food > FOOD_COST_TO_MULTIPLY
                ){
            food=0; 
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
                + ", has farmOwningBuilding: " + hasFarmOwningBuilding()
                + (hasFarmOwningBuilding() ? getFarmOwningBuilding().getInfo() : "")
                + '}';
    }
    
    
}