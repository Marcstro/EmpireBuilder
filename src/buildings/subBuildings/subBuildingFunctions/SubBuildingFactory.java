package buildings.subBuildings.subBuildingFunctions;

import buildings.subBuildings.AnimalHusbandry;
import buildings.subBuildings.Barracks;
import buildings.subBuildings.ElvenArcheryRange;
import buildings.subBuildings.GoblinArcheryRange;
import buildings.subBuildings.GoblinHut;
import buildings.subBuildings.MountedKnightSchool;
import buildings.subBuildings.OrcDen;
import buildings.subBuildings.OrcFortress;
import buildings.subBuildings.ShootingTower;
import buildings.subBuildings.SimpleGoblinScoutHut;
import buildings.subBuildings.Stable;
import buildings.subBuildings.WolfDen;

import java.util.Map;
import java.util.function.Function;

public class SubBuildingFactory {

    private static final Map<Class<? extends SubBuilding>, Function<AreaBuildingSlot, SubBuilding>> FACTORIES_BY_TYPE =
            Map.ofEntries(
                    Map.entry(AnimalHusbandry.class, AnimalHusbandry::new),
                    Map.entry(Barracks.class, Barracks::new),
                    Map.entry(ElvenArcheryRange.class, ElvenArcheryRange::new),
                    Map.entry(GoblinArcheryRange.class, GoblinArcheryRange::new),
                    Map.entry(GoblinHut.class, GoblinHut::new),
                    Map.entry(MountedKnightSchool.class, MountedKnightSchool::new),
                    Map.entry(OrcDen.class, OrcDen::new),
                    Map.entry(OrcFortress.class, OrcFortress::new),
                    Map.entry(ShootingTower.class, ShootingTower::new),
                    Map.entry(SimpleGoblinScoutHut.class, SimpleGoblinScoutHut::new),
                    Map.entry(Stable.class, Stable::new),
                    Map.entry(WolfDen.class, WolfDen::new)
            );

    public static SubBuilding create(Class<? extends SubBuilding> type, AreaBuildingSlot areaSlot) {
        Function<AreaBuildingSlot, SubBuilding> factory = FACTORIES_BY_TYPE.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("No factory mapping for " + type.getSimpleName());
        }
        return factory.apply(areaSlot);
    }
}
