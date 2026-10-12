package buildings;

import LandTypes.LandType;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Point;

public class TownArea extends AreaBuilding {
    
    Town townCenter;

    public TownArea(Point point, Town townCenter) {
        super(point, LandType.getBaseColor(LandType.TOWN), DEFAULT_BUILDING_HEALTH);
        this.townCenter = townCenter;
    }

    @Override
    public String getImagePath() {
        return "/resources/images/TownImage.png";
    }

    @Override
    public String getImageName() {
        return "town";
    }

    @Override
    public String getInfo() {
        StringBuilder info = new StringBuilder("TownArea{ TownCenter=")
                .append(townCenter.getPoint().getPositionString())
                .append(", isAlive=").append(isAlive())
                .append(", health=").append(String.format("%.1f", getHealth()))
                .append(", healthMax=").append(DEFAULT_BUILDING_HEALTH);
        appendSubBuildingInfo(info);
        info.append(" }");
        return info.toString();
    }

    public Town getTownCenter() {
        return townCenter;
    }

    @Override
    protected void registerSubBuilding(SubBuilding subBuilding) {
        townCenter.addSubBuilding(subBuilding);
    }

    @Override
    protected void unregisterSubBuilding(SubBuilding subBuilding) {
        townCenter.removeSubBuilding(subBuilding);
    }

    @Override
    public Building getMainOwner() {
        return townCenter;
    }

    public void setTownCenter(Town townCenter) {
        this.townCenter = townCenter;
    }
}