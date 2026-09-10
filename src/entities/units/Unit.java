package entities.units;

import buildings.UnitOwner;
import empirebuilder.Game;
import empirebuilder.Point;
import entities.Entity;
import entities.MovingEntity;
import entities.units.AI.Node;
import entities.units.AI.UnitOrder;
import pathfinding.Path;

public abstract class Unit extends MovingEntity {

    // early version of ai control. to be replaced
    public enum CombatStyle {
        MELEE,
        RANGED,
        SIEGE,
        MAGIC,
        NONE
    }

    double speed;
    double damage;
    CombatStyle combatStyle;
    int attackCooldown;
    int attackCooldownBase;

    int searchCooldown;

    Entity combatTarget;
    Point pointTarget;
    Point idleTarget = null;

    int loot;
    UnitOwner unitOwner = null;

    UnitOrder unitorder;
    private Node logicMotor;

    // priorityTier decides what unit can push what friendly other units out of the way
    // melee pushes ranged away, cavalry pushes melee away etc
    private int priorityTier = 0;
    private Path pathB = null;
    private int waypointIndexB = 0;

    //int factionId; // 1 = good, 2 = evil, 3 = neutral or other

    // --- Movement system ---
    private double lastVelX = 0;
    private double lastVelY = 0;
    private boolean facingLeft = false;

    private static final double DEFAULT_MELEE_BUFFER = 0.2;
    static final double DEFAULT_UNIT_SIZE = 0.2;

    public static final int IDLE_SEARCH_COOLDOWN = 100;
    // how far outwards units searches for target units
    public static final int COMBAT_SEARCH_CELLS = 4;
    // how far outwards units searches for target buildings
    public static final int BUILDING_SEARCH_CELLS = 3;

    // Position-based stuck detector — sampled every STUCK_SAMPLE_INTERVAL ticks
    private double stuckSampleX   = Double.MIN_VALUE;
    private double stuckSampleY   = Double.MIN_VALUE;
    private int    stuckSampleTick = 0;

    public Unit(double x, double y, double speed, double health, double damage, int factionId, double size, int attackCooldownBase) {
        super(x, y, health, size, factionId, speed);
        this.logicMotor = createAISystem();
        this.speed = speed;
        this.damage = damage;
        this.attackCooldownBase = attackCooldownBase;
        attackCooldown = 0;
        loot = 0;
        setUnitOrder(UnitOrder.NONE);
    }

    public boolean attemptMeleeAttack(Entity target, Game game){
        if (isTargetInMeleeRange(this, target) && attackCooldown <= 0){
            game.performMeleeAttack(this, target);
            if (!target.isAlive()){
                combatTarget = null;
                searchCooldown = 0;
            }
            resetAttackCooldown();
            return true;
        }
        return false;
    }

    public void resetAttackCooldown(){
        attackCooldown=attackCooldownBase;
    }

    public void tick(Game game){
        searchCooldown--;
        attackCooldown--;
        logicMotor.tick(this, game);
    }

    public abstract Node createAISystem();

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

    public void clearLoot(){
        this.loot = 0;
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

    public int getSearchForUnitsDistance(){
        return COMBAT_SEARCH_CELLS;
    }

    public int getSearchForBuildingsDistance(){
        return BUILDING_SEARCH_CELLS;
    }

    public boolean isSeachCooldownReady(){
        return searchCooldown <= 0;
    }

    public void resetSearchCooldown(){
        searchCooldown = IDLE_SEARCH_COOLDOWN;
    }

    public UnitOrder getUnitOrder() {
        return unitorder;
    }

    public void setUnitOrder(UnitOrder unitorder) {
        this.unitorder = unitorder;
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

    @Override
    public double getSpeed(){
        if (getUnitOrder().equals(UnitOrder.IDLING)){
            return super.getSpeed() * 0.1;
        }
        return speed;
    }

    public boolean isAttackReady(){
        return attackCooldown <= 0;
    }

    public void clearPointTarget()      { this.pointTarget = null; }
    public void setPointTarget(Point p) { this.pointTarget = p; }

    public double getDamage() { return damage; }

    public Entity getCombatTarget()         { return combatTarget; }
    public void  setCombatTarget(Entity target) { this.combatTarget = target; }
    public Point  getPointTarget()          { return pointTarget; }
    public Point  getIdleTarget()           { return idleTarget; }
    public void setIdleTarget(Point p) { this.idleTarget = p; }

    public double getLastVelX()             { return lastVelX; }
    public double getLastVelY()             { return lastVelY; }
    public void   setLastVelX(double v)     {
        lastVelX = v;
        if (v < -1e-6) {
            facingLeft = true;
        } else if (v > 1e-6) {
            facingLeft = false;
        }
    }
    public void   setLastVelY(double v)     { lastVelY = v; }
    public boolean isFacingLeft()           { return facingLeft; }

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
