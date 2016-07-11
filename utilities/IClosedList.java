package utilities;

import solvers.astar.State;

/**
 * Specifies methods for a closed list in an
 * A*-based solver
 */
public interface IClosedList {

    /**
     * Returns whether this state is in the closed list
     * @param state state to consider
     * @return true if this state is in the closed list, false otherwise
     */
    boolean contains(State state);

    /**
     * Adds this state to the closed list
     * @param state state to add
     */
    void add(State state);

    /**
     * Empties the closed list
     */
    void clear();

}
