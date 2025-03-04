package LandTypes;


import LandTypes.LandType;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public class Grassland extends Land {
    
    int fertilityLevel;
    ArrayList<Color> fertilityLevelColor;
    Color color;
    
    public Grassland() {
        super(LandType.GRASSLAND);
        this.fertilityLevelColor = new ArrayList<>(Arrays.asList(
            new Color(162, 228, 162), 
            Color.pink,//new Color(143, 228, 143), 
            new Color(116, 230, 116), 
            new Color(79, 226, 79), 
            new Color(8, 177, 8)
        ));
        fertilityLevel = 1;
        color = LandType.getBaseColor(LandType.GRASSLAND);
    }

    @Override
    public void applyEffects() {
        
    }
    
    public void improveFertility(){
        if(fertilityLevel != 5){
            fertilityLevel++;
            color = fertilityLevelColor.get(fertilityLevel-1);
        }
    }

    public void setFertilityLevel(int fertilityLevel) {
        this.fertilityLevel = fertilityLevel;
    }
    
    
    
    public int getFertilityLevel(){
        return fertilityLevel;
    }

    @Override
    public Color getColor() {
        return color;
    }
}