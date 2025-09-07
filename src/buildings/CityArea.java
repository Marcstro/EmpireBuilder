package buildings;

import LandTypes.LandType;
import empirebuilder.Point;

public class CityArea extends Building{

    City cityCenter;

    public CityArea(Point point, City cityCenter) {
        super(point, LandType.getBaseColor(LandType.CITY));
        this.cityCenter = cityCenter;
    }

    public CityArea() {
        super();
    }

    @Override
    public String getImagePath() {
        return "/resources/images/CityImage4.png";
    }

    @Override
    public String getInfo() {
        return "pos: " + getPoint().getPositionString()
                + "CityCenter: " + cityCenter.getPoint().getPositionString()
                + ".";    }
}
