package buildingsTools;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class FarmFertilityColors {
    private static final Map<Integer, Color> fertilityColors = new HashMap<>();

    static {
        fertilityColors.put(1, new Color(182, 215, 0));  //TODO fix better colors these are sheit
        fertilityColors.put(2, new Color(144, 238, 144));
        fertilityColors.put(3, new Color(34, 139, 34));
        fertilityColors.put(4, new Color(0, 100, 0));
        fertilityColors.put(5, new Color(0, 200, 0));
    }

    public static Color getColor(int fertilityLevel) {
        return fertilityColors.getOrDefault(fertilityLevel, Color.GRAY);
    }
}