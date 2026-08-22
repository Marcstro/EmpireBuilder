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

        if (isDarkSideActivated && evilSideBases.isEmpty()) {
            createNewEviLSideBase(game);
        }
        for(EvilSideBase evilSideBase : evilSideBases){
            evilSideBase.tick(game);
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

    // TODO make an evil lair save target for future raiding, change target if raiding isnt going well
    public Point getTargetLocation(){
       return game.getEvilSideTarget();
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
