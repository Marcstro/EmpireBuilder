package LandTypes;

import java.awt.*;

public class HillLand extends Land {
    LandType landType = LandType.HILL;
    boolean isWalkable = true;
    int walkingCost = 3;

    public HillLand() {
        super(LandType.HILL);
    }


    @Override
    public void applyEffects() {

    }

    @Override
    public double getTerrainWalkingCost() {
        return walkingCost;
    }

    @Override
    public boolean isWalkable() {
        return isWalkable;
    }

    @Override
    public Color getColor() {
        return LandType.getBaseColor(landType);
    }
}
