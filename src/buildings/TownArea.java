package buildings;

import LandTypes.LandType;
import empirebuilder.Point;

import java.awt.*;

public class TownArea extends Building{
    
    Town townCenter;

    public TownArea(Point point, Town townCenter) {
        super(point, LandType.getBaseColor(LandType.TOWN));
        this.townCenter = townCenter;
    }

    public TownArea(){
        super();
    }

    @Override
    public String getImagePath() {
        return "/resources/images/TownImage.png";
    }

    //Override
    //public Color getColor() {
    //    return LandType.getBaseColor(LandType.TOWN);
    //}

    @Override
    public String getInfo() {
        return "pos: " + getPoint().getPositionString() 
                + "TownCenter: " + townCenter.getPoint().getPositionString()
                + ".";
    }

    public Town getTownCenter() {
        return townCenter;
    }

    public void setTownCenter(Town townCenter) {
        this.townCenter = townCenter;
    }
}