package empirebuilder;

import LandTypes.LandType;
import buildings.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

class Game{
    
    
    GameManager gm;
    Random random;
    int tickCounter;
    List<Farm> farms;
    List<Village> villages;
    List<Town> towns;

    List<Farm> farmsToAdd;
    List<Farm> farmsToRemove;
    List<Farm> farmToConvertToVillage;
    List<Village> villagesToDestroy;
    
    int experimentTicker=1;
    
    final int FOOD_COST_TO_MULTIPLY = 10;
    final int FOOD_COST_TO_IMPROVE = 20;
    final int FARMS_TO_CREATE_VILLAGE = 8;
    final int DISTANCE_BETWEEN_FARMS_FOR_VILLAGE_CREATION = 3;
    final int VILLAGE_DOMAIN_LIMIT = 4;
    final int VILLAGE_DISTANCE_FOR_TOWN_FORMATION = 25;
    final int TownCheckDistance = 25;
    final int townFormDistance = 15;
    final int villageJoinTownDistance = 10;
    final int farmsForTownCreation = 5;
    
    final boolean LOGGING = false;
    
    Game(GameManager gameManager){
        this.gm = gameManager;
        random = new Random();
        farms = new LinkedList();
        villages = new LinkedList();
        towns = new LinkedList();
        tickCounter=0;

        farmsToAdd = new LinkedList();
        farmsToRemove = new LinkedList();
        farmToConvertToVillage = new LinkedList();
        villagesToDestroy = new LinkedList();
    }
    
    public void tick(){
        
        tickCounter++;
        for(Farm farm: farms) {

            farm.tick();

            if (!farm.belongsToFarmOwningBuilding()) {
                if (farm.lastPersonDied()) {
                    farmsToRemove.add(farm);
                    continue;
                }
                if (farm.isTimeToCreateNewFarm()) {
                    boolean farmWasCreatedNearby = true;

                    Point newFarmPoint;
                    Farm newFarm;

                    if (random.nextInt(10) == 0) {
                        farmWasCreatedNearby = false;
                        newFarmPoint = gm.getMap().getRandomEmptyPoint();
                        if (newFarmPoint == null || newFarmPoint.isOwnedByBuilding()) {
                            continue;
                        }
                    }
                    else {
                        newFarmPoint = gm.getMap().getRandomEmptyPointAdjecantToTarget(farm.getPoint());
                        if (newFarmPoint == null) {
                            continue;
                        }
                    }
                    newFarm = new Farm(newFarmPoint);
                    checkIfNewFarmIsPartOfVillageCenter(newFarm);
                    newFarmPoint.createNewLandForPoint(LandType.GRASSLAND);

                    farmsToAdd.add(newFarm);
                    farm.halvePeopleAmount();
                    gm.getMap().setBuildingOnPoint(newFarmPoint, newFarm);

                    if (LOGGING) {
                        System.out.println("Farm " + farm.getId() + ") split and a new farm " + newFarm.getId() + " was created at " + newFarmPoint.toString());
                    }
                    if (farmWasCreatedNearby) {
                        int foodStarter =
                                gm.getMap().getIndependentFarmsNearby(newFarmPoint, 2).size();
                        newFarm.setFood(foodStarter * 2);
                    }
                    if (newFarmPoint.getPointOwner() != null){ //TODO this should be earlier in the chain
                        newFarm.setFarmOwningBuilding(newFarmPoint.getPointOwner());
                        newFarmPoint.getPointOwner().addFarm(newFarm);
                    }
                    int independantFarmsNearby = gm.getMap().getIndependentFarmsNearby(newFarmPoint, DISTANCE_BETWEEN_FARMS_FOR_VILLAGE_CREATION).size();
                    if (independantFarmsNearby >= FARMS_TO_CREATE_VILLAGE) { //TODO remove when upgrades happe in different loop
                        farmToConvertToVillage.add(newFarm);
                    }
                }
            }
        }

        handleOwnedFarmsBuildingsLoop(villages);
        handleOwnedFarmsBuildingsLoop(towns);
        
        //TODO create destroy village
        //TODO create destroy building
        villages.removeAll(villagesToDestroy);

        farms.addAll(farmsToAdd);
        for (Farm toRemoveFarm : farmsToRemove) {
            if (toRemoveFarm.belongsToFarmOwningBuilding()) {
                toRemoveFarm.getFarmOwningBuilding().addEmptyPoint(toRemoveFarm.getPoint());
            }
            destroyFarm(toRemoveFarm);
        }
        farms.removeAll(farmsToRemove);
        farmToConvertToVillage.forEach(farmToConvert -> convertFarmToVillageCenter(farmToConvert));

        gm.getGridPanel().updateUI();

        farmsToAdd.clear();
        farmsToRemove.clear();;
        farmToConvertToVillage.clear();
        villagesToDestroy.clear();
    }

