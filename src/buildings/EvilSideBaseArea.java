package buildings;

import LandTypes.LandType;
import empirebuilder.Game;
import empirebuilder.Point;

import java.awt.*;

public class EvilSideBaseArea extends Building{

    EvilSideBase evilSideBase;

    public EvilSideBaseArea(Point point, EvilSideBase evilSideBase) {
        super(point, LandType.getBaseColor(LandType.RUINED),300);
        this.evilSideBase = evilSideBase;
    }

    @Override
    public void tick(Game game) {

    }

    @Override
    public String getImageName() {
        return "barrenLand";
    }

    @Override
    public String getInfo() {
        return "{EvilSideBaseArea" +
                ", isAlive=" + isAlive() +
                ", health=" + getHealth() +
                ", base@=" + evilSideBase.getPoint().getPositionString() +
                "}";
    }
}
