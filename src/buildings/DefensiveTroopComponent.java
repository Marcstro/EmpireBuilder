package buildings;

import empirebuilder.Game;
import empirebuilder.MapCell;
import empirebuilder.Point;
import entities.units.AI.UnitOrder;
import entities.units.FarmerMilitia;
import entities.units.Unit;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DefensiveTroopComponent {

    /** Citizens required to support one militiaman. */
    private static final int PEOPLE_PER_MILITIA = 100;

    // time to recover a single militia, slow process
    private static final int MILITIA_RECOVERY_BUILDING_TICKS = 120;
    private static final int MAX_MILITIA_PER_WAVE = 8;
    private static final int MILITIA_COST = 1000;
    private static final int MILITIA_COOLDOWN_BUILDING_TICKS = 120;

    /** How long (in building ticks) a building remembers being damaged. */
    private static final int DAMAGE_MEMORY_TICKS = 50;

    private Building ownerBuilding;
    private final LinkedList<MapCell> dangerList = new LinkedList<>();

    private int occupiedMilitiaSlots = 0;

    /** Slots belonging to dead militia, waiting to be reset */
    private int recoveringMilitiaSlots = 0;
    private int recoveryTimer = 0;

    private int damageMemoryTimer = 0;
    private int militiaCooldown = 0;

    public DefensiveTroopComponent(Building ownerBuilding) {
        this.ownerBuilding = ownerBuilding;
    }

    public void setOwnerBuilding(Building ownerBuilding) {
        this.ownerBuilding = Objects.requireNonNull(ownerBuilding);
    }

    public void addToDangerList(MapCell mapCell, Game game) {
        if (!dangerList.contains(mapCell)
                && game.canReachPoint(ownerBuilding.getPoint(), game.getPointByMapCell(mapCell))) {
            dangerList.add(mapCell);
        }
    }

    public void dangerIsOver(MapCell mapCell) {
        dangerList.remove(mapCell);
    }

    public boolean hasDanger() {
        return !dangerList.isEmpty();
    }

    public MapCell getNextDangerCell() {
        return dangerList.isEmpty() ? null : dangerList.getFirst();
    }

    public Point getDefensiveInstructions(Unit unit, Game game){
        if (hasDanger()) {
            unit.setUnitOrder(UnitOrder.DEFEND_EXTERNAL_AREA);
            return game.getPointByMapCell(getNextDangerCell());
        }
        unit.setUnitOrder(UnitOrder.NONE);
        return null;
    }

    public void tick() {
        if (militiaCooldown > 0) {
            militiaCooldown--;
        }
        if (recoveringMilitiaSlots > 0) {
            recoveryTimer--;
            if (recoveryTimer <= 0) {
                recoveringMilitiaSlots--;
                occupiedMilitiaSlots = Math.max(0, occupiedMilitiaSlots - 1);
                recoveryTimer = MILITIA_RECOVERY_BUILDING_TICKS;
            }
        }

        if (damageMemoryTimer > 0) {
            damageMemoryTimer--;
        }
    }

    public void recordDamageTaken() {
        damageMemoryTimer = DAMAGE_MEMORY_TICKS;
    }

    public boolean wasRecentlyDamaged() {
        return damageMemoryTimer > 0;
    }

    public void spawnMilitia(Game game) {
        if (!(ownerBuilding instanceof DefensiveTroopBuilding owner)) {
            throw new IllegalStateException("Militia require a defensive troop building owner");
        }
        if (militiaCooldown > 0) {
            return;
        }
        int amount = Math.min(Math.min(getMaxAvailableMilitia(), MAX_MILITIA_PER_WAVE),
                (int) (ownerBuilding.getGold() / MILITIA_COST));
        if (amount <= 0) {
            return;
        }
        List<Point> points = game.findGroupSpawnPoints(ownerBuilding.getPoint().getX(),
                ownerBuilding.getPoint().getY(), amount);
        if (points.isEmpty()) {
            return;
        }
        for (Point point : points) {
            FarmerMilitia militia = new FarmerMilitia(point.getX(), point.getY());
            militia.setUnitOwner(owner);
            game.spawnUnitAt(militia, point);
            owner.getUnitManagerComponent().addUnit(militia);
        }
        reserveMilitiaSlots(points.size());
        ownerBuilding.addGold(-points.size() * MILITIA_COST);
        militiaCooldown = MILITIA_COOLDOWN_BUILDING_TICKS;
    }

    /** Total militia this settlement could field if none were deployed or lost. */
    public int getMilitiaCapacity() {
        return ownerBuilding.getPeople() / PEOPLE_PER_MILITIA;
    }


    public int getMaxAvailableMilitia() {
        return Math.max(0, getMilitiaCapacity() - occupiedMilitiaSlots);
    }

    public void reserveMilitiaSlots(int amount) {
        occupiedMilitiaSlots += amount;
    }

    public void notifyMilitiaDeath() {
        recoveringMilitiaSlots++;
        if (recoveryTimer <= 0) {
            recoveryTimer = MILITIA_RECOVERY_BUILDING_TICKS;
        }
    }

    public void releaseMilitiaSlot() {
        occupiedMilitiaSlots = Math.max(0, occupiedMilitiaSlots - 1);
    }

    public int getOccupiedMilitiaSlots() {
        return occupiedMilitiaSlots;
    }
}
