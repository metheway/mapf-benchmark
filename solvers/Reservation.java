package solvers;

import solvers.astar.State;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;
import utilities.Coordinate;
import utilities.Node;
import utilities.Path;

import java.util.HashMap;
import java.util.Map;

public class Reservation {

    // TODO edge coordinate class

    private Map<Coordinate, Coordinate> reservedCoordinates;
    private Map<Node, Integer> agentDestinations;
    private int lastTimeStep;

    public Reservation() {
        reservedCoordinates = new HashMap<>();
        agentDestinations = new HashMap<>();
    }

    // reserve the coordinates in a Path
    // Map instead of Set so that we can tell which path a coordinate belongs to
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

    // free up reservations from a path
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
    }

    public boolean isValid(State state) {
        if (state instanceof SingleAgentState) {
            SingleAgentState singleAgentState = (SingleAgentState) state;
                    // reserved explicitly?
            return !(coordinateReserved(singleAgentState.coordinate())
                    // agent destination?
                    || blockedByStationaryAgent(singleAgentState.coordinate())
                    // does it cause a transposition?
                    || transpositionOccurred(singleAgentState.isRoot() ? null :
                                                                        ((SingleAgentState) singleAgentState.predecessor()).coordinate(),
                                                singleAgentState.coordinate()));
        } else {
            for (SingleAgentState singleAgentState : ((MultiAgentState) state).getSingleAgentStates()) {
                if (!isValid(singleAgentState)) {
                    return false;
                }
            }
            return true;
        }
    }

    public void reserveCoordinate(Coordinate coordinate) {
        reservedCoordinates.put(coordinate, null);
        lastTimeStep = Math.max(coordinate.getTimeStep(), lastTimeStep);
    }

    private void reserveDestination(Coordinate coordinate) {
        lastTimeStep = Math.max(coordinate.getTimeStep(), lastTimeStep);
        agentDestinations.put(coordinate.getNode(), coordinate.getTimeStep());
    }

    private void freeDestination(Coordinate coordinate) {
        agentDestinations.remove(coordinate.getNode());
    }

    private boolean coordinateReserved(Coordinate coordinate) {
        return reservedCoordinates.containsKey(coordinate);
    }

    public boolean blockedByStationaryAgent(Coordinate coordinate) {
        return agentDestinations.get(coordinate.getNode()) != null
                && agentDestinations.get(coordinate.getNode()) <= coordinate.getTimeStep();
    }

    public boolean transpositionOccurred(Coordinate previous, Coordinate current) {
        boolean result = false;
        if (previous == null) return result;
        current.setTimeStep(current.getTimeStep() - 1);
        previous.setTimeStep(previous.getTimeStep() + 1);
        if (reservedCoordinates.containsKey(previous)) {
            result = reservedCoordinates.get(previous).equals(current);
        }
        current.setTimeStep(current.getTimeStep() + 1);
        previous.setTimeStep(previous.getTimeStep() - 1);
        return result;
    }

    public int getLastTimeStep() {
        return lastTimeStep;
    }

    public void clear() {
        reservedCoordinates.clear();
        agentDestinations.clear();
        lastTimeStep = 0;
    }
}
