package empirebuilder;

import LandTypes.LandType;
import buildings.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Game{

    GameManager gm;
    Random random;
    int tickCounter;

    List<Farm> farms;
    List<Village> villages;
    List<Town> towns;
    List<City> cities;

    List<Farm> farmsToAdd;
    List<Farm> farmsToRemove;
    List<Farm> farmToConvertToVillage;

    LinkedList<Farm> inactiveFarms;
    
    int experimentTicker=1;
    
    final int FOOD_COST_TO_MULTIPLY = 10;
    final int FARMS_TO_CREATE_VILLAGE = 8;
    final int DISTANCE_BETWEEN_FARMS_FOR_VILLAGE_CREATION = 3;
    final int VILLAGE_DOMAIN_LIMIT = 4;
    final int townDomainRange = 25;
    final int townFormDistance = 15;
    final int villagesForTownCreation = 5;
    final int townsForCityCreation = 4;
    final int villageOwningRange = 20;
    final int cityDomainRange = 60;
    final int townToTownMinimumDistance = 15;
    final int cityToCityMinimumDistance = 35;
    final int villageSpreadingFarmsDistance = 12;
    final int adjacentFarmStartingPeopleAmount = 2;
    
    final boolean LOGGING = true;
    
    Game(GameManager gameManager){
        this.gm = gameManager;
        random = new Random();
        farms = new LinkedList();
        villages = new LinkedList();
        towns = new LinkedList();
        cities = new LinkedList();

        inactiveFarms = new LinkedList<>();
        int sizeOfPointsOnMap = gm.getMap().height * gm.getMap().getWidth();
        for (int i=0; i<sizeOfPointsOnMap; i++){
            Farm newFarm = new Farm(gm.getMap().getPoint(0,0));
            inactiveFarms.add(newFarm);
        }

        tickCounter=0;

        farmsToAdd = new LinkedList();
        farmsToRemove = new LinkedList();
        farmToConvertToVillage = new LinkedList();
    }

    public void tickUnits(){

    }

    public void tickEffects(){

    }

    public void tickWorld(){

        if (LOGGING && gm.getGridPanel().getSelectedPoint() != null){
            System.out.println(gm.getGridPanel().getSelectedPoint().getInfo());
        }
        for (Farm farm: farms) {
            farm.tick();
            if (farm.checkIfLastPersonOnFarmDies()){
                farmsToRemove.add(farm);
                continue;
            }
            if (farm.getFarmOwningBuilding() == null && farm.isTimeToCreateNewFarm()) {
                Point newFarmPoint = gm.getMap().getRandomEmptyWalkablePointAdjecantToTarget(farm.getPoint());
                if (newFarmPoint == null){
                    continue;
                }

                Farm newFarm = getFarmFromPool();
                newFarm.activate(newFarmPoint, adjacentFarmStartingPeopleAmount);
                farm.consumeFoodForNewFarm();
                farm.setPeople(farm.getPeople()-adjacentFarmStartingPeopleAmount);
                gm.getMap().setBuildingOnPoint(newFarm.getPoint(), newFarm);
                checkIfNewFarmIsPartOfVillageCenter(newFarm);

                if (newFarm.getPoint().getPointOwner() instanceof FarmOwningBuilding pointOwningBuilding){
                    pointOwningBuilding.addFarm(newFarm);
                    newFarm.setFarmOwningBuilding(pointOwningBuilding);
                }
                else {
                    int independentFarmsNearby = gm.getMap().getIndependentFarmsNearby(newFarm.getPoint(), DISTANCE_BETWEEN_FARMS_FOR_VILLAGE_CREATION).size();
                    if (independentFarmsNearby >= FARMS_TO_CREATE_VILLAGE) {
                        farmToConvertToVillage.add(newFarm);
                    }
                }
                farmsToAdd.add(newFarm);
            }
        }

        for(Village village: villages){
            village.tick();
            if (village.hasCommunalFoodToCreateNewFarm()){
                if (!village.timeToRedoNearbySearch()){ //TODO move village.timeToRedoNearbySearch() up 1 line to prevent villages with no empty space nearby from
                    village.convertCommunalFoodToTaxes(); // only sending excess food upwards when it has food enough to create a new farm
                    continue;
                }
                Point newFarmPoint = null;
                if (!village.getEmptyLand().isEmpty()){
                    newFarmPoint = village.getRandomEmptySpotWithinDomain();
                }
                else {
                    List<Point> availableSpots = gm.getMap().getAllEmptyAndWalkablePointsInCircleAroundTarget(
                            village.getPoint(), villageSpreadingFarmsDistance);
                    if (!availableSpots.isEmpty()){
                        newFarmPoint = availableSpots.get(ThreadLocalRandom.current().nextInt(availableSpots.size()));
                    }
                    else {
                        village.applySearchCoolDown();
                        continue;
                    }
                }

                if (newFarmPoint.getBuilding() != null) {
                    throw new RuntimeException("Error creating farm by village at " + newFarmPoint.getInfo() + ", by village: " + village.getInfo());
                }

                Farm newFarm = getFarmFromPool();
                newFarm.activate(newFarmPoint, 1);
                village.removeCostOfNewFarm();
                gm.getMap().setBuildingOnPoint(newFarm.getPoint(), newFarm);
                checkIfNewFarmIsPartOfVillageCenter(newFarm);

                if (newFarm.getPoint().getPointOwner() instanceof FarmOwningBuilding pointOwningBuilding){
                    pointOwningBuilding.addFarm(newFarm);
                    newFarm.setFarmOwningBuilding(pointOwningBuilding);
                }
                else {
                    int independentFarmsNearby = gm.getMap().getIndependentFarmsNearby(newFarm.getPoint(), DISTANCE_BETWEEN_FARMS_FOR_VILLAGE_CREATION).size();
                    if (independentFarmsNearby >= FARMS_TO_CREATE_VILLAGE) {
                        farmToConvertToVillage.add(newFarm);
                    }
                }
                farmsToAdd.add(newFarm);
            }
        }

         farms.addAll(farmsToAdd);

        for (Farm farmToRemove : farmsToRemove) {
            destroyFarm(farmToRemove);
            if (farmToRemove.belongsToFarmOwningBuilding()) {
                farmToRemove.getFarmOwningBuilding().destroyFarm(farmToRemove);
                farmToRemove.resetState();
                // TODO add how specific buildings handle losing farms
            }
            farmToRemove.resetState();
        }
        inactiveFarms.addAll(farmsToRemove);
        farms.removeAll(farmsToRemove);

        for (Town town: towns){
            town.tick();
        }
        for (City city: cities){
            city.tick();
        }

        farmToConvertToVillage.forEach(farmToConvert -> convertFarmToVillageCenter(farmToConvert));

        gm.getGridPanel().updateUI();

        farmsToAdd.clear();
        farmsToRemove.clear();;
        farmToConvertToVillage.clear();
    }

    public Farm getFarmFromPool(){
        if(!inactiveFarms.isEmpty()){
            return inactiveFarms.poll();
        }
        else {
            System.out.println("farm pool has run out of farms. INCREASE SIZE OF FARM POOL!");
            return new Farm();
        }
    }

    // TODO actually implement, currently I do not use this feature
    // I just do the below steps at each place where farms are returned to the pool
    public void returnFarmToPool(Farm farm){
        farm.resetState();
        inactiveFarms.add(farm);
    }

    public void checkForBuildingUpgrades(){

        // TODO change so that convertion from village-> town, and town->City, happens when sufficient
        // amount of roads have been created between destinations
        // after roads are implemented, of course
        List<Town> townsToTurnIntoCities = new LinkedList<>();

        for(Town town: towns){
            if (town.hasCity()){
                continue;
            }
            List<Town> nearbyIndependentTowns = towns.stream()
                    .filter(t -> t != town)
                    .filter(t -> !t.hasCity())
                    .filter(t -> calculateDistance(town.getPoint(), t.getPoint()) <= cityDomainRange)
                    .toList();
            if (nearbyIndependentTowns.size() >= townsForCityCreation){
                townsToTurnIntoCities.add(town);
            }
        }

        for (Town town: townsToTurnIntoCities){
            boolean hasNearbyCity = false;
            for (City city: cities){
                if (calculateDistance(town.getPoint(), city.getPoint()) < cityToCityMinimumDistance){
                    hasNearbyCity = true;
                    break;
                }
            }
            // verify there are still enough nearby towns for city creation
            List<Town> nearbyIndependentTowns = towns.stream()
                    .filter(t -> t != town)
                    .filter(t -> !t.hasCity())
                    .filter(t -> calculateDistance(town.getPoint(), t.getPoint()) <= cityDomainRange)
                    .toList();
            if (nearbyIndependentTowns.size() < townsForCityCreation) {
                continue;
            }
            if (!hasNearbyCity){
                createCity(town, nearbyIndependentTowns);
            }
        }

        List<Village> villagesToTurnToTowns = new LinkedList<>();
        //check for possible village -> town formation
        for(Village village: villages){
            if (village.hasOwner()){
                continue;
            }
            List<Village> nearbyIndependentVillages = villages.stream()
                    .filter(v -> v != village)
                    .filter(v -> !v.hasOwner())
                    .filter(v -> calculateDistance(village.getPoint(), v.getPoint()) <= villageOwningRange)
                    .toList();
            if (nearbyIndependentVillages.size() >= villagesForTownCreation){
                villagesToTurnToTowns.add(village);
            }
        }

        for (Village village: villagesToTurnToTowns){
            boolean hasNearbyTown = false;
            for (Town town: towns){
                if (calculateDistance(village.getPoint(), town.getPoint()) < townToTownMinimumDistance){
                    hasNearbyTown = true;
                }
            }
            // verify there are still enough nearby towns for city creation
            List<Village> nearbyIndependentVillages = villages.stream()
                    .filter(v -> v != village)
                    .filter(v -> !v.hasOwner())
                    .filter(v -> calculateDistance(village.getPoint(), v.getPoint()) <= townDomainRange)
                    .toList();
            if (nearbyIndependentVillages.size() < villagesForTownCreation) {
                continue;
            }
            if (!hasNearbyTown){
                createTown(village, nearbyIndependentVillages);
            }
        }
        /*
        for (Farm farm: farms){
            farm.uncommonFarmTick();
        }*/
    }

    public void tickOwningBuildingsGainControlOverIndependents(){

        for (City city: cities){
            List<Town> nearbyIndependentTowns = towns.stream()
                    .filter(t -> !t.hasCity())
                    .filter(t -> calculateDistance(city.getPoint(), t.getPoint()) <= cityDomainRange)
                    .toList();
            for (Town town: nearbyIndependentTowns){
                town.setCity(city);
                city.addTown(town);
            }
        }

        // update nearby villages to come into village owning buildings domain

        List<VillageOwningBuilding> villageOwners = Stream.concat(
                towns.stream(),
                cities.stream()
        ).toList();

        for (VillageOwningBuilding villageOwningBuilding : villageOwners){
            List<Village> nearbyIndependentVillages = villages.stream()
                    .filter(v -> !v.hasOwner())
                    .filter(v -> calculateDistance(villageOwningBuilding.getPoint(), v.getPoint()) <= villageOwningRange)
                    .toList();
            for (Village village: nearbyIndependentVillages){
                village.setOwner(villageOwningBuilding);
                village.markCenter();
                villageOwningBuilding.addVillage(village);
            }
        }

        // change farms success level change level
        for (Farm farm: farms){
            double roll1 = ThreadLocalRandom.current().nextDouble(0.03);
            double roll2 = ThreadLocalRandom.current().nextDouble(0.03);
            farm.setSuccessLevelChange((roll1+roll2)-0.03);
        }
    }

    public void tickUpdateBuildingOwnershipByDistance(){
        //update towns to come into the correct city's control
        for (Town town: towns){
            if (town.hasCity()){
                double currentDistanceToCity = calculateDistance(town.getPoint(), town.getCity().getPoint());
                for (City city: cities){
                    if (city == town.getCity()){
                        continue;
                    }
                    if (calculateDistance(town.getPoint(), city.getPoint()) < currentDistanceToCity){
                        town.getCity().releaseTown(town);
                        town.setCity(city);
                        city.addTown(town);
                        break;
                    }
                }
            }
        }

    }

    public void checkIfNewFarmIsPartOfVillageCenter(Farm farm){
        for (Point point: gm.getMap().getAllValidAdjecantPointsToTarget(farm.getPoint())){
            if (point.getBuilding() instanceof Village){
                farm.setIsPartOfVillageCenter(true);
                return;
            }
        }
    }
    
    public double calculateDistance(Point p1, Point p2) {
        int dx = p1.getX() - p2.getX();
        int dy = p1.getY() - p2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
   
    public void convertFarmToVillageCenter(Farm farm){
        
        Point farmCenter = farm.getPoint();
        
        //prevent too close village creation
        for (Village village: villages){
            if (calculateDistance(farmCenter, village.getPoint()) < ((VILLAGE_DOMAIN_LIMIT*2)-1)){
                return;
            }
        }          
        for (Town town: towns){
            if (calculateDistance(farmCenter, town.getPoint()) < (VILLAGE_DOMAIN_LIMIT*2)){
                return;
            }
        }

        for (City city: cities){
            if (calculateDistance(farmCenter, city.getPoint()) < (VILLAGE_DOMAIN_LIMIT*2)){
                return;
            }
        }

        // TODO this could be done better, maybe filter in Map class
        List<Point> villagePoints = gm.getMap().getAllValidAdjecantPointsToTarget(farmCenter);
        for (Point point: villagePoints){
            if(point.getBuilding() instanceof Farm surroundingFarm){
                surroundingFarm.setIsPartOfVillageCenter(true);
            }
        }

        Village newVillage = new Village(farmCenter);
        newVillage.setPeople(farm.getPeople());
        newVillage.setFood(farm.getFood());
        villages.add(newVillage);
        farms.remove(farm);
        farm.resetState();
        inactiveFarms.add(farm);

        gm.getMap().replaceBuilding(farmCenter, newVillage);

        List<Point> pointsBelongingToVillage = new LinkedList<>(
                gm.getMap()
            .getAllPointsInCircleAroundTarget(farmCenter, VILLAGE_DOMAIN_LIMIT)
            .stream()
            .filter(p -> !p.isOwnedByBuilding())
            .filter(Point::isTerrainWalkable)
            .toList());
        
        Collections.shuffle(pointsBelongingToVillage);
        
        for (Point point: pointsBelongingToVillage){
            point.setOwnerBuilding(newVillage);
        }
        newVillage.setControlledLand(pointsBelongingToVillage);

        newVillage.setEmptyLand(
                pointsBelongingToVillage.stream()
                .filter(point -> point.getBuilding() == null)
                .collect(Collectors.toCollection(LinkedList::new)));

        LinkedList<Farm> independentFarmsWithinVillageDomain = gm.getMap().getIndependentFarmsNearby(farmCenter, VILLAGE_DOMAIN_LIMIT);
        for (Farm nearbyFarm: independentFarmsWithinVillageDomain){
            nearbyFarm.setFarmOwningBuilding(newVillage);
        }
        
        newVillage.setFarms(independentFarmsWithinVillageDomain);
    }

    public void createTown(Village village, List<Village> nearbyVillages){
        Point midPoint = village.getPoint();
        List<Point> townPoints = gm.getMap().getTownShapePointList(midPoint.getX(), midPoint.getY());

        villages.remove(village);
        Town newTown = new Town(midPoint);
        midPoint.setOwnerBuilding(newTown);
        newTown.setPeople(village.getPeople());
        newTown.setFood(village.getFood());
        newTown.setGold(village.getGold());
        gm.getMap().replaceBuilding(midPoint, newTown);
        towns.add(newTown);
        newTown.setControlledLand(village.getControlledLand());

        for (Point point: village.getControlledLand()){
            if (point.equals(midPoint)){
                continue;
            }
            if (townPoints.contains(point)){
                if (point.getBuilding() instanceof Farm farm){
                    farm.setFarmOwningBuilding(null);
                    farms.remove(farm);
                    gm.getMap().removeBuildingFromPoint(point);
                    farm.resetState();
                    inactiveFarms.add(farm);
                    if (farm.getFarmOwningBuilding() instanceof FarmOwningBuilding farmOwner){
                        farmOwner.destroyFarm(farm);
                    }
                }
                else if (point.getBuilding() != null && point.getBuilding() != newTown){
                    throw new RuntimeException("townArea had building that wasnt farm, fix this code");
                }

                TownArea townArea = new TownArea(point, newTown);
                gm.getMap().setBuildingOnPoint(point, townArea);
                newTown.addTownArea(townArea);
                point.createNewLandForPoint(LandType.TOWN);
            }
            if(point.getBuilding() instanceof Farm farm){
                farm.getFarmOwningBuilding().destroyFarm(farm);
                farm.setFarmOwningBuilding(newTown);
                newTown.addFarm(farm);
            }
            else if (point.isEmpty()){
                newTown.addEmptyPoint(point);
            }
            point.setOwnerBuilding(newTown);
        }
        for(Village nearbyVillage: nearbyVillages){
            nearbyVillage.setOwner(newTown);
            newTown.addVillage(nearbyVillage);
            nearbyVillage.markCenter();
        }
    }

    // TODO this method could be rewritten more cleanly. The order of things happening is important and might cause bugs if changed
    public void createCity(Town town, List<Town> nearbyTowns){
        Point midPoint = town.getPoint();
        City newCity = new City(midPoint);
        newCity.setPeople(town.getPeople());
        newCity.setFood(town.getFood());
        newCity.setGold(town.getGold());
        newCity.setWealth(town.getWealth());
        gm.getMap().replaceBuilding(midPoint, newCity);

        // first change the points making up cityArea/townArea
        for (TownArea TA: town.getTownAreaPoints()){
            Point point = TA.getPoint();
            if (point == midPoint){
                continue;
            }
            gm.getMap().removeBuildingFromPoint(point);
            point.createNewLandForPoint(LandType.DIRT);
        }

        List<Point> cityAreaPoints = gm.getMap().getCityShapePointList(midPoint.getX(), midPoint.getY());
        for (Point cityAreaPoint: cityAreaPoints){
            cityAreaPoint.getPointOwner().removeFromControlledLand(cityAreaPoint);
            cityAreaPoint.getPointOwner().occupyPoint(cityAreaPoint);
            cityAreaPoint.setOwnerBuilding(newCity);
            cityAreaPoint.createNewLandForPoint(LandType.CITY);
            if (cityAreaPoint == midPoint){
                continue;
            }
            if (cityAreaPoint.getBuilding() instanceof Farm farm){
                if(farm.getFarmOwningBuilding() instanceof FarmOwningBuilding FOB){
                    FOB.destroyFarm(farm);
                    FOB.removeFromControlledLand(cityAreaPoint);
                    FOB.occupyPoint(cityAreaPoint);
                }
                farm.removeFarmingOwningBuilding();
                gm.getMap().removeBuildingFromPoint(cityAreaPoint);
                farms.remove(farm);
                farm.resetState();
                inactiveFarms.add(farm);
            }

            // TODO add else if farmBuilding isnt farm but something else, that building should be properly removed from all lists etc
            CityArea ca = new CityArea(cityAreaPoint, newCity);
            newCity.addCityArea(ca);
            if (!cityAreaPoint.isEmpty()){
                gm.getMap().replaceBuilding(cityAreaPoint, ca);
            } else {
                gm.getMap().setBuildingOnPoint(cityAreaPoint, ca);
            }
        }

        // change ownership/control of directly controlled land
        newCity.setControlledLand(town.getControlledLand());
        for(Point point: newCity.getControlledLand()){
            point.setOwnerBuilding(newCity);
            if (point.getBuilding() instanceof Farm farm){
                farm.setFarmOwningBuilding(newCity);
                newCity.addFarm(farm);
            }
            if (point.getBuilding() == null){
                newCity.addEmptyPoint(point);
            }
        }

        // change ownership of surrounding villages
        newCity.setVillages(town.getVillages());
        for (Village village: town.getVillages()){
            village.setOwner(newCity);
        }

        cities.add(newCity);
        towns.remove(town);
        for(Town nearbyTown: nearbyTowns){
            newCity.addTown(nearbyTown);
            nearbyTown.setCity(newCity);
        }
    }

    // TODO this should be redone and handled better. Deal with owner, remove from farmlist etc.
    public void destroyFarm(Farm farm){
        gm.getMap().removeBuildingFromPoint(farm.getPoint());
    }


    /**
     * ALL METHODS BELOW
     * are strictly used for testing purposes
     * remove freely, although buttons connected to them might need to be changed then
     */

    public void experiment2(){
        int a = experimentTicker % 5;
        System.out.println("a = " + a);
        ArrayList<Point> pointsToMake = gm.getMap().getPointsInCircleAroundTarget(new Point(50 + (a*50),50, LandType.GRASSLAND),experimentTicker);
        for (Point point: pointsToMake){
            createFarmAtPoint(point.getX(), point.getY());
        }
        experimentTicker++;
        gm.getGridPanel().updateUI();
    }
    
    public void experiment3(){
        int farmAmount = 0;
        Point[][] grid = gm.getMap().getGrid();
        for(int x=0; x< grid.length; x++){
            for(int y=0; y<grid[x].length; y++){
                if(grid[x][y].getBuilding() instanceof Farm){
                    farmAmount++;
                }
            }
        }
        System.out.println("Farms according to list: " + farms.size() + ", Real amount of farms: " + farmAmount + ", difference: " + (farms.size()-farmAmount));
        System.out.println("Amount of villages: " + villages.size() + ", towns: " + towns.size() + ", cities: " + cities.size());
    }
    
    public void experiment4(){
        if (farms == null || farms.isEmpty()) {
            System.out.println("The farms list is empty or not initialized. Cannot run test.");
            return;
        }

        System.out.println("Starting performance test: calling getRandomEmptyWalkablePointAdjecantToTarget 1,000,000 times...");
        System.out.println("Number of farms is: " + farms.size());

        // Use nanoTime() for high-resolution timing.
        long startTime = System.nanoTime();
        int iterations = 1000;

        // This is the optimized loop that avoids the slow LinkedList.get() operation
        int count = 0;
        for(int x=0; x<iterations; x++){
            for (Farm farm : farms) {
                gm.getMap().getRandomEmptyWalkablePointAdjecantToTarget(farm.getPoint());
                count++;
            }
            if (x%(iterations/100) == 0){
                System.out.println((x / (iterations/100))+"% done");
            }
        }


        long endTime = System.nanoTime();
        long durationInNanos = endTime - startTime;

        // Convert the duration to milliseconds for readability.
        double durationInSeconds = durationInNanos / 1_000_000_000.0;

        System.out.printf("Test completed. The operation took %.4f seconds.%n", durationInSeconds);
        System.out.println("Time");
        System.out.println("Iterations: " + iterations*farms.size() + ". Time per farm: " + durationInSeconds/(iterations*farms.size()) + " ms");
    }
    
    public void experiment5(){
        List<Point> points = List.of(
            gm.getMap().getGrid()[150][150],
                        gm.getMap().getGrid()[151][150],
                        gm.getMap().getGrid()[152][150],
                        gm.getMap().getGrid()[150][151],
                        gm.getMap().getGrid()[151][151],
                        gm.getMap().getGrid()[152][151]
        );
        
        for (Point point: points){
            Farm farm = createFarmAtPoint(point.getX(), point.getY());
            farm.setFood(10);
        }
    }

    public void experiment15(){
        for(int x=0; x<gm.getMap().getWidth(); x=x+2){
            for(int y=0; y<gm.getMap().getHeight(); y++){
                Point newFarmPoint = gm.getMap().getPoint(x,y);
                if (!newFarmPoint.isEmpty()){
                    continue;
                }
                Farm newFarm = getFarmFromPool();
                newFarm.activate(newFarmPoint, 2);
                gm.getMap().setBuildingOnPoint(newFarm.getPoint(), newFarm);
                checkIfNewFarmIsPartOfVillageCenter(newFarm);
                farms.add(newFarm);
            }
        }
        gm.getGridPanel().updateUI();
    }

    public void checkVillageDomain(){
        for(Village village: villages){
            for(Point point: village.getControlledLand()){
                if (point.getBuilding() != null && point.getBuilding() instanceof Farm farm && farm.getFarmOwningBuilding() != village){
                    System.out.println("SITUATION 32");
                    System.out.println(village.getInfo());
                    System.out.println(farm.getInfo());
                    System.out.println(point.getInfo());
                }
            }
        }
    }
    
    public Farm createFarmAtPoint(int x, int y){
        Point point = gm.getMap().getPoint(x, y);
        point.createNewLandForPoint(LandType.GRASSLAND);
        Farm farm = getFarmFromPool();
        farm.activate(point, 1);
        
        gm.getMap().setBuildingOnPoint(point, farm);
        farms.add(farm);
        gm.getGridPanel().updateUI();
        return farm;
    }

    public void printMapInfo(){
        System.out.println("map info: ");
        System.out.println("Farms: " + farms.size());
        System.out.println("Villages: " + villages.size());
        System.out.println("Towns: " + towns.size());
        int total = gm.getMap().getGrid().length * gm.getMap().getGrid()[0].length;
        System.out.println("Whole map size: " + total);
        System.out.println("All buildings is total: " + (farms.size()+towns.size()+villages.size()));
    }
    
    public void createCompleteVillageAt(int x, int y){
        Point point = gm.getMap().getPoint(x, y);
        List<Point> surroundings = gm.getMap().getAllValidAdjecantPointsToTarget(point);
        surroundings.add(point);
        for(Point p: surroundings){
            p.createNewLandForPoint(LandType.GRASSLAND);
            Farm farm = getFarmFromPool();
            farm.activate(p, 2);
            gm.getMap().setBuildingOnPoint(p, farm);
            farm.setFood(30);
            farm.setPeople(3);
            farms.add(farm);
            
        }
        convertFarmToVillageCenter((Farm)point.getBuilding());
        gm.getGridPanel().updateUI();
    }
    
    public void createWaterPath(){
        System.out.println("xx3");
        Point selectedPoint = gm.getGridPanel().getSelectedPoint();
        if (selectedPoint == null){
            return;
        }
        Point target = gm.getMap().getPoint(150,150);
        List<Point> road = gm.pathfinder.getPathBetween(selectedPoint, target);
        for(Point p: road){
            p.createNewLandForPoint(LandType.WATER);
        }
        System.out.println(road.size() + " = raodens langd");
        gm.getGridPanel().updateUI();
    }
    
    public Farm createFarmAtRandomPoint(){
        Point randomPoint = gm.getMap().getRandomPoint();
        if (!randomPoint.isEmpty()){
            return null;
        }
        randomPoint.createNewLandForPoint(LandType.GRASSLAND);
        Farm farm = getFarmFromPool();
        farm.activate(randomPoint, 1);
        
        gm.getMap().setBuildingOnPoint(randomPoint, farm);
        farms.add(farm);
        System.out.println("Farm created at " + farm.toString());
        gm.getGridPanel().updateUI();
        return farm;
    }

    public void create20FarmAtRandomPoint(){
        for (int x=0; x<20; x++){
            createFarmAtRandomPoint();
        }
    }

    // TODO eventually remove. but useful for now considering how often lists are adjusted
    public static <T> Set<T> findDuplicatesIterative(List<T> list) {
        Set<T> seen = new java.util.HashSet<>();
        Set<T> duplicates = new java.util.HashSet<>();

        for (T item : list) {
            // If 'add' returns false, the item was already in the 'seen' set,
            // meaning this is its second (or third, etc.) appearance.
            if (!seen.add(item)) {
                duplicates.add(item);
            }
        }
        return duplicates;
    }

    public void currentExperiment(){

        Point point = gm.getGridPanel().getSelectedPoint();
        List<Point> availableSpots = gm.getMap().getAllEmptyAndWalkablePointsInCircleAroundTarget(
                point, villageSpreadingFarmsDistance);
        System.out.println(availableSpots);
        if (!availableSpots.isEmpty()){
            Point p = availableSpots.get(ThreadLocalRandom.current().nextInt(availableSpots.size()));
            System.out.println(p.getInfo());
        }
    }

    public void experimentTimeMethod(){

        /**
         * In this method, create a for loop and call whatever feature you want thousands of times
         * to get an approximate time how long it takes to perform
         */
        long startTime = System.currentTimeMillis();
        Village village = villages.get(0);
        Point newFarmPoint;
        for (int i=0; i<2000; i++){

            if (!village.timeToRedoNearbySearch()){
                continue;
            }
            List<Point> availableSpots = gm.getMap().getAllEmptyAndWalkablePointsInCircleAroundTarget(
                    village.getPoint(), villageSpreadingFarmsDistance);
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
    }

}