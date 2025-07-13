package LandTypes;


import LandTypes.LandType;

public class LandFactory {
    public static Land createLand(LandType type) {
        return switch (type) {
            case GRASSLAND -> new Grassland();
            case WATER -> new Water();
            case DIRT -> new Dirt();
            case RUINED -> new Ruined();
            case VILLAGE -> new VillageLand();
            case TOWN -> new TownLand();
            case CITY -> new CityLand();
        };
    }
}