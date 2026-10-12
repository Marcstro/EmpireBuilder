package buildings;

import buildings.subBuildings.subBuildingFunctions.AreaBuildingSlot;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import buildings.subBuildings.SimpleGoblinScoutHut;
import buildings.subBuildings.ShootingTower;
import buildings.subBuildings.UnitProducingSubBuilding;
import empirebuilder.Game;
import empirebuilder.Point;

import java.awt.Color;
import java.util.Objects;

public abstract class AreaBuilding extends Building
        implements AreaBuildingSlot, AttackCapableBuilding {

    protected SubBuilding subBuilding;

    protected AreaBuilding(Point point, Color color, double health) {
        super(point, color, health);
    }

    @Override
    public AttackCapableBuildingComponent getAttackCapableBuildingComponent() {
        if (subBuilding instanceof AttackCapableBuilding attackCapableBuilding) {
            return attackCapableBuilding.getAttackCapableBuildingComponent();
        }
        return null;
    }

    @Override
    public void tick(Game game) {
        if (subBuilding != null) {
            subBuilding.tick(game);
            if (subBuilding.hasExpired()) {
                game.destroySubBuilding(subBuilding);
                return;
            }
        }

        if (!isAlive() && getHealth() <= 0) {
            cleanupDestruction(game);
        }
    }

    @Override
    public void cleanupDestruction(Game game) {
        game.getMapCellByPoint(getPoint()).removeBuilding(this);

        if (subBuilding instanceof AttackCapableBuilding) {
            game.getMapCellByPoint(getPoint()).removeAttackCapableBuilding(this);
            game.removeAttackCapableBuilding(this);
        }

        if (subBuilding != null) {
            unregisterSubBuilding(subBuilding);
            subBuilding.setMainOwner(null);
            subBuilding = null;
        }
    }

    @Override
    public SubBuilding getSubBuilding() {
        return subBuilding;
    }

    protected void appendSubBuildingInfo(StringBuilder info) {
        if (subBuilding == null) {
            info.append(", subBuilding=EMPTY");
            return;
        }

        info.append(", subBuilding=").append(subBuilding.getClass().getSimpleName())
                .append(", subBuildingPermanent=").append(subBuilding.isPermanent())
                .append(", subBuildingMaintenance=")
                .append(String.format("%.1f", subBuilding.getMaintenanceCost()));

        if (!subBuilding.isPermanent()) {
            info.append(", lifetime=")
                    .append(String.format("%.0f", subBuilding.getLifetime()));
        }

        if (subBuilding instanceof UnitProducingSubBuilding unitProducer) {
            appendSpawnInfo(
                    info,
                    unitProducer.getCurrentUnitCount(),
                    unitProducer.getMaximumUnitCount(),
                    unitProducer.getTicksUntilNextSpawn());
        } else if (subBuilding instanceof SimpleGoblinScoutHut scoutHut) {
            appendSpawnInfo(
                    info,
                    scoutHut.getScoutCount(),
                    scoutHut.getMaximumScoutCount(),
                    scoutHut.getTicksUntilNextSpawn());
        }
    }

    private void appendSpawnInfo(StringBuilder info, int currentUnits, int maximumUnits,
                                 int ticksUntilNextSpawn) {
        info.append(", units=").append(currentUnits).append("/").append(maximumUnits);
        if (ticksUntilNextSpawn < 0) {
            info.append(", nextSpawn=capacity");
        } else {
            info.append(", nextSpawnIn=").append(ticksUntilNextSpawn).append(" ticks");
        }
    }

    @Override
    public void setSubBuilding(SubBuilding subBuilding, Game game) {
        Objects.requireNonNull(subBuilding, "subBuilding");
        if (this.subBuilding != null) {
            throw new IllegalStateException("Area building already contains a sub-building");
        }
        if (!isAlive()) {
            throw new IllegalStateException("Cannot build on a destroyed area building");
        }

        subBuilding.setMainOwner(getMainOwner());
        registerSubBuilding(subBuilding);
        this.subBuilding = subBuilding;
        game.getMapCellByPoint(getPoint()).addBuilding(this);

        if (subBuilding instanceof ShootingTower) {
            game.getMapCellByPoint(getPoint()).addAttackCapableBuildings(this);
            game.addAttackCapableBuilding(this);
        }
    }

    protected abstract void registerSubBuilding(SubBuilding subBuilding);

    protected abstract void unregisterSubBuilding(SubBuilding subBuilding);
}
