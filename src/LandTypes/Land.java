package LandTypes;

import LandTypes.LandType;
import java.awt.Color;

public abstract class Land {
    protected LandType landType;
    int wealth;
    int danger;
    int idCounter=1;
    final int id;

    public Land(LandType landType) {
        this.id = idCounter++;
        this.landType = landType;
    }
    
    public int getId(){
        return id;
    }

    public abstract void applyEffects();

    public LandType getLandType() {
        return landType;
    }

    public void setLandType(LandType landType) {
        this.landType = landType;
    }
    
    

    public abstract Color getColor();

}
