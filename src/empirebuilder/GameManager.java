package empirebuilder;

import pathfinding.AStarPathfinder;

import javax.swing.*;

class GameManager{
    
    Engine engine;
    GridPanel gridPanel;
    ButtonPanel buttonPanel;
    MainWindow mainWindow;
    Map map;
    Game game;
    AStarPathfinder pathfinder;
    
    static final int WIDTH = 1400;
    static final int HEIGHT = 800;
    static final int BUTTON_PANEL_WIDTH = 200;
    static final int TILE_SIZE = 4;
    // TODO decide upon suitable map size
    static final int POINTS_WIDTH = (WIDTH-BUTTON_PANEL_WIDTH)/TILE_SIZE;
    static final int POINTS_HEIGHT = (HEIGHT)/TILE_SIZE;
    
    public GameManager(){
        
        engine = new Engine(this);
        map = new Map(this, POINTS_WIDTH, POINTS_HEIGHT);
        game = new Game(this);
        gridPanel = new GridPanel(this, map, POINTS_WIDTH, POINTS_HEIGHT, TILE_SIZE, BUTTON_PANEL_WIDTH);
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        buttonPanel = new ButtonPanel(this);
        mainWindow = new MainWindow(this, scrollPane, buttonPanel, WIDTH, HEIGHT);
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

    public Game getGame() {
        return game;
    }
    
}