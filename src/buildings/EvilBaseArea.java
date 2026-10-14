package buildings;

import LandTypes.LandType;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Point;

public class EvilBaseArea extends AreaBuilding {

    EvilBase evilBase;

    public EvilBaseArea(Point point, EvilBase evilBase) {
        super(point, LandType.getBaseColor(LandType.RUINED), 300);
        this.evilBase = evilBase;
        setFactionId(EvilBase.EVIL_FACTION_ID);
    }

    @Override
    public String getImageName() {
        return "barrenLand";
    }

    @Override
    protected void registerSubBuilding(SubBuilding subBuilding) {
        evilBase.addSubBuilding(subBuilding);
    }

    @Override
    protected void unregisterSubBuilding(SubBuilding subBuilding) {
        evilBase.removeSubBuilding(subBuilding);
    }

    @Override
    public Building getMainOwner() {
        return evilBase;
    }

    @Override
    public String getInfo() {
        StringBuilder info = new StringBuilder("{EvilBaseArea" +
                ", isAlive=" + isAlive() +
                ", health=" + String.format("%.1f", getHealth()) +
                ", healthMax=300.0" +
                ", base@=" + evilBase.getPoint().getPositionString());
        appendSubBuildingInfo(info);
        info.append("}");
        return info.toString();
    }
}
