package solvers.astar;


import constants.Keys;
import solvers.states.SingleAgentState;
import utilities.ProblemInstance;
import utilities.StateClosedList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Perform a breadth-first search on the graph in the problem instance,
 * resulting in a closed list that gives the exact cost from the root location
 * to each node
 */
public class BreadthFirstSearch extends GenericAStar {

    /**
     * Constructor that creates a breadth first search solver
     */
    public BreadthFirstSearch() {
        super(new HashMap<>());
    }

    @Override
    protected void setStateHeuristic(State s) {
        s.setHeuristic(0);
    }

    /**
     * Returns the closed list with computed optimal costs
     * @return the closed list
     */
    public Set<State> finalList() {
        return new HashSet<>(((StateClosedList)closedList).getMap().values());
    }

    public State createRoot(ProblemInstance problemInstance) {
        return new SingleAgentState(0, problemInstance);
    }

    @Override
    protected boolean isGoal(ProblemInstance p, State s) {
        return false;
    }

    @Override
    protected void init(ProblemInstance problem) {
        goal = null;
        openList.clear();
        closedList.clear();
    }

}
