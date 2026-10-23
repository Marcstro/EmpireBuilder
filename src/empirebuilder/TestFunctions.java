package empirebuilder;

import LandTypes.LandType;
import buildings.*;
import entities.units.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TestFunctions {

    private final GameManager gm;
    private int experimentTicker = 1;
    private volatile boolean quickstartActive;

    public TestFunctions(GameManager gameManager) {
        this.gm = gameManager;
    }

    private Game game() {
        return gm.getGame();
    }

    public void experiment2() {
        int a = experimentTicker % 5;
        System.out.println("a = " + a);
        ArrayList<Point> pointsToMake = gm.getMap().getPointsInCircleAroundTarget(
                new Point(50 + (a * 50), 50, LandType.GRASSLAND),
                experimentTicker
        );
        for (Point point : pointsToMake) {
            createFarmAtPoint(point.getX(), point.getY());
        }
        experimentTicker++;
        gm.getGridPanel().repaint();
    }

    public void experiment3() {
        Game game = game();
        int farmAmount = 0;
        Point[][] grid = gm.getMap().getGrid();
        for (Point[] points : grid) {
            for (Point point : points) {
                if (point.getBuilding() instanceof Farm) {
                    farmAmount++;
                }
            }
        }
        System.out.println("Farms according to list: " + game.farms.size()
                + ", Real amount of farms: " + farmAmount
                + ", difference: " + (game.farms.size() - farmAmount));
        System.out.println("Amount of villages: " + game.villages.size()
                + ", towns: " + game.towns.size()
                + ", cities: " + game.cities.size());
    }

    public void experiment5() {
        List<Point> points = List.of(
                gm.getMap().getGrid()[150][150],
                gm.getMap().getGrid()[151][150],
                gm.getMap().getGrid()[152][150],
                gm.getMap().getGrid()[150][151],
                gm.getMap().getGrid()[151][151],
                gm.getMap().getGrid()[152][151]
        );

        for (Point point : points) {
            Farm farm = createFarmAtPoint(point.getX(), point.getY());
            farm.setFood(10);
        }
    }

    public void create13FarmsInCircleAtRandomPoint() {
        Game game = game();
        List<Point> farmPoints = findEmptyWalkableCircle();
        if (farmPoints == null) {
            System.out.println("Could not find room for 13 farms in a walkable circle.");
            return;
        }

        for (Point point : farmPoints) {
            Farm farm = game.getFarmFromPool();
            farm.activate(point, 4);
            gm.getMap().setBuildingOnPoint(point, farm);
            game.farms.add(farm);
        }
        gm.getGridPanel().updateUI();
    }

    private List<Point> findEmptyWalkableCircle() {
        for (int attempt = 0; attempt < 1000; attempt++) {
            Point center = gm.getMap().getrandomValidPoint();
            List<Point> circle = gm.getMap().getAllPointsInCircleAroundTarget(center, 2);
            if (circle.size() == 13
                    && circle.stream().allMatch(point ->
                    point.getBuilding() == null && point.isTerrainWalkable())) {
                return circle;
            }
        }
        return null;
    }

    public void experiment15() {
        Game game = game();
        for (int x = 0; x < gm.getMap().getWidth(); x += 2) {
            for (int y = 0; y < gm.getMap().getHeight(); y++) {
                Point newFarmPoint = gm.getMap().getPoint(x, y);
                if (!newFarmPoint.isEmpty()) {
                    continue;
                }
                Farm newFarm = game.getFarmFromPool();
                newFarm.activate(newFarmPoint, 2);
                gm.getMap().setBuildingOnPoint(newFarm.getPoint(), newFarm);
                game.checkIfNewFarmIsPartOfVillageCenter(newFarm);
                game.farms.add(newFarm);
            }
        }
        gm.getGridPanel().updateUI();
    }

    public void checkVillageDomain() {
        Game game = game();
        for (Village village : game.villages) {
            for (Point point : village.getControlledLand()) {
                if (point.getBuilding() instanceof Farm farm
                        && farm.getFarmOwningBuilding() != village) {
                    System.out.println("SITUATION 32");
                    System.out.println(village.getInfo());
                    System.out.println(farm.getInfo());
                    System.out.println(point.getInfo());
                }
            }
        }
    }

    public Farm createFarmAtPoint(int x, int y) {
        Game game = game();
        Point point = gm.getMap().getPoint(x, y);
        Farm farm = game.getFarmFromPool();
        farm.activate(point, 1);

        gm.getMap().setBuildingOnPoint(point, farm);
        game.farms.add(farm);
        gm.getGridPanel().updateUI();
        return farm;
    }

    public void printMapInfo() {
        Game game = game();
        System.out.println("map info: ");
        System.out.println("total amount of units: " + game.units.size());
        System.out.println("Orcs: " + game.units.stream().filter(unit -> unit instanceof Orc).toList().size());
        System.out.println("Knights: " + game.units.stream().filter(unit -> unit instanceof Knight).toList().size());
        System.out.println("Farms: " + game.farms.size());
        System.out.println("Inactive farms: " + game.inactiveFarms.size());
        System.out.println("Villages: " + game.villages.size());
        System.out.println("Towns: " + game.towns.size());
        System.out.println("Cities: " + game.cities.size());
        int total = gm.getMap().getGrid().length * gm.getMap().getGrid()[0].length;
        System.out.println("Whole map size: " + total);
        System.out.println("All buildings is total: "
                + (game.farms.size() + game.towns.size() + game.villages.size()));
    }

    public void createCompleteVillageAt(int x, int y) {
        Game game = game();
        Point point = gm.getMap().getPoint(x, y);
        List<Point> surroundings = gm.getMap().getAllValidAdjecantPointsToTarget(point);
        surroundings.add(point);
        for (Point p : surroundings) {
            Farm farm = game.getFarmFromPool();
            farm.activate(p, 2);
            gm.getMap().setBuildingOnPoint(p, farm);
            farm.setFood(30);
            farm.setPeople(3);
            game.farms.add(farm);
        }
        game.convertFarmToVillageCenter((Farm) point.getBuilding());
        gm.getGridPanel().updateUI();
    }

    public void createWaterPath() {
        System.out.println("xx3");
        Point selectedPoint = gm.getGridPanel().getSelectedPoint();
        if (selectedPoint == null) {
            return;
        }
        Point target = gm.getMap().getPoint(150, 150);
        List<Point> road = gm.pathfinder.getPathBetween(selectedPoint, target);
        for (Point point : road) {
            point.createNewLandForPoint(LandType.WATER);
        }
        System.out.println(road.size() + " = road langd");
        gm.getGridPanel().updateUI();
    }

    public Farm createFarmAtRandomPoint() {
        Game game = game();
        Point randomPoint = gm.getMap().getRandomPoint();
        if (!randomPoint.isEmpty()) {
            return null;
        }
        Farm farm = game.getFarmFromPool();
        farm.activate(randomPoint, 1);

        gm.getMap().setBuildingOnPoint(randomPoint, farm);
        game.farms.add(farm);
        System.out.println("Farm created at " + farm.getInfo());
        gm.getGridPanel().updateUI();
        return farm;
    }

    public void create20FarmAtRandomPoint() {
        for (int x = 0; x < 20; x++) {
            createFarmAtRandomPoint();
        }
    }

    public void quickstart() {
        Engine engine = gm.getEngine();
        engine.stop();
        for (int x=0; x<8; x++){
            create13FarmsInCircleAtRandomPoint();
        }

        quickstartActive = true;
        engine.setSpeedMultiplier(Engine.MAX_SPEED_MULTIPLIER);
        engine.setAfterTickAction(this::checkQuickstartProgress);
        engine.start();
    }

    private void checkQuickstartProgress() {
        if (!quickstartActive || (game().getDarkside().isEvilSideSpawningEnabled() && game().evilBases.isEmpty()) || (!game().getDarkside().isEvilSideSpawningEnabled() && game().cities.isEmpty())) {
            return;
        }

        quickstartActive = false;
        Engine engine = gm.getEngine();
        engine.setAfterTickAction(null);
        engine.setSpeedMultiplier(Engine.NORMAL_SPEED_MULTIPLIER);
    }

    public static <T> Set<T> findDuplicatesIterative(List<T> list) {
        Set<T> seen = new HashSet<>();
        Set<T> duplicates = new HashSet<>();

        for (T item : list) {
            if (!seen.add(item)) {
                duplicates.add(item);
            }
        }
        return duplicates;
    }

    public void currentExperiment() {
        Game game = game();
        Point point = gm.getGridPanel().getSelectedPoint();
        if (point.getBuilding() == null) {
            System.out.println("No building there to destroy!");
        } else {
            if (point.getBuilding() instanceof CityArea cityArea) {
                game.citiesToRemove.add(cityArea.getCityCenter());
            } else if (point.getBuilding() instanceof TownArea townArea) {
                game.townsToRemove.add(townArea.getTownCenter());
            } else if (point.getBuilding() instanceof Farm farm
                    && farm.getFarmOwningBuilding() instanceof Village village) {
                game.villagesToRemove.add(village);
            } else if (point.getBuilding() instanceof Town town) {
                game.townsToRemove.add(town);
            } else if (point.getBuilding() instanceof City city) {
                game.citiesToRemove.add(city);
            } else if (point.getBuilding() instanceof Village village) {
                game.villagesToRemove.add(village);
            } else if (point.getBuilding() instanceof Farm farm) {
                game.farmsToRemove.add(farm);
            }
        }
    }

    public void currentExperiment2() {
        Game game = game();
        for (int x = 0; x < gm.getMap().width; x++) {
            for (int y = 0; y < gm.getMap().height; y++) {
                Point point = gm.getMap().getPoint(x, y);
                if (point.getBuilding() instanceof LandOwningBuilding landOwningBuilding) {
                    for (Point controlledPoint : landOwningBuilding.getControlledLand()) {
                        if (controlledPoint.getPointOwner() != landOwningBuilding) {
                            System.out.println("OBS OBS. One of buildings controlled points doesnt recognise owner");
                            System.out.println(controlledPoint.getInfo());
                            System.out.println(landOwningBuilding.getControlledLand());
                        }
                    }
                }
                if (point.getPointOwner() != null
                        && !point.getPointOwner().getControlledLand().contains(point)) {
                    System.out.println("OOBS OBS a point that points to an owner doesnt recognise its point!");
                    System.out.println(point.getInfo());
                    System.out.println(point.getPointOwner().getControlledLand());
                }
            }
        }
        System.out.println("end of search");
    }

    public void experimentTimeMethod() {
        /**
         * In this method, create a for loop and call whatever feature you want thousands of times
         * to get an approximate time how long it takes to perform
         *
         * Below is an example
         */
        /*
        long startTime = System.currentTimeMillis();
        Village village = game().villages.get(0);
        Point newFarmPoint;
        for (int i=0; i<2000; i++){

            if (!village.timeToRedoNearbySearch()){
                continue;
            }
            List<Point> availableSpots = gm.getMap().getAllEmptyAndWalkablePointsInCircleAroundTarget(
                    village.getPoint(), game().villageSpreadingFarmsDistance);
            if (!availableSpots.isEmpty()){
                newFarmPoint = availableSpots.get(ThreadLocalRandom.current().nextInt(availableSpots.size()));
            }
            else {
                village.applySearchCoolDown();
            }
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println(", Duration: " + duration + "ms");
         */
    }

    public void spawnOrcRandomlyTest() {
        Game game = game();
        for (int x = 0; x < 4; x++) {
            if (gm.getMap().isValidAndWalkable(x, x)) {
                Orc orc = new Orc(x, x);
                game.unitsToBeAdded.add(orc);
            }
        }
        spawnKnightsRandomlyTest();
    }

    public void spawnKnightsRandomlyTest() {
        Game game = game();
        for (int x = 0; x < 2; x++) {
            if (gm.getMap().isValidAndWalkable(x, x + 100)) {
                Knight knight = new Knight(x, x + 100);
                game.unitsToBeAdded.add(knight);
            }
        }
    }

    public void spawnTwoOppositeFightingGroups() {
        Game game = game();
        int troopAmount = ThreadLocalRandom.current().nextInt(5);
        List<Point> knightPoints = game.findGroupSpawnPoints(250, 100, troopAmount);
        int a = 0;
        for (Point point : knightPoints) {
            a++;
            if (a % 2 == 0) {
                game.unitsToBeAdded.add(new ElfArcher(point.getX(), point.getY()));
            } else {
                game.unitsToBeAdded.add(new Knight(point.getX(), point.getY()));
            }
        }

        List<Point> orcPoints = game.findGroupSpawnPoints(50, 100, troopAmount);
        for (Point point : orcPoints) {
            a++;
            if (a % 2 == 0) {
                game.unitsToBeAdded.add(new Orc(point.getX(), point.getY()));
            } else {
                game.unitsToBeAdded.add(new GoblinArcher(point.getX(), point.getY()));
            }
        }
    }

    public void spawnOneTroopForPathFinding() {
        Game game = game();
        if (game.units.size() < 100) {
            Point point = game.getRandomWalkablePoint();
            Knight knight = new Knight(point.getX(), point.getY());
            game.unitsToBeAdded.add(knight);
        }
    }
}
