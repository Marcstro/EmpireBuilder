package buildings;

import LandTypes.LandType;
import empirebuilder.Game;
import empirebuilder.Point;
import empirebuilder.TheDarkSide;
import entities.units.*;
import entities.units.AI.Focus;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EvilSideBase extends Building implements UnitOwner {

    int respawnBaseCooldown = 5; // TODO change
    int respawnCooldownRemaining;
    int unitsToSpawn;
    final static int startingHealth = 300;
    TheDarkSide theDarkSide;
    final int dragonCost = 10000;
    List<EvilSideBaseArea> evilSideBaseAreas;

    final static int MAX_AMOUNT_OF_SCOUTS = 10;

    private final UnitManagerComponent unitManager = new UnitManagerComponent(this);

    public EvilSideBase(Point point, TheDarkSide theDarkSide) {
        super(point, LandType.getBaseColor(LandType.RUINED), startingHealth);
        respawnCooldownRemaining = respawnBaseCooldown;
        unitsToSpawn = 6;
        this.theDarkSide = theDarkSide;
        evilSideBaseAreas = new ArrayList<>();
    }


    public void spawnUnits(Game game){
        //calculate suitable amount of units

        if (getGold() > dragonCost){
            List<Point> dragonPoint = game.findGroupSpawnPoints(getPoint().getX(), getPoint().getY(), 1);
            Dragon dragon = new Dragon(dragonPoint.getFirst().getX(), dragonPoint.getFirst().getY());
            dragon.setUnitOwner(this);
            unitManager.addUnit(dragon);
            game.spawnUnitAt(dragon, dragonPoint.getFirst());
            dragon.setLongtermTarget(theDarkSide.getTargetLocation());
            addGold(-dragonCost);
        }

        int amountOfOrcs = 1;//ThreadLocalRandom.current().nextInt(2, 5);
        int amountOfGoblins = 1;//ThreadLocalRandom.current().nextInt(2, 5);

        List<Point> orcPoints = game.findGroupSpawnPoints(getPoint().getX(), getPoint().getY(), amountOfOrcs);
        List<Point> goblinPoints = game.findGroupSpawnPoints(getPoint().getX(), getPoint().getY(), amountOfGoblins);

        for (Point point: orcPoints){
            Orc orc = new Orc(point.getX(), point.getY());
            orc.setUnitOwner(this);
            unitManager.addUnit(orc);
            game.spawnUnitAt(orc, point);
            orc.setLongtermTarget(theDarkSide.getTargetLocation());
        }
        for (Point point: goblinPoints){
            GoblinArcher goblinArcher = new GoblinArcher(point.getX(), point.getY());
            goblinArcher.setUnitOwner(this);
            unitManager.addUnit(goblinArcher);
            game.spawnUnitAt(goblinArcher, point);
            goblinArcher.setLongtermTarget(theDarkSide.getTargetLocation());
        }
    }

    @Override
    public void tick(Game game) {
        if (respawnCooldownRemaining > 0) {
            respawnCooldownRemaining--;
        } else {
            if (theDarkSide.isDarkSideActivated() && getUnitManagerComponent().getUnits().size() < MAX_AMOUNT_OF_SCOUTS) {
                spawnUnits(game);
                respawnCooldownRemaining = respawnBaseCooldown;
            }
        }

        //future site for changing the instructions for owned units
        /*for (Unit unit: unitManager.getUnits()){
            // nothing here for now, could do
            //unit.tick(game);
        }*/
    }

    @Override
    public UnitManagerComponent getUnitManagerComponent() {
        return unitManager;
    }

    @Override
    public Point getInstructions(Unit unit, Game game) {
        if (unit.getLoot() > 1000){
            unit.setCurrentFocus(Focus.IS_RETURNING_WITH_LOOT);
            return getPoint();
        }
        unit.setCurrentFocus(Focus.RAIDING);
        return theDarkSide.getTargetLocation();
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
        long orcs = unitManager.getUnits().stream().filter(u -> u instanceof Orc).count();
        long goblins = unitManager.getUnits().stream().filter(u -> u instanceof GoblinArcher).count();
        long dragons = unitManager.getUnits().stream().filter(u -> u instanceof Dragon).count();
        return "{EvilSideBase: " + getId() +
                ", health: " + getHealth() + "/" + startingHealth +
                ", isAlive=" + isAlive() +
                ", gold=" + String.format("%.0f", getGold()) +
                ", units=" + unitManager.getUnits().size() + "/" + MAX_AMOUNT_OF_SCOUTS +
                " (orcs=" + orcs + ", goblins=" + goblins + ", dragons=" + dragons + ")" +
                ", spawnCooldown=" + respawnCooldownRemaining + "/" + respawnBaseCooldown +
                ", darkSideActive=" + theDarkSide.isDarkSideActivated() +
                ", target=" + (theDarkSide.getTargetLocation() != null ? theDarkSide.getTargetLocation().getPositionString() : "none") +
                "}";
    }
}
