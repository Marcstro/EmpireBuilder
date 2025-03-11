package empirebuilder;

import LandTypes.LandType;
import buildings.Farm;
import buildings.Village;
import buildings.Town;
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
    
    int experimentTicker=1;
    
    final int FOOD_COST_TO_MULTIPLY = 10;
    final int FOOD_COST_TO_IMPROVE = 20;
    final int FARMS_TO_CREATE_VILLAGE = 8;
    final int DISTANCE_BETWEEN_FARMS_FOR_VILLAGE_CREATION = 3;
    final int VILLAGE_DOMAIN_LIMIT = 4;
    
    final boolean LOGGING = false;
    
    Game(GameManager gameManager){
        this.gm = gameManager;
        random = new Random();
        farms = new LinkedList();
        villages = new LinkedList();
        towns = new LinkedList();
        tickCounter=0;
    }
    
    public void tick(){
        
        tickCounter++;

        //gm.getMap().tick();
        
        
        List<Farm> farmsToAdd = new ArrayList();
        List<Farm> farmsToRemove = new ArrayList();
        List<Farm> farmToConvertToVillage = new ArrayList();
        
        for(Farm farm: farms) {

            farm.tick();

            if (!farm.hasVillage()) {

                if (
//                        farm.getPoint().getLandType() != LandType.VILLAGE &&
                        farm.lastPersonDied()) { //TODO adjust when farms in villages no longer die
                    farmsToRemove.add(farm);
                    continue;
                }

                if (farm.hasEnoughToStartNewFarm()) {
                    boolean farmWasCreatedNearby = true;

                    Point newFarmPoint;
                    Farm newFarm;

                    if (random.nextInt(10) == 0) {
                        farmWasCreatedNearby = false;
                        newFarmPoint = gm.getMap().getRandomEmptyPoint();
                        if (newFarmPoint.hasVillage()) {
                            continue;
                        }
                        newFarm = new Farm(newFarmPoint);
                    }
                    else {
                        newFarmPoint = gm.getMap().getRandomEmptyPointAdjecantToTarget(farm.getPoint());
                        if (newFarmPoint == null) {
                            continue;
                        }
                        newFarm = new Farm(newFarmPoint);
                    }

                    if (newFarmPoint.getLandType() != LandType.VILLAGE) {
                        newFarmPoint.createNewLandForPoint(LandType.GRASSLAND);
                    }

                    farmsToAdd.add(newFarm);
                    farm.halvePeopleAmount();
                    newFarmPoint.setBuilding(newFarm);

                    if (LOGGING) {
                        System.out.println("Farm " + farm.getId() + ") split and a new farm " + newFarm.getId() + " was created at " + newFarmPoint.toString());
                    }
                    if (farmWasCreatedNearby) {
                        int foodStarter =
                                gm.getMap().getIndependentFarmsNearby(newFarmPoint, 2).size();
                        newFarm.setFood(foodStarter * 2);
                    }
                        int independantFarmsNearby = gm.getMap().getIndependentFarmsNearby(newFarmPoint, DISTANCE_BETWEEN_FARMS_FOR_VILLAGE_CREATION).size();
                        if (independantFarmsNearby >= FARMS_TO_CREATE_VILLAGE) {
                            farmToConvertToVillage.add(newFarm);
                        }
                }
            }
        }

        for (Village village: villages){
            if (village.hasFoodToCreateNewFarm()){
                Point newFarmPoint;
                village.deductNewFarmCost();
                boolean wasCreatedWithinDomain = true;
                if (!village.getEmptyLand().isEmpty()){
                    newFarmPoint = village.getRandomEmptySpotWithinDomain();
                }
                else {
                    boolean createdAtRandomPlace = random.nextInt(10) == 0;
                    if (createdAtRandomPlace){
                        wasCreatedWithinDomain = false;
                        newFarmPoint = gm.getMap().getRandomEmptyPoint();
                    }
                    else {
                        continue;
                    }
                }
                if (newFarmPoint == null){
                    System.out.println("OBS OBS SHOULD NOT HAPPEN");
                    throw new RuntimeException("village failed to create farm");
                }
                if (newFarmPoint.getLandType() != LandType.VILLAGE) {
                    newFarmPoint.createNewLandForPoint(LandType.GRASSLAND);
                }
                
                Farm newFarm = new Farm(newFarmPoint);
                newFarmPoint.setBuilding(newFarm);
                if(wasCreatedWithinDomain){
                    newFarm.setVillage(village);
                    village.addFarm(newFarm);
                }
                farmsToAdd.add(newFarm);
            }
        }



        farms.addAll(farmsToAdd);
        for (Farm toRemoveFarm : farmsToRemove) {
            if (toRemoveFarm.hasVillage()) {
                toRemoveFarm.getVillage().addEmptyPoint(toRemoveFarm.getPoint());
            }
            destroyFarm(toRemoveFarm);
        }
        farms.removeAll(farmsToRemove);
        farmToConvertToVillage.forEach(farmToConvert -> convertFarmToVillageCenter(farmToConvert));

        gm.getGridPanel().updateUI();
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
        
        List<Point> villagePoints = gm.getMap().getAllValidAdjecantPointsToTarget(farmCenter);
        villagePoints.add(farmCenter);
        
        //first set up village center
        Village newVillage = new Village(farmCenter, farmCenter);
        villages.add(newVillage);
        farms.remove(farm);
        farmCenter.setBuilding(newVillage);
        
        //set all adjecant points to have village land (While having farm building)
        for(Point point: villagePoints){
            //point.setLand(LandType.VILLAGE);
            point.createNewLandForPoint(LandType.VILLAGE);
        }
        
        //set all points within village distance radius to have village = this one
        List<Point> pointsBelongingToVillage = new LinkedList<>(
                gm.getMap()
            .getAllPointsInCircleAroundTarget(farmCenter, VILLAGE_DOMAIN_LIMIT)
            .stream()
            .filter(p -> !p.hasVillage())
            .toList());
        Collections.shuffle(pointsBelongingToVillage);
        
        for (Point point: pointsBelongingToVillage){
            point.setVillage(newVillage);
        }
        newVillage.setControlledLand(pointsBelongingToVillage);

        newVillage.setEmptyLand(
                pointsBelongingToVillage.stream()
                .filter(point -> point.getBuilding() == null)
                .collect(Collectors.toCollection(LinkedList::new))); 
        List<Point> farmsBelongingToVillage = gm.getMap().getIndependentFarmsNearby(farmCenter, VILLAGE_DOMAIN_LIMIT);
        
        for(Point point: farmsBelongingToVillage){
            if (point.getBuilding() instanceof Farm farm1){
                farm1.setVillage(newVillage);
            }
        }
        newVillage.setFarms((LinkedList)(farmsBelongingToVillage));
        
//        if (villages.size() > 3){
//            System.out.println("OMG OMG");
//            List<Point> townPoints = newVillage.getControlledLand();// gm.getMap().getAllValidAdjecantPointsToTarget(farm.getPoint());
//            //townPoints.add(farm.getPoint());
//            Town town = new Town(farmCenter);
//            for (Point point: townPoints){
//                point.setBuilding(town);
//                point.createNewLandForPoint(LandType.TOWN);
//            }
//            towns.add(town);
//        }
    }
    
    public void destroyVillage(Village village){
        for (Point point: village.getControlledLand()){
            point.setVillage(null);
        }
    }
    
    public void destroyFarm(Farm farm){
        farm.getPoint().createNewLandForPoint(LandType.DIRT);
        farm.getPoint().setBuilding(null);
    }
    
    public void experiment(){
        Point prevPoint = farms.get(farms.size()-1).getPoint();
        Point newPoint = gm.getMap().getGrid()[prevPoint.getX()+1][prevPoint.getY()];//.setLandType(LandType.GRASSLAND));
        newPoint.createNewLandForPoint(LandType.GRASSLAND);
        
        Farm farm = new Farm(newPoint);
        
        newPoint.setBuilding(farm);
        farms.add(farm);
        //gm.getMap().setPoint(newPoint);
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
    
    public Farm createFarmAtPoint(int x, int y){
        Point point = gm.getMap().getPoint(x, y);
        point.createNewLandForPoint(LandType.GRASSLAND);
        Farm farm = new Farm(point);
        
        point.setBuilding(farm);
        farms.add(farm);
        System.out.println("Farm created at " + farm.toString());
        gm.getGridPanel().updateUI();
        return farm;
    }
    
    
    public Farm createFarmAtRandomPoint(){
        //TODO make the 
        Point randomPoint = gm.getMap().getRandomEmptyPoint();
        randomPoint.createNewLandForPoint(LandType.GRASSLAND);
        Farm farm = new Farm(randomPoint);
        
        randomPoint.setBuilding(farm);
        farms.add(farm);
        System.out.println("Farm created at " + farm.toString());
        gm.getGridPanel().updateUI();
        return farm;
    }
    
}