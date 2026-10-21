package empirebuilder;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Engine {
    public static final double BASE_TICKS_PER_SECOND = 60;
    public static final double NORMAL_SPEED_MULTIPLIER = 1;
    public static final double MAX_SPEED_MULTIPLIER = Double.POSITIVE_INFINITY;
    public static final List<Double> SPEED_MULTIPLIERS = List.of(
            1.0 / 1024, 1.0 / 512, 1.0 / 256, 1.0 / 128, 1.0 / 64,
            1.0 / 32, 1.0 / 16, 1.0 / 8, 1.0 / 4, 1.0 / 2,
            1.0, 2.0, 3.0, 4.0, 5.0, 10.0, MAX_SPEED_MULTIPLIER);

    private final Object stateLock = new Object();
    private final TickRateTracker tickRateTracker = new TickRateTracker();
    private final GameManager gameManager;
    private volatile boolean running = false;
    private volatile int speedIndex = SPEED_MULTIPLIERS.indexOf(NORMAL_SPEED_MULTIPLIER);
    private volatile int tickCounter;
    private Thread gameThread;
    private javax.swing.Timer renderTimer;
    private volatile Runnable afterTickAction;
    private boolean stopRequested;
    private boolean tickInProgress;
    private int pendingSteps;
    private long timingVersion;

    Engine(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void start() {
        synchronized (stateLock) {
            if (running) {
                return;
            }
            ensureGameThread();
            running = true;
            pendingSteps = 0;
            resetTiming();
        }
        startRendering();
    }

    public void pause() {
        synchronized (stateLock) {
            running = false;
            pendingSteps = 0;
            resetTiming();
        }
    }

    public void advanceOneTick() {
        synchronized (stateLock) {
            if (running) {
                throw new IllegalStateException("Pause the simulation before advancing one tick");
            }
            ensureGameThread();
            pendingSteps++;
            stateLock.notifyAll();
        }
        startRendering();
    }

    public void stop() {
        Thread worker;
        synchronized (stateLock) {
            running = false;
            pendingSteps = 0;
            stopRequested = true;
            afterTickAction = null;
            resetTiming();
            worker = gameThread;
        }
        if (worker != null && worker != Thread.currentThread()) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while stopping the simulation", e);
            }
        }
        stopRendering();
    }

    private void ensureGameThread() {
        if (gameThread == null) {
            stopRequested = false;
            gameThread = new Thread(this::runLoop, "EmpireBuilder-Simulation");
            gameThread.setDaemon(true);
            gameThread.start();
        } else if (stopRequested) {
            throw new IllegalStateException("The simulation is still stopping");
        }
    }

    private void runLoop() {
        TickSchedule schedule = new TickSchedule();
        long scheduledVersion = -1;
        try {
            while (true) {
                boolean singleStep;
                long tickVersion;
                synchronized (stateLock) {
                    while (!stopRequested && !running && pendingSteps == 0) {
                        stateLock.wait();
                    }
                    if (stopRequested) {
                        return;
                    }
                    singleStep = !running;
                    tickVersion = timingVersion;
                    if (singleStep) {
                        pendingSteps--;
                    } else {
                        if (scheduledVersion != timingVersion) {
                            double targetTps = getTargetTicksPerSecond();
                            long interval = Double.isInfinite(targetTps)
                                    ? 0 : Math.round(1_000_000_000.0 / targetTps);
                            schedule.reset(System.nanoTime(), interval);
                            scheduledVersion = timingVersion;
                        }
                        long delay = schedule.delayNanos(System.nanoTime());
                        if (delay > 0) {
                            TimeUnit.NANOSECONDS.timedWait(stateLock, delay);
                            continue;
                        }
                    }
                    tickInProgress = true;
                }

                tick();

                synchronized (stateLock) {
                    tickInProgress = false;
                    if (!singleStep && running && tickVersion == timingVersion) {
                        long now = System.nanoTime();
                        tickRateTracker.recordTick(now);
                        schedule.tickCompleted(now);
                    }
                    stateLock.notifyAll();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("The simulation thread was interrupted", e);
        } finally {
            synchronized (stateLock) {
                running = false;
                tickInProgress = false;
                pendingSteps = 0;
                afterTickAction = null;
                tickRateTracker.reset();
                gameThread = null;
                stateLock.notifyAll();
            }
        }
    }

    private void startRendering() {
        if (gameManager.getGridPanel() == null) {
            return;
        }
        Runnable start = () -> {
            if (renderTimer == null) {
                renderTimer = new javax.swing.Timer(16, e -> gameManager.getGridPanel().repaint());
            }
            renderTimer.start();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            start.run();
        } else {
            SwingUtilities.invokeLater(start);
        }
    }

    private void stopRendering() {
        if (gameManager.getGridPanel() == null) {
            return;
        }
        Runnable stop = () -> {
            if (renderTimer != null) {
                renderTimer.stop();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            stop.run();
        } else {
            SwingUtilities.invokeLater(stop);
        }
    }

    public void tick() {
        tickCounter++;

        gameManager.getGame().tickEffects();
        gameManager.getGame().tickUnits(); // TODO determine suitable tick rate for units and effects
        gameManager.getGame().attackingBuildingsTick();
        //this cleaning method needs to be called AFTER effects and units
        gameManager.getGame().removeDeadOrExpired();
        // eventsTicks are for Map alteringen effects
        // like calls that affects the dark side, all good building etc

        if (tickCounter % 50 == 0){
            gameManager.getGame().eventsTick();
        }
        if (tickCounter % 10 == 0){
            gameManager.getGame().tickBuildings();
        }
        if (tickCounter % 50 == 0){
            gameManager.getGame().checkForBuildingUpgrades();
        }
        if (tickCounter % 150 == 0){
            gameManager.getGame().seldomTick();
        }
        if (tickCounter % 1000 == 0){
            //TODO extract these into a method, don't have them just here
            gameManager.getGame().tickUpdateBuildingOwnershipByDistance();
            CivilizationDevelopmentLevel previousCivilizationDevelopmentLevel =
                    gameManager.getWorldState().getCivilizationDevelopmentLevel();
            gameManager.getWorldState().calculateWorldState();
            CivilizationDevelopmentLevel civilizationDevelopmentLevel =
                    gameManager.getWorldState().getCivilizationDevelopmentLevel();
            if (previousCivilizationDevelopmentLevel != civilizationDevelopmentLevel) {
                System.out.println("The civilization has changed and is now at " + civilizationDevelopmentLevel);
                gameManager.getGame().getDarkside().updateActiveStatus(civilizationDevelopmentLevel);
            }
        }

        Runnable action = afterTickAction;
        if (action != null) {
            action.run();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void increaseSpeed() {
        synchronized (stateLock) {
            setSpeedIndex(Math.min(speedIndex + 1, SPEED_MULTIPLIERS.size() - 1));
        }
    }

    public void decreaseSpeed() {
        synchronized (stateLock) {
            setSpeedIndex(Math.max(speedIndex - 1, 0));
        }
    }

    public int getSpeedIndex() {
        return speedIndex;
    }

    public void setSpeedIndex(int index) {
        if (index < 0 || index >= SPEED_MULTIPLIERS.size()) {
            throw new IllegalArgumentException("Unknown simulation speed index: " + index);
        }
        synchronized (stateLock) {
            if (speedIndex != index) {
                speedIndex = index;
                resetTiming();
            }
        }
    }

    public double getSpeedMultiplier() {
        return SPEED_MULTIPLIERS.get(speedIndex);
    }

    public void setSpeedMultiplier(double multiplier) {
        int index = SPEED_MULTIPLIERS.indexOf(multiplier);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown simulation speed multiplier: " + multiplier);
        }
        setSpeedIndex(index);
    }

    public double getTargetTicksPerSecond() {
        return BASE_TICKS_PER_SECOND * getSpeedMultiplier();
    }

    /** Average over up to 200 completed tick intervals, excluding pauses and previous speed settings. */
    public double getActualTicksPerSecond() {
        synchronized (stateLock) {
            return running ? tickRateTracker.getTicksPerSecond() : 0;
        }
    }

    private void resetTiming() {
        timingVersion++;
        tickRateTracker.reset();
        stateLock.notifyAll();
    }

    public void setAfterTickAction(Runnable action) {
        afterTickAction = action;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public void resetTickCounter(){
        synchronized (stateLock) {
            if (running || tickInProgress || pendingSteps > 0) {
                throw new IllegalStateException("Stop the simulation before resetting its tick counter");
            }
            tickCounter=0;
            resetTiming();
        }
    }

    static final class TickSchedule {
        private static final int MAX_CATCH_UP_TICKS = 5;
        private long intervalNanos;
        private long nextTickNanos;
        private int catchUpTicks;

        void reset(long now, long interval) {
            intervalNanos = interval;
            nextTickNanos = now;
            catchUpTicks = 0;
        }

        long delayNanos(long now) {
            return intervalNanos == 0 ? 0 : Math.max(0, nextTickNanos - now);
        }

        void tickCompleted(long now) {
            if (intervalNanos == 0) {
                return;
            }
            nextTickNanos += intervalNanos;
            if (nextTickNanos <= now) {
                if (++catchUpTicks >= MAX_CATCH_UP_TICKS) {
                    // Drop accumulated wall-clock debt, not simulation ticks.
                    nextTickNanos = now;
                    catchUpTicks = 0;
                }
            } else {
                catchUpTicks = 0;
            }
        }
    }

    static final class TickRateTracker {
        private final long[] completionTimes = new long[201];
        private int nextIndex;
        private int sampleCount;
        private double ticksPerSecond;

        void recordTick(long now) {
            completionTimes[nextIndex] = now;
            nextIndex = (nextIndex + 1) % completionTimes.length;
            sampleCount = Math.min(sampleCount + 1, completionTimes.length);
            if (sampleCount > 1) {
                int oldestIndex = sampleCount == completionTimes.length ? nextIndex : 0;
                long elapsed = now - completionTimes[oldestIndex];
                ticksPerSecond = elapsed > 0
                        ? (sampleCount - 1) * 1_000_000_000.0 / elapsed : 0;
            }
        }

        double getTicksPerSecond() {
            return ticksPerSecond;
        }

        void reset() {
            nextIndex = 0;
            sampleCount = 0;
            ticksPerSecond = 0;
        }
    }
}
