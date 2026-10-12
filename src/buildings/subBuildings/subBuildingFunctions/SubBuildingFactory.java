package buildings.subBuildings.subBuildingFunctions;

public class SubBuildingFactory {

    public static SubBuilding create(Class<? extends SubBuilding> clazz, AreaBuildingSlot areaSlot) {
        try {
            return clazz.getDeclaredConstructor(AreaBuildingSlot.class).newInstance(areaSlot);
        } catch (Exception e) {
            System.err.println("Failed to instantiate sub-building: " + clazz.getSimpleName() + " - " + e.getMessage());
            return null;
        }
    }
}
