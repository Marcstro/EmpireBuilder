package buildings;

import LandTypes.LandType;
import empirebuilder.Game;
import empirebuilder.Point;

public class CityArea extends Building{

    City cityCenter;

    public CityArea(Point point, City cityCenter) {
        super(point, LandType.getBaseColor(LandType.CITY), DEFAULT_BUILDING_HEALTH);
        this.cityCenter = cityCenter;
        owner=cityCenter;
    }

    @Override
    public String getImagePath() {
        return "/resources/images/CityImage4.png";
    }

    @Override
    public void tick(Game game) {

    }

    public City getCityCenter() {
        return cityCenter;
    }

    public void setCityCenter(City cityCenter) {
        this.cityCenter = cityCenter;
    }

    public String getImageName(){
        return "city";
    }

    @Override
    public String getInfo() {
        return "pos: " + getPoint().getPositionString()
                + "CityCenter: " + cityCenter.getPoint().getPositionString()
                + ".";    }
}
