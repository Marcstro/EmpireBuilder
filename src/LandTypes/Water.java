package LandTypes;


import LandTypes.LandType;
import java.awt.Color;

public class Water extends Land {
    
    LandType landType = LandType.WATER;
    boolean isWalkable = false;

    public Water() {
        super(LandType.WATER);
    }

    @Override
    public void applyEffects() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Color getColor() {
        return LandType.getBaseColor(landType);
    }

    @Override
    public double getTerrainWalkingCost() {
        return defaultWalkingCost;
    }

    @Override
    public boolean isWalkable(){
        return isWalkable;
    }
    
}