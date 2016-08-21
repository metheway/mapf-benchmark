package solvers;

import solvers.astar.State;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;
import utilities.Coordinate;
import utilities.Node;
import utilities.Path;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a reservation table for constrained solvers.
 */
public class Reservation {

    public Map<Coordinate, Coordinate> reservedCoordinates;
    private Map<Node, Integer> agentDestinations;
    private int lastTimeStep;

    /**
     * Constructor that creates an empty reservation table
     */
    public Reservation() {
        reservedCoordinates = new HashMap<>();
        agentDestinations = new HashMap<>();
    }

    /**
     * Reserve all coordinates in a given path
     * @param path the path to get reservations from
     */
    public void reservePath(Path path) {
        if (path.getLast() instanceof SingleAgentState) {
            path.forEach(state ->
                    reserveSingleAgentStateCoordinate((SingleAgentState) state));
            SingleAgentState end = (SingleAgentState) path.getLast();
            reserveDestination(end.coordinate());
        } else {
            path.forEach(state -> ((MultiAgentState) state).getSingleAgentStates()
                            .forEach(singleAgentState -> reserveSingleAgentStateCoordinate(singleAgentState)));
            MultiAgentState end = (MultiAgentState) path.getLast();
            end.getSingleAgentStates().forEach(singleAgentState -> reserveDestination(singleAgentState.coordinate()));
        }
    }

    private void reserveSingleAgentStateCoordinate(SingleAgentState singleAgentState) {

        if (singleAgentState.isRoot()) reservedCoordinates.put(singleAgentState.coordinate(), null);
        else reservedCoordinates.put(singleAgentState.coordinate(),
                ((SingleAgentState)singleAgentState.predecessor()).coordinate());
    }

    /**
     * Free all reservations from a given Path
     * @param path the Path to remove reservations from
     */
    public void freeReservation(Path path) {
        if (path.getLast() instanceof SingleAgentState) {
            path.forEach(state ->
                    reservedCoordinates.remove(((SingleAgentState) state).coordinate()));
            SingleAgentState end = (SingleAgentState) path.getLast();
            freeDestination(end.coordinate());
        } else {
            path.forEach(state -> ((MultiAgentState) state).getSingleAgentStates()
                    .forEach(singleAgentState -> reservedCoordinates.remove(singleAgentState.coordinate())));
            MultiAgentState end = (MultiAgentState) path.getLast();
            end.getSingleAgentStates().forEach(singleAgentState -> freeDestination(singleAgentState.coordinate()));
        }
        updateLastTimeStep();
    }

    /**
     * Returns whether the given state is considered valid
     * given the reservations.
     * @param state the state to evaluate
     * @return true if the state is valid, false otherwise
     */
    public boolean isValid(State state) {
        if (state instanceof SingleAgentState) {
            SingleAgentState singleAgentState = (SingleAgentState) state;
                    // reserved explicitly?
            return !(coordinateReserved(singleAgentState.coordinate())
                    // agent destination?
                    || blockedByStationaryAgent(singleAgentState.coordinate())
                    // does it cause a transposition?
                    || transpositionOccurred(reservedCoordinates, singleAgentState.isRoot() ? null :
                                                                        ((SingleAgentState) singleAgentState.predecessor()).coordinate(),
                                                singleAgentState.coordinate()));
        } else if (state instanceof MultiAgentState){
            for (SingleAgentState singleAgentState : ((MultiAgentState) state).getSingleAgentStates()) {
                if (!isValid(singleAgentState)) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * Reserve a single coordinate
     * @param coordinate the coordinate to reserve
     * @param previous the coordinate that would cause a transposition
     */
    public void reserveCoordinate(Coordinate coordinate, Coordinate previous) {
        reservedCoordinates.put(coordinate, previous);
        lastTimeStep = Math.max(coordinate.getTimeStep(), lastTimeStep);
    }

    /**
     * Free a single coordinate from the reservations
     * @param coordinate the coordinate to free
     */
    public void freeCoordinate(Coordinate coordinate) {
        reservedCoordinates.remove(coordinate);
        agentDestinations.remove(coordinate.getNode());
        updateLastTimeStep();
    }


    /**
     * Reserve a destination, creating a reservation that holds
     * past the time step of the coordinate
     * @param coordinate the destination to reserve
     */
    public void reserveDestination(Coordinate coordinate) {
        lastTimeStep = Math.max(coordinate.getTimeStep(), lastTimeStep);
        agentDestinations.put(coordinate.getNode(), coordinate.getTimeStep());
    }

    /**
     * Free a destination reservation
     * @param coordinate the coordinate to free
     */
    private void freeDestination(Coordinate coordinate) {
        agentDestinations.remove(coordinate.getNode());
    }

    protected boolean coordinateReserved(Coordinate coordinate) {
        return reservedCoordinates.containsKey(coordinate);
    }

    public boolean blockedByStationaryAgent(Coordinate coordinate) {
        return agentDestinations.get(coordinate.getNode()) != null
                && agentDestinations.get(coordinate.getNode()) <= coordinate.getTimeStep();
    }

    private boolean transpositionOccurred(Map<Coordinate, Coordinate> reservedCoordinates, Coordinate previous, Coordinate current) {
        boolean result = false;
        if (previous == null) return result;
        current.setTimeStep(current.getTimeStep() - 1);
        previous.setTimeStep(previous.getTimeStep() + 1);
        if (reservedCoordinates.containsKey(previous)
                && reservedCoordinates.get(previous) != null) {
            result = reservedCoordinates.get(previous).equals(current);
        }
        current.setTimeStep(current.getTimeStep() + 1);
        previous.setTimeStep(previous.getTimeStep() - 1);
        return result;
    }

    /**
     * Returns the last time step in the reservation
     * table, excluding agent destinations
     * @return the last time step of a reservation
     */
    public int getLastTimeStep() {
        return lastTimeStep;
    }

    /**
     * Resets the reservation table
     */
    public void clear() {
        reservedCoordinates.clear();
        agentDestinations.clear();
        lastTimeStep = 0;
    }

    private void updateLastTimeStep() {
        lastTimeStep = 0;
        for (Coordinate coordinate : reservedCoordinates.keySet()) {
            lastTimeStep = Math.max(lastTimeStep, coordinate.getTimeStep());
        }
    }
}
