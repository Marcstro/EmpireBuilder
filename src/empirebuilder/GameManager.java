package empirebuilder;

import graphics.ImageManager;
import pathfinding.AStarPathfinder;

class GameManager{
    
    Engine engine;
    GridPanel gridPanel;
    ButtonPanel buttonPanel;
    MainWindow mainWindow;
    Map map;
    Game game;
    WorldSettings worldSettings;
    AStarPathfinder pathfinder;

    // TODO move all of these into world settings.
    static final int WIDTH = 1400;
    static final int HEIGHT = 800;
    static final int BUTTON_PANEL_WIDTH = 200;
    static final int TILE_SIZE = 4;
    // TODO decide upon suitable map size
    static final int POINTS_WIDTH = (WIDTH-BUTTON_PANEL_WIDTH)/TILE_SIZE; //400;// alternative set size
    static final int POINTS_HEIGHT = (HEIGHT)/TILE_SIZE; //200; //alternative set size
    
    public GameManager(){

        //static classes that can be preloaded before any others
        ImageManager.preloadAllBaseImages();

        //rest
        engine = new Engine(this);
        worldSettings = new WorldSettings();
        map = new Map(this, POINTS_WIDTH, POINTS_HEIGHT);
        game = new Game(this);
        gridPanel = new GridPanel(this, map, POINTS_WIDTH, POINTS_HEIGHT, TILE_SIZE, BUTTON_PANEL_WIDTH);
        buttonPanel = new ButtonPanel(this);
        mainWindow = new MainWindow(this, gridPanel, buttonPanel, WIDTH, HEIGHT);
        pathfinder = new AStarPathfinder(map);
        
        gridPanel.updateUI();
    }

    public Engine getEngine() {
        return engine;
    }

    public GridPanel getGridPanel() {
        return gridPanel;
    }

    public ButtonPanel getButtonPanel() {
        return buttonPanel;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public Map getMap() {
        return map;
    }

    public WorldSettings getWorldSettings() {
        return worldSettings;
    }

    public Game getGame() {
        return game;
    }

    public void recreateWorld(){
        engine.stop();
        map = new Map(this, POINTS_WIDTH, POINTS_HEIGHT);
        gridPanel.updateMap(map);
        game = new Game(this);
        pathfinder = new AStarPathfinder(map);

        gridPanel.repaint();
    }
    
}