package empirebuilder;

import LandTypes.*;
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

import buildingsTools.TerrainGenerator;
import math.CircleSearch;

public class Map {
    
    GameManager gameManager;
        
    int width;
    int height;
    private final Point[][] grid;
    private Set<Point> emptyPoints;
    private List<Point> emptyPointList;
    Random random;
    CircleSearch circleSearch;
    final int FARM_EXTEND_DISTANCE = 10;
    private TerrainGenerator terrainGenerator;

    public Map(GameManager gameManager, int pixelWidth, int pixelHeight) {
        
        this.gameManager = gameManager;
        width = pixelWidth;
        height = pixelHeight;
        
        grid = new Point[width][height];
        random = new Random();
        this.circleSearch = new CircleSearch(FARM_EXTEND_DISTANCE, width, height);

        emptyPoints = new HashSet();
        emptyPointList = new LinkedList();

        if(gameManager.getWorldSettings().isGenerateTerrain()){
            terrainGenerator = new TerrainGenerator(this, gameManager.getWorldSettings().getGeneratorType());
            terrainGenerator.generateTerrain();
        }
        else {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    grid[x][y] = new Point(x, y, LandType.DIRT);
                    emptyPoints.add(grid[x][y]);
                    emptyPointList.add(grid[x][y]);
                }
            }
        }

        Collections.shuffle(emptyPointList);
    }

    public List<Point> getAllValidNeighbors(Point point) {
        List<Point> neighbors = new ArrayList<>();
        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

        for (int i = 0; i < 8; i++) {
            int nx = point.getX() + dx[i];
            int ny = point.getY() + dy[i];
            if (isValid(nx, ny)) {
                neighbors.add(grid[nx][ny]);
            }
        }
        return neighbors;
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

    public Set<Point> getEmptyPoints() {
        return emptyPoints;
    }

    public List<Point> getEmptyPointList() {
        return emptyPointList;
    }
    
    public Point getRandomEmptyPoint() {
        // TODO don't pick an entirely random point, only pick among points not owned by buildings
        if (emptyPointList.isEmpty()) return null;

        int index = new Random().nextInt(emptyPointList.size());
        return emptyPointList.get(index);
    }

    public void replaceBuilding(Point point, Building building){
        removeBuildingFromPoint(point);
        setBuildingOnPoint(point, building);
    }
    
    public void setBuildingOnPoint(Point point, Building building){

        if (point.getBuilding() != null){
            if (point.getBuilding() instanceof Farm farm){
                if (farm.belongsToFarmOwningBuilding()) {
                    farm.getFarmOwningBuilding().destroyFarm(farm);
                }
            }
            removeBuildingFromPoint(point);
        }
        point.setBuilding(building);
        if (emptyPoints.contains(point)){
            emptyPoints.remove(point);
            emptyPointList.remove(point);
        }
    }

    public void removeBuildingFromPoint(Point point){
        if (point.getBuilding() == null){
            throw new RuntimeException("Tried to remove building at " + point.getPositionString() + " but no building there!");
        }
        point.setBuilding(null);
        emptyPoints.add(point);
        emptyPointList.add(point);
    }
    
    public Point findNeighboringSpotForFarm(int x, int y) {
        for (int radius = 1; radius <= FARM_EXTEND_DISTANCE; radius++) {
            ArrayList<int[]> possiblePoints = new ArrayList<>(circleSearch.getSingleLinePositionsAroundTargetInCircle(x, y, radius));

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
            .filter(farm -> !farm.belongsToFarmOwningBuilding())
            .count(); 
    }
    
    public LinkedList<Farm> getIndependentFarmsNearby(Point point, int radius) {
        return getAllPointsInCircleAroundTarget(point, radius).stream()
            .map(Point::getBuilding)
            .filter(building -> building instanceof Farm)
            .map(building -> (Farm) building)
            .filter(farm -> !farm.belongsToFarmOwningBuilding())
            .collect(Collectors.toCollection(LinkedList::new));
    }
    
    
    public LinkedList<Point> getAllPointsInCircleAroundTarget(Point originalPoint, int radius){
                return circleSearch.getAllPositionsInCircle(
                originalPoint.getX(), originalPoint.getY(), radius).stream()
                .map(pos -> grid[pos[0]][pos[1]])
                .collect(Collectors.toCollection(LinkedList::new));
    }
    
    public List<Point> getTownShapePointList(int centerX, int centerY) {
        List<Point> result = new ArrayList<>();

        for (int[] offset : circleSearch.getTownShapePointList()) {
            int newX = centerX + offset[0];
            int newY = centerY + offset[1];

            if (isValid(newX, newY) && grid[newX][newY].isTerrainWalkable()) {
                result.add(grid[newX][newY]);
            }
        }

        return result;
    }

    public List<Point> getCityShapePointList(int centerX, int centerY) {
        List<Point> result = new ArrayList<>();

        for (int[] offset : circleSearch.getCityShapePointList()) {
            int newX = centerX + offset[0];
            int newY = centerY + offset[1];

            if (isValid(newX, newY) && grid[newX][newY].isTerrainWalkable()) {
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

    public Point getRandomEmptyWalkablePointAdjecantToTarget(Point originalPoint){
        LinkedList<int[]> emptyPoints = circleSearch.getValidAdjacentPoints(originalPoint.getX(), originalPoint.getY()).stream()
            .filter(p -> grid[p[0]][p[1]].isEmpty())
            .filter(p -> grid[p[0]][p[1]].isTerrainWalkable())
            .collect(Collectors.toCollection(LinkedList::new));

        if (emptyPoints.isEmpty()) return null;

        Collections.shuffle(emptyPoints, random);
        int[] chosen = emptyPoints.get(0);

        return grid[chosen[0]][chosen[1]]; 
    }
    
    
    
 public ArrayList<Point> getPointsInCircleAroundTarget(Point originalPoint, int radius) {
    ArrayList<Point> result = new ArrayList<>();

    ArrayList<int[]> relativePositions = new ArrayList<>(circleSearch.getSingleLinePositionsAroundTargetInCircle(originalPoint.getX(), originalPoint.getY(), radius));

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

        // TODO not sure if needed since this function isnt used
        // TODO here, create a list, or array, of the squares around the position. Then shuffle the order. then check one after the other
        
        int[] dx = {-1, -1, -1,  0,  0,  1,  1,  1};
        int[] dy = {-1,  0,  1, -1,  1, -1,  0,  1};

        for (int i = 0; i < 8; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            if (isValid(nx, ny)) {
                Point neighbor = grid[nx][ny];
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

                    if (isValid(newX, newY) && !grid[newX][newY].equals(grid[px][py])) {
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist <= distance && grid[newX][newY].isEmpty()) {
                            return grid[newX][newY];
                        }
                        queue.add(new int[]{newX, newY});
                    }
                }
            }
        }
        return null;
    }
    
    public boolean isValidAndEmpty(int x, int y){
        return grid[x][y].isEmpty() && (x >= 0 && x < width && y >= 0 && y < height);
    }
    
    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    private boolean isValidAndWalkable(int x, int y){
        return isValid(x, y) && grid[x][y].isTerrainWalkable();
    }

    public List<Point> getAllWalkableValidNeighbours(Point point){
        List<Point> validNeighbours = new ArrayList<>();

        int x = point.getX();
        int y = point.getY();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0){
                    continue;
                }
                if (isValidAndWalkable(x, y)){
                    validNeighbours.add(getPoint((x+dx), (y+dy)));
                }

            }
        }
        return validNeighbours;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
