package buildings;

import empirebuilder.Point;

import java.awt.*;

public abstract class Building{
    
    private static int idCounter=1;
    private final int id;
    private Point point;
    private Color color;
    
    public Building(Point point, Color color){
        this.id = idCounter++;
        this.point=point;
        this.color = color;
    }
    
    public Point getPoint(){
        return point;
    }
    
    public int getId(){
        return id;
    }

    public Color getColor(){
        return color;
    };

    public void setColor(Color color){
        this.color = color;
    };
    
    public abstract String getInfo();
    
}