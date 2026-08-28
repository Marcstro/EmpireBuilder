Todo:

- balance units
- create specific "evil scout troops"
- create "militia" units, basic units
- create "mounted knights"
- fix bug where units overlapse when they just stand around at arrived spot
- create proper AI for units (large)
- implement fortress?
- land/point/mapCell value
- possible "mapsections", like 50x50 points. will hold threatlevle instead of mapcells
- give raiding units good defenseless targets if possible
- Add all removeUnit/effect/building etc in their own tick that gets ticked the fastest of all


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
- Exchange old object-oriented inheritance system into an Entity-component system (ECS pattern)


- Implementations that could be changed:

Thoughts:
- legend for what colours represents
- Hover over box when having mouse hover over/click a point in grid? Displaying info about that point