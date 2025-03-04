package buildings;

import empirebuilder.Point;

public abstract class Building{
    
    private static int idCounter=1;
    private final int id;
    private Point point;
    
    public Building(Point point){
        this.id = idCounter++;
        this.point=point;
    }
    
    public Point getPoint(){
        return point;
    }
    
    public int getId(){
        return id;
    }
    
    public abstract String getInfo();
    
}