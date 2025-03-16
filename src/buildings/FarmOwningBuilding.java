package buildings;

import empirebuilder.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class FarmOwningBuilding extends Building{
    
    LinkedList<Farm> farms;
    List<Point> controlledLand;
    LinkedList<Point> emptyLand;

    public FarmOwningBuilding(Point point) {
        super(point);
        farms = new LinkedList();
        controlledLand = new ArrayList();
        emptyLand = new LinkedList();
    }

    @Override
    public String getInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}