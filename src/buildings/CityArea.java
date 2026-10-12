package buildings;

import LandTypes.LandType;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Point;

public class CityArea extends AreaBuilding {
    
    City cityCenter;

    public CityArea(Point point, City cityCenter) {
        super(point, LandType.getBaseColor(LandType.CITY), DEFAULT_BUILDING_HEALTH);
        this.cityCenter = cityCenter;
        this.owner = cityCenter;
    }

    @Override
    public String getImagePath() {
        return "/resources/images/CityImage4.png";
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
    protected void registerSubBuilding(SubBuilding subBuilding) {
        cityCenter.addSubBuilding(subBuilding);
    }

    @Override
    protected void unregisterSubBuilding(SubBuilding subBuilding) {
        cityCenter.removeSubBuilding(subBuilding);
    }

    @Override
    public Building getMainOwner() {
        return cityCenter;
    }

    @Override
    public String getInfo() {
        StringBuilder info = new StringBuilder("CityArea{ pos=")
                .append(getPoint().getPositionString())
                .append(", CityCenter=").append(cityCenter.getPoint().getPositionString())
                .append(", isAlive=").append(isAlive())
                .append(", health=").append(String.format("%.1f", getHealth()))
                .append(", healthMax=").append(DEFAULT_BUILDING_HEALTH);
        appendSubBuildingInfo(info);
        info.append(" }");
        return info.toString();
    }
}
