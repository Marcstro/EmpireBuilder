package buildingsTools;

import LandTypes.LandFactory;
import LandTypes.LandType;
import empirebuilder.Map;
import empirebuilder.Point;

import java.util.Random;

public class TerrainGenerator {

    private final Map map;
    TerrainGeneratorType terrainGeneratorType;
    Random random;

    public TerrainGenerator(Map map, TerrainGeneratorType terrainGeneratorType) {
        this.map = map;
        this.terrainGeneratorType = terrainGeneratorType;
        random = new Random();
    }

    public void generateTerrain(){
        if (terrainGeneratorType.equals(TerrainGeneratorType.PERLIN_BASED_V1_LARGE_TERRAIN)) {
            generate_PERLIN_BASED_V1(0.01);
        }
        else if (terrainGeneratorType.equals(TerrainGeneratorType.PERLIN_BASED_V1_MEDIUM_TERRAIN)) {
            generate_PERLIN_BASED_V1(0.02);
        }
        else if (terrainGeneratorType.equals(TerrainGeneratorType.PERLIN_BASED_V1_SMALL_TERRAIN)){
            generate_PERLIN_BASED_V1(0.05);
        }
    }

    private void generate_PERLIN_BASED_V1(double sizeOfTerrain){
        PerlinNoise perlin = new PerlinNoise(random.nextInt()); // TODO incorporate seeds here to have fixed terrain

        double scale = sizeOfTerrain; // Smaller = bigger features
        double elevationScale = 255;
        int mountainHeight = 150;
        int seaLevel = 105;
        int hillLevel = 140;

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                map.getGrid()[x][y] = new Point(x, y, LandType.DIRT); // default

                double nx = x * scale;
                double ny = y * scale;

                double noiseVal = 0;
                double frequency = 1.0;
                double amplitude = 1.0;
                double max = 0.0;

                int octaves = 4; // higher = more details

                for (int o = 0; o < octaves; o++) {
                    noiseVal += perlin.noise(nx * frequency, ny * frequency) * amplitude;
                    max += amplitude;
                    amplitude *= 0.5;   // Smaller details contribute less
                    frequency *= 2.0;   // Next layer: more frequent changes
                }

// Normalize to [-1,1]
                noiseVal /= max;

// Shift to [0,1]
                noiseVal = (noiseVal + 1) / 2.0;

                int elevation = (int)(noiseVal * elevationScale);

                map.getGrid()[x][y].setElevation(elevation);

                if (elevation < seaLevel) {
                    map.getGrid()[x][y].setLand(LandFactory.createLand(LandType.WATER));
                } else if (elevation < hillLevel) {
                    map.getGrid()[x][y].setLand(LandFactory.createLand(LandType.GRASSLAND));
                    map.getEmptyPoints().add(map.getGrid()[x][y]);
                    map.getEmptyPointList().add(map.getGrid()[x][y]);
                } else if (elevation < mountainHeight) {
                    map.getGrid()[x][y].setLand(LandFactory.createLand(LandType.HILL));
                    map.getEmptyPoints().add(map.getGrid()[x][y]);
                    map.getEmptyPointList().add(map.getGrid()[x][y]);
                } else {
                    map.getGrid()[x][y].setLand(LandFactory.createLand(LandType.MOUNTAIN));
                }
            }
        }

        //generateRivers2(5);
    }

    // TODO doesnt work in this version
    public void generateRivers2(int numRivers) {
        for (int i = 0; i < numRivers; i++) {
            // 1) Pick random high-elevation source
            int attempts = 0;
            Point source = null;
            while (attempts < 100) {
                int x = random.nextInt(map.getWidth());
                int y = random.nextInt(map.getHeight());
                if (map.getGrid()[x][y].getElevation() > 200) {
                    source = map.getGrid()[x][y];
                    break;
                }
                attempts++;
            }
            if (source == null) continue; // Couldn’t find source, skip

            carveRiver2(source, 2); // 2 = width of river
        }
    }

    private void carveRiver2(Point source, int riverWidth) {
        Point current = source;

        while (current.getElevation() > 85) { // 85 = sea level/coastline
            // Mark a band of tiles around current as RIVER
            for (int dx = -riverWidth; dx <= riverWidth; dx++) {
                for (int dy = -riverWidth; dy <= riverWidth; dy++) {
                    int nx = current.getX() + dx;
                    int ny = current.getY() + dy;
                    if (map.isValid(nx, ny)) {
                        map.getGrid()[nx][ny].setLand(LandFactory.createLand(LandType.WATER));
                    }
                }
            }

            // Find steepest neighbor downhill
            Point next = null;
            int lowest = current.getElevation();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = current.getX() + dx;
                    int ny = current.getY() + dy;
                    if (map.isValid(nx, ny)) {
                        Point neighbor = map.getGrid()[nx][ny];
                        if (neighbor.getElevation() < lowest) {
                            lowest = neighbor.getElevation();
                            next = neighbor;
                        }
                    }
                }
            }

            if (next == null) break; // Stuck, no lower neighbor
            current = next;
        }
    }
}
