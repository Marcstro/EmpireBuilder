package LandTypes;


import LandTypes.LandType;
import java.awt.Color;

public class VillageLand extends Land {
    
    LandType landType = LandType.VILLAGE;
    boolean isWalkable = false;
    int walkingCost = 0;
    static final double villageLandFertilitylevel = 1.0;

    public VillageLand() {
        super(LandType.VILLAGE, villageLandFertilitylevel);
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