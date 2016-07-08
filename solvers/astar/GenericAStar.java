package solvers.astar;

import java.util.*;

import solvers.ConstrainedSolver;
import utilities.*;

/**
 * Abstract class that specifies an A*-based solver.
 */
public abstract class GenericAStar extends ConstrainedSolver {

    private Map<String, ?> params;
    protected PriorityQueue<State> openList;
    protected IClosedList closedList;
    protected State goal;
    private TDHeuristic heuristic;

    public GenericAStar() {
        this(new HashMap<>());
    }

    /**
     * Creates an A* solver with default behavior that can be altered by
     * a map of parameters
     * @param params parameters to alter the behavior of an A* solver
     */
    public GenericAStar(Map<String, ?> params) {
        this.params = params;
        openList = new PriorityQueue<>();
        closedList = new StateClosedList();
    }

    /**
     * Creates and returns a root state for the specified problem instance
     * @param problemInstance problem instance to create root state from
     * @return the root state for the problem instance
     */
    public abstract State createRoot(ProblemInstance problemInstance);

    /**
     * Solves the search problem specified by the root state
     * @return true if the goal was reached, false otherwise
     */
    public boolean solve(ProblemInstance problem) {
        init(problem);
        State current = createRoot(problem);
        setStateHeuristic(current);
        openList.add(current);
        closedList.add(current);
        while (!openList.isEmpty()) {
            current = openList.remove();
            if (isGoal(problem, current) && current.timeStep() >= getReservation().getLastTimeStep()) {
                goal = current;
                return true;
            }
            List<State> neighbors = current.expand(problem);

            for (State s : neighbors) {
                handleNeighbor(s);
            }
        }
        return false;
    }

    protected void handleNeighbor(State state) {
        if (getReservation().isValid(state)) {
            if (!closedList.contains(state)) {
                setStateHeuristic(state);
                openList.add(state);
                closedList.add(state);
            }
        }
    }

    /**
     * Get the path taken to the goal state
     * @return a List of States leading to the goal state (in reverse)
     */
    public Path getPath() {
		List<State> pre = new ArrayList<>();
    	if (goal != null) {
    		State current = goal;
    		while (current != null) {
    			pre.add(current);
    			current = current.predecessor();
    		}
    	}
        Collections.reverse(pre);
    	return new Path(pre);
    }


    protected void setStateHeuristic(State s) {
        s.setHeuristic(heuristic);
    }
    
    /**
     * Reset the open and closed lists to solve a new problem of the same type.
     */
    protected void init(ProblemInstance problem) {
    	goal = null;
    	openList.clear();
        closedList = new StateClosedList();
        heuristic = new TDHeuristic(problem);
    }
    
    protected boolean isGoal(ProblemInstance p, State s) {
    	return s.goalTest(p);
    }

    @Override
    public String toString() {
        return "A*";
    }

}
