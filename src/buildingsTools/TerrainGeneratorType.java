package buildingsTools;

public enum TerrainGeneratorType {
    PERLIN_BASED_V1_LARGE_TERRAIN("Large perlin based"),
    PERLIN_BASED_V1_MEDIUM_TERRAIN("Medium perlin based"),
    PERLIN_BASED_V1_SMALL_TERRAIN("Small perlin based");

    private final String displayName;

    TerrainGeneratorType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
