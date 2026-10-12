package buildings;

import buildings.ai.GoodSideThreatAssessment;
import buildings.subBuildings.UnitProducingSubBuilding;
import empirebuilder.Game;
import empirebuilder.Point;
import entities.units.FarmerMilitia;
import entities.units.Unit;

import java.util.ArrayList;
import java.util.List;

public class UnitManagerComponent {

    private final List<Unit> units = new ArrayList<>();
    private final Building ownerBuilding;
    List<Building> knownHostileBuildings = new ArrayList<>();

    private int enemySightingMemory = 0;
    private static final int ENEMY_SIGHTING_MEMORY_EVALUATIONS = 20;

    private int recentUnitLosses = 0;

    private static final int MAX_MILITIA_PER_WAVE = 8;

    private static final int MILITIA_COOLDOWN_EVALUATIONS = 6;

    private int militiaCooldown = 0;

    public UnitManagerComponent(Building ownerBuilding) {
        this.ownerBuilding = ownerBuilding;
    }

    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public void removeUnit(Unit unit) {
        units.remove(unit);
    }

    public List<Unit> getUnits() {
        return units;
    }

    public Building getOwnerBuilding() {
        return ownerBuilding;
    }

    // TODO adjust according to threatassessment level
    // TODO implement a minimum: a farm with 10 people shouldnt be able to spawn any units at all
        public void spawnMilitia(Game game, GoodSideThreatAssessment assessment){
        if (!(ownerBuilding instanceof DefensiveTroopBuilding def)){
            return;
        }
        if (militiaCooldown > 0){
            return;
        }

        DefensiveTroopComponent defensiveComponent = def.getDefensiveTroopComponent();
        int amountOfMilitia = Math.min(defensiveComponent.getMaxAvailableMilitia(), MAX_MILITIA_PER_WAVE);
        if (amountOfMilitia <= 0){
            return;
        }

        List<Point> points = game.findGroupSpawnPoints(ownerBuilding.getPoint().getX(),
                ownerBuilding.getPoint().getY(), amountOfMilitia);
        if (points.isEmpty()){
            return;
        }

        for (Point point : points){
            FarmerMilitia militia = new FarmerMilitia(point.getX(), point.getY());
            militia.setUnitOwner(def);
            game.spawnUnitAt(militia, point);
            addUnit(militia);
        }

        // actual amount of militia spawned, regardless of requested amount
        defensiveComponent.reserveMilitiaSlots(points.size());
        militiaCooldown = MILITIA_COOLDOWN_EVALUATIONS;
    }

    public void reportHostileBuilding(Building building){
        if (!knownHostileBuildings.contains(building)){
            knownHostileBuildings.add(building);
        }
    }

    public List<Building> getKnownHostileBuildings() {
        return knownHostileBuildings;
    }

    // subbuilding sends units to this function so its added to hte main owner
    public void receiveUnit(Unit unit, Game game) {
        List<Point> spawnPoints = game.findGroupSpawnPoints(ownerBuilding.getPoint().getX(),
                ownerBuilding.getPoint().getY(), 1);
        if (spawnPoints.isEmpty()) {
            return;
            // TODO add unit to "unenlisted list"
        }
        Point spawnAt = spawnPoints.getFirst();
        unit.setX(spawnAt.getX());
        unit.setY(spawnAt.getY());
        unit.setUnitOwner((UnitOwner) ownerBuilding);
        game.spawnUnitAt(unit, spawnAt);
        addUnit(unit);
    }

    public void reportHostileUnit(){
        enemySightingMemory = ENEMY_SIGHTING_MEMORY_EVALUATIONS;
    }

    public void recordUnitLoss(Unit unit){
        recentUnitLosses++;
        if (unit instanceof FarmerMilitia && ownerBuilding instanceof DefensiveTroopBuilding def){
            def.getDefensiveTroopComponent().notifyMilitiaDeath();
        }
        UnitProducingSubBuilding subBuildingOwner = unit.getSubBuildingOwner();
        if (subBuildingOwner != null) {
            subBuildingOwner.releaseSpot(unit);
        }
    }

    public void decayThreatMemory(){
        if (enemySightingMemory > 0){
            enemySightingMemory--;
        }
        if (recentUnitLosses > 0){
            recentUnitLosses--;
        }
        if (militiaCooldown > 0){
            militiaCooldown--;
        }
    }

    public boolean hasRecentlySightedEnemy(){
        return enemySightingMemory > 0;
    }

    public int getRecentUnitLosses(){
        return recentUnitLosses;
    }

}
