package buildings.ai;

import buildings.Building;
import buildings.subBuildings.subBuildingFunctions.SubBuilding;
import empirebuilder.Game;

/**
 * BuildingAI links to a custom created AI for a building and handles the
 * evaluation and ticking, but actual AI lies in the aiProfile
 */
public class BuildingAI {

    private final BuildingAIProfile profile;
    // how often the building re-evaluates what to do, this is in building ticks (1/10 normal ticks)
    public final int evaluationInterval;
    public int ticksSinceLastEvaluation;

    private Class<? extends SubBuilding> subBuildingToBuildNext = null;

    public BuildingAI(BuildingAIProfile profile, int evaluationInterval) {
        this.profile = profile;
        this.evaluationInterval = evaluationInterval;
        this.ticksSinceLastEvaluation = (int) (Math.random() * evaluationInterval);
    }

    public void tick(Building building, Game game) {
        ticksSinceLastEvaluation++;

        if (ticksSinceLastEvaluation >= evaluationInterval) {
            ticksSinceLastEvaluation = 0;
            profile.evaluate(building, game);
        }

        profile.execute(building, game);
    }

    public Class<? extends SubBuilding> getSubBuildingToBuildNext() {
        return subBuildingToBuildNext;
    }

    public void setSubBuildingToBuildNext(Class<? extends SubBuilding> subBuildingClass) {
        this.subBuildingToBuildNext = subBuildingClass;
    }

    public void clearSubBuildingToBuildNext() {
        this.subBuildingToBuildNext = null;
    }

    public BuildingAIProfile getProfile() {
        return profile;
    }
}
