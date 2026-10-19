package empirebuilder;

import buildings.EvilBase;
import buildings.EvilBaseArea;

import java.util.ArrayList;
import java.util.List;

public class TheDarkSide {

    Game game;
    List<EvilBase> evilBases;

    boolean isDarkSideActivated;
    private boolean evilSideSpawningEnabled = true;
    CivilizationDevelopmentLevel civilizationDevelopmentLevel;

    int evilBaseCreationCooldown = 20;

    public TheDarkSide(Game game) {
        this.game = game;
        isDarkSideActivated = false;
        evilBases =  new ArrayList<EvilBase>();
    }

    public void tick(Game game){

        //test methods
        // this is purely for testing combat. it continously spawns troops on opposites sides that target and fight eachother
        //game.getTestFunctions().spawnTwoOppositeFightingGroups();

        // this spawns a single troop that can be used to see if pathfinding around terrain works
        //game.getTestFunctions().spawnOneTroopForPathFinding();

        // TODO implement for more than 1 evilbase to be spawned, after x time
        // should have a limit tho.
        if (evilSideSpawningEnabled && evilBases.isEmpty() && isDarkSideActivated) {
            if (evilBaseCreationCooldown <= 0) {
                createNewEvilBase(game);
                evilBaseCreationCooldown = 200;
            } else {
                evilBaseCreationCooldown--;
            }
        }

    }

    public void updateActiveStatus(CivilizationDevelopmentLevel civilizationDevelopmentLevel){
        this.civilizationDevelopmentLevel =  civilizationDevelopmentLevel;
        if (civilizationDevelopmentLevel != CivilizationDevelopmentLevel.PRIMITIVE){
            activate();
            System.out.println("EVIL SIDE ACTIVATED");
        } else {
            deactivate();
        }
    }

    public void createNewEvilBase(Game game){
        Point point = game.findPositionForEvilBase();
        EvilBase evilBase = game.createEvilBase(point, this);
        evilBases.add(evilBase);
        System.out.println("Created evil side lair at " + point.getPositionString());
    }

    public void activate(){
        isDarkSideActivated = true;
    }

    public void removeEvilBase(EvilBase evilBase) {
        evilBases.remove(evilBase);
    }

    public void deactivate(){
        isDarkSideActivated = false;
    }

    public boolean isDarkSideActivated(){
        return isDarkSideActivated;
    }

    public boolean isEvilSideSpawningEnabled() {
        return evilSideSpawningEnabled;
    }

    public void setEvilSideSpawningEnabled(boolean enabled) {
        evilSideSpawningEnabled = enabled;
    }
}
