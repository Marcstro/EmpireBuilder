package LandTypes;


import LandTypes.LandType;
import java.awt.Color;

public class Dirt extends Land {
    
    LandType landType = LandType.DIRT;

    public Dirt() {
        super(LandType.DIRT);
    }

    @Override
    public void applyEffects() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Color getColor() {
        return LandType.getBaseColor(landType);
    }
    
}