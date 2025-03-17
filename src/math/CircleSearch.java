package math;

import java.util.*;

public class CircleSearch {
    private final Map<Integer, List<int[]>> precomputedOffsets = new HashMap<>();
    private final Map<Integer, List<int[]>> precomputedCircles = new HashMap<>();
    private final List<int[]> townShapePointList = new ArrayList<>();

    private final int maxRadius;
    int gridHeight;
    int gridWidth;

    public CircleSearch(int maxRadius, int gridWidth, int gridHeight) {
        this.maxRadius = maxRadius;
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        precomputeOffsets();
        precomputeTownShape();
    }

    // Precompute relative positions for each radius
    private void precomputeOffsets() {
        //calculate cirles around target
        for (int r = 1; r <= maxRadius; r++) {
            List<int[]> offsets = new ArrayList<>();

            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    if (distance <= r && distance > (r - 1)) { 
                        offsets.add(new int[]{dx, dy});
                    }
                }
            }
            precomputedOffsets.put(r, offsets);
        }
        
        //calculate filled circles around target
        for (int r = 1; r <= maxRadius; r++) {
            List<int[]> offsets = new ArrayList<>();

            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    if (
                            distance <= r 
                            ) { 
                        offsets.add(new int[]{dx, dy});
                    }
                }
            }
            precomputedCircles.put(r, offsets);
        }
    }

    // Returns a list of int arrays
    // Each array contains 2 values, 
    // 1st value is directions in X dimension
    // 2nd value is directions in Y dimension
    // [-2, 3] means 2 steps left and 3 steps up from the original point
    // The entire list will create a single point line in the form of a circle around the target point 
    // DOES check for border edges
    public List<int[]> getSingleLinePositionsAroundTargetInCircle(int x, int y, int radius) {
        List<int[]> results = new ArrayList<>();

        if (radius < 1 || radius > maxRadius || !precomputedOffsets.containsKey(radius)) {
            return results; // Return empty if radius is out of range
        }

        for (int[] offset : precomputedOffsets.get(radius)) {
            int newX = x + offset[0];
            int newY = y + offset[1];

            // Ensure the point is within the grid bounds
            if (newX >= 0 && newX < gridWidth && newY >= 0 && newY < gridHeight) {
                results.add(new int[]{newX, newY});
            }
        }
        return results;
    }
    
    // Returns a list of int arrays
    // Each array contains 2 values, 
    // 1st value is directions in X dimension
    // 2nd value is directions in Y dimension
    // [-2, 3] means 2 steps left and 3 steps up from the original point
    // The entire list will create a FILLED circle of positions around the target point 
    // DOES check for border edges
    public List<int[]> getAllPositionsInCircle(int centerX, int centerY, int radius) {
        List<int[]> result = new ArrayList<>();

        if (!precomputedCircles.containsKey(radius)) {
            return result; // Return empty if radius is not precomputed
        }

        for (int[] offset : precomputedCircles.get(radius)) {
            int newX = centerX + offset[0];
            int newY = centerY + offset[1];

            if (newX >= 0 && newX < gridWidth && newY >= 0 && newY < gridHeight) {
                result.add(new int[]{newX, newY});
            }
        }
        return result;
    }
    
    public void precomputeTownShape() {
        
        townShapePointList.add(new int[]{-2, -2}); //towers
        townShapePointList.add(new int[]{0, -2});
        townShapePointList.add(new int[]{2, -2});

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy < 3; dy++) {
                townShapePointList.add(new int[]{dx, dy});
            }
        }
    }
    
    //probably remove
    public List<int[]> getTownShapePoints(int centerX, int centerY, int gridWidth, int gridHeight) {
        List<int[]> result = new ArrayList<>();

        for (int[] offset : townShapePointList) {
            int newX = centerX + offset[0];
            int newY = centerY + offset[1];

            if (newX >= 0 && newX < gridWidth && newY >= 0 && newY < gridHeight) {
                result.add(new int[]{newX, newY});
            }
        }

        return result;
    }

    public List<int[]> getTownShapePointList() {
        return townShapePointList;
    }
    
    

    
    public List<int[]> getValidAdjacentPoints(int x, int y) {
        List<int[]> result = new ArrayList<>();

        int[][] directions = {
            {-1, -1}, {0, -1}, {1, -1}, // Top-left, Top, Top-right
            {-1,  0},         {1,  0},  // Left,        Right
            {-1,  1}, {0,  1}, {1,  1}  // Bottom-left, Bottom, Bottom-right
        };

        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];

            if (newX >= 0 && newX < gridWidth && newY >= 0 && newY < gridHeight) {
                result.add(new int[]{newX, newY});
            }
        }
        return result;
    }



    //for testing purposes
//    public static void main(String[] args) {
//        int gridWidth = 300, gridHeight = 200;
//        CircleSearch circleSearch = new CircleSearch(10); // Precompute up to radius 10
//
//        int x = 150, y = 100, radius = 5;
//        List<int[]> points = circleSearch.getPointsAround(x, y, radius, gridWidth, gridHeight);
//
//        System.out.println("Points around (" + x + ", " + y + ") with radius " + radius + ":");
//        for (int[] point : points) {
//            System.out.println("(" + point[0] + ", " + point[1] + ")");
//        }
//        System.out.println("that is " + points.size() + " points");
//    }
}
