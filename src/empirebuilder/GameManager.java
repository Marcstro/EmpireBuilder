package empirebuilder;

class GameManager{
    
    Engine engine;
    GridPanel gridPanel;
    ButtonPanel buttonPanel;
    MainWindow mainWindow;
    Map map;
    Game game;
    
    static final int WIDTH = 1400;
    static final int HEIGHT = 800;
    static final int BUTTON_PANEL_WIDTH = 200;
    static final int TILE_SIZE = 4;
    
    public GameManager(){
        
        engine = new Engine(this);
        map = new Map(this, (WIDTH/TILE_SIZE), (HEIGHT/TILE_SIZE));
        gridPanel = new GridPanel(this, map, WIDTH/TILE_SIZE, HEIGHT/TILE_SIZE, TILE_SIZE, BUTTON_PANEL_WIDTH);
        buttonPanel = new ButtonPanel(this);
        mainWindow = new MainWindow(this, gridPanel, buttonPanel, WIDTH, HEIGHT);
        game = new Game(this);
        
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