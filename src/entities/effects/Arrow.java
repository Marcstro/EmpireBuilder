package entities.effects;

import empirebuilder.Game;

public class Arrow extends Missile{

    static double arrowDamage = 3;
    static final String imageName = "arrow";
    static final double arrowSpeed = 1;
    static final int INITIAL_ARROW_DURATION = 20;
    static final double arrowWidth = 0.1;

    public Arrow(double x, double y, double targetX, double targetY, int factionId) {
        super(x, y, INITIAL_ARROW_DURATION, targetX, targetY, arrowDamage, factionId, arrowSpeed, arrowWidth);
    }

    @Override
    public void tick(Game game) {
        double oldX = this.x;
        double oldY = this.y;
        this.x += this.vx;
        this.y += this.vy;

        if (game.checkArrowHit(this, oldX, oldY)) {
            setRemainingDuration(0);
        }
        lowerDuration();
    }

    @Override
    public String getImageName() {
        return imageName;
    }
}
