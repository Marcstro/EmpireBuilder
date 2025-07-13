package LandTypes;

import java.awt.*;

public class CityLand extends Land{

    LandType landType = LandType.CITY;

    public CityLand() {
        super(LandType.CITY);
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
        return false;
    }

    @Override
    public Color getColor() {
        return LandType.getBaseColor(landType);
    }
}
