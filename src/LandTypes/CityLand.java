package LandTypes;

import java.awt.*;

public class CityLand extends Land{

    LandType landType = LandType.CITY;
    static final double cityFertilityLevel = 1.0;

    public CityLand() {
        super(LandType.CITY, cityFertilityLevel);
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
        return true;
    }

    @Override
    public Color getColor() {
        return LandType.getBaseColor(landType);
    }
}
