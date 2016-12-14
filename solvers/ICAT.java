package solvers;

import solvers.astar.State;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;
import utilities.Conflict;
import utilities.Coordinate;
import utilities.Node;
import utilities.Path;

import java.util.List;
import java.util.Map;


public interface ICAT {

    int violation(SingleAgentState state);

    int violation(MultiAgentState state);

    void addPath(Path path);

    Conflict simulatePath(Path path, int group);

    boolean isValid(State state);

    Map<Node, int[]> getAgentDestinations();

    Map<Coordinate, List<Integer>> getGroupOccupantTable();

    Map<Coordinate, List<Coordinate>> getCoordinateTable();

    void setRelevantGroups(List<Integer> relevantGroups);

    void setAgentGroups(Map<Integer, Integer> agentGroups);

    Map<Integer, Integer> getAgentGroups();

    List<Integer> getRelevantGroups();

}
