package buildings;

import LandTypes.LandType;
import empirebuilder.Point;

public class Village extends FarmOwningBuilding{
    
    Point villageCenter;
    VillageOwningBuilding owner;
    private int ticksUntilNextSearch;
    private double communalFoodForNewFarms;
    private double currentCommunalFoodIncome;
    private double lastIterationCommunalFoodIncome;
    
    final static int INITIAL_FOOD_NEEDED_TO_CREATE_FARM = 30;
    final double TAXATION_VILLAGE_RATE = 0.6;

    public Village(Point point) {
        super(point, INITIAL_FOOD_NEEDED_TO_CREATE_FARM, LandType.getBaseColor(LandType.VILLAGE));
        this.villageCenter = point;
        food = 0;
        ticksUntilNextSearch = 0;
        communalFoodForNewFarms = 0;
        currentCommunalFoodIncome = 0;
        lastIterationCommunalFoodIncome=0;
    }

    public Village(){
        super();
    }

    @Override
    public void tick(){
        resetCurrentCommunalFoodIncome();
        ticksUntilNextSearch--;
        super.tick();
    }

    public void convertCommunalFoodToTaxes(){
        processTaxation(getCommunalFoodForNewFarms());
        clearCommunalFoodStorage();
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
        return FOOD_NEEDED_FOR_NEW_PERSON + getFarms().size()*2; // Administrative cost. Adjust?
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
    Building getTopOwner() {
        if (getOwner() == null){
            return this;
        }
        else return getOwner().getTopOwner();
    }

    @Override
    public String getImagePath() {
        return "/resources/images/VillageImage.png";
    }

    @Override
    void processTaxation(double foodIncome) {
        if (hasOwner()){
            getOwner().processTaxation(foodIncome*TAXATION_VILLAGE_RATE);
            food += foodIncome*(1-TAXATION_VILLAGE_RATE);
            addToCurrentFoodTaxIncome(foodIncome*(1-TAXATION_VILLAGE_RATE));
        }
        else {
            food += foodIncome;
            addToCurrentFoodTaxIncome(foodIncome);
        }
    }
    
    public void markCenter(){
        villageCenter.createNewLandForPoint(LandType.TOWN);
        setColor(LandType.getBaseColor(LandType.TOWN));
    }

    public boolean hasOwner(){
        return owner != null;
    }

    public VillageOwningBuilding getOwner() {
        return owner;
    }

    public void setOwner(VillageOwningBuilding owner) {
        this.owner = owner;
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
                ", people=" + getPeople() +
                ", food=" + String.format("%.2f", getFood()) +
                ", communalFood=" + String.format("%.2f", getCommunalFoodForNewFarms()) +
                "/" + calculateFoodToCreateNewFarm() +
                ", amount of farms controlled: " + getFarms().size() +
                "/" + (getControlledLand().size()-1) + // excluding center point
                ", communalFoodIncome="+ String.format("%.2f", getLastIterationCommunalFoodIncome()) +
                ", currentFoodTaxIncome=" + String.format("%.2f", getLastIterationFoodTaxIncome()) +
                (hasOwner() ? " (" + String.format("%.2f",(TAXATION_VILLAGE_RATE*getLastIterationFoodTaxIncome())) + " taxed)" : "") +
                ", owner=" + (hasOwner() ? (owner.getPoint().getPositionString() + ", class: " + owner.getClass()) : "NONE") +
                ", gold=" + String.format("%.2f", getGold()) +
                ", wealth=" + String.format("%.2f", getWealth()) +
                "}";
    }
    
}