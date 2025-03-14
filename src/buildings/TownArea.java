package buildings;

import empirebuilder.Point;

public class TownArea extends Building{
    
    Town townCenter;

    public TownArea(Point point, Town townCenter) {
        super(point);
        this.townCenter = townCenter;
    }

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