package entities.effects;

import empirebuilder.Game;

public class LingeringFallenArrow extends Missile {

    static final int LINGER_DURATION = 10;

    public LingeringFallenArrow(double x, double y, double rotation) {
        super(x, y, LINGER_DURATION, x + 1, y, 0, 0, 0, 0);
        setRotation(rotation);
    }

    @Override
    public void tick(Game game) {
        lowerDuration();
    }

    @Override
    public String getImageName() {
        return "arrow";
    }
}
