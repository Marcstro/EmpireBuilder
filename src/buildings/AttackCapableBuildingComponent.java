package buildings;

import empirebuilder.Game;
import entities.Entity;
import entities.effects.Arrow;
import entities.units.Unit;

public class AttackCapableBuildingComponent {

    private int damage;
    private double attackRange;
    private int baseAttackCooldown;
    private int attackCooldown;
    private Entity attackTarget;
    private boolean isAttackReady;
    private int timeSinceLastShot;
    private final int arrowTravelTime;
    private final int ticksToFallAsleep = 60;

    private final Building building;

    public AttackCapableBuildingComponent(Building building, int damage, double attackRange, int travelTime, int attackCooldown) {
        this.building = building;
        this.damage = damage;
        this.attackRange = attackRange;
        this.arrowTravelTime = travelTime;
        this.baseAttackCooldown = attackCooldown;
        this.attackCooldown = attackCooldown;
        isAttackReady = false;
    }

    public double getAttackRange() {
        return attackRange;
    }

    public void setAttackReady(Boolean state) {
        this.isAttackReady = state;
    }

    public void setTarget(Entity attackTarget){
        this.attackTarget = attackTarget;
    }

    public boolean isAttackReady() {
        return isAttackReady;
    }

    public void resetTimeSinceLastShot() {
        this.timeSinceLastShot = 0;
    }

    public void resetAttackCooldown() {
        this.attackCooldown = baseAttackCooldown;
    }

    public void tickAttack(Game game){
        if (!isAttackReady()){
            return;
        }

        attackCooldown--;
        timeSinceLastShot++;

        if (attackCooldown <= 0){
            if (attackTarget != null){
                if (!attackTarget.isAlive()) {
                    resetTarget();
                    return;
                }
                if (game.calculateDistance(building.getX(), building.getY(), attackTarget.getX(), attackTarget.getY()) > attackRange){
                    attackTarget = null;
                }
                else {
                    Arrow arrow = new Arrow(building.getX(), building.getY(), attackTarget.getX(), attackTarget.getY(),
                            building.getFactionId(), damage, arrowTravelTime);
                    game.spawnArrow(arrow, building, attackTarget, building.getFactionId());
                    resetTimeSinceLastShot();
                    resetAttackCooldown();
                }
            }
            else {
                Unit target = game.getNearestUnitSearchOutwards(building.getX(), building.getY(), null,
                        game.rangeToMapCellDistance(attackRange), (unit) -> building.isHostileTo(unit.getFactionId()));
                if (target != null){
                    attackTarget = target;
                }
                else {
                    return;
                }
            }
        }
        if (timeSinceLastShot > ticksToFallAsleep){
            resetAttackCooldown();
            resetTimeSinceLastShot();
            setAttackReady(false);
        }
    }

    public void resetTarget(){
        this.attackTarget = null;
    }

    public Building getBuilding() {
        return building;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setAttackRange(double attackRange) {
        this.attackRange = attackRange;
    }

    public void setAttackCooldown(int attackCooldown) {
        this.baseAttackCooldown = attackCooldown;
    }
}
