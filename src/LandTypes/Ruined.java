package LandTypes;


import LandTypes.LandType;
import java.awt.Color;

public class Ruined extends Land {
    
    LandType landType = LandType.RUINED;
    boolean isWalkable = true;
    int walkingCost = 3;

    public Ruined() {
        super(LandType.RUINED);
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