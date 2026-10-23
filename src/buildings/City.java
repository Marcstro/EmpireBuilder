package buildings;

import LandTypes.LandType;
import buildings.ai.profiles.SettlementAI;
import empirebuilder.Game;
import empirebuilder.Point;
import entities.units.Unit;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class City extends VillageOwningBuilding implements AttackCapableBuilding, UnitOwner, DefensiveTroopBuilding {

    Set<CityArea> cityAreaPoints;
    LinkedList<Town> towns;
    boolean isAttackReady;
    int timeSinceLastShot;

    static final int INITIAL_FOOD_NEEDED_TO_GROW = 50;
    final int CITY_ATTACK_RANGE = 10;
    final int CITY_ATTACK_DAMAGE = 12;
    final int CITY_ARROW_DISTANCE = 14;
    final int CITY_ATTACK_COOLDOWN = 50;

    private final UnitManagerComponent unitManager;
    private final DefensiveTroopComponent defensiveTroopComponent;
    private final AttackCapableBuildingComponent attackCapableBuildingComponent =
            new AttackCapableBuildingComponent(this, CITY_ATTACK_DAMAGE, CITY_ATTACK_RANGE, CITY_ARROW_DISTANCE, CITY_ATTACK_COOLDOWN);

    public City(Point point) {
        this(point, null);
    }

    public City(Town previousTown) {
        this(previousTown.getPoint(), previousTown);
    }

    private City(Point point, Town previousTown) {
        super(point, INITIAL_FOOD_NEEDED_TO_GROW, LandType.getBaseColor(LandType.CITY), DEFAULT_BUILDING_HEALTH);
        unitManager = previousTown != null
                ? previousTown.getUnitManagerComponent() : new UnitManagerComponent(this);
        defensiveTroopComponent = previousTown != null
                ? previousTown.getDefensiveTroopComponent() : new DefensiveTroopComponent(this);
        unitManager.setOwnerBuilding(this);
        defensiveTroopComponent.setOwnerBuilding(this);
        towns = new LinkedList<>();
        cityAreaPoints = new HashSet();
        isAttackReady=false;
        timeSinceLastShot=0;
        initBuildingAI();
        if (previousTown != null) {
            inheritIncomeHistory(previousTown);
            getBuildingAI().ticksSinceLastEvaluation = getBuildingAI().evaluationInterval;
        }
    }

    @Override
    public void tick(Game game) {
        defensiveTroopComponent.tick();
        tickBuildingAI(game);
        tickSubBuildingsWithMaintenance(game);
    }

    @Override
    public Point getInstructions(Unit unit, Game game) {
        Point p = defensiveTroopComponent.getDefensiveInstructions(unit, game);
        if (p != null){
            return p;
        }
        if (game.calculateDistance(game.getPoint(unit.getX(), unit.getY()), getPoint()) > 5){
            unit.setIdleTarget(null);
            return getPoint();
        }
        return null;
    }

    @Override
    public DefensiveTroopComponent getDefensiveTroopComponent() {
        return defensiveTroopComponent;
    }

    public void addCityArea(CityArea cityArea){
        cityAreaPoints.add(cityArea);
    }

    public void removeCityArea(CityArea cityArea){
        if(cityAreaPoints.contains(cityArea)){
            cityAreaPoints.remove(cityArea);
        }
    }

    public Set<CityArea> getCityAreaPoints(){
        return cityAreaPoints;
    }

    @Override
    public String getImagePath() {
        return "/resources/images/CityImage4.png";
    }

    public String getImageName(){
        return "city";
    }

    @Override
    public void processTotalFoodIncome() {
        double foodIncome = collectTotalFoodAcquired();
        addToCurrentFoodTaxIncome(foodIncome);
        food += foodIncome;
    }

    public void addTown(Town town){
        towns.add(town);
    }

    public void releaseTown(Town town){
        towns.remove(town);
    }

    public LinkedList<Town> getTowns() {
        return towns;
    }

    public void setTowns(LinkedList<Town> towns) {
        this.towns = towns;
    }

    @Override
    public UnitManagerComponent getUnitManagerComponent() {
        return unitManager;
    }

    @Override
    public AttackCapableBuildingComponent getAttackCapableBuildingComponent() {
        return attackCapableBuildingComponent;
    }

    @Override
    public String getInfo() {
        double totalMaintenanceCost = calculateTotalSubBuildingMaintenance();
        String activeThreats = getBuildingAI().getProfile() instanceof SettlementAI ai
                ? ai.getAssessment().getActiveFacts().toString() : "unavailable";
        return "{City: " + getId() +
                ", health: " + getHealth() + "/" + DEFAULT_BUILDING_HEALTH +
                ", isAlive=" + isAlive() +
                ", food=" + String.format("%.2f", getFood()) +
                ", people=" + getPeople() +
                ", gold=" + String.format("%.2f", getGold()) +
                ", wealth=" + String.format("%.2f", getWealth()) +
                ", subBuilding maintenance cost=" + String.format("%.2f", totalMaintenanceCost) +
                ", towns=" + getTowns().size() +
                ", villages=" + getVillages().size() +
                ", indirect villages=" + (getTowns().stream().mapToInt(town -> town.getVillages().size()).sum()) +
                ", currentTaxIncome=" + String.format("%.2f", getLastIterationFoodTaxIncome()) +
                ", active threats: " + activeThreats +
                ", " + unitManager.getInfo() +
                ".} ";
    }
}
