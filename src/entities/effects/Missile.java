package entities.effects;

public abstract class Missile extends Effect{

    double vy;
    double vx;
    double damage;
    double rotation;
    double width;

    public Missile(double x, double y,  int initialDuration, double targetX, double targetY, double damage, int factionId, double speed, double width) {
        super(x, y, initialDuration, factionId, speed);
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            this.vx = (dx / distance) * speed;
            this.vy = (dy / distance) * speed;
            this.rotation = Math.atan2(dy, dx);
        } else {
            this.vx = 0;
            this.vy = 0;
            this.rotation = 0;
        }
        this.damage=damage;
        this.width=width;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getWidth() {
        return width;
    }

}
