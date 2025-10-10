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
    static final int MAP_WIDTH = 300; //300 == smooth // 900 = slight lagg
    static final int MAP_HEIGHT = 200; // 200 == smooth // 600 == slight laggy
    static final int TILE_SIZE = 4;
    static final int GRID_PANEL_WIDTH = (WIDTH-BUTTON_PANEL_WIDTH)/TILE_SIZE;
    static final int GRID_PANEL_HEIGHT = (HEIGHT)/TILE_SIZE;
    
    public GameManager(){

        ImageManager.preloadAllBaseImages();

        engine = new Engine(this);
        worldSettings = new WorldSettings();
        map = new Map(this, MAP_WIDTH, MAP_HEIGHT);
        game = new Game(this);
        gridPanel = new GridPanel(this, map, GRID_PANEL_WIDTH, GRID_PANEL_HEIGHT, MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, BUTTON_PANEL_WIDTH);
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
        engine.resetTickCounter();
        map = new Map(this, MAP_WIDTH, MAP_HEIGHT);
        gridPanel.updateMap(map);
        game = new Game(this);
        pathfinder = new AStarPathfinder(map);

        gridPanel.repaint();
    }
    
}