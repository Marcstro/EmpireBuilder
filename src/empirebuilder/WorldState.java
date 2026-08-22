package empirebuilder;

public class WorldState {

    CivilizationDevelopmentLevel civilizationDevelopmentLevel;
    GameManager gameManager;
    Game game;

    public WorldState(GameManager gameManager, Game game) {
        this.gameManager = gameManager;
        this.game = game;
        civilizationDevelopmentLevel = CivilizationDevelopmentLevel.PRIMITIVE;
    }

    public CivilizationDevelopmentLevel getCivilizationDevelopmentLevel() {
        return civilizationDevelopmentLevel;
    }

    public void calculateWorldState(){
        civilizationDevelopmentLevel = game.calculateWorldState();
    }
}
