package utilities;

import solvers.astar.State;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a closed list of individual states
 */
public class StateClosedList implements IClosedList {

    private Map<State, State> map;

    /**
     * Creates an empty closed list
     */
    public StateClosedList() {
        map = new HashMap<>();
    }

    @Override
    public boolean contains(State state) {
        if (!map.containsKey(state)) {
            return false;
        } else {
            State other = map.get(state);
            if (state.gValue() < other.gValue()) {
                map.remove(other);
                return false;
            }
            return true;
        }
    }

    @Override
    public void add(State state) {
        if (state.belongsInClosedList()) map.put(state, state);
    }

    @Override
    public void clear() {
        map.clear();
    }

    /**
     * Returns the map that backs
     * this closed list
     * @return the map that backs this closed list
     */
    public Map<State, State> getMap() {
        return map;
    }
}
