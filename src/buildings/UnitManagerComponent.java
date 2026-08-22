package buildings;

import empirebuilder.Game;
import empirebuilder.Point;
import entities.units.ElfArcher;
import entities.units.Knight;
import entities.units.Unit;

import java.util.ArrayList;
import java.util.List;

public class UnitManagerComponent {

    private final List<Unit> units = new ArrayList<>();
    private final Building ownerBuilding;

    public UnitManagerComponent(Building ownerBuilding) {
        this.ownerBuilding = ownerBuilding;
    }

    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public void removeUnit(Unit unit) {
        units.remove(unit);
    }

    public List<Unit> getUnits() {
        return units;
    }

    public Building getOwnerBuilding() {
        return ownerBuilding;
    }

    public void handleDefenses(Game game){
        if (ownerBuilding instanceof DefensiveTroopBuilding def && def.getDefensiveTroopComponent().hasDanger()
        ){
            if (ownerBuilding.getGold() > 50000){
                ownerBuilding.addGold(-50000);
                List<Point> points = game.findGroupSpawnPoints(ownerBuilding.getPoint().getX(), ownerBuilding.getPoint().getY(), 4 );

                Knight knight1 = new Knight(points.getFirst().getX(), points.getFirst().getY());
                knight1.setUnitOwner(def);
                game.spawnUnitAt(knight1, points.getFirst());
                def.getUnitManagerComponent().addUnit(knight1);

                Knight knight2 = new Knight(points.get(1).getX(), points.get(1).getY());
                knight2.setUnitOwner(def);
                game.spawnUnitAt(knight2, points.get(1));
                def.getUnitManagerComponent().addUnit(knight2);

                ElfArcher elfArcher1 = new ElfArcher(points.get(2).getX(), points.get(2).getY());
                elfArcher1.setUnitOwner(def);
                game.spawnUnitAt(elfArcher1, points.get(2));
                def.getUnitManagerComponent().addUnit(elfArcher1);

                ElfArcher elfArcher2 = new ElfArcher(points.get(3).getX(), points.get(3).getY());
                elfArcher2.setUnitOwner(def);
                game.spawnUnitAt(elfArcher2, points.get(3));
                def.getUnitManagerComponent().addUnit(elfArcher2);
            }
        }
    }


}
