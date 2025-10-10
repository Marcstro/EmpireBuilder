package LandTypes;

import LandTypes.LandType;
import java.awt.Color;

public abstract class Land {
    protected LandType landType;
    int wealth;
    int danger;
    int idCounter=1;
    final int id;
    double defaultWalkingCost = 1;
    double fertilityLevel;

    public Land(LandType landType, double fertilityLevel) {
        this.id = idCounter++;
        this.landType = landType;
        this.fertilityLevel = fertilityLevel;
    }

    public Land(LandType landType) {
        this(landType, 0);
    }
    
    public int getId(){
        return id;
    }

    public abstract void applyEffects();

    public LandType getLandType() {
        return landType;
    }

    public void setLandType(LandType landType) {
        this.landType = landType;
    }

    public abstract double getTerrainWalkingCost();

    public abstract boolean isWalkable();

    public abstract Color getColor();

    public double getFertilityLevel(){
        return fertilityLevel;
    };

}
