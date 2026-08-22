package empirebuilder;

import graphics.ImageManager;
import pathfinding.AStarPathfinder;
import pathfinding.PathfindingSystem;

public class GameManager{
    
    Engine engine;
    GridPanel gridPanel;
    ButtonPanel buttonPanel;
    MainWindow mainWindow;
    Map map;
    Game game;
    WorldState worldState;
    WorldSettings worldSettings;
    AStarPathfinder pathfinder;
    PathfindingSystem pathfindingSystem;

    // TODO move all of these into world settings.
    static final int WIDTH = 1400;
    static final int HEIGHT = 800;
    static final int BUTTON_PANEL_WIDTH = 200;
    static final int MAP_WIDTH = 300; //300 == smooth // 900 = slight lagg
    static final int MAP_HEIGHT = 200; // 200 == smooth // 600 == slight laggy
    static final int POINT_SIZE = 4;
    static final int GRID_PANEL_WIDTH = (WIDTH-BUTTON_PANEL_WIDTH)/ POINT_SIZE;
    static final int GRID_PANEL_HEIGHT = (HEIGHT)/ POINT_SIZE;
    
    public GameManager(){

        ImageManager.loadAllAssets();

        engine = new Engine(this);
        worldSettings = new WorldSettings();
        map = new Map(this, MAP_WIDTH, MAP_HEIGHT);
        game = new Game(this);
        worldState = new WorldState(this, game);
        gridPanel = new GridPanel(this, map, GRID_PANEL_WIDTH, GRID_PANEL_HEIGHT, MAP_WIDTH, MAP_HEIGHT, POINT_SIZE, BUTTON_PANEL_WIDTH);
        buttonPanel = new ButtonPanel(this);
        mainWindow = new MainWindow(this, gridPanel, buttonPanel, WIDTH, HEIGHT);
        pathfinder = new AStarPathfinder(map);
        pathfindingSystem = new PathfindingSystem(this);

        gridPanel.updateUI();
    }

    /**
     * Headless constructor for unit testing.
     * Creates only the pure-logic layers (Map, Game, pathfinder, MapCellGraph) — no Swing/UI.
     */
    public GameManager(int mapWidth, int mapHeight, WorldSettings settings) {
        // Headless constructor — also builds PathfindingSystem
        this.worldSettings     = settings;
        this.map               = new Map(this, mapWidth, mapHeight);
        this.game              = new Game(this);
        this.pathfinder        = new AStarPathfinder(this.map);
        this.pathfindingSystem = new PathfindingSystem(this);
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

    public WorldState getWorldState() {
        return worldState;
    }

    public AStarPathfinder getPathfinder() { return pathfinder; }

    public PathfindingSystem getPathfindingSystem() {
        return pathfindingSystem;
    }

    public void recreateWorld(){
        engine.stop();
        engine.resetTickCounter();
        map = new Map(this, MAP_WIDTH, MAP_HEIGHT);
        gridPanel.updateMap(map);
        game = new Game(this);
        pathfinder = new AStarPathfinder(map);
        pathfindingSystem = new PathfindingSystem(this);
        pathfindingSystem.reset();

        gridPanel.repaint();
    }
    
}