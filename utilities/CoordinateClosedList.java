package utilities;

import solvers.Reservation;
import solvers.astar.State;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;

import java.util.HashMap;
import java.util.Map;

public class CoordinateClosedList implements IClosedList{

    private Map<Coordinate, Double> map;
    private Reservation reservation;

    public CoordinateClosedList(Reservation reservation) {
        this.reservation = reservation;
        map = new HashMap<>();
    }

    @Override
    public boolean contains(State state) {
        SingleAgentState singleAgentState = ((MultiAgentState) state).getSingleAgentStates().get(0);
        Coordinate otherCoordinate;
        if (singleAgentState.coordinate().getTimeStep() <= reservation.getLastTimeStep()) {
            if (!map.containsKey(singleAgentState.coordinate())) {
                return false;
            } else {
                otherCoordinate = singleAgentState.coordinate();
                if (singleAgentState.gValue() < map.get(singleAgentState.coordinate())) {
                    map.remove(singleAgentState.coordinate());
                    return false;
                }
            }
        } else {
            Node location = singleAgentState.coordinate().getNode();
            otherCoordinate = new Coordinate(-1, location);
            if (!map.containsKey(otherCoordinate)) {
                return false;
            }
        }
        if (singleAgentState.gValue() < map.get(otherCoordinate)) {
            add(singleAgentState);
        }
        return true;
    }

    @Override
    public void add(State state) {
        SingleAgentState singleAgentState = ((MultiAgentState) state).getSingleAgentStates().get(0);
        Coordinate newCoordinate = singleAgentState.coordinate();
        if (singleAgentState.coordinate().getTimeStep() > reservation.getLastTimeStep()) {
            newCoordinate = new Coordinate(-1, singleAgentState.coordinate().getNode());
        }
        map.put(newCoordinate, singleAgentState.gValue());
    }

    @Override
    public void clear() {
        map.clear();
    }
}