    public void handleOwnedFarmsBuildingsLoop(List<? extends FarmOwningBuilding> buildingList){
        for (FarmOwningBuilding building: buildingList){

            //TODO implement slow village decline
            if (building.hasFoodToCreateNewFarm()){
                Point newFarmPoint;
                building.deductNewFarmCost();
                boolean wasCreatedWithinDomain = true;
                if (!building.getEmptyLand().isEmpty()){
                    newFarmPoint = building.getRandomEmptySpotWithinDomain();
                }
                else {
                    boolean createdAtRandomPlace = random.nextInt(10) == 0;
                    if (createdAtRandomPlace){
                        wasCreatedWithinDomain = false;
                        newFarmPoint = gm.getMap().getRandomEmptyPoint();
                        if (newFarmPoint == null || newFarmPoint.isOwnedByBuilding()){
                            continue;
                        }
                    }
                    else {
                        continue;
                    }
                }
                if (newFarmPoint == null){
                    System.out.println("OBS OBS SHOULD NOT HAPPEN");
                    throw new RuntimeException("village failed to create farm");
                }
                newFarmPoint.createNewLandForPoint(LandType.GRASSLAND);
                if (newFarmPoint.getBuilding() instanceof Town || newFarmPoint.getBuilding() instanceof TownArea) {
                    continue;
                }
                Farm newFarm = new Farm(newFarmPoint);
                checkIfNewFarmIsPartOfVillageCenter(newFarm);
                gm.getMap().setBuildingOnPoint(newFarmPoint, newFarm);
                if (wasCreatedWithinDomain){
                    newFarm.setFarmOwningBuilding(building);
                    building.addFarm(newFarm);
                }
                else if (newFarmPoint.getPointOwner() != null){
                    newFarm.setFarmOwningBuilding(newFarmPoint.getPointOwner());
                    newFarmPoint.getPointOwner().addFarm(newFarm);
                }
                farmsToAdd.add(newFarm);
            }
        }
    }

