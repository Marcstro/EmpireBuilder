package empirebuilder;

import buildings.EvilSideBase;
import buildings.EvilSideBaseArea;

import java.util.ArrayList;
import java.util.List;

public class TheDarkSide {

    Game game;
    List<EvilSideBase> evilSideBases;

    boolean isDarkSideActivated;
    CivilizationDevelopmentLevel civilizationDevelopmentLevel;

    int evilSideBaseCreationCooldown = 20;

    public TheDarkSide(Game game) {
        this.game = game;
        isDarkSideActivated = false;
        evilSideBases =  new ArrayList<EvilSideBase>();
    }

    public void tick(Game game){

        //test methods
        // this is purely for testing combat. it continously spawns troops on opposites sides that target and fight eachother
        //game.spawnTwoOppositeFightingGroups();

        // this spawns a single troop that can be used to see if pathfinding around terrain works
        //game.spawnOneTroopForPathFinding();

        // TODO implement for more than 1 evilsidebase to be spawned, after x time
        // should have a limit tho.
        if (evilSideBases.isEmpty() && isDarkSideActivated) {
            if (evilSideBaseCreationCooldown <= 0) {
                createNewEviLSideBase(game);
                evilSideBaseCreationCooldown = 200;
            } else {
                evilSideBaseCreationCooldown--;
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

    public void createNewEviLSideBase(Game game){
        Point point = game.findPositionForEvilBase();
        EvilSideBase evilSideBase = game.createEvilSideBase(point, this);
        evilSideBases.add(evilSideBase);
        System.out.println("Created evil side lair at " + point.getPositionString());
    }

    public void activate(){
        isDarkSideActivated = true;
    }

    public void deactivate(){
        isDarkSideActivated = false;
    }

    public boolean isDarkSideActivated(){
        return isDarkSideActivated;
    }
}
