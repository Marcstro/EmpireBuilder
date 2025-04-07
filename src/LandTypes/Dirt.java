package LandTypes;


import LandTypes.LandType;
import java.awt.Color;

public class Dirt extends Land {
    
    LandType landType = LandType.DIRT;
    double terrainWalkingCost = 1;
    boolean isWalkable = true;

    public Dirt() {
        super(LandType.DIRT);
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