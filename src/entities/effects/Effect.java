package entities.effects;

import entities.MovingEntity;

public abstract class Effect extends MovingEntity {

    int initialDuration;
    int remainingDuration;
    double direction;

    public Effect(double x, double y, int initialDuration, int factionId, double speed) {
        super(x, y, 0, 0, factionId, speed);
        this.initialDuration = initialDuration;
        remainingDuration = initialDuration;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public int getRemainingDuration() {
        return remainingDuration;
    }

    public void setRemainingDuration(int remainingDuration) {
        this.remainingDuration = remainingDuration;
    }

    public void lowerDuration(){
        remainingDuration--;
    }
}
