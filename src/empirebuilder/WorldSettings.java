package empirebuilder;

import buildingsTools.TerrainGeneratorType;

public class WorldSettings {

    private boolean generateTerrain;
    private TerrainGeneratorType generatorType;

    public WorldSettings(){
        generateTerrain = true;
        generatorType = TerrainGeneratorType.PERLIN_BASED_V1_LARGE_TERRAIN;
    }

    public boolean isGenerateTerrain() {
        return generateTerrain;
    }

    public void setGenerateTerrain(boolean generateTerrain) {
        this.generateTerrain = generateTerrain;
    }

    public TerrainGeneratorType getGeneratorType() {
        return generatorType;
    }

    public void setGeneratorType(TerrainGeneratorType generatorType) {
        this.generatorType = generatorType;
    }
}
