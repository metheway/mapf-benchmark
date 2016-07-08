package utilities;

import solvers.astar.State;

import java.util.HashMap;
import java.util.Map;

public class StateClosedList implements IClosedList {

    private Map<State, State> map;

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

    public Map<State, State> getMap() {
        return map;
    }
}
