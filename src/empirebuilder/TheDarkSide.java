package empirebuilder;

public class TheDarkSide {

    Game game;

    public TheDarkSide(Game game) {
        this.game = game;
    }

    public void tick(){

        //here the destructive forces logic will be written

        //test methods
        game.spawnTwoOppositeFightingGroups();
        //game.spawnOneTroopForPathFinding();
    }
}
