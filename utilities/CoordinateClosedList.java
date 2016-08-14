package utilities;

import solvers.Reservation;
import solvers.astar.State;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a closed list of coordinates, rather than
 * individual states.
 */
public class CoordinateClosedList implements IClosedList{

    private static final int PERMANENT = -1; // denotes reserved locations regardless of time step

    private Map<Coordinate, Double> map;
    private Reservation reservation;

    /**
     * Constructor that creates an empty closed list given
     * a reservation
     * @param reservation the reservation
     */
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
            otherCoordinate = new Coordinate(PERMANENT, location);
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
        SingleAgentState singleAgentState = state instanceof SingleAgentState ? (SingleAgentState) state :
                ((MultiAgentState) state).getSingleAgentStates().get(0);
        Coordinate newCoordinate = singleAgentState.coordinate();
        if (singleAgentState.coordinate().getTimeStep() > reservation.getLastTimeStep()) {
            newCoordinate = new Coordinate(PERMANENT, singleAgentState.coordinate().getNode());
        }
        map.put(newCoordinate, singleAgentState.gValue());
    }

    @Override
    public void clear() {
        map.clear();
    }
}
