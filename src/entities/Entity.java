package entities;

import empirebuilder.Game;

public abstract class Entity {

    protected double x;
    protected double y;
    double health;
    boolean isAlive;
    int factionId;
    public double size;

    public Entity(double x, double y, double health, double size, int factionId) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.factionId = factionId;
        this.size = size;
        isAlive = true;
    }

    public void causeHealthLoss(double healthLoss){
        health -= healthLoss;
        if (health <= 0){
           // System.out.println("Entity " + this.getClass().getSimpleName() + " took " + healthLoss + " damage AND FUCKING DIED!");
            setIsAlive(false);
        }
        /*else {
          //  System.out.println("Entity " + this.getClass().getSimpleName() + " took " + healthLoss + " damage, health remaining: " + health);
        }*/
    }

    public abstract void tick(Game game);

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setIsAlive(boolean alive) {
        isAlive = alive;
    }

    public int getFactionId() {
        return factionId;
    }

    public void setFactionId(int factionId) {
        this.factionId = factionId;
    }

    public boolean isHostileTo(int otherFactionId) {
        return this.factionId != otherFactionId;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public abstract String getImageName();
}
