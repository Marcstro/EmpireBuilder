package entities.units;

import buildings.Building;
import buildings.DefensiveTroopBuilding;
import buildings.UnitOwner;
import empirebuilder.Game;
import empirebuilder.MapCell;
import empirebuilder.Point;
import entities.Entity;
import entities.MovingEntity;
import entities.units.AI.Focus;
import pathfinding.Path;

public abstract class Unit extends MovingEntity {

    // early version of ai control. to be replaced
    public enum CombatStyle {
        MELEE,
        RANGED,
        SIEGE,
        MAGIC
    }

    double speed;
    double damage;
    CombatStyle combatStyle;
    int attackCooldown;

    int searchCooldown;

    Entity combatTarget;
    Point pointTarget;
    Point idleTarget = null;

    int loot;
    UnitOwner unitOwner = null;

    Focus currentFocus;

    static final int idleWalkCooldown = 15;
    int idleCooldown = 0;

    // priorityTier decides what unit can push what friendly other units out of the way
    // melee pushes ranged away, cavalry pushes melee away etc
    private int priorityTier = 0;
    private Path pathB = null;
    private int waypointIndexB = 0;

    int factionId; // 1 = good, 2 = evil, 3 = neutral or other

    // --- Movement system ---
    private double lastVelX = 0;
    private double lastVelY = 0;

    private static final double DEFAULT_MELEE_BUFFER = 0.2;
    static final double DEFAULT_UNIT_SIZE = 0.2;

    public static final int IDLE_SEARCH_COOLDOWN = 70;
    // how far outwards units searches for target units
    public static final int COMBAT_SEARCH_CELLS = 4;
    // how far outwards units searches for target buildings
    public static final int BUILDING_SEARCH_CELLS = 3;

    // Position-based stuck detector — sampled every STUCK_SAMPLE_INTERVAL ticks
    private double stuckSampleX   = Double.MIN_VALUE;
    private double stuckSampleY   = Double.MIN_VALUE;
    private int    stuckSampleTick = 0;

    public Unit(double x, double y, double speed, double health, double damage, int factionId, double size) {
        super(x, y, health, size, factionId, speed);
        this.speed = speed;
        this.damage = damage;
        attackCooldown = 0;
        loot = 0;
        currentFocus=Focus.IDLING;
    }

    // TODO major redo here, create a proper ai system
    @Override
    public void tick(Game game) {
        attackCooldown--;
        if (combatTarget != null && !combatTarget.isAlive()) {
            combatTarget = null;
            searchCooldown = 0;
        }

        if (getLoot() > 1000 && combatTarget == null){
            pointTarget=getUnitOwner().getInstructions(this, game);
            setCurrentFocus(Focus.IS_RETURNING_WITH_LOOT);
        }
        else if (combatTarget != null) {
            if (combatTarget == this){
                System.out.println("Unit targeted itself. it has hurt itself in confusion. OBVIOUS ERROR");
            }
            if (this.getCombatStyle() == CombatStyle.MELEE) {
                if (isTargetInMeleeRange(this, combatTarget)) {
                    if (attackCooldown <= 0){
                        attackCooldown = 6;
                        game.performMeleeAttack(this, combatTarget);
                        if (!combatTarget.isAlive()){
                            combatTarget = null;
                            searchCooldown = 0;
                        }
                        else {
                            clearPathB();
                        }
                        return;
                    }
                }
            }
            else if (getCombatStyle() == CombatStyle.RANGED){
                if (isTargetInRangedAttack(this, combatTarget)){
                    if (attackCooldown <= 0){
                        game.unitShootArrow(this, combatTarget, getFactionId());
                        attackCooldown = 12;
                    }
                    clearPathB();
                }
            }
        }

        if (getCurrentFocus() != Focus.IS_RETURNING_WITH_LOOT && searchCooldown <= 0) {
            searchCooldown = IDLE_SEARCH_COOLDOWN;

            Unit unitTarget = game.getNearestUnit(getX(), getY(), this, COMBAT_SEARCH_CELLS, (neighbor) ->
                    neighbor.isAlive() && this.isHostileTo(neighbor.getFactionId())
            );

            if (unitTarget != null) {
                combatTarget = unitTarget;
            } else {
                // TODO only once get local buildings, then sort by distance and prio attackCapableBuildings
                Building buildingTarget = (Building)game.getNearestAttackCapableBuilding(this, BUILDING_SEARCH_CELLS);
                if (buildingTarget == null){
                    buildingTarget = game.getNearestLargeBuilding(this, BUILDING_SEARCH_CELLS);
                }
                if (buildingTarget != null) {
                    combatTarget = buildingTarget;
                }
            }
        } else {
            searchCooldown--;
        }

        // long term position goal. ADJUST THIS OBVIOSLY
        if (combatTarget == null && pointTarget == null) {
            if (getCurrentFocus() == Focus.IDLING && idleCooldown <= 0) {
                pointTarget = getUnitOwner().getInstructions(this, game);
                idleTarget = game.getIdleWalkTarget(getUnitOwner().getUnitManagerComponent().getOwnerBuilding().getPoint());
                idleCooldown = IDLE_SEARCH_COOLDOWN;
            } else if (getCurrentFocus() == Focus.IDLING) {
                idleCooldown--;
            }
            else {
                getUnitOwner().getInstructions(this, game);
            }
        }

        // reached current position target? Reset target
        if (combatTarget == null && pointTarget != null ) {
            if (game.isUnitInDestinedMapCell(this)){
                if (getCurrentFocus() == Focus.IS_RETURNING_WITH_LOOT){
                    getUnitOwner().getUnitManagerComponent().getOwnerBuilding().addGold((int)loot);
                    loot=0;
                    setCurrentFocus(Focus.IDLING);
                }

                else if (getCurrentFocus() == Focus.DEFENDING_EXTERNAL_AREA) {
                    if (getUnitOwner() instanceof DefensiveTroopBuilding def) {
                        MapCell mapCell = game.getMapCellByPoint(game.getPoint(getX(), getY()));
                        def.getDefensiveTroopComponent().dangerIsOver(mapCell);
                        mapCell.localDangerIsOver();
                        setCurrentFocus(Focus.RETURNING_TO_BASE);
                        pointTarget = def.getInstructions(this, game);
                    }
                }
                else{
                    pointTarget = null;

                }

            }
        }
        else if (combatTarget == null && pointTarget == null && getCurrentFocus() == Focus.IDLING) {
            if (idleTarget != null && game.isUnitInDestinedPoint(this, idleTarget)) {
                idleTarget = null;
            }
        }
    }

