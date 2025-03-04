package buildings;

import LandTypes.Grassland;
import LandTypes.Land;
import empirebuilder.Point;

public class Farm extends Building{
    
    int people;
    Land land;
    static final int EXPAND_TRESHHOLD = 5;
    final int MAXIMUM_TIME_BEFORE_DEATH = 25;
    final int FOOD_COST_TO_MULTIPLY = 10;
    final int FARM_CAPACITY = 6;
    int food;
    Village village;
    int timeUntilNextDeath;

        
    public Farm(int people, Point point) {
        super(point);
        this.people = people;
        food=0;
        land=point.getLand();
        timeUntilNextDeath = (int)(Math.random()*MAXIMUM_TIME_BEFORE_DEATH);
    }
    
    public Farm(Point point) {
        this(1, point);
    }
    
    public void tick(){
        //System.out.println(toString());
        if(land instanceof Grassland grassland){
            food+=grassland.getFertilityLevel();
        }
        timeUntilNextDeath--;
        if (getFood() >= FOOD_COST_TO_MULTIPLY && people < (FARM_CAPACITY+((Grassland)land).getFertilityLevel())){
            increasePeople();
            setFood(0);
            if (hasVillage() && ((Grassland)land).getFertilityLevel() == 2){
                ((Grassland)land).improveFertility();
            }
        }
    }
    
    public boolean hasVillage(){
        return village != null;
    }
    
    public boolean lastPersonDied(){
        if (timeUntilNextDeath <= 0){
            people--;
            //System.out.println("Farm " + getId() + " person died, " + people + " people left");
            if (people == 1){
                if (land instanceof Grassland grassland){
                    grassland.setFertilityLevel(1);
                    //System.out.println("Farm " + getId() + " decreased fertility to level " + grassland.getFertilityLevel() + "");
                }
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
        if(land instanceof Grassland grassland){
            food+=grassland.getFertilityLevel();
        }
    }
    
    public void increasePeople(){
        people++;
        if (land instanceof Grassland grassland && grassland.getFertilityLevel() == 1 && people >= 3){
            grassland.improveFertility();
            //System.out.println("Farm " + getId() + " increased fertility to level " + grassland.getFertilityLevel() + "");
        }
        //System.out.println("Farm " + getId() + " increased to " + people + " people");
    }
    
    public boolean hasEnoughToStartNewFarm(){
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



    public Village getVillage() {
        return village;
    }

    public void setVillage(Village village) {
        this.village = village;
    }
    
    public void removeVillage(){
        this.village=null;
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
    
    
    


    public Land getLand() {
        return land;
    }
    
    public int getInhabitants() {
        return people;
    }

    public void setInhabitants(int inhabitants) {
        this.people = inhabitants;
    }

    @Override
    public String toString() {
        return "Farm{" + "people=" + people + ", land=" + land + ", MAXIMUM_TIME_BEFORE_DEATH=" + MAXIMUM_TIME_BEFORE_DEATH + ", FOOD_COST_TO_MULTIPLY=" + FOOD_COST_TO_MULTIPLY + ", FARM_CAPACITY=" + FARM_CAPACITY + ", food=" + food + ", village=" + village + ", timeUntilNextDeath=" + timeUntilNextDeath + '}';
    }



    @Override
    public String getInfo(){
                return "Farm{" + "people=" + people 
                        + ", land=" + land 
                        + ", fertility level: " + ((Grassland)(land)).getFertilityLevel()
                        + ", FARM_CAPACITY=" + FARM_CAPACITY 
                        + ", food=" + food 
                        + ", has village: " + hasVillage()
                        + ", timeUntilNextDeath=" + timeUntilNextDeath + '}';
    }
    
    
}