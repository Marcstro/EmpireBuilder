package LandTypes;

import LandTypes.Land;
import LandTypes.LandType;
import java.awt.Color;



class TownLand extends Land{

    LandType landType = LandType.TOWN;

    public TownLand() {
        super(LandType.VILLAGE);
    }

    @Override
    public void applyEffects() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Color getColor() {
        return Color.RED;
    }
    
}