    public Focus getCurrentFocus() {
        return currentFocus;
    }

    public void setCurrentFocus(Focus currentFocus) {
        this.currentFocus = currentFocus;
    }

    public void setLongtermTarget(Point pointTarget) {
        this.pointTarget = pointTarget;
    }

    public void addLoot(int newLoot){
        loot +=  newLoot;
    }

    public int getLoot(){
        return loot;
    }

    public void removeLoot(int loot){
        this.loot -= loot;
    }

    public void setUnitOwner(UnitOwner unitOwner) {
        this.unitOwner = unitOwner;
    }

    public UnitOwner getUnitOwner() {
        return unitOwner;
    }

    public double getMeleeRange() {
        return this.getSize() + DEFAULT_MELEE_BUFFER;
    }

    public abstract double getAttackRange();

    public abstract CombatStyle getCombatStyle();

    public boolean isTargetInMeleeRange(Unit unit, Entity target) {
        double minDistance = unit.getMeleeRange() + target.getSize();
        double dx = unit.getX() - target.getX();
        double dy = unit.getY() - target.getY();
        return Math.sqrt(dx * dx + dy * dy) <= minDistance;
    }

    public boolean isTargetInRangedAttack(Unit unit, Entity target){
        double minDistance = unit.getAttackRange() + target.getSize();
        double dx = unit.getX() - target.getX();
        double dy = unit.getY() - target.getY();
        return Math.sqrt(dx * dx + dy * dy) <= minDistance;
    }

    public void resetTarget(){
        pointTarget = null;
        combatTarget = null;
        idleTarget = null;
    }

    public void clearPointTarget()      { this.pointTarget = null; }
    public void setPointTarget(Point p) { this.pointTarget = p; }

    public double getDamage() { return damage; }

    public Entity getCombatTarget()         { return combatTarget; }
    public Point  getPointTarget()          { return pointTarget; }
    public Point  getIdleTarget()           { return idleTarget; }
    public void setIdleTarget(Point p) { this.idleTarget = p; }

    public double getLastVelX()             { return lastVelX; }
    public double getLastVelY()             { return lastVelY; }
    public void   setLastVelX(double v)     { lastVelX = v; }
    public void   setLastVelY(double v)     { lastVelY = v; }

    public double getStuckSampleX()         { return stuckSampleX; }
    public double getStuckSampleY()         { return stuckSampleY; }
    public int    getStuckSampleTick()      { return stuckSampleTick; }
    public void   setStuckSampleX(double v) { stuckSampleX = v; }
    public void   setStuckSampleY(double v) { stuckSampleY = v; }
    public void   setStuckSampleTick(int t) { stuckSampleTick = t; }

    public int  getPriorityTier()           { return priorityTier; }
    public void setPriorityTier(int tier)   { this.priorityTier = tier; }

    public Path getPathB()                 { return pathB; }

    public void setPathB(Path path, int startIndex) {
        this.pathB = path;
        this.waypointIndexB = startIndex;
    }

    public int  getWaypointIndexB()         { return waypointIndexB; }
    public void setWaypointIndexB(int idx)  { this.waypointIndexB = idx; }

    public void clearPathB() {
        this.pathB = null;
        this.waypointIndexB = 0;
    }
}
