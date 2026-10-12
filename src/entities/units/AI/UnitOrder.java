package entities.units.AI;

// states that units can be in, determines their actions
public enum UnitOrder {
    NONE,
    DEFEND_EXTERNAL_AREA,
    IDLING,
    RAIDING,
    RETURN_LOOT,
    RETURN_REPORT,
    AWAITING_INSTUCTIONS
}
