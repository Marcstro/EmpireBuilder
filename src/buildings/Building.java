package buildings;

import empirebuilder.Point;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public abstract class Building{
    
    private static int idCounter=1;
    private final int id;
    private Point point;
    private Color color;

    private static final Map<Class<? extends Building>, BufferedImage> imageCache = new HashMap<>();

    // TODO this is necessary to initialise buildings in order to get getImage(), maybe find another workaround
    protected Building() {
        this.id = idCounter++;
    }

    public Building(Point point, Color color){
        this.id = idCounter++;
        this.point = point;
        this.color = color;
    }

    // Default, TODO create default image to displaying missing images
    public String getImagePath() {
         return "/resources/images/farmImage.png";
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