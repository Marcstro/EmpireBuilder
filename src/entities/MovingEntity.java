package entities;

public abstract class MovingEntity extends Entity {

    protected int mapCellListIndex;
    protected int mapCellX, mapCellY;
    double speed;

    public MovingEntity(double x, double y, double health, double size, int factionId, double speed) {
        super(x, y, health, size, factionId);
        mapCellListIndex = -1;
        this.speed = speed;
    }

    public int getMapCellListIndex() {
        return mapCellListIndex;
    }

    public void setMapCellListIndex(int mapCellListIndex) {
        this.mapCellListIndex = mapCellListIndex;
    }

    public int getMapCellX() {
        return mapCellX;
    }

    public void setMapCellX(int mapCellX) {
        this.mapCellX = mapCellX;
    }

    public int getMapCellY() {
        return mapCellY;
    }

    public void setMapCellY(int mapCellY) {
        this.mapCellY = mapCellY;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
