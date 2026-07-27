package buildings;

import LandTypes.LandType;
import empirebuilder.Game;
import empirebuilder.Point;
import entities.units.Unit;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


public class Town extends VillageOwningBuilding implements AttackCapableBuilding {

    Set<TownArea> townAreaPoints;
    City city = null;
    
    static final int INITIAL_FOOD_NEEDED_TO_GROW = 50;
    final double TOWN_TAXATION_RATE = 0.6;
    final double ATTACK_RANGE = 18;

    private Unit attackTarget;
    private int shootCooldown = 0;
    private final int TOWN_ATTACK_COOLDOWN = 20;
    private boolean attackReady;
    int timeSinceLastShot;
    int ticksToFallAsleep = 60;


    //these should be removed
    private static final int AGGRESSIVE_COOLDOWN_LIMIT = 50; // Ticks between shots (when aggressive)
    private static final int LONG_SLEEP_TIMER = 500;       // Ticks before checking to go to sleep

    public Town(Point point) {
        super(point, INITIAL_FOOD_NEEDED_TO_GROW, LandType.getBaseColor(LandType.TOWN), DEFAULT_BUILDING_HEALTH);
        farms = new LinkedList();
        food = 0;
        townAreaPoints = new HashSet();
        timeSinceLastShot=0;
    }

    @Override
    public String getImagePath() {
        return "/resources/images/TownImage.png";
    }

    public String getImageName(){
        return "town";
    }

    @Override
    void processTaxation(double foodIncome) {
        if (city != null){
            city.processTaxation(((foodIncome*TOWN_TAXATION_RATE)));
            addToCurrentFoodTaxIncome((foodIncome*(1-TOWN_TAXATION_RATE)));
            food += (foodIncome*(1-TOWN_TAXATION_RATE));
        }
        else {
            addToCurrentFoodTaxIncome(foodIncome);
            food += foodIncome;
        }
    }

    @Override
    Building getTopOwner() {
        if (hasCity()){
            return city.getTopOwner();
        }
        else return this;
    }

    @Override
    public double getAttackRange() {
        return ATTACK_RANGE;
    }

    @Override
    public void setTarget(Unit unit) {
        this.attackTarget = unit;
    }

    // TODO move all to AttackCapableBuildings
    @Override
    public void tickAttack(Game game) {

        if (!isAttackReady()){
            return;
        }

        shootCooldown--;
        timeSinceLastShot++;

        if (shootCooldown <= 0){
            if (attackTarget != null){
                if (!attackTarget.isAlive() || !isHostileTo(attackTarget.getFactionId())){
                    attackTarget = null;
                    return;
                }
                double dx = getX() - attackTarget.getX();
                double dy = getY() - attackTarget.getY();
                if (Math.sqrt(dx * dx + dy * dy) > getAttackRange()) { //checking if target is out of range
                    attackTarget = null;
                    return;
                }
                System.out.println("Shooting from town : " + this.getInfo());
                game.spawnArrow(this, attackTarget, getFactionId()); // TODO change factionId if more factions introduced
                resetAttackCoolDown();
                resetTimeSinceLastShot();
            }
            else{
                Unit target = game.getNearestUnit(getX(), getY(),null, 2, (unit) -> this.isHostileTo(unit.getFactionId()));
                if (target == null){
                    resetAttackCoolDown();
                    return;
                }
                setTarget(target);
            }
        }
        if (timeSinceLastShot > ticksToFallAsleep){
            shootCooldown=0;
            resetTimeSinceLastShot();
            setAttackReady(false);
        }
    }

    @Override
    public void setAttackReady(boolean state) {
        this.attackReady = state;
    }

    @Override
    public int getAttackCooldown() {
        return shootCooldown;
    }

    @Override
    public void resetAttackCoolDown() {
        shootCooldown = TOWN_ATTACK_COOLDOWN;
    }

    @Override
    public boolean isAttackReady() {
        return attackReady;
    }

    @Override
    public void tick(Game game) {

    }

    @Override
    public double getX() {
        return getPoint().getX();
    }

    @Override
    public double getY() {
        return getPoint().getY();
    }

    @Override
    public void resetTimeSinceLastShot() {
        timeSinceLastShot=0;
    }

    @Override
    public boolean hasSufficientTimePassedSinceLastShot() {
        return timeSinceLastShot > 100;
    }

    public boolean isHostileTo(int factionId){
        return factionId != 1;
    }

    public void setShootCooldown(int newCooldown){
        shootCooldown=0;
    }

    public void setCity(City city){
        this.city = city;
    }

    public void removeCity(){
        city = null;
    }

    public City getCity(){
        return city;
    }

    public boolean hasCity(){
        return city != null;
    }
    
    public void addTownArea(TownArea townArea){
        townAreaPoints.add(townArea);
    }

    public Set<TownArea> getTownAreaPoints() {
        return townAreaPoints;
    }

    public void setTownAreaPoints(Set<TownArea> townAreaPoints) {
        this.townAreaPoints = townAreaPoints;
    }

    public void removeTownArea(TownArea townArea){
        if (townAreaPoints.contains(townArea)){
            townAreaPoints.remove(townArea);
        }
    }

    @Override
    public String getInfo() {
        return "{Town: " + getId() +
                ", health: " + getHealth() + "/" + DEFAULT_BUILDING_HEALTH +
                ", isAlive=" + isAlive() +
                ", food = " + String.format("%.2f", getFood()) +
                ", people=" + getPeople() +
                ", gold=" + String.format("%.2f", getGold()) +
                ", amount of farms controlled: " + getFarms().size() +
                "/" + (getControlledLand().size()-1) + // excluding center point
                ", wealth=" + String.format("%.2f", getWealth()) +
                ", villages=" + getVillages().size() +
                ", currentTaxIncome=" + String.format("%.2f", getLastIterationFoodTaxIncome()) +
                (getCity()!=null ? " (" + String.format("%.2f",(TOWN_TAXATION_RATE*getLastIterationFoodTaxIncome())) + " taxed)" : "") +
                (this.hasCity() ? ", City: " + getCity().getPoint().getPositionString() : ", Has city: None")
                + ".} ";
    }
}