package buildings;

import empirebuilder.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class Village extends Building{
    
    LinkedList<Farm> farms;
    List<Point> controlledLand;
    LinkedList<Point> emptyLand;
    Point villageCenter;
    int food;
    
    public Village(Point point, Point villageCenter) {
        super(point);
        farms = new LinkedList();
        controlledLand = new ArrayList();
        emptyLand = new LinkedList();
        this.villageCenter = villageCenter;
        food = 0;
    }
    
    public void increaseFood(int foodAdd){
        food+=foodAdd;
    }
    
    public int getFood(){
        return food;
    }

    public Point getVillageCenter() {
        return villageCenter;
    }

    public void setVillageCenter(Point villageCenter) {
        this.villageCenter = villageCenter;
    }
    
    public void addEmptyPoint(Point point){
        emptyLand.add(point);
        Collections.shuffle(emptyLand);
    }

    public LinkedList<Farm> getFarms() {
        return farms;
    }

    public void setFarms(LinkedList<Farm> farms) {
        this.farms = farms;
    }
    
    public void addFarm(Farm farm){
        farms.add(farm);
    }

    public List<Point> getControlledLand() {
        return controlledLand;
    }

    public void setControlledLand(List<Point> controlledLand) {
        this.controlledLand = controlledLand;
    }

    public LinkedList<Point> getEmptyLand() {
        return emptyLand;
    }

    public void setEmptyLand(LinkedList<Point> emptyLand) {
        this.emptyLand = emptyLand;
    }
    
    public Point getRandomEmptySpotWithinDomain(){
        return emptyLand.pollFirst();
    }

    @Override
    public String toString() {
        return "666";
//                "Village{" + "farms=" + farms +
//                ", controlledLand=" + controlledLand +
//                ", emptyLand=" + emptyLand +
//                ", villageCenter=" + villageCenter +
//                ", food=" + food + '}';
    }
    
    
    
    @Override
    public String getInfo(){
                return "Village{" + "amount of farms controlled: " + getFarms().size() +
                ", emptyLand remaining: " + emptyLand.size() +
                ", villageCenter=" + villageCenter.getX() + "," + villageCenter.getY() +
                ", food=" + food + '}';
    }
    
}