package entities.effects;

import empirebuilder.Game;

public class Arrow extends Missile{

    static double arrowDamage = 3;
    static final String imageName = "arrow";
    static final double arrowSpeed = 1;
    static final int INITIAL_ARROW_DURATION = 20;
    static final double standardArrowWidth = 0.1;

    public Arrow(double x, double y, double targetX, double targetY, int factionId) {
        super(x, y, INITIAL_ARROW_DURATION, targetX, targetY, arrowDamage, factionId, arrowSpeed, standardArrowWidth);
    }

    public Arrow(double x, double y, double targetX, double targetY, int factionId,
                 double damage, int duration) {
        super(x, y, duration, targetX, targetY, damage, factionId, arrowSpeed, standardArrowWidth);
    }

    @Override
    public void tick(Game game) {
        double oldX = this.x;
        double oldY = this.y;
        this.x += this.vx;
        this.y += this.vy;

        entities.Entity hit = game.checkArrowHit(this, oldX, oldY);
        if (hit != null) {
            this.x = hit.getX();
            this.y = hit.getY();
            game.spawnLingeringFallenArrow(this);
            setRemainingDuration(0);
        } else {
            lowerDuration();
            if (getRemainingDuration() <= 0) {
                game.spawnLingeringFallenArrow(this);
            }
        }
    }

    @Override
    public String getImageName() {
        return imageName;
    }
}
