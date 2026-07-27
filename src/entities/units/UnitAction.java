package entities.units;

public interface UnitAction {
    /**
     * Executes an action against a neighboring unit.
     * @param neighbor The unit being checked.
     * @return true if the search should end immediately (e.g., a target was found).
     */
    boolean execute(Unit neighbor);
}
