package buildings;

import LandTypes.LandType;
import empirebuilder.Game;
import empirebuilder.Point;

import java.awt.*;

public class TownArea extends Building{
    
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
    public void tick(Game game) {

    }

    public String getImageName(){
        return "town";
    }

    @Override
    public String getInfo() {
        return "TownArea{ TownCenter=" + townCenter.getPoint().getPositionString()
                + "}";
    }

    public Town getTownCenter() {
        return townCenter;
    }

    public void setTownCenter(Town townCenter) {
        this.townCenter = townCenter;
    }
}