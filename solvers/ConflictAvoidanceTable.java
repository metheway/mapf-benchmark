package solvers;

import constants.ConflictType;
import solvers.astar.State;
import solvers.independence_detection.IndependenceDetection;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;
import utilities.Conflict;
import utilities.Coordinate;
import utilities.Node;
import utilities.Path;

import java.util.*;

public class ConflictAvoidanceTable {

    public static final int NO_CONFLICT = -1;
    public static final int NO_GROUP = -1;

    protected int lastTimeStep;

    // coordinate => prev(s)
    protected Map<Coordinate, List<Coordinate>> coordinateTable;
    // coordinate => group at coordinate
    protected Map<Coordinate, List<Integer>> groupOccupantTable;

    protected Map<Node, int[]> agentDestinations;
    protected List<Integer> relevantGroups;
    protected Map<Integer, Integer> agentGroups;

    private static final int DEST_TIME_STEP = 0;
    private static final int DEST_GROUP = 1;

    private Conflict earliestConflict;

    public ConflictAvoidanceTable(Map<Coordinate, List<Coordinate>> coordinateTable,
                                  Map<Coordinate, List<Integer>> groupOccupantTable,
                                  Map<Node, int[]> agentDestinations,
                                  int lastTimeStep,
                                  List<Integer> relevantGroups,
                                  Map<Integer, Integer> agentGroups) {
        this.coordinateTable = coordinateTable;
        this.groupOccupantTable = groupOccupantTable;
        this.agentDestinations = agentDestinations;
        this.lastTimeStep = lastTimeStep;
        this.relevantGroups = relevantGroups;
        this.agentGroups = agentGroups;
    }

    public ConflictAvoidanceTable(Map<Coordinate, List<Coordinate>> coordinateTable,
                                  Map<Coordinate, List<Integer>> groupOccupantTable,
                                  Map<Node, int[]> agentDestinations,
                                  int lastTimeStep) {
        this(coordinateTable, groupOccupantTable, agentDestinations, lastTimeStep, new ArrayList<>(), new HashMap<>());
    }

