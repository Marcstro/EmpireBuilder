package buildings.ai;

/**
 * INTEGRATION GUIDE
 * 
 * Step 1: Add to Building.java
 * ============================
 * In the Building base class, add:
 * 
 *     private BuildingAI buildingAI;
 *     
 *     public BuildingAI getBuildingAI() {
 *         return buildingAI;
 *     }
 *     
 *     protected void initBuildingAI() {
 *         this.buildingAI = BuildingAIFactory.createAI(this);
 *     }
 * 
 * Then in each Building subclass constructor, call initBuildingAI() at the end.
 * 
 * Step 2: Add to Game.java
 * ========================
 * Add a new tick method:
 * 
 *     public void tickBuildingAI() {
 *         for (Building building : buildings) {
 *             if (building.getBuildingAI() != null) {
 *                 building.getBuildingAI().tick(building, this);
 *             }
 *         }
 *     }
 * 
 * Call this from Engine or Game.tick() periodically.
 * It will run frequently (every tick) but profiles internally gate their logic.
 * 
 * Example tick order in Engine.java:
 * 
 *     tickUnit();         // 60 TPS
 *     tickEffects();      // 60 TPS
 *     tickBuildings();    // 60 TPS
 *     tickBuildingAI();   // 60 TPS, but profiles evaluate every ~10 ticks internally
 *     tickTerrainChange(); // Less frequent
 * 
 * Step 3: Add Building AI to existing buildings
 * ==============================================
 * When you create a Fortress, City, Town, Village, etc.:
 * 
 *     public Fortress(...) {
 *         super(...);
 *         // ... other initialization ...
 *         initBuildingAI();  // <-- Call this at the end
 *     }
 * 
 * DESIGN NOTES
 * ============
 * - Each building type gets its own AI profile (FortressAI, CityAI, etc.)
 * - Profiles implement BuildingAIProfile interface
 * - evaluate() returns what state the building should be in (DEFENDING, EXPANDING, etc.)
 * - execute() performs actions for the current state
 * - State transitions are infrequent (every 10+ ticks), but actions run every tick
 * - This keeps heavy decision-making infrequent while letting buildings stay responsive
 * 
 * EXTENDING
 * =========
 * To add a new building type:
 * 1. Create ProfileNameAI.java in src/buildings/ai/profiles/
 * 2. Implement BuildingAIProfile
 * 3. Add a case to BuildingAIFactory.createAI()
 * 4. Call initBuildingAI() from the building's constructor
 */

