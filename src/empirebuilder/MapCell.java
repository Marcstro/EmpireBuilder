package empirebuilder;

import buildings.*;
import entities.effects.Effect;
import entities.units.Unit;

import java.util.ArrayList;
import java.util.List;


public class MapCell {

    private final List<Unit> units = new ArrayList<>();
    private final List<Effect> effects = new ArrayList<>();
    private final List<AttackCapableBuilding> attackCapableBuildings = new ArrayList<>();
    private List<FarmOwningBuilding> largeBuildingsList = new ArrayList<>();

    private final int cellX;
    private final int cellY;

    private int dangerLevel = 0;
    private int accumulatedWealth = 0;

    private boolean isSearchOnCoolDown = false;
    boolean plunderCooldown = true;
    boolean isBeingRaided = false;

    public MapCell(int cellX, int cellY) {
        this.cellX = cellX;
        this.cellY = cellY;
    }

    public void addUnit(Unit unit) {
        unit.setMapCellListIndex(this.units.size());
        this.units.add(unit);
    }

    public void removeUnit(Unit unit) {
        final int indexToRemove = unit.getMapCellListIndex();
        final int lastIndex = this.units.size() - 1;

        if (indexToRemove < lastIndex) {
            Unit lastUnit = this.units.get(lastIndex);

            this.units.set(indexToRemove, lastUnit);

            lastUnit.setMapCellListIndex(indexToRemove);
        }

        this.units.remove(lastIndex);
        unit.setMapCellListIndex(-1);
    }

    public void addEffect(Effect effect) {
        effect.setMapCellListIndex(this.effects.size());
        this.effects.add(effect);
    }

    public void removeEffect(Effect effect) {
        final int indexToRemove = effect.getMapCellListIndex();
        final int lastIndex = this.effects.size() - 1;

        if (indexToRemove < lastIndex) {
            Effect lastEffect = this.effects.get(lastIndex);
            this.effects.set(indexToRemove, lastEffect);
            lastEffect.setMapCellListIndex(indexToRemove);
        }

        this.effects.remove(lastIndex);
        effect.setMapCellListIndex(-1);
    }

    public List<AttackCapableBuilding> getAttackCapableBuildings() {
        return attackCapableBuildings;
    }

    public void addAttackCapableBuildings(AttackCapableBuilding attackCapableBuilding){
        attackCapableBuildings.add(attackCapableBuilding);
    }

    public void removeAttackCapableBuilding(AttackCapableBuilding attackCapableBuilding){
        attackCapableBuildings.remove(attackCapableBuilding);
    }

    public List<FarmOwningBuilding> getLargeBuildingsList(Game game){
        return largeBuildingsList;
    }

    public void addBuilding(FarmOwningBuilding building){
        if (!largeBuildingsList.contains(building)){
            largeBuildingsList.add(building);
        }
    }

    public void removeBuilding(FarmOwningBuilding building){
        if (largeBuildingsList.contains(building)){
            largeBuildingsList.remove(building);
        }
    }

    public void attemptToPlunder(Game game, int currentTick, Unit unit){
        if (plunderCooldown){
            game.plunderMapCell(this, unit);
            plunderCooldown = false;
        }
        // TODO fix this ASAP once Building.class has a getOwner() function
        if (!isBeingRaided){
            System.out.println("MapCell is being raided, notifying all defensive buildings in the cell");
            isBeingRaided = true;
            for(FarmOwningBuilding building : largeBuildingsList){
                System.out.println(building.getPoint().getInfo() + "is trying to inform others it is being raided");
                if (building instanceof DefensiveTroopBuilding def){
                    def.getDefensiveTroopComponent().addToDangerList(this);
                    System.out.println(((Building)def).getInfo() + " has been notified of the raid");
                    if (def instanceof Town town && town.hasCity() && town.getCity() instanceof DefensiveTroopBuilding def2){
                        def2.getDefensiveTroopComponent().addToDangerList(this);
                        System.out.println(((Building)def2).getInfo() + " has been notified of the raid");

                    }
                }
                if (building instanceof Village village
                        && village.hasOwner()
                        && village.getOwner() instanceof DefensiveTroopBuilding def2){
                    def2.getDefensiveTroopComponent().addToDangerList(this);
                    System.out.println(((Building)def2).getInfo() + " has been notified of the raid");

                    if (def2 instanceof Town town && town.hasCity() && town.getCity() instanceof DefensiveTroopBuilding def3){
                        def3.getDefensiveTroopComponent().addToDangerList(this);
                        System.out.println(((Building)def3).getInfo() + " has been notified of the raid");

                    }
                }
            }
        }
    }

    public void localDangerIsOver(){
        isBeingRaided =false;
    }

    public void resetCooldowns(){
        //isSearchOnCoolDown = false;
        plunderCooldown = true;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public int getCellX() {
        return cellX;
    }

    public int getCellY() {
        return cellY;
    }

    public int getDangerLevel() {
        return dangerLevel;
    }

    public void setDangerLevel(int dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

    public int getAccumulatedWealth() {
        return accumulatedWealth;
    }

    public void setAccumulatedWealth(int accumulatedWealth) {
        this.accumulatedWealth = accumulatedWealth;
    }
}
