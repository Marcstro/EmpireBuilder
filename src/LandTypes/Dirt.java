package LandTypes;


import LandTypes.LandType;
import java.awt.Color;

public class Dirt extends Land {
    
    LandType landType = LandType.DIRT;
    boolean isWalkable = true;
    static final double dirtFertilityLevel = 0.7;

    public Dirt() {
        super(LandType.DIRT, dirtFertilityLevel);
    }

    @Override
    public void applyEffects() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getTerrainWalkingCost() {
        return defaultWalkingCost;
    }

    @Override
    public boolean isWalkable(){
        return isWalkable;
    }

    @Override
    public Color getColor() {
        return LandType.getBaseColor(landType);
    }
    
}