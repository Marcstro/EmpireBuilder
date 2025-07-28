package LandTypes;

import java.awt.*;

public class MountainLand extends Land {
    LandType landType = LandType.MOUNTAIN;
    boolean isWalkable = false;

    public MountainLand() {
        super(LandType.MOUNTAIN);
    }


    @Override
    public void applyEffects() {

    }

    @Override
    public double getTerrainWalkingCost() {
        return 0;
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
