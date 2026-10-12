package buildings;

import empirebuilder.Game;
import empirebuilder.Point;

import java.awt.*;
import java.util.LinkedList;

public abstract class VillageOwningBuilding extends FarmOwningBuilding{

    LinkedList<Village> villages;

    public void attemptToSpawnAnimal(Game game){
        while (!villages.isEmpty()){
            Village villageWithRoom = null;
            for (Village village : villages){
                if (village.hasRoomForAdditionalAnimals()){
                    villageWithRoom = village;
                    break;
                }
            }
            if (villageWithRoom == null){
                break;
            }
            villageWithRoom.addAnimals(game);
        }
    }

    public VillageOwningBuilding(Point point, int foodNeededToCreateNewFarm, Color color, double health) {
        super(point, foodNeededToCreateNewFarm, color, health);
        villages = new LinkedList<>();
    }

    public void addVillage(Village village){
        villages.add(village);
    }

    public LinkedList<Village> getVillages() {
        return villages;
    }

    public void removeVillage(Village village){
        villages.remove(village);
    }

    public void setVillages(LinkedList<Village> villages) {
        this.villages = villages;
    }
}
