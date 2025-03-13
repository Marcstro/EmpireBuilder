package buildings;

import empirebuilder.Point;
import java.util.LinkedList;


public class Town extends Building{

    LinkedList<Village> villages;
    
    public Town(Point point) {
        super(point);
        villages = new LinkedList<>();
    }

    @Override
    public String getInfo() {
        return "Town: " + getId() +
                ", point="+getPoint().getPositionString() +
                ", villages=" + getVillages().size()
                + ".";
    }
    
    public void addVillage(Village village){
        villages.add(village);
    }

    public LinkedList<Village> getVillages() {
        return villages;
    }

    public void setVillages(LinkedList<Village> villages) {
        this.villages = villages;
    }
}