    public ConflictAvoidanceTable() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), 0, new ArrayList<>(), new HashMap<>());
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

        int result = coordinateConflict(thisCoordinate, state.getAgentGoal());
        if (result == NO_CONFLICT) {
            result = findTransposition(prevCoordinate, thisCoordinate, state.getAgentGoal());
        }
        if (result == NO_CONFLICT) {
            result = destinationConflict(thisCoordinate, state.getAgentGoal());
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

    private int findTransposition(Coordinate previous, Coordinate coordinate, int agentGoal) {
        int conflictingGroup = NO_CONFLICT;
        if (!(previous == null || coordinateTable.get(previous) == null)) {
            coordinate.setTimeStep(coordinate.getTimeStep() - 1);
            previous.setTimeStep(previous.getTimeStep() + 1);
            int index = coordinateTable.containsKey(previous) ?
                    coordinateTable.get(previous).indexOf(coordinate) : -1;
            List<Coordinate> possibleTranspositions = new ArrayList<>();
            if (index != -1) {
                int len = coordinateTable.get(previous).size();
                for (int i = index; i < len && conflictingGroup == NO_CONFLICT; i++) {
                    Coordinate possibleConflictCoordinate = coordinateTable.get(previous).get(i);
                    List<Integer> possibleConflicts = groupOccupantTable.get(possibleConflictCoordinate);
                    Iterator<Integer> groupIter = possibleConflicts.iterator();
                    while (groupIter.hasNext() && conflictingGroup == NO_CONFLICT) {
                        int possibleConflict = groupIter.next();
                        if (relevantGroups.contains(agentGroups.get(possibleConflict)) && possibleConflict != agentGoal) {
                            conflictingGroup = possibleConflict;
                        }
                    }
                }
            }
            coordinate.setTimeStep(coordinate.getTimeStep() + 1);
            previous.setTimeStep(previous.getTimeStep() - 1);
        }
        return conflictingGroup;
    }

    private int coordinateConflict(Coordinate coordinate, int agentGoal) {
        if (groupOccupantTable.get(coordinate) == null) {
            return NO_CONFLICT;
        }

        for (Integer possibleConflict : groupOccupantTable.get(coordinate)) {
            if (relevantGroups.contains(agentGroups.get(possibleConflict)) && possibleConflict != agentGoal) {
                return possibleConflict;
            }
        }

        return NO_CONFLICT;
    }

    private int destinationConflict(Coordinate coordinate, int agentGoal) {
        int conflictingGroup = NO_CONFLICT;
        Node node = coordinate.getNode();
        if (agentDestinations.containsKey(node)) {
            int[] data = agentDestinations.get(node);
            if (data[DEST_TIME_STEP] <= coordinate.getTimeStep()) {
                conflictingGroup = data[DEST_GROUP];
            }
        }
        boolean relevantConflict = relevantGroups.contains(agentGroups.get(conflictingGroup)) ;
        return relevantConflict ? conflictingGroup : NO_CONFLICT;
    }

    public void addPath(Path path) {
        if (path.getLast() instanceof SingleAgentState) {
            path.forEach(state -> addSingleAgentStateCoordinate((SingleAgentState) state, ((SingleAgentState) state).getAgentGoal()));
            SingleAgentState finalState = (SingleAgentState) path.getLast();
            addDestination(finalState.coordinate(), finalState.getAgentGoal());
        } else {
            path.forEach(state -> ((MultiAgentState) state).getSingleAgentStates()
                    .forEach(singleAgentState -> addSingleAgentStateCoordinate(singleAgentState, singleAgentState.getAgentGoal())));
            MultiAgentState finalState = (MultiAgentState) path.getLast();
            finalState.getSingleAgentStates()
                    .forEach(singleAgentState -> addDestination(singleAgentState.coordinate(), singleAgentState.getAgentGoal()));
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

        for (int time = 0; time < path.size() && result == earliestConflict; time++) {
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
                                                coordinate.getNode(),
                                                ConflictType.COLLISION);
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
                                                        prev.getNode(),
                                                        ConflictType.TRANSPOSITION);
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
                                        coordinate.getNode(),
                                        ConflictType.DESTINATION);
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

    public void setRelevantGroups(List<Integer> relevantGroups) {
        this.relevantGroups = relevantGroups;
    }

    public void setAgentGroups(Map<Integer, Integer> agentGroups) {
        this.agentGroups = agentGroups;
    }

    public Map<Integer, Integer> getAgentGroups() {
        return agentGroups;
    }

    public List<Integer> getRelevantGroups() {
        return relevantGroups;
    }

    public ConflictAvoidanceTable deepCopy() {
        // deep copy of coordinate table
        Map<Coordinate, List<Coordinate>> newCoordinateTable = new HashMap<>();
        System.out.println("copying coordinate table");
        for (Coordinate key : getCoordinateTable().keySet()) {
            List<Coordinate> val = getCoordinateTable().get(key);
            newCoordinateTable.put(key, new ArrayList<>(val));
        }

        //deep copy of group table
        Map<Coordinate, List<Integer>> newGroupTable = new HashMap<>();
        System.out.println("copying group mapping");
        for (Coordinate key : getGroupOccupantTable().keySet()) {
            List<Integer> val = getGroupOccupantTable().get(key);
            newGroupTable.put(key, new ArrayList<>(val));
        }

        // deep copy of agent destinations
        Map<Node, int[]> newAgentDestinations = new HashMap<>();
        System.out.println("copying agent destinations");
        for (Node key: getAgentDestinations().keySet()) {
            int val[] = getAgentDestinations().get(key);
            newAgentDestinations.put(key, new int[] {val[0], val[1]});
        }

        return new ConflictAvoidanceTable(newCoordinateTable, newGroupTable, newAgentDestinations, lastTimeStep);

    }

    public int getLastTimeStep() {
        return lastTimeStep;
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
