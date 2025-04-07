package LandTypes;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public class Grassland extends Land {
    
    ArrayList<Color> fertilityLevelColor;
    Color color;
    boolean isWalkable = true;
    
    public Grassland() {
        super(LandType.GRASSLAND);
        this.fertilityLevelColor = new ArrayList<>(Arrays.asList(
            new Color(162, 228, 162), 
            new Color(143, 228, 143),//Color.pink,//new Color(143, 228, 143), 
            new Color(116, 230, 116), 
            new Color(79, 226, 79), 
            new Color(8, 177, 8)
        ));
        color = LandType.getBaseColor(LandType.GRASSLAND);
    }

    @Override
    public void applyEffects() {
        
    }

    @Override
    public double getTerrainWalkingCost() {
        return defaultWalkingCost;
    }

    @Override
    public boolean isWalkable(){
        return isWalkable;
    }

    
    public void updateColor(int fertilityLevel){
         color = fertilityLevelColor.get(fertilityLevel-1);
    }

    @Override
    public Color getColor() {
        return color;
    }
}