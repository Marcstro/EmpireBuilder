package buildings.subBuildings;

import buildings.AttackCapableBuilding;
import buildings.AttackCapableBuildingComponent;
import buildings.Building;
import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuildingRequirement;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Game;

public class ShootingTower extends SubBuilding implements AttackCapableBuilding {
    private static final double UPFRONT_COST = 10000;
    private static final double MAINTENANCE_COST_PER_TICK = 80;
    public static final java.util.List<SubBuildingRequirement> SUB_BUILDING_REQUIREMENTS = java.util.List.of();

    private final int TOWER_ATTACK_COOLDOWN = 45;
    private final int TOWER_ATTACK_DAMAGE = 10;
    private final double TOWER_ATTACK_RANGE = 10;
    private final int TOWER_ARROW_DISTANCE = 12;

    private final AttackCapableBuildingComponent attackCapableBuildingComponent;

    public ShootingTower(AreaBuildingSlot areaBuilding) {
        super(areaBuilding, SUB_BUILDING_REQUIREMENTS);
        if (!(areaBuilding instanceof Building areaBuildingEntity)) {
            throw new IllegalArgumentException("ShootingTower requires a Building area slot");
        }
        attackCapableBuildingComponent = new AttackCapableBuildingComponent(
                areaBuildingEntity,
                TOWER_ATTACK_DAMAGE,
                TOWER_ATTACK_RANGE,
                TOWER_ARROW_DISTANCE,
                TOWER_ATTACK_COOLDOWN);
        setValue(10000.0);
        setPermanent(true);
        setUpfrontCost(UPFRONT_COST);
    }

    @Override
    public void tick(Game game) {
        // shooting is done in game's List<attackCapableBuildings> iteration
    }

    @Override
    public double getMaintenanceCost() {
        return MAINTENANCE_COST_PER_TICK;
    }

    @Override
    public AttackCapableBuildingComponent getAttackCapableBuildingComponent() {
        return attackCapableBuildingComponent;
    }

    @Override
    public double getX() {
        return AttackCapableBuilding.super.getX();
    }

    @Override
    public double getY() {
        return AttackCapableBuilding.super.getY();
    }
}
