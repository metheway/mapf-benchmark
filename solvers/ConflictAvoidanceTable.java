package solvers;

import solvers.astar.State;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;
import utilities.Conflict;
import utilities.Coordinate;
import utilities.Node;
import utilities.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConflictAvoidanceTable {

    public static final int NO_CONFLICT = -1;
    public static final int NO_GROUP = -1;

    // coordinate => prev(s)
    private Map<Coordinate, List<Coordinate>> coordinateTable;
    // coordinate => group at coordinate
    private Map<Coordinate, List<Integer>> groupOccupantTable;

    private Map<Node, int[]> agentDestinations;
    private static final int DEST_TIME_STEP = 0;
    private static final int DEST_GROUP = 1;

    private Conflict earliestConflict;

    public ConflictAvoidanceTable() {
        coordinateTable = new HashMap<>();
        groupOccupantTable = new HashMap<>();
        agentDestinations = new HashMap<>();
    }

    public boolean isValid(State state) {
        if (state instanceof SingleAgentState)
            return violation((SingleAgentState) state) == NO_CONFLICT;

        if (state instanceof MultiAgentState)
            return violation((MultiAgentState) state) == NO_CONFLICT;

        return false;
    }

    public Conflict getEarliestConflict() {
        return earliestConflict;
    }
    /**
     * Returns the group that the state conflicts with, if any
     * @param state
     * @return the group that the state conflicts with, -1 otherwise
     */
    public int violation(SingleAgentState state) {
        Coordinate thisCoordinate = state.coordinate();
        Coordinate prevCoordinate = state.isRoot() ?
                null : ((SingleAgentState) state.predecessor()).coordinate();

        int result = coordinateConflict(thisCoordinate);
        if (result == NO_CONFLICT) {
            result = findTransposition(prevCoordinate, thisCoordinate);
        }
        if (result == NO_CONFLICT) {
            result = destinationConflict(thisCoordinate);
        }
        return result;
    }

    /**
     * Returns the group that the state conflicts with, if any
     * @param state
     * @return the group that the state conflicts with, -1 otherwise
     */
    public int violation(MultiAgentState state) {
        int result = NO_CONFLICT;
        for (int i = 0; i < state.getSingleAgentStates().size() && result == NO_CONFLICT; i++) {
            SingleAgentState singleAgentState = state.getSingleAgentStates().get(i);
            result = violation(singleAgentState);
        }
        return result;
    }

    private int findTransposition(Coordinate previous, Coordinate coordinate) {
        int conflictingGroup = NO_CONFLICT;
        if (!(previous == null || coordinateTable.get(previous) == null)) {
            coordinate.setTimeStep(coordinate.getTimeStep() - 1);
            previous.setTimeStep(previous.getTimeStep() + 1);
            int index = coordinateTable.containsKey(previous) ?
                    coordinateTable.get(previous).indexOf(coordinate) : -1;
            if (index != -1) {
                conflictingGroup = groupOccupantTable.get(previous).get(index);
            }
            coordinate.setTimeStep(coordinate.getTimeStep() + 1);
            previous.setTimeStep(previous.getTimeStep() - 1);
        }
        return conflictingGroup;
    }

    private int coordinateConflict(Coordinate coordinate) {
        return groupOccupantTable.get(coordinate) == null ?
                NO_CONFLICT : groupOccupantTable.get(coordinate).get(0);
    }

    private int destinationConflict(Coordinate coordinate) {
        int conflictingGroup = NO_CONFLICT;
        Node node = coordinate.getNode();
        if (agentDestinations.containsKey(node)) {
            int[] data = agentDestinations.get(node);
            if (data[DEST_TIME_STEP] <= coordinate.getTimeStep()) {
                conflictingGroup = data[DEST_GROUP];
            }
        }
        return conflictingGroup;
    }

    public void addPath(Path path, int group) {
        if (path.getLast() instanceof SingleAgentState) {
            path.forEach(state -> addSingleAgentStateCoordinate((SingleAgentState) state, group));
            SingleAgentState finalState = (SingleAgentState) path.getLast();
            addDestination(finalState.coordinate(), group);
        } else {
            path.forEach(state -> ((MultiAgentState) state).getSingleAgentStates()
                    .forEach(singleAgentState -> addSingleAgentStateCoordinate(singleAgentState, group)));
            MultiAgentState finalState = (MultiAgentState) path.getLast();
            finalState.getSingleAgentStates()
                    .forEach(singleAgentState -> addDestination(singleAgentState.coordinate(), group));
        }
    }

    /**
     * Returns the earliest conflict that a path introduces, or the
     * earliest conflict found while populating the CAT
     * @param path the path to run
     * @param group the group number of the path
     * @return the earliest conflict found
     */
    public Conflict simulatePath(Path path, int group) {
        Conflict result = earliestConflict;

        int endTime = earliestConflict != null ?
                earliestConflict.getTimeStep() : path.size();

        final int TIME_LIMIT = Math.min(endTime, path.size());
        for (int time = 0; time < TIME_LIMIT && result == earliestConflict; time++) {
            MultiAgentState multiAgentState = (MultiAgentState) path.get(time);
            int violation = violation(multiAgentState);
            if (violation != NO_CONFLICT) {
                result = new Conflict(time, group, violation);
            }
        }
        return result;
    }

    private void addSingleAgentStateCoordinate(SingleAgentState singleAgentState, int group) {
        SingleAgentState pred = (SingleAgentState) singleAgentState.predecessor();
        Coordinate prev = singleAgentState.isRoot() ? null : pred.coordinate();
        addCoordinate(singleAgentState.coordinate(), prev, group);
    }

    protected void addCoordinate(Coordinate coordinate, Coordinate prev, int group) {
        Conflict updatedConflict = earliestConflict;

        // collision
        if (!coordinateTable.containsKey(coordinate)) {
            ArrayList<Coordinate> prevList = new ArrayList<>();
            prevList.add(prev);
            ArrayList<Integer> groupList = new ArrayList<>();
            groupList.add(group);
            coordinateTable.put(coordinate, prevList);
            groupOccupantTable.put(coordinate, groupList);
        } else {
            coordinateTable.get(coordinate).add(prev);
            groupOccupantTable.get(coordinate).add(group);
            int otherGroup = groupOccupantTable.get(coordinate).get(0);
            Conflict newConflict = new Conflict(coordinate.getTimeStep(),
                                                group,
                                                otherGroup,
                                                coordinate.getNode(),
                                                coordinate.getNode());
            boolean earlier = earliestConflict == null
                            || newConflict.getTimeStep() < earliestConflict.getTimeStep();
            updatedConflict = earlier ? newConflict : earliestConflict;
        }

        int timeToCheck = earliestConflict == null ? Integer.MAX_VALUE : earliestConflict.getTimeStep();

        // transposition
        if (coordinateTable.containsKey(coordinate)
                && updatedConflict == earliestConflict
                && prev != null) {
            coordinate.setTimeStep(coordinate.getTimeStep() - 1);
            prev.setTimeStep(prev.getTimeStep() + 1);

            if (coordinateTable.get(prev) != null) {
                List<Coordinate> beforeCoords = coordinateTable.get(prev);
                for (int i = 0; i < beforeCoords.size() && updatedConflict == earliestConflict; i++) {
                    Coordinate before = beforeCoords.get(i);
                    if (before.equals(coordinate)) {
                        updatedConflict = new Conflict(coordinate.getTimeStep() + 1,
                                                        i,
                                                        group,
                                                        coordinate.getNode(),
                                                        prev.getNode());
                    }
                }
            }

            coordinate.setTimeStep(coordinate.getTimeStep() + 1);
            prev.setTimeStep(prev.getTimeStep() - 1);

            // destination collision

            if (updatedConflict == earliestConflict
                    && agentDestinations.containsKey(coordinate.getNode())) {
                int[] destData = agentDestinations.get(coordinate.getNode());
                if (destData[DEST_TIME_STEP] <= coordinate.getTimeStep()) {
                    updatedConflict = new Conflict(coordinate.getTimeStep(),
                                        destData[DEST_GROUP],
                                        group,
                                        coordinate.getNode(),
                                        coordinate.getNode());
                }
            }
        }

        if (coordinate.getTimeStep() < timeToCheck) {
            boolean shouldReplace = (earliestConflict == null && updatedConflict != earliestConflict)
                                ||  (earliestConflict != null && updatedConflict.getTimeStep() < earliestConflict.getTimeStep());
            if (shouldReplace) {
                earliestConflict = updatedConflict;
            }
        }
    }

    protected void addDestination(Coordinate coordinate, int group) {
        agentDestinations.put(coordinate.getNode(),
                                new int[] {coordinate.getTimeStep(), group});
    }

    public Map<Node, int[]> getAgentDestinations() {
        return agentDestinations;
    }

    public Map<Coordinate, List<Integer>> getGroupOccupantTable() {
        return groupOccupantTable;
    }

    public Map<Coordinate, List<Coordinate>> getCoordinateTable() {
        return coordinateTable;
    }

    public void clear() {
        coordinateTable = new HashMap<>();
        groupOccupantTable = new HashMap<>();
        agentDestinations = new HashMap<>();
        earliestConflict = null;
    }

    public String toString() {
        return "Coordinate table: " + coordinateTable + "\n"
                + "Groups table: " + groupOccupantTable + "\n"
                + "Agent destinations: " + agentDestinations;
    }
}
