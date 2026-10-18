package buildings;

import buildings.ai.GoodSideThreatAssessment;
import buildings.ai.profiles.SettlementAI;
import buildings.subBuildings.UnitProducingSubBuilding;
import empirebuilder.Game;
import empirebuilder.Point;
import entities.units.AI.UnitOrder;
import entities.units.FarmerMilitia;
import entities.units.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class UnitManagerComponent {

    private final List<Unit> units = new ArrayList<>();
    final List<Unit> garrison = new ArrayList<>();
    private Building ownerBuilding;
    List<Building> knownHostileBuildings = new ArrayList<>();

    private int enemySightingMemory = 0;
    private static final int ENEMY_SIGHTING_MEMORY_EVALUATIONS = 20;
    private static final int SPAWNED_AT_ONCE = 3;

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

    public int getGarrisonCount() {
        return garrison.size();
    }

    public String getInfo() {
        String garrisonInfo = garrison.stream()
                .collect(Collectors.groupingBy(unit -> unit.getClass().getSimpleName(),
                        TreeMap::new, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
        return "units=" + (units.size() + garrison.size())
                + " (deployed=" + units.size() + "), garrison={" + garrisonInfo + "}";
    }

    public Building getOwnerBuilding() {
        return ownerBuilding;
    }

    public void setOwnerBuilding(Building ownerBuilding) {
        if (!(ownerBuilding instanceof UnitOwner owner)) {
            throw new IllegalArgumentException("Unit manager owner must implement UnitOwner");
        }
        this.ownerBuilding = ownerBuilding;
        for (Unit unit : units) {
            unit.setUnitOwner(owner);
        }
        for (Unit unit : garrison) {
            unit.setUnitOwner(owner);
        }
    }

    public void spawnUnitsFromGarrison(Game game) {
        if (garrison.isEmpty()) {
            return;
        }
        int unitsToSpawn = Math.min(SPAWNED_AT_ONCE, garrison.size());
        List<Point> spawnPoints = game.findGroupSpawnPoints(ownerBuilding.getPoint().getX(),
                ownerBuilding.getPoint().getY(), unitsToSpawn);
        if (spawnPoints.isEmpty()) {
            return;
        }
        for (Point spawnAt : spawnPoints) {
            Unit unit = garrison.get(0);
            unit.setX(spawnAt.getX());
            unit.setY(spawnAt.getY());
            units.add(unit);
            game.spawnUnitAt(unit, spawnAt);
            garrison.remove(unit);
        }
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
        unit.setUnitOwner((UnitOwner) ownerBuilding);
        boolean supportsGarrison = ownerBuilding instanceof Town || ownerBuilding instanceof City;
        boolean safe = supportsGarrison
                && ownerBuilding.getBuildingAI().getProfile() instanceof SettlementAI ai
                && !ai.getAssessment().shouldDeployGarrisonedUnits();
        if (safe && units.stream().anyMatch(existing ->
                existing.isAlive() && existing.getClass() == unit.getClass())) {
            garrisonUnit(unit);
            return;
        }
        List<Point> spawnPoints = game.findGroupSpawnPoints(ownerBuilding.getPoint().getX(),
                ownerBuilding.getPoint().getY(), 1);
        if (spawnPoints.isEmpty()) {
            if (supportsGarrison) {
                garrisonUnit(unit);
            }
            return;
        }
        Point spawnAt = spawnPoints.getFirst();
        unit.setX(spawnAt.getX());
        unit.setY(spawnAt.getY());
        game.spawnUnitAt(unit, spawnAt);
        addUnit(unit);
    }

    private void garrisonUnit(Unit unit) {
        unit.clearAllTargets();
        unit.clearPathB();
        unit.setUnitOrder(UnitOrder.AWAITING_INSTUCTIONS);
        garrison.add(unit);
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
