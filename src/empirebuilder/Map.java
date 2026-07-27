package empirebuilder;

import LandTypes.*;
import buildings.Building;
import buildings.Farm;

import java.util.*;
import java.util.stream.Collectors;

import buildings.Village;
import buildingsTools.TerrainGenerator;
import math.CircleSearch;

public class Map {
    
    GameManager gameManager;
        
    int width;
    int height;
    private final Point[][] grid;
    Random random;
    CircleSearch circleSearch;
    final int FARM_EXTEND_DISTANCE = 10;
    private TerrainGenerator terrainGenerator;

    public Map(GameManager gameManager, int pixelWidth, int pixelHeight) {
        System.out.println("pixelwidth: " + pixelWidth +  ", " + "pixelheight: "+ pixelHeight);
        this.gameManager = gameManager;
        width = pixelWidth;
        height = pixelHeight;
        
        grid = new Point[width][height];

        random = new Random();
        this.circleSearch = new CircleSearch(FARM_EXTEND_DISTANCE, width, height);

        if(gameManager.getWorldSettings().isGenerateTerrain()){
            terrainGenerator = new TerrainGenerator(this, gameManager.getWorldSettings().getGeneratorType());
            terrainGenerator.generateTerrain();
        }
        else {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    grid[x][y] = new Point(x, y, LandType.GRASSLAND);
                }
            }
        }
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

    public Point getPointInMiddleOfMap(){
        int x = width /2;
        int y = height / 2;
        return grid[x][y];
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

    public void replaceBuilding(Point point, Building building){
        removeBuildingFromPoint(point);
        setBuildingOnPoint(point, building);
    }
    
    public void setBuildingOnPoint(Point point, Building building){
        if (point.getBuilding() != null){
            if (point.getBuilding() instanceof Farm farm){
                if (farm.belongsToFarmOwningBuilding()) {
                    farm.getFarmOwningBuilding().removeFromFarmList(farm);
                }
            }
            removeBuildingFromPoint(point);
        }
        point.setBuilding(building);
    }

    public void removeBuildingFromPoint(Point point){
        if (point.getBuilding() == null){
            System.out.println("Tried to remove building at " + point.getPositionString() + " but no building there!");
            throw new RuntimeException("Tried to remove building at " + point.getPositionString() + " but no building there!");
        }
        point.setBuilding(null);
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
        List<Point> result = new LinkedList<>();

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
        List<Point> result = new LinkedList<>();

        for (int[] offset : circleSearch.getCityShapePointList()) {
            int newX = centerX + offset[0];
            int newY = centerY + offset[1];

            if (isValid(newX, newY) && grid[newX][newY].isTerrainWalkable()) {
                result.add(grid[newX][newY]);
            }
        }

        return result;
    }

    public LinkedList<Point> getAllEmptyAndWalkablePointsInCircleAroundTarget(Point originalPoint, int radius){
        return getAllPointsInCircleAroundTarget(originalPoint, radius).stream()
            .filter(Point::isEmpty)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public Point getFirstEmptyAndWalkablePointInCircle(Point originalPoint, int radius) {
        return getAllPointsInCircleAroundTarget(originalPoint, radius).stream()
                .filter(Point::isEmpty)
                .filter(Point::isTerrainWalkable)
                .findAny()
                .orElse(null);
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

    public Point getOwnedAdjecantFarm(Point originalPoint){
        List<int[]> adjecantPoints = circleSearch.getValidAdjacentPoints(originalPoint.getX(), originalPoint.getY());

        Collections.shuffle(adjecantPoints);
        for (int[] coord : adjecantPoints) {
            Point p = grid[coord[0]][coord[1]];

            if (p.getBuilding() instanceof Farm farm &&
                    farm.getFarmOwningBuilding() instanceof Village) {
                return p;
            }
        }

        return null;
    }

     public ArrayList<Point> getPointsInCircleAroundTarget(Point originalPoint, int radius) {
        ArrayList<Point> result = new ArrayList<>();

        ArrayList<int[]> relativePositions = new ArrayList<>(circleSearch.getSingleLinePositionsAroundTargetInCircle(originalPoint.getX(), originalPoint.getY(), radius));

         System.out.println("size: " + relativePositions.size());
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

    public Point getrandomValidPoint(){
        Point point = null;
        while (point == null){
            int randomX = random.nextInt(width);
            int randomY = random.nextInt(height);
            if (isValidAndWalkable(randomX, randomY)){
                point = getPoint(randomX, randomY);
            }
        }
        return point;
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
        return grid[x][y].isEmpty() && ( x < width && y < height);
    }
    
    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isValidAndWalkable(int x, int y){
        return isValid(x, y) && grid[x][y].isTerrainWalkable();
    }

    public boolean isValidAndWalkable(double x, double y){
        return (x >= 0.0001 && x < (width-1) && y >= 0.0001 && y < (height-1) && grid[(int)x][(int)y].isTerrainWalkable());
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
                if (isValidAndWalkable(x+dx, y+dy)){
                    validNeighbours.add(getPoint((x+dx), (y+dy)));
                }

            }
        }
        return validNeighbours;
    }


    /**
     * Bresenham line-of-sight check between two world-space positions.
     * Steps along every integer grid cell on the line; returns false as soon as
     * an unwalkable cell is encountered.
     */
    public boolean hasLineOfSight(double x1, double y1, double x2, double y2) {
        int ax = (int) x1, ay = (int) y1;
        int bx = (int) x2, by = (int) y2;

        int dx  =  Math.abs(bx - ax);
        int dy  = -Math.abs(by - ay);
        int sx  = ax < bx ? 1 : -1;
        int sy  = ay < by ? 1 : -1;
        int err = dx + dy;

        while (true) {
            if (!isValidAndWalkable(ax, ay)) return false;
            if (ax == bx && ay == by)        return true;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; ax += sx; }
            if (e2 <= dx) { err += dx; ay += sy; }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
