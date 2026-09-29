package entities.units.AI;

// keeps track of the closest hostile unit and the direction to flee from it
public class ThreatScan {

    public double repulsionX;
    public double repulsionY;
    public double nearestDistanceSq;
    public int threatCount;

    public void reset() {
        repulsionX = 0;
        repulsionY = 0;
        nearestDistanceSq = Double.MAX_VALUE;
        threatCount = 0;
    }

    public boolean foundThreat() {
        return threatCount > 0;
    }

    public boolean isNearestThreatWithin(double radius) {
        return threatCount > 0 && nearestDistanceSq <= radius * radius;
    }
}