    public void checkIfNewFarmIsPartOfVillageCenter(Farm farm){
        for (Point point: gm.getMap().getAllValidAdjecantPointsToTarget(farm.getPoint())){
            if (point.getBuilding() instanceof Village){
                farm.setIsPartOfVillageCenter(true);
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
            if (calculateDistance(farmCenter, village.getPoint()) < (VILLAGE_DOMAIN_LIMIT*2)){
                return;
            }
        }          
        for (Town town: towns){
            if (calculateDistance(farmCenter, town.getPoint()) < (VILLAGE_DOMAIN_LIMIT*2)){
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

        Village newVillage = new Village(farmCenter, farmCenter);
        newVillage.setFood(10);
        villages.add(newVillage);
        farms.remove(farm);

        gm.getMap().replaceBuilding(farmCenter, newVillage);

        List<Point> pointsBelongingToVillage = new LinkedList<>(
                gm.getMap()
            .getAllPointsInCircleAroundTarget(farmCenter, VILLAGE_DOMAIN_LIMIT)
            .stream()
            .filter(p -> !p.isOwnedByBuilding())
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

        List<Farm> independentFarmsWithinVillageDomain = gm.getMap().getIndependentFarmsNearby(farmCenter, VILLAGE_DOMAIN_LIMIT);
        for (Farm nearbyFarm: independentFarmsWithinVillageDomain){
            nearbyFarm.setFarmOwningBuilding(newVillage);
        }
        
        newVillage.setFarms((LinkedList)(independentFarmsWithinVillageDomain));
        
        boolean nearTown = false;
        for(Town town: towns){
            if(calculateDistance(town.getPoint(), newVillage.getPoint()) < villageJoinTownDistance){
                nearTown=true;
                newVillage.setTown(town);
                town.addVillage(newVillage);
                newVillage.markCenter();
            }
        }
        if(!nearTown){
            checkForTownFormation(newVillage);
        }
    }
    
    public void checkForTownFormation(Village newVillage) {
        // check if there's sufficient villages to create town
        List<Village> nearbyIndependentVillages = villages.stream()
                .filter(v -> v != newVillage)
                .filter(v -> !v.hasTown())
                .filter(v -> calculateDistance(newVillage.getPoint(), v.getPoint()) <= TownCheckDistance)
                .collect(Collectors.toList());

        nearbyIndependentVillages.add(newVillage);
        if (nearbyIndependentVillages.size() < farmsForTownCreation){
            return;
        }
        
        // find what village to become a towncenter
        for (Village candidate : nearbyIndependentVillages) {
            List<Village> surroundingVillags = nearbyIndependentVillages.stream()
                    .filter(v -> v != candidate)
                    .filter(v -> calculateDistance(candidate.getPoint(), v.getPoint()) <= townFormDistance)
                    .collect(Collectors.toCollection(LinkedList::new));
            //surroundingVillags.add(candidate);

            if (surroundingVillags.size() >= farmsForTownCreation) {
                createTown(candidate, surroundingVillags);
                return;
            }
        }
    }

    public void createTown(Village villageCenter, List<Village> surroundingVillages){
        Point midPoint = villageCenter.getPoint();
        List<Point> townPoints = gm.getMap().getTownShapePointList(midPoint.getX(), midPoint.getY());
        
        villages.remove(villageCenter);
        Town town = new Town(midPoint);
        gm.getMap().replaceBuilding(midPoint, town);
        towns.add(town);
        town.setControlledLand(villageCenter.getControlledLand());
        for (Point point: villageCenter.getControlledLand()){
            if (townPoints.contains(point)){
                if (point.getBuilding() instanceof Farm farm){
                    farm.setFarmOwningBuilding(null);
                    farms.remove(farm);
                }
                else if (point.getBuilding() != null && point.getBuilding() != town){
                    throw new RuntimeException("townArea had building that wasnt farm, fix this code");
                }

                TownArea townArea = new TownArea(point, town);
                gm.getMap().setBuildingOnPoint(point, townArea);
                town.addTownArea(townArea);
                point.createNewLandForPoint(LandType.TOWN);
            }
            if(point.getBuilding() instanceof Farm farm){
                farm.setFarmOwningBuilding(town);
                town.addFarm(farm);
            }
            else if (point.isEmpty()){
                town.addEmptyPoint(point);
            }
            point.setOwnerBuilding(town);
        }
        for(Village village: surroundingVillages){
            village.setTown(town);
            village.markCenter();
        }
        town.setVillages((LinkedList<Village>) surroundingVillages);
    }
    
    public void destroyVillage(Village village){
        for (Point point: village.getControlledLand()){
            point.setOwnerBuilding(null);
        }
    }
    
    public void destroyFarm(Farm farm){
        farm.getPoint().createNewLandForPoint(LandType.DIRT);
        gm.getMap().removeBuildingFromPoint(farm.getPoint());
    }
    
    public void experiment(){
        Point prevPoint = farms.get(farms.size()-1).getPoint();
        Point newPoint = gm.getMap().getGrid()[prevPoint.getX()+1][prevPoint.getY()];
        newPoint.createNewLandForPoint(LandType.GRASSLAND);
        
        Farm farm = new Farm(newPoint);
        
        gm.getMap().setBuildingOnPoint(newPoint, farm);
        farms.add(farm);
        gm.getGridPanel().updateUI();
    }
    
    public void experiment2(){
        int a = experimentTicker % 5;
        System.out.println("aa");
        ArrayList<Point> pointsToMake = gm.getMap().getPointsInCircleAroundTarget(new Point(50 + (a*50),50, LandType.GRASSLAND),experimentTicker);
        for (Point point: pointsToMake){
            createFarmAtPoint(point.getX(), point.getY());
        }
        experimentTicker++;
        gm.getGridPanel().updateUI();
    }
    
    public void experiment3(){
        System.out.println("bb");
        LinkedList<Point> pointsToMake = gm.getMap().getAllEmptyPointsInCircleAroundTarget(new Point(50,50, LandType.GRASSLAND), 5);
        for (Point point: pointsToMake){
            createFarmAtPoint(point.getX(), point.getY());
        }
         gm.getGridPanel().updateUI();
    }
    
    public void experiment4(){
        Point[][] grid = gm.getMap().getGrid();
        for(int x=0; x<grid.length-1; x++){
            for(int y=0; y<grid[x].length-1; y++){
                if(grid[x][y].getLandType() ==LandType.GRASSLAND){
                    System.out.println("INTE ANDRATS!");
                }
                if(grid[x][y].getLand().getLandType() ==LandType.GRASSLAND){
                    System.out.println("INTE ANDRATS2!");
                }
                if(!grid[x][y].isEmpty()){
                    System.out.println("fel3");
                    System.out.println(grid[x][y].toString());
                    grid[x][y] = new Point(x, y, LandType.WATER);
                }
            }
        }
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
        Farm farm = new Farm(point);
        
        gm.getMap().setBuildingOnPoint(point, farm);
        farms.add(farm);
        System.out.println("Farm created at " + farm.toString());
        gm.getGridPanel().updateUI();
        return farm;
    }

    public void printMapInfo(){
        System.out.println("map info: ");
        System.out.println("empty set points: " + gm.getMap().getEmptyPoints().size());
        System.out.println("empty list points: " + gm.getMap().getEmptyPointList().size());
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
            Farm farm = new Farm(p);
            gm.getMap().setBuildingOnPoint(p, farm);
            farm.setFood(30);
            farm.setPeople(3);
            farms.add(farm);
            
        }
        convertFarmToVillageCenter((Farm)point.getBuilding());
        gm.getGridPanel().updateUI();
    }
    
    
    
    public Farm createFarmAtRandomPoint(){
        //TODO make the 
        Point randomPoint = gm.getMap().getRandomEmptyPoint();
        randomPoint.createNewLandForPoint(LandType.GRASSLAND);
        Farm farm = new Farm(randomPoint);
        
        gm.getMap().setBuildingOnPoint(randomPoint, farm);
        farms.add(farm);
        System.out.println("Farm created at " + farm.toString());
        gm.getGridPanel().updateUI();
        return farm;
    }
    
}