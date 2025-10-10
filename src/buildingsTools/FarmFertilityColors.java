package buildingsTools;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class FarmFertilityColors {
    private static final Map<Integer, Color> fertilityColors = new HashMap<>();

    static {
        fertilityColors.put(1, new Color(160, 140, 90));  //TODO fix better colors these are sheit
        fertilityColors.put(2, new Color(139, 101, 79));
        fertilityColors.put(3, new Color(107, 156, 75));
        fertilityColors.put(4, new Color(85, 139, 47));
        fertilityColors.put(5, new Color(46, 100, 24));
    }

    public static Color getColor(int fertilityLevel) {
        return fertilityColors.getOrDefault(fertilityLevel, Color.GRAY);
    }
}