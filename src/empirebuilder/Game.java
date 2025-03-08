package empirebuilder;

import LandTypes.Grassland;
import LandTypes.LandType;
import buildings.Farm;
import buildings.Village;
import empirebuilder.Point;
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
        tickCounter=0;
    }
    
    public void tick(){
        
        tickCounter++;

        //gm.getMap().tick();
        
        
        List<Farm> farmsToAdd = new ArrayList();
        List<Farm> farmsToRemove = new ArrayList();
        List<Farm> farmToConvertToVillage = new ArrayList();
        
        for(Farm farm: farms){
            
            
            
            
            
            if (farm.getLand() instanceof Grassland grassland){
                farm.tick();
                if (farm.lastPersonDied()){
                    farmsToRemove.add(farm);
                    continue;
                }
            }
            
            //THE FOLLOWING IS THE OLD ALGORITHM, REVERT FROM BELOW
            
            
            if (farm.hasEnoughToStartNewFarm()){
                
                
                boolean farmWasCreatedNearby = true;
                Point newFarmPoint = null;
                boolean abortCreation = false;
                
                if (random.nextInt(10)==0){
                    farmWasCreatedNearby=false;
                    newFarmPoint = gm.getMap().getRandomEmptyPoint();
                    for (Village village: villages){
                        if (calculateDistance(newFarmPoint, village.getPoint()) < VILLAGE_DOMAIN_LIMIT){
                            abortCreation = true;
                        }
                    }  
                }
                else if (farm.hasVillage()){
                    newFarmPoint = farm.getVillage().getRandomEmptySpotWithinDomain();
                    if (newFarmPoint == null){
                        continue;
                    }
                }
                else {
                    newFarmPoint = gm.getMap().getRandomEmptyPointAdjecantToTarget(farm.getPoint());
                    if (newFarmPoint == null){
                        continue;
                    }
                }
                
                if (abortCreation){
                    continue;
                }
                
                newFarmPoint.setLandType(LandType.GRASSLAND);
                Farm newFarm = new Farm(newFarmPoint);
                farmsToAdd.add(newFarm);
                farm.halvePeopleAmount();
                newFarmPoint.setBuilding(newFarm);

                if (LOGGING){
                    System.out.println("Farm "+farm.getId()+") split and a new farm " + newFarm.getId() + " was created at " + newFarmPoint.toString());
                }
                if (farm.hasVillage() && farmWasCreatedNearby){
                    newFarm.setFood(10);
                }
                else if (farmWasCreatedNearby){
                    int foodStarter = 
                        gm.getMap().getIndependentFarmsNearby(newFarmPoint, 2).size();
                    newFarm.setFood(foodStarter*2);
                }
                
                if (farm.hasVillage() && farmWasCreatedNearby){
                    newFarm.setVillage(farm.getVillage());
                }
                else {
                    int independantFarmsNearby = gm.getMap().getIndependentFarmsNearby(newFarmPoint, DISTANCE_BETWEEN_FARMS_FOR_VILLAGE_CREATION).size();
                    if (independantFarmsNearby >= FARMS_TO_CREATE_VILLAGE){
                        farmToConvertToVillage.add(newFarm);
                    }
                }
                
                //create suitable point
                
                //create farm at point
                //set variables for farm
                
                //village stuff:
                
                //if farm belongs to village AND was created locally
                //new farm should belong to same village
                
                //otherwise
                 //assuming
                 // new farm does NOT belong to village or was NOT created locally
                 //see if new farm should become a village
                
            }
            
            /*
            
            if (
                   //TODO add 
                    // !farm.hasvillage()
                    //because if there is a village, then the village should produce new farms, not the farms themselves
                    farm.hasEnoughToStartNewFarm()) {
                boolean farmWasCreatedNearby = true; //default value
                Point point;
                if (random.nextInt(10)==0){
                    farmWasCreatedNearby=false;
                    point = gm.getMap().createGrassAtRandomPoint();
                }
                else if (farm.hasVillage()){
                    
                    point = farm.getVillage().getEmptyLand().pollFirst();
                    
                    if (point==null){
                        continue;
                    }
                }
                else {
                    point = gm.getMap().getRandomEmptyPointAdjecantToTarget(farm.getPoint());
                    if(point==null){
                        continue;
                    }
                }
                
                point.setLandType(LandType.GRASSLAND);
                //gm.getMap().setPoint(point);
                Farm newFarm = new Farm(point);
                newFarm.setInhabitants(1);
                //farm.halfPeopleAmount();
                farmsToAdd.add(newFarm);
                point.setBuilding(newFarm);
                if (farm.hasVillage() && farmWasCreatedNearby){
                    newFarm.setVillage(farm.getVillage());
                }
                if (!farm.hasVillage() && farmWasCreatedNearby){
                    int foodStarter = 
                            gm.getMap().getIndependentFarmsNearby(point, 2).size();
                    newFarm.setFood(foodStarter*2);
                } 
                else if(farm.hasVillage()) {
                    newFarm.setFood(10);
                }
                System.out.println("Farm "+farm.getId()+") split and a new farm " + newFarm.getId() + " was created at " + point.toString());

                if (
                    (!farm.hasVillage() && farmWasCreatedNearby)
                    //&& farmWasCreatedNearby // TODO decide on this later
                    && gm.getMap().independentFarmsNearby(farm.getPoint(), DISTANCE_BETWEEN_FARMS_FOR_VILLAGE_CREATION) > FARMS_TO_CREATE_VILLAGE){
                    farmToConvertToVillage.add(farm);
                }    
                else if (!farmWasCreatedNearby
                        && gm.getMap().independentFarmsNearby(point, DISTANCE_BETWEEN_FARMS_FOR_VILLAGE_CREATION) > FARMS_TO_CREATE_VILLAGE){
                            farmToConvertToVillage.add(newFarm);
                }
            }
            
            */
        }
        
        farms.addAll(farmsToAdd);
        for(Farm farm: farmsToRemove){
            if (farm.hasVillage()){
                farm.getVillage().addEmptyPoint(farm.getPoint());
            }
            destroyFarm(farm);
        }
        farms.removeAll(farmsToRemove);
        farmToConvertToVillage.forEach(farm -> convertFarmToVillageCenter(farm));
        
        
        gm.getGridPanel().updateUI();
    }
    
    public double calculateDistance(Point p1, Point p2) {
        int dx = p1.getX() - p2.getX();
        int dy = p1.getY() - p2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
   
    public void convertFarmToVillageCenter(Farm farm){
        
        
        //prevent too close villages
        
        Point farmCenter = farm.getPoint();
        
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
        //farmCenter = new Point(farm.getPoint().getX(), farm.getPoint().getY(), LandType.VILLAGE);
        
        
        //set all adjecant points to village side 
        for(Point point: villagePoints){
            farms.remove((Farm)point.getBuilding());
            point.setLandType(LandType.VILLAGE);
            point.setBuilding(newVillage);
        }
        
        //set all points within village distance radius to have village = this one
        List<Point> pointsBelongingToVillage = gm.getMap().getAllPointsInCircleAroundTarget(farmCenter, VILLAGE_DOMAIN_LIMIT);
        Collections.shuffle(pointsBelongingToVillage);
        newVillage.setControlledLand(pointsBelongingToVillage);

        newVillage.setEmptyLand(
                pointsBelongingToVillage.stream()
                .filter(point -> point.getBuilding() == null)
                .collect(Collectors.toCollection(LinkedList::new))); 
//        for(Point pointg: newVillage.getEmptyLand()){
//            createFarmAtPoint(pointg.getX(), pointg.getY());
//        }
        List<Point> farmsBelongingToVillage = gm.getMap().getIndependentFarmsNearby(farmCenter, VILLAGE_DOMAIN_LIMIT);
        
        for(Point point: farmsBelongingToVillage){
            if (point.getBuilding() instanceof Farm farm1){
                //System.out.println("awawd");
                farm1.setVillage(newVillage);
            }
        }
        
        
        
        
//        farmCenter = new Point(farmCenter.getX(), farmCenter.getY(), LandType.VILLAGE);
//        Village village = new Village(point);
//        point.setBuilding(village);
//        gm.getMap().getGrid()[point.getX()][point.getY()] = point;
//        LinkedList<Point> farmPoints = gm.getMap().getIndependentFarmsNearby(point, DISTANCE_BETWEEN_FARMS_FOR_VILLAGE_CREATION);
//        
//        
//        for(Point pointen: farmPoints){
//            if(pointen.getBuilding() instanceof Farm farmen){
//                village.addFarm(farm);
//                farmen.setVillage(village);
//                if (farmen.getLand() instanceof Grassland grassland){
//                    grassland.improveFertility();
//                    grassland.improveFertility();grassland.improveFertility();
//                    grassland.improveFertility();
//                    grassland.improveFertility();
//                    
//                }
//            }
//        }
    }
    
    public void destroyFarm(Farm farm){
        farm.getPoint().setLandType(LandType.DIRT);
        farm.getPoint().setBuilding(null);
    }
    
    public void experiment(){
        Point prevPoint = farms.get(farms.size()-1).getPoint();
        Point newPoint = gm.getMap().getGrid()[prevPoint.getX()+1][prevPoint.getY()];//.setLandType(LandType.GRASSLAND));
        newPoint.setLandType(LandType.GRASSLAND);
               // new Point(prevPoint.getX()+1, prevPoint.getY(), LandType.GRASSLAND);
        
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
            createFarmAtPoint(point.getX(), point.getY());
        }
    }
    
    public Farm createFarmAtPoint(int x, int y){
        Point point = gm.getMap().getPoint(x, y);
        point.setLandType(LandType.GRASSLAND);
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
        randomPoint.setLandType(LandType.GRASSLAND);
        Farm farm = new Farm(randomPoint);
        
        randomPoint.setBuilding(farm);
        farms.add(farm);
        System.out.println("Farm created at " + farm.toString());
        gm.getGridPanel().updateUI();
        return farm;
    }
    
}