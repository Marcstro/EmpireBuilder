package buildings;

import LandTypes.LandType;
import empirebuilder.Game;
import empirebuilder.Point;
import entities.units.Unit;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


public class Town extends VillageOwningBuilding implements AttackCapableBuilding, UnitOwner, DefensiveTroopBuilding {

    Set<TownArea> townAreaPoints;
    
    static final int INITIAL_FOOD_NEEDED_TO_GROW = 50;
    final double TOWN_TAXATION_RATE = 0.6;

    private final int TOWN_ATTACK_COOLDOWN = 20;
    private final int TOWN_ATTACK_DAMAGE = 8;
    private final double TOWN_ATTACK_RANGE = 15;
    private final int TOWN_ARROW_DISTANCE = 35;

    private final UnitManagerComponent unitManager = new UnitManagerComponent(this);
    private final DefensiveTroopComponent defensiveTroopComponent = new DefensiveTroopComponent();
    private final AttackCapableBuildingComponent attackCapableBuildingComponent =
            new AttackCapableBuildingComponent(this, TOWN_ATTACK_DAMAGE, TOWN_ATTACK_RANGE, TOWN_ARROW_DISTANCE, TOWN_ATTACK_COOLDOWN);

    public Town(Point point) {
        super(point, INITIAL_FOOD_NEEDED_TO_GROW, LandType.getBaseColor(LandType.TOWN), DEFAULT_BUILDING_HEALTH);
        farms = new LinkedList();
        food = 0;
        townAreaPoints = new HashSet();
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
        if (hasCity()){
            getCity().processTaxation(((foodIncome*TOWN_TAXATION_RATE)));
            addToCurrentFoodTaxIncome((foodIncome*(1-TOWN_TAXATION_RATE)));
            food += (foodIncome*(1-TOWN_TAXATION_RATE));
        }
        else {
            addToCurrentFoodTaxIncome(foodIncome);
            food += foodIncome;
        }
    }

    @Override
    public AttackCapableBuildingComponent getAttackCapableBuildingComponent() {
        return attackCapableBuildingComponent;
    }

    @Override
    public void tick(Game game) {
        getUnitManagerComponent().handleDefenses(game);
    }

    public void setCity(City city){
        setOwner(city);
    }

    public void removeCity(){
        setOwner(null);
    }

    public City getCity(){
        return (City) getOwner();
    }

    public boolean hasCity(){
        return hasOwner();
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
    public UnitManagerComponent getUnitManagerComponent() {
        return unitManager;
    }

    @Override
    public DefensiveTroopComponent getDefensiveTroopComponent() {
        return defensiveTroopComponent;
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
                (this.hasCity() ? ", City: " + getCity().getPoint().getPositionString() : ", Has city: None") +
                ", is aware of danger: " + getDefensiveTroopComponent().hasDanger() +
                 ".} ";
    }
}