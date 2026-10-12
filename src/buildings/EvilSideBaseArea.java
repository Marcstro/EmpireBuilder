package buildings;

import LandTypes.LandType;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Point;

public class EvilSideBaseArea extends AreaBuilding {

    EvilSideBase evilSideBase;

    public EvilSideBaseArea(Point point, EvilSideBase evilSideBase) {
        super(point, LandType.getBaseColor(LandType.RUINED), 300);
        this.evilSideBase = evilSideBase;
        setFactionId(EvilSideBase.EVIL_FACTION_ID);
    }

    @Override
    public String getImageName() {
        return "barrenLand";
    }

    @Override
    protected void registerSubBuilding(SubBuilding subBuilding) {
        evilSideBase.addSubBuilding(subBuilding);
    }

    @Override
    protected void unregisterSubBuilding(SubBuilding subBuilding) {
        evilSideBase.removeSubBuilding(subBuilding);
    }

    @Override
    public Building getMainOwner() {
        return evilSideBase;
    }

    @Override
    public String getInfo() {
        StringBuilder info = new StringBuilder("{EvilSideBaseArea" +
                ", isAlive=" + isAlive() +
                ", health=" + String.format("%.1f", getHealth()) +
                ", healthMax=300.0" +
                ", base@=" + evilSideBase.getPoint().getPositionString());
        appendSubBuildingInfo(info);
        info.append("}");
        return info.toString();
    }
}
