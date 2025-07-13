package buildings;

import LandTypes.LandType;
import empirebuilder.Point;

public class CityArea extends Building{

    City cityCenter;

    public CityArea(Point point, City cityCenter) {
        super(point, LandType.getBaseColor(LandType.CITY));
        this.cityCenter = cityCenter;
    }

    @Override
    public String getInfo() {
        return "pos: " + getPoint().getPositionString()
                + "CityCenter: " + cityCenter.getPoint().getPositionString()
                + ".";    }
}
