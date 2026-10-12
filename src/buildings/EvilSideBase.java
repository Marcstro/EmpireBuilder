package buildings;

import LandTypes.LandType;
import buildings.ai.profiles.EvilSideBaseAI;
import buildings.subBuildings.SimpleGoblinScoutHut;
import empirebuilder.Game;
import empirebuilder.Point;
import empirebuilder.TheDarkSide;
import entities.units.*;

import java.util.ArrayList;
import java.util.List;

public class EvilSideBase extends Building implements UnitOwner {

    public static final int EVIL_FACTION_ID = 2;
    final static int startingHealth = 300;
    TheDarkSide theDarkSide;
    List<EvilSideBaseArea> evilSideBaseAreas;

    private final UnitManagerComponent unitManager = new UnitManagerComponent(this);

    public EvilSideBase(Point point, TheDarkSide theDarkSide) {
        super(point, LandType.getBaseColor(LandType.RUINED), startingHealth);
        this.theDarkSide = theDarkSide;
        evilSideBaseAreas = new ArrayList<>();
        setFactionId(EVIL_FACTION_ID);
        initBuildingAI();
        setGold(10000);
    }

    /** Call this after all areas have been added to bootstrap with a free SimpleGoblinScoutHut. */
    public void bootstrapWithScoutHut(empirebuilder.Game game) {
        if (!evilSideBaseAreas.isEmpty()) {
            int randomIndex = (int) (Math.random() * evilSideBaseAreas.size());
            EvilSideBaseArea bootstrapArea = evilSideBaseAreas.get(randomIndex);
            if (bootstrapArea.getSubBuilding() == null) {
                bootstrapArea.setSubBuilding(new SimpleGoblinScoutHut(bootstrapArea), game);
            }
        }
    }

    @Override
    public void tick(Game game) {
        tickBuildingAI(game);
        tickAreaBuildingSubBuildings(game);
    }

    private void tickAreaBuildingSubBuildings(Game game) {
        tickAreaBuildingsWithMaintenance(evilSideBaseAreas, game);
    }

    @Override
    public UnitManagerComponent getUnitManagerComponent() {
        return unitManager;
    }

    @Override
    public Point getInstructions(Unit unit, Game game) {
        var ai = (EvilSideBaseAI) getBuildingAI().getProfile();
        return ai.getUnitInstructions(this, unit);
    }

    public List<EvilSideBaseArea> getEvilSideBaseAreas() {
        return evilSideBaseAreas;
    }

    public void setEvilSideBaseAreas(List<EvilSideBaseArea> evilSideBaseAreas) {
        this.evilSideBaseAreas = evilSideBaseAreas;
    }

    public void addEvilSideBaseArea(EvilSideBaseArea area) {
        this.evilSideBaseAreas.add(area);
    }

    public void removeEvilSideBaseArea(EvilSideBaseArea area) {
        this.evilSideBaseAreas.remove(area);
    }

    @Override
    public String getImageName() {
        return "EvilLair";
    }

    @Override
    public String getInfo() {
        long scouts = unitManager.getUnits().stream().filter(u -> u instanceof GoblinScout).count();
        long orcs = unitManager.getUnits().stream().filter(u -> u instanceof Orc).count();
        long goblins = unitManager.getUnits().stream().filter(u -> u instanceof GoblinArcher).count();
        long dragons = unitManager.getUnits().stream().filter(u -> u instanceof Dragon).count();
        double totalMaintenanceCost = calculateTotalSubBuildingMaintenance();
        return "{EvilSideBase: " + getId() +
                ", health: " + getHealth() + "/" + startingHealth +
                ", isAlive=" + isAlive() +
                ", gold=" + String.format("%.0f", getGold()) +
                ", units=" + unitManager.getUnits().size() +
                " (scouts=" + scouts + ", orcs=" + orcs + ", goblins=" + goblins + ", dragons=" + dragons + ")" +
                ", subBuilding maintenance cost=" + String.format("%.2f", totalMaintenanceCost) +
                ", darkSideActive=" + theDarkSide.isDarkSideActivated() +
                "}";
    }
}
