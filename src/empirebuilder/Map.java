package empirebuilder;

import LandTypes.LandType;
import LandTypes.Grassland;
import LandTypes.Land;
import buildings.Building;
import buildings.Farm;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import math.CircleSearch;

public class Map {
    
    GameManager gameManager;
        
    int width;
    int height;
    private final Point[][] grid;
    Set<Point> emptyPoints;
    private List<Point> emptyPointList;
    Random random;
    CircleSearch circleSearch;
    final int FARM_EXTEND_DISTANCE = 10;

    public Map(GameManager gameManager, int pixelWidth, int pixelHeight) {
        
        this.gameManager = gameManager;
        width = pixelWidth;
        height = pixelHeight;
        
        grid = new Point[width][height];
        random = new Random();
        this.circleSearch = new CircleSearch(FARM_EXTEND_DISTANCE, width, height);

        emptyPoints = new HashSet();
        emptyPointList = new LinkedList();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = new Point(x, y, LandType.DIRT);
                emptyPoints.add(grid[x][y]);
                emptyPointList.add(grid[x][y]);
            }
        }
        Collections.shuffle(emptyPointList);
    }
    
    public void setPoint(Point point){
        grid[point.getX()][point.getY()]=point;
    }

    public Point getPoint(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return grid[x][y];
        }
        return null;
    }
    
    public Point getRandomEmptyPoint() {
//        if(emptyPoints.isEmpty()){
//            return null;
//        }
//        return emptyPoints.iterator().next();
        if (emptyPointList.isEmpty()) return null;

        int index = new Random().nextInt(emptyPointList.size());
        return emptyPointList.get(index);
    }
    
    public void buildingHasBeenRemovedAtPoint(Point point){
        emptyPoints.add(point);
        emptyPoints.add(point);
    }
    
    public void setBuildingOnPoint(Point point, Building building){
        point.setBuilding(building);
        emptyPoints.remove(point);
        emptyPointList.remove(point);
    }
    
    public Point findNeighboringSpotForFarm(int x, int y) {
        for (int radius = 1; radius <= FARM_EXTEND_DISTANCE; radius++) {
            ArrayList<int[]> possiblePoints = new ArrayList<>(circleSearch.getPositionsAroundTargetInCircle(x, y, radius));

            possiblePoints.removeIf(p -> !grid[p[0]][p[1]].isEmpty());

            if (!possiblePoints.isEmpty()) {
                Collections.shuffle(possiblePoints, random);

                int[] chosen = possiblePoints.get(0);
                return grid[chosen[0]][chosen[1]];
            }
        }
        return null;
    }
    
    public int independentFarmsNearby(Point point, int radius){
        LinkedList<Point> allPoints = getAllPointsInCircleAroundTarget(point, radius);
        return (int) allPoints.stream()
            .map(Point::getBuilding)  
            .filter(building -> building instanceof Farm)
            .map(building -> (Farm) building) 
            .filter(farm -> !farm.hasVillage())
            .count(); 
    }
    
    public LinkedList<Farm> getIndependentFarmsNearby(Point point, int radius) {
        return getAllPointsInCircleAroundTarget(point, radius).stream()
            .map(Point::getBuilding)
            .filter(building -> building instanceof Farm)
            .map(building -> (Farm) building)
            .filter(farm -> !farm.hasVillage())
            .collect(Collectors.toCollection(LinkedList::new));
    }
    
    
    public LinkedList<Point> getAllPointsInCircleAroundTarget(Point originalPoint, int radius){
                return circleSearch.getAllPositionsAroundTargetInCircle(
                originalPoint.getX(), originalPoint.getY(), radius).stream()
                .map(pos -> grid[pos[0]][pos[1]])
                .collect(Collectors.toCollection(LinkedList::new));
    }
    
    public List<Point> getTownShapePointList(int centerX, int centerY) {
        List<Point> result = new ArrayList<>();

        for (int[] offset : circleSearch.getTownShapePointList()) {
            int newX = centerX + offset[0];
            int newY = centerY + offset[1];

            if (isValid(newX, newY)) {
                result.add(grid[newX][newY]);
            }
        }

        return result;
    }    
    
    public LinkedList<Point> getAllEmptyPointsInCircleAroundTarget(Point originalPoint, int radius){
        return getAllPointsInCircleAroundTarget(originalPoint, radius).stream()
            .filter(Point::isEmpty)
            .collect(Collectors.toCollection(LinkedList::new));
    }
    
    public List<Point> getAllValidAdjecantPointsToTarget(Point originalPoint){
        return circleSearch.getValidAdjacentPoints(originalPoint.getX(), originalPoint.getY()).stream()
        .filter(pos -> isValid(pos[0], pos[1]))
        .map(pos -> grid[pos[0]][pos[1]])
        .collect(Collectors.toList());           
    }
    
    public Point getRandomEmptyPointAdjecantToTarget(Point originalPoint){
        LinkedList<int[]> emptyPoints = circleSearch.getValidAdjacentPoints(originalPoint.getX(), originalPoint.getY()).stream()
            .filter(p -> grid[p[0]][p[1]].isEmpty())
            .collect(Collectors.toCollection(LinkedList::new));


        if (emptyPoints.isEmpty()) return null;

        Collections.shuffle(emptyPoints, random);
        int[] chosen = emptyPoints.get(0);

        return grid[chosen[0]][chosen[1]]; 
    }
    
    
    
 public ArrayList<Point> getPointsInCircleAroundTarget(Point originalPoint, int radius) {
    ArrayList<Point> result = new ArrayList<>();
    
    // Get relative positions for the given radius
    ArrayList<int[]> relativePositions = new ArrayList<>(circleSearch.getPositionsAroundTargetInCircle(originalPoint.getX(), originalPoint.getY(), radius));

    // Convert coordinates to actual Point objects
    for (int[] pos : relativePositions) {
        result.add(grid[pos[0]][pos[1]]);
    }

    return result;
}


    public void setLandTypeAtPoint(int x, int y, LandType landType){
        
        grid[x][y].createNewLandForPoint(landType);
    }
    
    public Point getRandomPoint(){
        int randomX = random.nextInt(width);
        int randomY = random.nextInt(height);
        return grid[randomX][randomY];
    }



    public Point[][] getGrid() {
        return grid;
    }
    
    public Point getClosestEmptyPoint(int x, int y){
        
        // TODO here, create a list, or array, of the squares around the position. Then shuffle the order. then check one after the other
        
        int[] dx = {-1, -1, -1,  0,  0,  1,  1,  1};
        int[] dy = {-1,  0,  1, -1,  1, -1,  0,  1};

        for (int i = 0; i < 8; i++) {
            int nx = x + dx[i]; // New x position
            int ny = y + dy[i]; // New y position

            // Check if the position is within bounds
            if (isValid(nx, ny)) {
                Point neighbor = grid[nx][ny];
                // Do something with the neighbor (e.g., check if it's empty)
                if (neighbor.isEmpty()) {
                    return neighbor;
                }
            }
        }
        return null;
    }
    
    public Point getClosestEmptyPointWithinRadius(int x, int y, int distance){
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{x, y});

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int px = pos[0], py = pos[1];

            for (int dx = -distance; dx <= distance; dx++) {
                for (int dy = -distance; dy <= distance; dy++) {
                    int newX = px + dx;
                    int newY = py + dy;

                    // Only check if within the given radius (true circle check)
                    if (isValid(newX, newY) && !grid[newX][newY].equals(grid[px][py])) {
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist <= distance && grid[newX][newY].isEmpty()) {
                            return grid[newX][newY]; // Found closest empty point
                        }
                        queue.add(new int[]{newX, newY});
                    }
                }
            }
        }
        return null; // No empty space found within distance
    }
    
    public boolean isValidAndEmpty(int x, int y){
        return grid[x][y].isEmpty() && (x >= 0 && x < width && y >= 0 && y < height);
    }
    
    private boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    public void tick(){
        
    }
    
    /***
     * Deprecated
     */
    public void oldTick(){

        for (int x = 0; x < 350; x++) {
            for (int y = 0; y < 200; y++) {
                Point p = grid[x][y];
                Land land = p.getLand();

                if (p.getLandType() == LandType.DIRT) {
                    int adjacentGrasslands = 0;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (dx == 0 && dy == 0) continue;

                            int nx = x + dx;
                            int ny = y + dy;

                            if (nx >= 0 && nx < 350 && ny >= 0 && ny < 200) {
                                Point neighbor = grid[nx][ny];
                                if (neighbor.getLand() instanceof Grassland neighborGrassland) {
                                    //if (neighborGrassland.getFertilityLevel() >= fertilityLevel) {
                                        adjacentGrasslands++;
                                    //}
                                }
                            }
                        }
                    }
                    double probability = (Math.pow(adjacentGrasslands+1, 5)) / 10000.0;
                    if (random.nextDouble() < probability) {
                        grid[x][y] = new Point(x, y, LandType.GRASSLAND);
                    }
                    continue; // Skip the rest of the loop for this point
                }
                if (p.getBuilding() instanceof Farm farm) {
                    int fertilityLevel = farm.getFertilityLevel();
                    int adjacentGrasslands = 0;

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (dx == 0 && dy == 0) continue; // Skip the current point
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx >= 0 && nx < 350 && ny >= 0 && ny < 200) {
                                Point neighbor = grid[nx][ny];
                                if (neighbor.getLand() instanceof Grassland neighborGrassland) {
                                    //if (neighborGrassland.getFertilityLevel() >= fertilityLevel) {
                                        adjacentGrasslands++;
                                    //}
                                }
                            }
                        }
                    }
                    double probability = (Math.pow(adjacentGrasslands, 2)) / 100.0;

                    if (random.nextDouble() < probability) {
                        farm.improveFertility();
                    }
                }
            }
        }
    }
}
