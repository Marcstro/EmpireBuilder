package LandTypes;

import java.awt.Color;

public enum LandType {
    GRASSLAND,
    WATER,
    DIRT,
    RUINED,
    VILLAGE,
    TOWN,
    CITY;
    
    public static Color getBaseColor(LandType type) {
        return switch (type) {
            case GRASSLAND -> new Color(162, 228, 162);
            case WATER -> Color.blue;
            case DIRT -> new Color(129, 64, 10);
            case RUINED -> new Color(65, 36, 12);
            case VILLAGE -> Color.blue;
            case TOWN -> Color.RED;
            case CITY -> new Color( 188, 86, 220 );
        };
    }
}
