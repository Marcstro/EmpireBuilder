package LandTypes;


import LandTypes.LandType;
import java.awt.Color;

public class VillageLand extends Land {
    
    LandType landType = LandType.VILLAGE;

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
    
}