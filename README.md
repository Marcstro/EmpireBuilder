Todo:

- balance units
- fix bug where units overlapse when they just stand around at arrived spot
- implement fortress?
- land/point/mapCell value
- possible "mapsections", like 50x50 points. will hold threatlevle instead of mapcells
- give raiding units good defenseless targets if possible
- Add all removeUnit/effect/building etc in their own tick that gets ticked the fastest of all
- Add "SeldomTick" in the building class, for non every tick things.
- Make farmAnimal-owning villages get the possibility to handle farm animals, instead of just giving them the animals
- UnitOwningTroops need a "can units reach a Point from my location"? that is checked before sending troops somewhere, also handle what to do if its a non possible destination
- change plundercooldown in mapcell to be a number ticking down instead of a boolean
- EvilBase can't be informed of nearby units, since only FarmOwningBuildings are informed
- function detectAllyNeedsHelp() needs developing, buildings should call other buildings asking for help when possible
- only up to 3 units should be idling outside of a UnitOwner, rest should jump into "garrison" where they away deployment
- Regarding garrison, have some sort of "visualisation" how many units a building have in garrison. like 10^x, display x stars
- evilBase should own all of its points
- make destruction of evilBase safe, current will bug if more than 1 exist
- Shooting towers (subbuildings) should not fetch stats from owning building, also new types of shooting towers

Nearest TODOs:
- new Farms should not create grassland on their spot
- mapCells should have id values that can be retrieved and are then the same for every object that needs to use such values
- Create safe "removeBuilding(Building building)"
that takes everything into consideration
- dont have CityLand or Townland. MultiPointSize buildings should be a thing. where there is Point middlePoint and List<Point> exterior points
- completely remove "setLand", or maybe the other function
merge them so that they can and will be used safely
- create effect for farmOwningBuildings when they lose too many farms
- villages look for independent nearby farms outside their direct controlled area on occasion, independent farms should not be permanently independent if avoidable
- Implement "request pathfinding"-queue with limits for how many new paths can be calculated each tick
- Completely rewrite the "multiple frames effects" system

Future TODOs:
- Divert all effects into an "effects manager class", both functional and cosmetic effects. Purpose: Separation of concern, Game does not need to handle everything

- Implementations that could be changed:

Thoughts:
- legend for what colours represents
- Hover over box when having mouse hover over/click a point in grid? Displaying info about that point