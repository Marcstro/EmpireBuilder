package buildings;

import LandTypes.LandType;
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
    final int CITY_ATTACK_RANGE = 25;
    final int CITY_ATTACK_DAMAGE = 12;
    final int CITY_ARROW_DISTANCE = 35;
    final int CITY_ATTACK_COOLDOWN = 20;

    private final UnitManagerComponent unitManager = new UnitManagerComponent(this);
    private final DefensiveTroopComponent defensiveTroopComponent = new DefensiveTroopComponent();
    private final AttackCapableBuildingComponent attackCapableBuildingComponent =
            new AttackCapableBuildingComponent(this, CITY_ATTACK_DAMAGE, CITY_ATTACK_RANGE, CITY_ARROW_DISTANCE, CITY_ATTACK_COOLDOWN);

    public City(Point point) {
        super(point, INITIAL_FOOD_NEEDED_TO_GROW, LandType.getBaseColor(LandType.CITY), DEFAULT_BUILDING_HEALTH);
        towns = new LinkedList<>();
        cityAreaPoints = new HashSet();
        isAttackReady=false;
        timeSinceLastShot=0;
    }

    @Override
    public void tick(Game game) {
        //unitManager.getUnits().removeIf(u -> !u.isAlive());
        getUnitManagerComponent().handleDefenses(game);
    }

    @Override
    public Point getInstructions(Unit unit, Game game) {
        Point p = defensiveTroopComponent.getDefensiveInstructions(unit, game);
        if (p != null){
            return p;
        }
        if (game.calculateDistance(game.getPoint(unit.getX(), unit.getY()), getPoint()) > 5){
            unit.setCurrentFocus(entities.units.AI.Focus.RETURNING_TO_BASE);
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
    void processTaxation(double foodIncome) {
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
        return "{City: " + getId() +
                ", health: " + getHealth() + "/" + DEFAULT_BUILDING_HEALTH +
                ", isAlive=" + isAlive() +
                ", food=" + String.format("%.2f", getFood()) +
                ", people=" + getPeople() +
                ", gold=" + String.format("%.2f", getGold()) +
                ", wealth=" + String.format("%.2f", getWealth()) +
                ", towns=" + getTowns().size() +
                ", villages=" + getVillages().size() +
                ", indirect villages=" + (getTowns().stream().mapToInt(town -> town.getVillages().size()).sum()) +
                ", currentTaxIncome=" + String.format("%.2f", getLastIterationFoodTaxIncome()) +
                ".} ";
    }
}
