package solvers;


import solvers.astar.State;
import solvers.states.SingleAgentState;
import utilities.Conflict;
import utilities.Coordinate;
import utilities.Node;
import utilities.Path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class implementing a conflict avoidance table for multi-layered solvers.
 * Wraps a list of conflict avoidance tables, and all methods that shadow CAT methods
 * call the corresponding method in the last conflict avoidance table (the one relevant
 * to the most recently added CAT.
 */
public class MultiLevelCAT {

    private List<ConflictAvoidanceTable> catList;

    public MultiLevelCAT() {
        catList = new ArrayList<>();
    }

    public void addLevel() {
        catList.add(new ConflictAvoidanceTable());
    }

    public void removeLevel() {
        catList.remove(catList.size() - 1);
    }

    /**
     * Return the first violation found at the lowest level
     * @param state state to check
     * @return the first violation found
     */
    public int violation(SingleAgentState state) {
        return catList.get(catList.size() - 1).violation(state);
    }

    public int totalViolations(SingleAgentState state) {
        List<Integer> violations = new ArrayList<>();
        for (ConflictAvoidanceTable conflictAvoidanceTable : catList) {
            int violation = conflictAvoidanceTable.violation(state);
            if (!violations.contains(violation) && violation != ConflictAvoidanceTable.NO_CONFLICT) {
                violations.add(violation);
            }
        }
        return violations.size();
    }

    public void addPath(Path path) {
        catList.get(catList.size() - 1).addPath(path);
    }

    public Conflict simulatePath(Path path, int group) {
        return catList.get(catList.size() - 1).simulatePath(path, group);
    }

    public boolean isValid(State state) {
        return catList.get(catList.size() - 1).isValid(state);
    }

    public Map<Node, int[]> getAgentDestinations() {
        return catList.get(catList.size() - 1).getAgentDestinations();
    }

    public Map<Coordinate, List<Integer>> getGroupOccupantTable() {
        return catList.get(catList.size() - 1).getGroupOccupantTable();
    }

    public Map<Coordinate, List<Coordinate>> getCoordinateTable() {
        return catList.get(catList.size() - 1).getCoordinateTable();
    }

    public void setRelevantGroups(List<Integer> relevantGroups) {
        catList.get(catList.size() - 1).setRelevantGroups(relevantGroups);
    }

    public void setAgentGroups(Map<Integer, Integer> agentGroups) {
        catList.get(catList.size() - 1).setAgentGroups(agentGroups);
    }

    public Map<Integer, Integer> getAgentGroups() {
        return catList.get(catList.size() - 1).getAgentGroups();
    }

    public List<Integer> getRelevantGroups() {
        return catList.get(catList.size() - 1).getRelevantGroups();
    }

    public void clear() {
        catList.get(catList.size() - 1).clear();
    }


}
