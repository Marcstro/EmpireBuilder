package buildings;

public enum FarmTechLevel {
    LEVEL_1( 0), // base
    LEVEL_2( 1), // has at least 4 people
    LEVEL_3( 2), // belongs to village
    LEVEL_4( 3), // belongs to town
    LEVEL_5( 4), // belongs to city
    LEVEL_6( 7); // fertilised city farm with tools

    private final int level;

    FarmTechLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public FarmTechLevel increaseLevel() {
        int nextIndex = this.ordinal() + 1;
        FarmTechLevel[] levels = FarmTechLevel.values();
        if (nextIndex < levels.length) {
            return levels[nextIndex];
        }
        return this;
    }

    public FarmTechLevel decreaseLevel() {
        int prevIndex = this.ordinal() - 1;
        FarmTechLevel[] levels = FarmTechLevel.values();
        if (prevIndex >= 0) {
            return levels[prevIndex];
        }
        return this;
    }
}
