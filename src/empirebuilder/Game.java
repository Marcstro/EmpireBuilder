package empirebuilder;

import LandTypes.LandType;
import buildings.*;
import entities.Entity;
import entities.effects.Arrow;
import entities.effects.BloodSpark;
import entities.effects.Effect;
import entities.effects.LingeringFallenArrow;
import entities.units.*;
import pathfinding.PathfindingSystem;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Game{

    GameManager gm;
    TheDarkSide darkside;
    int tickCounter;
    static final int MAP_CELL_SIZE = 5;

    private MapCell[][] mapCellGrid;

    final List<Unit> units;
    List<Unit> unitsToBeAdded;

    final List<Effect> effects;
    List<Effect> effectsToBeAdded;

    List<Farm> farms;
    List<Village> villages;
    List<Town> towns;
    List<City> cities;
    List<AttackCapableBuilding> attackCapableBuildings;

    List<EvilSideBase> evilSideBases;

    List<Farm> farmsToAdd;
    List<Farm> farmToConvertToVillage;

    Set<Farm> farmsToRemove;
    Set<Village> villagesToRemove;
    Set<Town> townsToRemove;
    Set<City> citiesToRemove;


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
    final int evilLairSize = 3;

    final double UNIT_BASE_SIZE = 0.2;
    
    final boolean LOGGING = true;
    
    Game(GameManager gameManager){
        this.gm = gameManager;
        darkside = new TheDarkSide(this);
        farms = new LinkedList<>();
        villages = new LinkedList<Village>();
        towns = new LinkedList<Town>();
        cities = new LinkedList<City>();
        attackCapableBuildings = new ArrayList<>();
        evilSideBases = new ArrayList<>();

        effects = new ArrayList<>();
        units = new ArrayList<>();
        unitsToBeAdded = new ArrayList<>();
        effectsToBeAdded = new ArrayList<>();

        inactiveFarms = new LinkedList<>();
        int mapHeight = gm.getMap().getHeight();
        int mapWidth = gm.getMap().getWidth();
        int sizeOfPointsOnMap = mapHeight * mapWidth;
        for (int i=0; i<sizeOfPointsOnMap; i++){
            Farm newFarm = new Farm(gm.getMap().getPoint(0,0));
            inactiveFarms.add(newFarm);
        }

        mapCellGrid = new MapCell[mapWidth/MAP_CELL_SIZE][mapHeight/MAP_CELL_SIZE];
        for (int x = 0; x < mapWidth/MAP_CELL_SIZE; x++) {
            for (int y = 0; y < mapHeight/MAP_CELL_SIZE; y++) {
                this.mapCellGrid[x][y] = new MapCell(x, y);
            }
        }

        tickCounter=0;

        farmsToAdd = new LinkedList();
        farmsToRemove = new HashSet<>();
        farmToConvertToVillage = new LinkedList();

        villagesToRemove = new HashSet();
        townsToRemove = new HashSet();
        citiesToRemove = new HashSet();
    }

    public void tickUnits() {
        gm.getPathfindingSystem().beginTick();
        synchronized (units) {
            Iterator<Unit> it = units.iterator();

            while (it.hasNext()) {
                Unit unit = it.next();

                if (!unit.isAlive()) {
                    handleUnitDeath(unit);
                    it.remove();
                    continue;
                }

                unit.tick(this);
                Coordinates newPos = attemptToMoveUnit(unit);

                if (newPos != null) {
                    moveUnit(unit, newPos);
                }
            }
        }

        synchronized (units) {
            for (Unit unit : unitsToBeAdded) {
                addNewUnit(unit, (int) unit.getX(), (int) unit.getY());
            }
            unitsToBeAdded.clear();
        }
    }

    public void tickEffects(){
        synchronized (effects) {
            Iterator<Effect> it = effects.iterator();

            while (it.hasNext()) {
                Effect effect = it.next();

                effect.tick(this);
                if (effect.getRemainingDuration() <= 0) {
                    // TODO remove effect from its MapCell ?
                    // TODO investigate this with both effects and units, see that they are not lingering in MapCell objects as variables
                    it.remove();
                }
            }
            if (!effectsToBeAdded.isEmpty()) {
                effects.addAll(effectsToBeAdded);
                effectsToBeAdded.clear();
            }
        }
    }

    public void eventsTick(){
        darkside.tick(this);
    }

    public void attackingBuildingsTick(){
        for(AttackCapableBuilding attackCapableBuilding: attackCapableBuildings){
            attackCapableBuilding.tickAttack(this);
        }
    }

    public void tickBuildings(){

        List<Farm> farmsSnapshot = new ArrayList<>(farmsToRemove);
        for (Farm farmToRemove: farmsSnapshot) {
            destroyBuilding(farmToRemove);
            farmToRemove.resetState();
            farmsToRemove.remove(farmToRemove);
        }

        List<Village> villagesSnapshot = new ArrayList<>(villagesToRemove);
        for(Village village: villagesSnapshot){
            destroyBuilding(village);
            villagesToRemove.remove(village);
        }

        List<Town> townsSnapshot = new ArrayList<>(townsToRemove);
        for(Town town: townsSnapshot){
            destroyBuilding(town);
            townsToRemove.remove(town);
        }

        List<City> citiesSnapshot = new ArrayList<>(citiesToRemove);
        for(City city: citiesSnapshot){
            destroyBuilding(city);
            citiesToRemove.remove(city);
        }

        if (LOGGING && gm.getGridPanel().getSelectedPoint() != null){
            System.out.println(gm.getGridPanel().getSelectedPoint().getInfo());
        }
        for (Farm farm: farms) {
            if (!farm.isAlive()){
                farmsToRemove.add(farm);
                continue;
            }
            farm.tick();
            if (farm.checkIfLastPersonOnFarmDies()){
                farmsToRemove.add(farm);
                continue;
            }
            if (farm.getFarmOwningBuilding() == null && farm.isTimeToCreateNewFarm()) {
                Point newFarmPoint = gm.getMap().getRandomEmptyWalkablePointAdjecantToTarget(farm.getPoint());
                if (newFarmPoint == null){
                    if (farm.timeToSearchForNewOwner()){
                        Point newNeighbourPoint = gm.getMap().getOwnedAdjecantFarm(farm.getPoint());
                        farm.resetSearchForNewOwnerCoolDown();
                        if (newNeighbourPoint == null){
                            continue;
                        }
                        FarmOwningBuilding newOwner = newNeighbourPoint.getPointOwner();
                        farm.setFarmOwningBuilding(newOwner);
                        newOwner.addFarm(farm);
                        newOwner.addToControlledLand(farm.getPoint());
                        farm.getPoint().setOwnerBuilding(newOwner);
                    }
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
            if(!village.isAlive()){
                villagesToRemove.add(village);
                continue;
            }
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

        for (Town town: towns){ // TODO move this to a better suitable place. all buildings should check this at once
            if (!town.isAlive()){
                townsToRemove.add(town);
                continue;
            }
            town.tick();
            town.tick(this);
        }
        for (City city: cities){
            if (!city.isAlive()){
                citiesToRemove.add(city);
                continue;
            }
            city.tick();
            city.tick(this);
        }

        farmToConvertToVillage.forEach(farmToConvert -> convertFarmToVillageCenter(farmToConvert));

        farmsToAdd.clear();
        farmToConvertToVillage.clear();
    }

    public Farm getFarmFromPool(){
        if(!inactiveFarms.isEmpty()){
            return inactiveFarms.poll();
        }
        else {
            System.out.println("farm pool has run out of farms. INCREASE SIZE OF FARM POOL!");
            return new Farm(gm.getMap().getPoint(0,0));
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

        // currently a village must have sufficient amount of people and owned farms to upgrade
        for(Village village: villages){
            if (village.hasOwner()){
                continue;
            }
            if (!village.hasOwner() && village.getPeople() > 50 && village.getFarms().size() > 65){
                List<Village> nearbyIndependentVillages = villages.stream()
                        .filter(v -> v != village)
                        .filter(v -> !v.hasOwner())
                        .filter(v -> calculateDistance(village.getPoint(), v.getPoint()) <= villageOwningRange)
                        .toList();
                if (nearbyIndependentVillages.size() >= villagesForTownCreation){
                    villagesToTurnToTowns.add(village);
                }
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
        // currently we adjust a farms tech level when something with it changes
        // this is an alternative, check all farms on occasion
        /*
        for (Farm farm: farms){
            farm.uncommonFarmTick();
        }*/
    }

    public void seldomTick(){

        // reset all occasional cooldowns
        for(int x=0; x<mapCellGrid.length; x++){
            for(int y=0; y<mapCellGrid[x].length; y++){
                getMapCellInMapCellGrid(x,y).resetCooldowns();
            }
        }

        // buildings gain control over independent lesser buildings
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
                )
                .map(building -> (VillageOwningBuilding) building)
                .toList();

        for (VillageOwningBuilding villageOwningBuilding : villageOwners){
            List<Village> nearbyIndependentVillages = villages.stream()
                    .filter(v -> !v.hasOwner())
                    .filter(v -> calculateDistance(villageOwningBuilding.getPoint(), v.getPoint()) <= villageOwningRange)
                    .toList();
            for (Village village: nearbyIndependentVillages){
                village.setVillageOwningBuilding(villageOwningBuilding);
                village.markCenter();
                villageOwningBuilding.addVillage(village);
            }
        }

        // change farms success level change level
        // current not in use
        /*for (Farm farm: farms){
            double roll1 = ThreadLocalRandom.current().nextDouble(0.03);
            double roll2 = ThreadLocalRandom.current().nextDouble(0.03);
            farm.setSuccessLevelChange((roll1+roll2)-0.03);
        }*/
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

    public void addNewUnit(Unit unit, int x, int y) {
        unit.setX(x);
        unit.setY(y);

        int cellX = getCellCoord(x);
        int cellY = getCellCoord(y);

        MapCell targetCell = mapCellGrid[cellX][cellY];

        unit.setMapCellX(cellX);
        unit.setMapCellY(cellY);

        // Stagger the position-based stuck-check timer so all units spawned in the same
        // batch do NOT fire their stuck check at the same tick.  Without this, every unit
        // born at the same time reaches stuckSampleTick=0 simultaneously, triggering a
        // burst of full-map A* calls that causes 40-80 ms frame spikes.
        unit.setStuckSampleTick(ThreadLocalRandom.current().nextInt(PathfindingSystem.STUCK_SAMPLE_INTERVAL));

        this.units.add(unit);
        targetCell.addUnit(unit);
    }

    public void removeUnit(Unit unit) {
        int cellX = unit.getMapCellX();
        int cellY = unit.getMapCellY();
        mapCellGrid[cellX][cellY].removeUnit(unit);
        this.units.remove(unit);
    }

    public void addNewEffect(Effect effect, int x, int y) {
        effect.setX(x);
        effect.setY(y);

        int mapCellX = getCellCoord(x);
        int mapCellY = getCellCoord(y);

        MapCell targetMapCell = mapCellGrid[mapCellX][mapCellY];

        effect.setMapCellX(mapCellX);
        effect.setMapCellY(mapCellY);

        this.effectsToBeAdded.add(effect);
        targetMapCell.addEffect(effect);
    }

    public void removeEffect(Effect effect) {
        int mapCellX = effect.getMapCellX();
        int mapCellY = effect.getMapCellY();
        mapCellGrid[mapCellX][mapCellY].removeEffect(effect);
        this.effects.remove(effect);
    }

    public MapCell getMapCellByPoint(Point point){
        return mapCellGrid[point.getX()/MAP_CELL_SIZE][point.getY()/MAP_CELL_SIZE];
    }

    public Point getPointByMapCell(MapCell mapCell){
        return gm.getMap().getPoint(mapCell.getCellX()*MAP_CELL_SIZE, mapCell.getCellY()*MAP_CELL_SIZE);
    }

    public MapCell getMapCellByPointCoordinates(int x, int y){
        return mapCellGrid[x/MAP_CELL_SIZE][y/MAP_CELL_SIZE];
    }

    public MapCell getMapCellInMapCellGrid(int x, int y){
        return mapCellGrid[x][y];
    }

    /**
     * Direct access to the raw cell grid — use only for read-only iteration
     * (e.g. ORCA neighbour gathering) where avoiding lambda dispatch matters.
     */
    public MapCell[][] getMapCellGrid() { return mapCellGrid; }

    private int getCellCoord(int mapCoord) {
        return mapCoord / MAP_CELL_SIZE;
    }

    public Point getPoint(double x, double y){
        return (gm.getMap().getPoint((int)x, (int)y));
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
            if (calculateDistance(farmCenter, village.getPoint()) < ((VILLAGE_DOMAIN_LIMIT*2))){
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
        MapCell mapCell = getMapCellByPoint(farmCenter);
        mapCell.addBuilding(newVillage);
        farms.remove(farm); //this here was temporarily removed, should it remain removed? Testing shows it should remain here
        farm.resetState();
        inactiveFarms.add(farm);

        gm.getMap().replaceBuilding(farmCenter, newVillage);

        Set<Point> pointsBelongingToVillage = gm.getMap()
                .getAllPointsInCircleAroundTarget(farmCenter, VILLAGE_DOMAIN_LIMIT)
                .stream()
                .filter(p -> !p.isOwnedByBuilding())
                .filter(p-> p.isTerrainWalkable()).collect(Collectors.toSet());
        
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
        List<Point> townAreaPoints = gm.getMap().getTownShapePointList(midPoint.getX(), midPoint.getY());

        villages.remove(village);
        Town newTown = new Town(midPoint);
        midPoint.setOwnerBuilding(newTown);
        newTown.setPeople(village.getPeople());
        newTown.setFood(village.getFood());
        newTown.setGold(village.getGold());
        gm.getMap().replaceBuilding(midPoint, newTown);
        towns.add(newTown);
        MapCell mapCell =  getMapCellByPoint(midPoint);
        mapCell.addBuilding(newTown);
        mapCell.removeBuilding(village);
        attackCapableBuildings.add(newTown);
        getMapCellByPoint(newTown.getPoint()).addAttackCapableBuildings(newTown);
        newTown.setControlledLand(village.getControlledLand());

        for (Point point: newTown.getControlledLand()){
            if (point.equals(midPoint)){
                continue;
            }
            if (townAreaPoints.contains(point)){
                if (point.getBuilding() instanceof Farm farm){
                    destroyBuilding(farm);
                    if (farmsToRemove.contains(farm)) {
                        farmsToRemove.remove(farm);
                    }
                }
                else if (point.getBuilding() != null && point.getBuilding() != newTown){
                    throw new RuntimeException("townArea had building that wasnt farm, fix this code");
                }

                TownArea townArea = new TownArea(point, newTown);
                gm.getMap().setBuildingOnPoint(point, townArea);
                newTown.addTownArea(townArea);
            }
            if (point.getBuilding() instanceof Farm farm){
                farm.getFarmOwningBuilding().removeFromFarmList(farm);
                farm.getFarmOwningBuilding().removePointFromEmptyPointList(farm.getPoint());
                farm.setFarmOwningBuilding(newTown);
                newTown.addFarm(farm);
            }
            else if (point.isEmpty()){
                newTown.addEmptyPoint(point);
            }
            point.setOwnerBuilding(newTown);
        }
        for(Village nearbyVillage: nearbyVillages){
            nearbyVillage.setVillageOwningBuilding(newTown);
            newTown.addVillage(nearbyVillage);
            nearbyVillage.markCenter();
        }
    }

    // TODO this method could be rewritten more cleanly.
    // the order of things happening is important and might cause bugs if changed
    public void createCity(Town town, List<Town> nearbyTowns){

        attackCapableBuildings.remove(town);
        getMapCellByPoint(town.getPoint()).removeAttackCapableBuilding(town);
        Point midPoint = town.getPoint();
        City newCity = new City(midPoint);
        attackCapableBuildings.add(newCity);
        getMapCellByPoint(newCity.getPoint()).addAttackCapableBuildings(newCity);
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
        }

        List<Point> cityAreaPoints = gm.getMap().getCityShapePointList(midPoint.getX(), midPoint.getY());
        for (Point cityAreaPoint: cityAreaPoints){
            cityAreaPoint.getPointOwner().removePointFromEmptyPointList(cityAreaPoint);
            cityAreaPoint.setOwnerBuilding(town);
            town.addToControlledLand(cityAreaPoint);
            if (cityAreaPoint == midPoint){
                continue;
            }
            if (cityAreaPoint.getBuilding() instanceof Farm farm){
                farms.remove(farm);
                if (farm.getFarmOwningBuilding() instanceof FarmOwningBuilding FOB && FOB != town) {
                    FOB.removeFromFarmList(farm);
                    FOB.removePointFromEmptyPointList(farm.getPoint());
                    FOB.removeFromControlledLand(cityAreaPoint);
                }
                gm.getMap().removeBuildingFromPoint(farm.getPoint());
                farm.resetState();
                inactiveFarms.add(farm);
                if (farmsToRemove.contains(farm)) {
                    farmsToRemove.remove(farm);
                }
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
            village.setVillageOwningBuilding(newCity);
        }

        cities.add(newCity);
        towns.remove(town);
        MapCell mapCell =  getMapCellByPoint(midPoint);
        mapCell.addBuilding(newCity);
        mapCell.removeBuilding(town);
        for(Town nearbyTown: nearbyTowns){
            newCity.addTown(nearbyTown);
            nearbyTown.setCity(newCity);
        }
    }

    private Coordinates attemptToMoveUnit(Unit unit) {
        return gm.getPathfindingSystem().computeNextPosition(unit);
    }

    private void moveUnit(Unit unit, Coordinates newPos) {
        int newMapCellX = getCellCoord((int) newPos.x());
        int newMapCellY = getCellCoord((int) newPos.y());

        if (newMapCellX != unit.getMapCellX() || newMapCellY != unit.getMapCellY()) {
            if (newMapCellX >= 0 && newMapCellX < mapCellGrid.length &&
                newMapCellY >= 0 && newMapCellY < mapCellGrid[newMapCellX].length) {

                MapCell oldCell = mapCellGrid[unit.getMapCellX()][unit.getMapCellY()];
                MapCell newCell = mapCellGrid[newMapCellX][newMapCellY];
                oldCell.removeUnit(unit);
                newCell.addUnit(unit);
                if (unit.getFactionId()==2){
                    newCell.attemptToPlunder(this, tickCounter, unit);
                }

                unit.setMapCellX(newMapCellX);
                unit.setMapCellY(newMapCellY);


                if (unit.getFactionId() == 2) {
                    wakeAttackCapableBuildings(unit, 1);
                }
            }
        }

        unit.setX(newPos.x());
        unit.setY(newPos.y());
    }

    public boolean searchOtherLocalUnits(int centerX, int centerY, int distanceOut, UnitAction action) {
        int startX = getCellCoord(centerX) - distanceOut;
        int endX = getCellCoord(centerX) + distanceOut;
        int startY = getCellCoord(centerY) - distanceOut;
        int endY = getCellCoord(centerY) + distanceOut;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {

                if (x >= 0 && x < mapCellGrid.length && y >= 0 && y  < mapCellGrid[x].length ) {

                    MapCell cell = mapCellGrid[x][y];

                    for (Unit neighbor : cell.getUnits()) {
                        // We cannot check if neighbor is the searcher here,
                        // so all neighbors must be checked by the action.
                        if (action.execute(neighbor)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean searchOtherLocalUnits(Unit unit, int distanceOut, UnitAction action) {
        int startX = unit.getMapCellX() - distanceOut;
        int endX = unit.getMapCellX() + distanceOut;
        int startY = unit.getMapCellY() - distanceOut;
        int endY = unit.getMapCellY() + distanceOut;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {

                if (x >= 0 && x < mapCellGrid.length && y >= 0 && y  < mapCellGrid[x].length ) {

                    MapCell cell = mapCellGrid[x][y];

                    for (Unit neighbor : cell.getUnits()) {
                        if (neighbor != unit) {

                            if (action.execute(neighbor)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }



    //call with
    /**
     *
     attackTarget = game.getNearestUnit(this, 1, (neighbor) ->
     neighbor.isAlive() && this.isHostileTo(neighbor)
     );
     */
    public Unit getNearestUnit(double lookerX, double lookerY, Unit searcher, int distanceOut, UnitPredicate filter) {
        Unit nearestUnit = null;
        double minDistanceSq = Double.MAX_VALUE;

        double sX = lookerX;
        double sY = lookerY;

        int startX = getCellCoord((int)lookerX) - distanceOut;
        int endX = getCellCoord((int)lookerX) + distanceOut;
        int startY = getCellCoord((int)lookerY) - distanceOut;
        int endY = getCellCoord((int)lookerY) + distanceOut;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {

                if (x >= 0 && x < mapCellGrid.length && y >= 0 && y < mapCellGrid[x].length) {
                    MapCell cell = mapCellGrid[x][y];

                    for (Unit neighbor : cell.getUnits()) {
                        if (neighbor != searcher && filter.test(neighbor)) {

                            double nX = neighbor.getX();
                            double nY = neighbor.getY();
                            double dx = sX - nX;
                            double dy = sY - nY;
                            double distanceSq = (dx * dx) + (dy * dy);

                            if (distanceSq < minDistanceSq) {
                                minDistanceSq = distanceSq;
                                nearestUnit = neighbor;
                            }
                        }
                    }
                }
            }
        }
        return nearestUnit;
    }

    // TODO implement unit/effect size?
    // has future usages
    private boolean checkSimpleCollision(double x1, double y1, Unit neighbor) {
        double minSeparation = neighbor.getSize() + UNIT_BASE_SIZE;// instead of base size, check targeters size
        double minSeparationSq = minSeparation * minSeparation;

        double dx = x1 - neighbor.getX();
        double dy = y1 - neighbor.getY();
        double distanceSq = (dx * dx) + (dy * dy);

        return distanceSq < minSeparationSq;
    }

    // checks if a missile, starting from p1, going to p2, hits unit c that has radius r
    public boolean checkRaycastCollision(double p1x, double p1y, double p2x, double p2y, double cX, double cY, double r) {

        double dx = p2x - p1x;
        double dy = p2y - p1y;

        // Vector from P1 to C
        double pcX = cX - p1x;
        double pcY = cY - p1y;

        // Dot product of (P1->P2) and (P1->C)
        double dot = dx * pcX + dy * pcY;

        // Length squared of P1->P2 vector
        double lenSq = dx * dx + dy * dy;

        // Parameter t: projects C onto the line defined by P1 and P2
        double t = (lenSq == 0) ? 0 : dot / lenSq;

        // Clamp t to the segment [0, 1] to ensure we only check the line segment
        if (t < 0) t = 0;
        if (t > 1) t = 1;

        // Find the closest point on the segment to C (Px, Py)
        double closestX = p1x + t * dx;
        double closestY = p1y + t * dy;

        // Distance squared from C to the closest point (Px, Py)
        double distSq = (cX - closestX) * (cX - closestX) + (cY - closestY) * (cY - closestY);

        // Check if distance is less than radius squared
        return distSq < r * r;
    }

    public Point getRandomValidPoint(){
        return gm.getMap().getrandomValidPoint();
    }

    public Point getRandomTownPoint(){
        if (towns.isEmpty()){
            return null;
        }
        return towns.get((int)(Math.random()*(towns.size()))).getPoint();
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public List<Unit> getUnits() {
        return units;
    }

    private void createEffect(Effect effect) {
        int cellX = getCellCoord((int)effect.getX());
        int cellY = getCellCoord((int)effect.getY());

        effect.setMapCellX(cellX);
        effect.setMapCellY(cellY);

        mapCellGrid[cellX][cellY].addEffect(effect);
        effectsToBeAdded.add(effect);
    }

    // TODO rewrite, add many more variables
    // possibly implement some sort of "missile creation factory"
    public void spawnArrow(AttackCapableBuilding shooter, Unit target, int senderFactionId) {
        Arrow arrow = new Arrow(shooter.getX(), shooter.getY(), target.getX(), target.getY(), senderFactionId);
        createEffect(arrow);
    }

    public void spawnLingeringFallenArrow(Arrow arrow) {
        createEffect(new LingeringFallenArrow(arrow.getX(), arrow.getY(), arrow.getRotation()));
    }

    public void unitShootArrow(Unit shooter, Entity target, int senderFactionId) {
        Arrow arrow = new Arrow(shooter.getX(), shooter.getY(), target.getX(), target.getY(), senderFactionId);
        createEffect(arrow);
    }

    // TODO extend so this allows for other missiles
    public Entity checkArrowHit(Arrow arrow, double oldX, double oldY) {
        int cellAx = getCellCoord((int) oldX);
        int cellAy = getCellCoord((int) oldY);
        int cellBx = getCellCoord((int) arrow.getX());
        int cellBy = getCellCoord((int) arrow.getY());
        boolean differentCells = !(cellAx == cellBx && cellAy == cellBy);

        Entity hit = rayCastCheckUnitsInCell(arrow, oldX, oldY, cellAx, cellAy);
        if (hit == null && differentCells) {
            hit = rayCastCheckUnitsInCell(arrow, oldX, oldY, cellBx, cellBy);
        }

        if (hit == null) {
            hit = rayCastCheckBuildingsInCell(arrow, oldX, oldY, cellAx, cellAy);
            if (hit == null && differentCells) {
                hit = rayCastCheckBuildingsInCell(arrow, oldX, oldY, cellBx, cellBy);
            }
        }

        if (hit == null) return null;

        hit.causeHealthLoss(arrow.getDamage());
        if (hit instanceof Unit) {
            createEffect(new BloodSpark(hit.getX(), hit.getY()));
            // TODO replace bloodspark with different effect when hitting non bleeding targets (i.e. buildings)
        }
        return hit;
    }

    private Entity rayCastCheckUnitsInCell(Arrow arrow, double oldX, double oldY, int cellX, int cellY) {
        if (cellX < 0 || cellX >= mapCellGrid.length || cellY < 0 || cellY >= mapCellGrid[cellX].length) return null;
        for (Unit candidate : mapCellGrid[cellX][cellY].getUnits()) {
            if (!candidate.isHostileTo(arrow.getFactionId())) continue;
            if (checkRaycastCollision(oldX, oldY, arrow.getX(), arrow.getY(),
                    candidate.getX(), candidate.getY(), candidate.getSize() + arrow.getWidth())) {
                return candidate;
            }
        }
        return null;
    }

    private Entity rayCastCheckBuildingsInCell(Arrow arrow, double oldX, double oldY, int cellX, int cellY) {
        if (cellX < 0 || cellX >= mapCellGrid.length || cellY < 0 || cellY >= mapCellGrid[cellX].length) return null;
        for (FarmOwningBuilding candidate : mapCellGrid[cellX][cellY].getLargeBuildingsList(this)) {
            if (!candidate.isHostileTo(arrow.getFactionId())) continue;
            if (checkRaycastCollision(oldX, oldY, arrow.getX(), arrow.getY(),
                    candidate.getX(), candidate.getY(), candidate.getSize() + arrow.getWidth())) {
                return candidate;
            }
        }
        return null;
    }

    public void wakeAttackCapableBuildings(Unit unit, int distanceOut) {
        if (unit.getFactionId() == 1) {
            return;
        }

        int startX = unit.getMapCellX() - distanceOut;
        int endX = unit.getMapCellX() + distanceOut;
        int startY = unit.getMapCellY() - distanceOut;
        int endY = unit.getMapCellY() + distanceOut;
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {

                if (x >= 0 && x < mapCellGrid.length && y >= 0 && y < mapCellGrid[x].length) {

                    MapCell cell = mapCellGrid[x][y];

                    for (AttackCapableBuilding building : cell.getAttackCapableBuildings()) {
                        building.setAttackReady(true);
                    }
                }
            }
        }
    }

    // TODO expand, instead of hostileFactionId, use friendlyFactionId
    public boolean isHostileUnitInRadius(int cellX, int cellY, int distanceOut, int hostileFactionId) {
        return searchOtherLocalUnits(cellX, cellY, distanceOut, (neighbor) -> {
            return (neighbor.isAlive() && neighbor.getFactionId() == hostileFactionId);
        });
    }

    // TODO expand so all factions buildings can be attackCapable
    public void attemptToPutAttackCapableBuildingsAsleep(int centerCellX, int centerCellY, int distanceOut) {

        // We only care about cells that might have been under threat and contain buildings.
        int buildingSearchDistance = 2; // Check 5x5 area for buildings that might be active

        int startX = centerCellX - buildingSearchDistance;
        int endX = centerCellX + buildingSearchDistance;
        int startY = centerCellY - buildingSearchDistance;
        int endY = centerCellY + buildingSearchDistance;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {

                if (x >= 0 && x < mapCellGrid.length && y >= 0 && y < mapCellGrid[x].length) {

                    MapCell cell = mapCellGrid[x][y];

                    if (cell.getAttackCapableBuildings().isEmpty()) {
                        continue;
                    }

                    // TODO this method already does a double for loop to search outwards, unnecessary
                    // that 2 methods both attempt to do the scan
                    boolean threatPresent = isHostileUnitInRadius(x, y, 0, 2);

                    if (!threatPresent) {
                        for (AttackCapableBuilding building : cell.getAttackCapableBuildings()) {
                            building.setAttackReady(false);
                        }
                    }
                }
            }
        }
    }

    private void handleUnitDeath(Unit unit) {

        // TODO possible make nearby areas react on death unit?
        unit.getUnitOwner().getUnitManagerComponent().removeUnit(unit);
         MapCell mapCell = mapCellGrid[unit.getMapCellX()][unit.getMapCellY()];
         mapCell.removeUnit(unit);
    }

    public AttackCapableBuilding getNearestAttackCapableBuilding(Unit searcher, int distanceOut) {
        AttackCapableBuilding nearestBuilding = null;
        double minDistanceSq = Double.MAX_VALUE;

        int startX = searcher.getMapCellX() - distanceOut;
        int endX = searcher.getMapCellX() + distanceOut;
        int startY = searcher.getMapCellY() - distanceOut;
        int endY = searcher.getMapCellY() + distanceOut;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (x >= 0 && x < mapCellGrid.length && y >= 0 && y < mapCellGrid[x].length) {

                    MapCell cell = mapCellGrid[x][y];

                    for (AttackCapableBuilding building : cell.getAttackCapableBuildings()) {
                        if (searcher.isHostileTo(building.getFactionId())) {

                            double bX = building.getX(); // Building's getX()
                            double bY = building.getY(); // Building's getY()
                            double dx = searcher.getX() - bX;
                            double dy = searcher.getY() - bY;
                            double distanceSq = (dx * dx) + (dy * dy);

                            if (distanceSq < minDistanceSq) {
                                minDistanceSq = distanceSq;
                                nearestBuilding = building;
                            }
                        }
                    }
                }
            }
        }
        return nearestBuilding;
    }

    public FarmOwningBuilding getNearestLargeBuilding(Unit searcher, int distanceOut){
        FarmOwningBuilding nearestLargeBuilding = null;
        double minDistanceSq = Double.MAX_VALUE;

        int startX = searcher.getMapCellX() - distanceOut;
        int endX = searcher.getMapCellX() + distanceOut;
        int startY = searcher.getMapCellY() - distanceOut;
        int endY = searcher.getMapCellY() + distanceOut;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                    if (x >= 0 && x < mapCellGrid.length && y >= 0 && y < mapCellGrid[x].length) {
                    MapCell cell = mapCellGrid[x][y];

                    for (FarmOwningBuilding building : cell.getLargeBuildingsList(this)) {
                        if (searcher.isHostileTo(building.getFactionId()) && building.isAlive()) {

                            double bX = building.getX(); // Building's getX()
                            double bY = building.getY(); // Building's getY()
                            double dx = searcher.getX() - bX;
                            double dy = searcher.getY() - bY;
                            double distanceSq = (dx * dx) + (dy * dy);

                            if (distanceSq < minDistanceSq) {
                                minDistanceSq = distanceSq;
                                nearestLargeBuilding = building;
                            }
                        }
                    }
                }
            }
        }
        return nearestLargeBuilding;
    }

    public boolean performMeleeAttack(Unit attacker, Entity target) {

        target.causeHealthLoss(attacker.getDamage());
        BloodSpark bloodSpark = new BloodSpark(target.getX(), target.getY());
        createEffect(bloodSpark);
        //System.out.println(attacker.getClass().getSimpleName() + " attacked and did " + attacker.getDamage() + " damage on " + target + ", target has " + target.getHealth() + " health left.");
        return true;
    }

    public List<FarmOwningBuilding> getAllFarmOwningBuildingsInMapCell(MapCell mapcell){
        List<FarmOwningBuilding> list = new ArrayList<>();
        for (int x=mapcell.getCellX()*MAP_CELL_SIZE; x<(mapcell.getCellX()+1)*MAP_CELL_SIZE; x++){
            for (int y=mapcell.getCellY()*MAP_CELL_SIZE; y<(mapcell.getCellY()+1)*MAP_CELL_SIZE; y++){
                if (gm.getMap().getPoint(x,y).getBuilding() instanceof FarmOwningBuilding building){
                    list.add(building);
                }
            }
        }
        return list;
    }

    public void divideLootFromBuilding(Building building){
        List<Unit> unitsToGetLoot = new ArrayList<>();
        MapCell middle = getMapCellByPoint(building.getPoint());
        for(int x=middle.getCellX()-1; x<middle.getCellX()+1; x++){
            for(int y=middle.getCellY()-1; y<middle.getCellY()+1; y++){
                if (isMapCellValid(x, y)){
                    MapCell cell = getMapCellInMapCellGrid(x,y);
                    for(Unit unit: cell.getUnits()){
                        if (unit.isHostileTo(building.getFactionId())){
                            unitsToGetLoot.add(unit);
                        }
                    }
                }
            }
        }
        if (unitsToGetLoot.isEmpty()){
            return;
        }
        double individualLoot = building.getGold()/unitsToGetLoot.size();
        for(Unit unit:  unitsToGetLoot){
            unit.addLoot((int)individualLoot);
        }
    }

    // TODO review for possible improvements
    // TODO extract "loot building" into separate method later. currently it is fine
    public void destroyBuilding(Building building){

        if (building == null){
            return;
        }

        switch (building) {
            case City city -> {
                cities.remove(city);
                divideLootFromBuilding(city);
                List<CityArea> snapshot = new ArrayList<>(city.getCityAreaPoints());
                snapshot.forEach(cityArea -> destroyBuilding(cityArea));
                getMapCellByPoint(city.getPoint()).removeBuilding(city);
                gm.getMap().removeBuildingFromPoint(city.getPoint());
                for(Town town: city.getTowns()){
                    town.removeCity();
                    for (Village village: town.getVillages()){
                        villageReactToOwnerBeingDestroyed(village);
                    }
                }
                city.setTowns(null);
            }
            case Town town -> {
                towns.remove(town);
                divideLootFromBuilding(town);
                List<TownArea> snapshot = new ArrayList<>(town.getTownAreaPoints());
                snapshot.forEach(townArea -> destroyBuilding(townArea));
                getMapCellByPoint(town.getPoint()).removeBuilding(town);
                gm.getMap().removeBuildingFromPoint(town.getPoint());
                if (town.hasCity()){
                    town.getCity().releaseTown(town);
                    town.removeCity();
                }
            }
            case Village village -> {
                gm.getMap().removeBuildingFromPoint(village.getPoint());
                divideLootFromBuilding(village);
                village.unmarkCenter();
                villages.remove(village);
                if (village.hasOwner()){
                    village.getVillageOwningBuilding().removeVillage(village);
                }
                village.setVillageOwningBuilding(null);
            }
            case Farm farm -> {
                gm.getMap().removeBuildingFromPoint(farm.getPoint());
                farms.remove(farm);
                if(farm.belongsToFarmOwningBuilding()){
                    farm.getFarmOwningBuilding().removeFromFarmList(farm);
                }
                farm.resetState();
                inactiveFarms.add(farm);
            }
            case TownArea townArea -> {
                gm.getMap().removeBuildingFromPoint(townArea.getPoint());
                townArea.getTownCenter().removeTownArea(townArea);
                townArea.setTownCenter(null);
            }
            case CityArea cityArea -> {
                gm.getMap().removeBuildingFromPoint(cityArea.getPoint());
                cityArea.getCityCenter().removeCityArea(cityArea);
                cityArea.setCityCenter(null);
            }
            case null, default -> throw new RuntimeException("x196 what kind of building was destroyed???");
        }

        if (building instanceof VillageOwningBuilding villageOwningBuilding){
            for (Village village: villageOwningBuilding.getVillages()){
                village.setVillageOwningBuilding(null);
                village.unmarkCenter();
                updateVillageControlledLand(village);
                villageReactToOwnerBeingDestroyed(village);
            }
            villageOwningBuilding.setVillages(null);
        }

        if (building instanceof FarmOwningBuilding farmOwningBuilding){
            for (Point point: farmOwningBuilding.getControlledLand()){
                point.setOwnerBuilding(null);
            }
            farmOwningBuilding.setControlledLand(null);
            List<Farm> snapshot = new ArrayList<>(farmOwningBuilding.getFarms());
            snapshot.forEach(farm -> destroyBuilding(farm));
        }

        if (building instanceof AttackCapableBuilding attackCapableBuilding){
            getMapCellByPointCoordinates((int)attackCapableBuilding.getX(), (int)attackCapableBuilding.getY()).removeAttackCapableBuilding(attackCapableBuilding);
            attackCapableBuildings.remove(attackCapableBuilding);
        }
    }

    public void updateVillageControlledLand(Village village){
        List<Point> pointToBeGivenToVillage = new LinkedList<>(
                gm.getMap()
                        .getAllPointsInCircleAroundTarget(village.getPoint(), VILLAGE_DOMAIN_LIMIT)
                        .stream()
                        .filter(p -> !p.isOwnedByBuilding())
                        .filter(Point::isTerrainWalkable)
                        .toList());
        for(Point point: pointToBeGivenToVillage){
            if (point.getBuilding() instanceof Farm farm){
                village.addFarm(farm);
                farm.setFarmOwningBuilding(village);
            }
            else {
                village.addEmptyPoint(point);
            }
            village.addToControlledLand(point);
            point.setOwnerBuilding(village);
        }
    }

    public void villageReactToOwnerBeingDestroyed(Village village){
        List<Point> baseDomainArea = gm.getMap().getAllPointsInCircleAroundTarget(village.getPoint(), VILLAGE_DOMAIN_LIMIT);
        List<Farm> farmsSnapshot = new ArrayList<>(village.getFarms());
        for(Farm farm: farmsSnapshot){
            //50% for each owned farm to be destroyed
            if (!baseDomainArea.contains(farm.getPoint())){
                farmsToRemove.add(farm);
            }
            else {
                if (ThreadLocalRandom.current().nextDouble() > 0.5){
                    farm.halvePeopleAmount();
                }
                else {
                    farmsToRemove.add(farm);
                }
            }
        }
    }

    public boolean isUnitInDestinedMapCell(Unit unit){
        MapCell targetMapCell = getMapCellByPoint(unit.getPointTarget());
        MapCell unitMapCell = getMapCellByPoint(getPoint((int)unit.getX(), (int)unit.getY()));
        return targetMapCell == unitMapCell;
    }

    public boolean isUnitInDestinedPoint(Unit unit){
        Point pt = unit.getPointTarget();
        if (pt == null) return false;
        double dx = unit.getX() - pt.getX();
        double dy = unit.getY() - pt.getY();
        return Math.sqrt(dx * dx + dy * dy) <= PathfindingSystem.POINT_TARGET_ARRIVAL_RADIUS;
    }

    public boolean isUnitInDestinedPoint(Unit unit, Point point){
        return unit.getX() == point.getX() && unit.getY() == point.getY();
    }

    public Point getRandomWalkablePoint(){
        int x = -1;
        int y = -1;
        while (!gm.getMap().isValidAndWalkable(x,y)){
            x = ThreadLocalRandom.current().nextInt(gm.getMap().getWidth());
            y = ThreadLocalRandom.current().nextInt(gm.getMap().getHeight());
        }
        return getPoint(x,y);
    }

    public Point getNewPointTarget(){
        return getRandomWalkablePoint();
    }

    public Point getIdleWalkTarget(Point startPoint) {
        List<Point> candidates = gm.getMap().getWalkablePointsOnRing(startPoint, 2);
        if (candidates.isEmpty()) return startPoint;
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    /**
     * Returns up to [count] valid spawn points near (centerX, centerY).
     * Walkable points without other units on it
     *
     * The search expands outward ring-by-ring so the closest walkable tiles are
     * always preferred.
     */
    public List<Point> findGroupSpawnPoints(int centerX, int centerY, int count) {
        List<Point> result = new ArrayList<>(count);
        Set<Long> claimed = new HashSet<>();

        for (Unit queued : unitsToBeAdded) {
            claimed.add(pointKey((int) queued.getX(), (int) queued.getY()));
        }

        for (int radius = 0; radius <= 50 && result.size() < count; radius++) {
            for (int dx = -radius; dx <= radius && result.size() < count; dx++) {
                for (int dy = -radius; dy <= radius && result.size() < count; dy++) {
                    if (Math.abs(dx) != radius && Math.abs(dy) != radius) continue;

                    int x = centerX + dx;
                    int y = centerY + dy;

                    if (!gm.getMap().isValidAndWalkable(x, y)) continue;

                    long key = pointKey(x, y);
                    if (claimed.contains(key)) continue;
                    if (isPointOccupiedByUnit(x, y)) continue;

                    claimed.add(key);
                    result.add(gm.getMap().getPoint(x, y));
                }
            }
        }

        if (result.size() < count) {
            System.out.println("[spawnGroup] Warning: only found " + result.size()
                    + "/" + count + " spawn points near (" + centerX + "," + centerY + ")");
        }
        return result;
    }

    /** Packs two ints into a long for use as a set key. */
    private static long pointKey(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    /**
     * Returns true if any unit already placed on the map occupies the given
     * integer-grid point (within 0.9 world-units — close enough to be "on" it).
     */
    private boolean isPointOccupiedByUnit(int x, int y) {
        int cellX = x / MAP_CELL_SIZE;
        int cellY = y / MAP_CELL_SIZE;
        if (cellX < 0 || cellX >= mapCellGrid.length
                || cellY < 0 || cellY >= mapCellGrid[cellX].length) return false;
        for (Unit u : mapCellGrid[cellX][cellY].getUnits()) {
            double ddx = u.getX() - x;
            double ddy = u.getY() - y;
            if (ddx * ddx + ddy * ddy < 0.81) return true; // 0.9² = 0.81
        }
        return false;
    }

    public int getMAP_CELL_SIZE(){
        return MAP_CELL_SIZE;
    }

    public CivilizationDevelopmentLevel calculateWorldState(){
        if (cities.size() >= 3){
            return CivilizationDevelopmentLevel.MULTIPLE_CITIES;
        }
        else if (!cities.isEmpty()){
            return CivilizationDevelopmentLevel.CITY;
        }
        else if (towns.size() >= 3){
            return CivilizationDevelopmentLevel.MULTIPLE_TOWNS;
        }
        else if (!towns.isEmpty()){
            return CivilizationDevelopmentLevel.MULTIPLE_TOWNS;
        }
        else {
            return CivilizationDevelopmentLevel.PRIMITIVE;
        }
    }

    public TheDarkSide getDarkside() {
        return darkside;
    }

    public Point findPositionForEvilBase(){
        return gm.getMap().findWalkableSpotForCircleNearEdge(evilLairSize, 20);
    }

    public EvilSideBase createEvilSideBase(Point point, TheDarkSide theDarkSide){
        EvilSideBase evilSideBase = new EvilSideBase(point, theDarkSide);
        // TODO make evilside base attackCapable
        //getMapCellByPoint(point).addAttackCapableBuildings(evilSideBase);
        //attackCapableBuildings.add(evilSideBase);
        gm.getMap().setBuildingOnPoint(point, evilSideBase);
        evilSideBases.add(evilSideBase);
        List<Point> areaPoints = gm.getMap().getAllPointsInCircleAroundTarget(point, evilLairSize);
        for(Point p: areaPoints){
            if(p.equals(point)){
                continue;
            }
            if (p.getBuilding() instanceof Farm farm){
                destroyBuilding(farm);
                if (farmsToRemove.contains(farm)) {
                    farmsToRemove.remove(farm);
                }
            }
            EvilSideBaseArea area = new EvilSideBaseArea(p, evilSideBase);
            evilSideBase.addEvilSideBaseArea(area);
            gm.getMap().setBuildingOnPoint(p, area);
        }
        return evilSideBase;
    }

    public Unit spawnUnitAt(Unit unit, Point point){
        unitsToBeAdded.add(unit);
        return unit;
    }

    public Point getEvilSideTarget(){
        if (!cities.isEmpty()){
            return cities.getFirst().getPoint();
        }
        else if (!towns.isEmpty()){
            return towns.getFirst().getPoint();
        }
        else if (!villages.isEmpty()){
            return villages.getFirst().getPoint();
        }
        else return null;
    }

    public void plunderMapCell(MapCell mapCell, Unit unit){
        for (int x = mapCell.getCellX()*MAP_CELL_SIZE; x<(mapCell.getCellX()+1)*MAP_CELL_SIZE; x++){
            for (int y=mapCell.getCellY()*MAP_CELL_SIZE; y<(mapCell.getCellY()+1)*MAP_CELL_SIZE; y++){
                Point point = gm.getMap().getPoint(x,y);
                if (point.getBuilding() instanceof Farm farm && farm.isAlive()){
                    unit.addLoot(farm.getPeople()*10);
                    farm.setAlive(false);
                    farmsToRemove.add(farm);
                }
            }
        }
    }

    public boolean isMapCellValid(int x, int y){
        return (x >= 0 && x < mapCellGrid.length && y >= 0 && y < mapCellGrid[0].length);
    }


    // ------------------------------- TEST POINT -----------------

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
        gm.getGridPanel().repaint();
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
        Farm farm = getFarmFromPool();
        farm.activate(point, 1);
        
        gm.getMap().setBuildingOnPoint(point, farm);
        farms.add(farm);
        gm.getGridPanel().updateUI();
        return farm;
    }

    public void printMapInfo(){
        System.out.println("map info: ");
        System.out.println("total amount of units: " + units.size());
        System.out.println("Orcs: " + units.stream().filter(unit -> unit instanceof Orc).toList().size());
        System.out.println("Knights: " + units.stream().filter(unit -> unit instanceof Knight).toList().size());
        System.out.println("Farms: " + farms.size());
        System.out.println("Inactive farms: " + inactiveFarms.size());
        System.out.println("Villages: " + villages.size());
        System.out.println("Towns: " + towns.size());
        System.out.println("Cities: " + cities.size());
        int total = gm.getMap().getGrid().length * gm.getMap().getGrid()[0].length;
        System.out.println("Whole map size: " + total);
        System.out.println("All buildings is total: " + (farms.size()+towns.size()+villages.size()));
    }
    
    public void createCompleteVillageAt(int x, int y){
        Point point = gm.getMap().getPoint(x, y);
        List<Point> surroundings = gm.getMap().getAllValidAdjecantPointsToTarget(point);
        surroundings.add(point);
        for(Point p: surroundings){
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
        System.out.println(road.size() + " = road langd");
        gm.getGridPanel().updateUI();
    }
    
    public Farm createFarmAtRandomPoint(){
        Point randomPoint = gm.getMap().getRandomPoint();
        if (!randomPoint.isEmpty()){
            return null;
        }
        Farm farm = getFarmFromPool();
        farm.activate(randomPoint, 1);
        
        gm.getMap().setBuildingOnPoint(randomPoint, farm);
        farms.add(farm);
        System.out.println("Farm created at " + farm.getInfo());
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
        if (point.getBuilding() == null){
            System.out.println("No building there to destroy!");
        }
        else {
            if (point.getBuilding() instanceof CityArea cityArea){
                //destroyBuilding(cityArea.getCityCenter());
                citiesToRemove.add(cityArea.getCityCenter());
            }
            else if (point.getBuilding() instanceof TownArea townArea){
                //destroyBuilding(townArea.getTownCenter());
                townsToRemove.add(townArea.getTownCenter());
            }
            else if (point.getBuilding() instanceof Farm farm && farm.getFarmOwningBuilding() instanceof Village village){
                //destroyBuilding(village);
                villagesToRemove.add(village);
            }
            else if (point.getBuilding() instanceof Town town){
                townsToRemove.add(town);
            }
            else if (point.getBuilding() instanceof City city){
                citiesToRemove.add(city);
            }
            else if (point.getBuilding() instanceof Village village){
                villagesToRemove.add(village);
            }
            else if (point.getBuilding() instanceof Farm farm){
                farmsToRemove.add(farm);
            }

        }
    }

    public void currentExperiment2(){
        for(int x=0; x<gm.getMap().width; x++){
            for(int y=0; y<gm.getMap().height; y++){
                Point point = gm.getMap().getPoint(x,y);
                if (point.getBuilding() != null && point.getBuilding() instanceof FarmOwningBuilding farmOwningBuilding){
                    for (Point p: farmOwningBuilding.getControlledLand()){
                        if (p.getPointOwner() != farmOwningBuilding){
                            System.out.println("OBS OBS. One of buildings controlled points doesnt recognise owner");
                            System.out.println(p.getInfo());
                            System.out.println(farmOwningBuilding.getControlledLand());
                        }
                    }
                }
                if (point.getPointOwner() != null && !point.getPointOwner().getControlledLand().contains(point)){
                    System.out.println("OOBS OBS a point that points to an owner doesnt recognise its point!");
                    System.out.println(point.getInfo());
                    System.out.println(point.getPointOwner().getControlledLand());
                }
            }
        }
        System.out.println("end of search");
    }

    public void experimentTimeMethod(){

        /**
         * In this method, create a for loop and call whatever feature you want thousands of times
         * to get an approximate time how long it takes to perform
         *
         * Below is an example
         */
        /*
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

         */

        /*
        long startTime = System.currentTimeMillis();


        double x1 = 153.2;
        double y1 = 157.2;
        double size1 = 0.2;
        double x2 = 120.0;
        double y2 = 120.0;
        double size2 = 0.8;
        boolean test = true;
        for (int x=0; x<1000000; x++){
            test = checkCollision(x1, y1, size1, x2, y2, size2);
        }
         */

        /*
        // test 1
        populateTestArea(25);
        performGetUnitListTest(2);
        performCallAllUnitInMapCellsTest(2);
        performGetUnitListTest(4);
        performCallAllUnitInMapCellsTest(4);
        populateTestArea(75);
        performGetUnitListTest(2);
        performCallAllUnitInMapCellsTest(2);
        performGetUnitListTest(4);
        performCallAllUnitInMapCellsTest(4);
        populateTestArea(125);
        performGetUnitListTest(0);
        performCallAllUnitInMapCellsTest(0);
        performGetUnitListTest(2);
        performCallAllUnitInMapCellsTest(2);

         */
        /*long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println(", Duration: " + duration + "ms");

         */
    }

    public void spawnOrcRandomlyTest(){
        for (int x=0; x<4; x++){
            if (gm.getMap().isValidAndWalkable(x,x)){
                Orc orc = new Orc(x,x);
                unitsToBeAdded.add(orc);
            }
        }
        spawnKnightsRandomlyTest();
    }

    public void spawnKnightsRandomlyTest(){
        for (int x=0; x<2; x++){
            if (gm.getMap().isValidAndWalkable(x,x+100)){
                Knight knight = new Knight(x,x+100);
                unitsToBeAdded.add(knight);
            }
        }
    }

    public void spawnTwoOppositeFightingGroups() {
        // Spawn 20 knights dispersed around (250, 150)
        int troopAmount = ThreadLocalRandom.current().nextInt(5);
        List<Point> knightPoints = findGroupSpawnPoints(250, 100, troopAmount);
        int a =0;
        for (Point p : knightPoints) {
            a++;
            if (a % 2 == 0){
                unitsToBeAdded.add(new ElfArcher(p.getX(), p.getY()));
            }
            else{
                unitsToBeAdded.add(new Knight(p.getX(), p.getY()));
            }
        }

        List<Point> orcPoints = findGroupSpawnPoints(50, 100, troopAmount);
        for (Point p : orcPoints) {
            a++;
            if (a % 2 == 0){
                unitsToBeAdded.add(new Orc(p.getX(), p.getY()));
            }
            else{
                unitsToBeAdded.add(new GoblinArcher(p.getX(), p.getY()));
            }        }

        /*
        double random = ThreadLocalRandom.current().nextDouble();
        if (random > 0.99){
            orcPoints = findGroupSpawnPoints(50, 150, 1);
            for (Point p : orcPoints) {
                unitsToBeAdded.add(new Dragon(p.getX(), p.getY()));
            }
        }*/

    }

    public void spawnOneTroopForPathFinding(){
        if (units.size() < 100){
            Point p = getRandomWalkablePoint();
            Knight knight = new Knight(p.getX(), p.getY());
            unitsToBeAdded.add(knight);
        }

    }

}