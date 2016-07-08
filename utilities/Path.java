package utilities;

import solvers.astar.State;

import java.util.Iterator;
import java.util.List;

/**
 * Thin wrapper for a list of State objects.
 */
public class Path implements Iterable<State> {
    // TODO function to get constraints, merge paths
    // TODO change return type of getPath() to path in solvers
    private List<State> stateList;

    /**
     * Constructor that creates a Path with the given
     * list of State objects
     * @param stateList the list of State objects
     */
    public Path(List<State> stateList) {
        this.stateList = stateList;
    }

    /**
     * Accessor for the list of State objects
     * @return the list of State objects
     */
    public List<State> getStateList() {
        return stateList;
    }

    /**
     * Computes the cost associated with this path
     * @return the cost of this path
     */
    public double cost() {
        return stateList.get(stateList.size() - 1).gValue();
    }

    /**
     * Returns the length of this path
     * @return the length of this path
     */
    public int size() {
        return stateList.size();
    }

    /**
     * Returns the State at the given time step
     * @param timeStep the time step to access
     * @return the State at this time step
     */
    public State get(int timeStep) {
        return stateList.get(timeStep);
    }

    public State getLast() {
        return stateList.get(stateList.size() - 1);
    }

    public Iterator<State> iterator() {
        return stateList.iterator();
    }

    public String toString() {
        return "Length: " + size() + " Cost: " + cost() + "\n" + stateList.toString();
    }

}
