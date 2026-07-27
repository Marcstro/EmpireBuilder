package entities.units;

import buildings.Building;
import empirebuilder.Game;
import empirebuilder.Point;
import entities.Entity;
import entities.MovingEntity;
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

    public static final int IDLE_SEARCH_COOLDOWN = 15;
    // how far outwards units searches for target units
    public static final int COMBAT_SEARCH_CELLS = 1;
    // how far outwards units searches for target buildings
    public static final int BUILDING_SEARCH_CELLS = 2;

    // Position-based stuck detector — sampled every STUCK_SAMPLE_INTERVAL ticks
    private double stuckSampleX   = Double.MIN_VALUE;
    private double stuckSampleY   = Double.MIN_VALUE;
    private int    stuckSampleTick = 0;

    public Unit(double x, double y, double speed, double health, double damage, int factionId, double size) {
        super(x, y, health, size, factionId, speed);
        this.speed = speed;
        this.damage = damage;
        attackCooldown = 0;
    }

    @Override
    public void tick(Game game) {
        attackCooldown--;
        if (combatTarget != null && !combatTarget.isAlive()) {
            combatTarget = null;
            searchCooldown = 0;
        }

        if (combatTarget != null) {
            if (combatTarget == this){
                System.out.println("Unit targeted itself. it has hurt itself in confusion. OBVIOUS ERROR");
            }
            if (this.getCombatStyle() == CombatStyle.MELEE) {
                if (isTargetInMeleeRange(this, combatTarget)) {
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
            else if (getCombatStyle() == CombatStyle.RANGED){
                if (isTargetInRangedAttack(this, combatTarget)){
                    if (attackCooldown <= 0){
                        game.unitShootArrow(this, (Unit)combatTarget, getFactionId());
                        attackCooldown = 12;
                    }
                    clearPathB();
                }
            }
        }

        if (searchCooldown <= 0) {
            searchCooldown = IDLE_SEARCH_COOLDOWN;

            Unit unitTarget = game.getNearestUnit(getX(), getY(), this, COMBAT_SEARCH_CELLS, (neighbor) ->
                    neighbor.isAlive() && this.isHostileTo(neighbor.getFactionId())
            );

            if (unitTarget != null) {
                combatTarget = unitTarget;
            } else {
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
            pointTarget = game.getPoint(150, 100);
        }

        // reached current position target? Reset target
        if (combatTarget == null && pointTarget != null) {
            if (game.isUnitInDestinedMapCell(this)){
                pointTarget = null;
            }
        }
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
    }

    public void clearPointTarget()      { this.pointTarget = null; }
    public void setPointTarget(Point p) { this.pointTarget = p; }

    public double getDamage() { return damage; }

    public Entity getCombatTarget()         { return combatTarget; }
    public Point  getPointTarget()          { return pointTarget; }

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
