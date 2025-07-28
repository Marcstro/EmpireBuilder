package LandTypes;


import LandTypes.LandType;
import java.awt.Color;

public class VillageLand extends Land {
    
    LandType landType = LandType.VILLAGE;
    boolean isWalkable = false;
    int walkingCost = 0;

    public VillageLand() {
        super(LandType.VILLAGE);
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
        return walkingCost;
    }

    @Override
    public boolean isWalkable(){
        return isWalkable;
    }
    
}