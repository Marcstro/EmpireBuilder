package LandTypes;

import java.awt.Color;

public enum LandType {
    GRASSLAND,
    WATER,
    DIRT,
    RUINED,
    VILLAGE,
    TOWN,
    CITY,
    MOUNTAIN,
    HILL;
    
    public static Color getBaseColor(LandType type) {
        return switch (type) {
            case GRASSLAND -> new Color(34, 139, 34);//(162, 228, 162);
            case WATER -> new Color(30, 144, 255);//(102, 221, 236);
            case DIRT -> new Color(129, 64, 10);
            case RUINED -> new Color(65, 36, 12);
            case VILLAGE -> new Color(255, 215, 0);// Color.blue;
            case TOWN -> new Color(220, 20, 60);//Color.RED;
            case CITY -> new Color(128, 0, 128);//( 188, 86, 220 );
            case MOUNTAIN -> new Color(169, 169, 169);//(138, 135, 135);
            case HILL -> new Color(85, 107, 47);//(218, 234, 151);
        };
    }
}
