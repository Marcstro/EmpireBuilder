package buildings;

import LandTypes.LandType;
import buildings.ai.profiles.EvilBaseAI;
import buildings.subBuildings.SimpleGoblinScoutHut;
import empirebuilder.Game;
import empirebuilder.Point;
import empirebuilder.TheDarkSide;
import entities.units.*;

import java.util.ArrayList;
import java.util.List;

public class EvilBase extends Building implements UnitOwner {

    public static final int EVIL_FACTION_ID = 2;
    final static int startingHealth = 300;
    private static final double INITIAL_GOLD = 12000;
    TheDarkSide theDarkSide;
    List<EvilBaseArea> evilBaseAreas;

    private final UnitManagerComponent unitManager = new UnitManagerComponent(this);

    public EvilBase(Point point, TheDarkSide theDarkSide) {
        super(point, LandType.getBaseColor(LandType.RUINED), startingHealth);
        this.theDarkSide = theDarkSide;
        evilBaseAreas = new ArrayList<>();
        setFactionId(EVIL_FACTION_ID);
        initBuildingAI();
        setGold(INITIAL_GOLD);
    }

    public void bootstrapWithScoutHut(empirebuilder.Game game) {
        if (!evilBaseAreas.isEmpty()) {
            int randomIndex = (int) (Math.random() * evilBaseAreas.size());
            EvilBaseArea bootstrapArea = evilBaseAreas.get(randomIndex);
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
        tickAreaBuildingsWithMaintenance(evilBaseAreas, game);
    }

    @Override
    public UnitManagerComponent getUnitManagerComponent() {
        return unitManager;
    }

    @Override
    public Point getInstructions(Unit unit, Game game) {
        var ai = (EvilBaseAI) getBuildingAI().getProfile();
        return ai.getUnitInstructions(this, unit);
    }

    public List<EvilBaseArea> getEvilBaseAreas() {
        return evilBaseAreas;
    }

    public void setEvilBaseAreas(List<EvilBaseArea> evilBaseAreas) {
        this.evilBaseAreas = evilBaseAreas;
    }

    public void addEvilBaseArea(EvilBaseArea area) {
        this.evilBaseAreas.add(area);
    }

    public void removeEvilBaseArea(EvilBaseArea area) {
        this.evilBaseAreas.remove(area);
    }

    @Override
    public String getImageName() {
        return "EvilLair";
    }

    @Override
    public String getInfo() {
        double totalMaintenanceCost = calculateTotalSubBuildingMaintenance();
        return "{EvilBase: " + getId() +
                ", health: " + getHealth() + "/" + startingHealth +
                ", isAlive=" + isAlive() +
                ", gold=" + String.format("%.0f", getGold()) +
                ", " + unitManager.getInfo() +
                ", subBuilding maintenance cost=" + String.format("%.2f", totalMaintenanceCost) +
                ", darkSideActive=" + theDarkSide.isDarkSideActivated() +
                "}";
    }
}
