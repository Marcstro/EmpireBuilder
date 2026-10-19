package entities.effects;

import empirebuilder.Game;

public class Grave extends Effect {
    static final int LINGER_DURATION = 60 * 20;

    public Grave(double x, double y) {
        super(x, y, LINGER_DURATION, 0, 0);
    }

    @Override
    public void tick(Game game) {
        lowerDuration();
    }

    @Override
    public String getImageName() {
        return "grave";
    }
}
