package empirebuilder;

public class Engine {
    private volatile boolean running = false; // Ensures thread safety
    private Thread gameThread;
    GameManager gameManager;
    private int tickCounter;
    
    public static double REFRESH_RATE = 0.05;
    private static final double MIN_REFRESH_RATE = 0.01;
    private static final double MAX_REFRESH_RATE = 5.0;

    Engine(GameManager gameManager) {
        this.gameManager = gameManager;
        tickCounter=0;
    }
    
    public void start() {
        if (running) {
            System.out.println("Engine is already running!");
            return;
        }

        running = true;
        gameThread = new Thread(() -> {
            while (running) {
                tick();
                try {
                    Thread.sleep((int)(REFRESH_RATE * 1000)); //milliseconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        gameThread.start();
        System.out.println("Engine started.");
    }

    public void stop() {
        if (!running) {
            System.out.println("Engine is already stopped!");
            return;
        }

        running = false;
        try {
            gameThread.join(); // Wait for the thread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Engine stopped.");
    }

    public void tick() {
        //System.out.println("Engine Tick: " + tickCounter++);
        gameManager.getGame().tick();
    }

    public boolean isRunning() {
        return running;
    }
    
    public void increaseSpeed() {
        if (REFRESH_RATE > MIN_REFRESH_RATE) {
            REFRESH_RATE /= 2;
        }
    }

    public void decreaseSpeed() {
        if (REFRESH_RATE < MAX_REFRESH_RATE) {
            REFRESH_RATE *= 2;
        }
    }

    public double getTickRate() {
        return REFRESH_RATE;
    }
    
    
}
