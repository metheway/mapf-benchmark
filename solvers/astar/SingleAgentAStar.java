package solvers.astar;

import constants.CostFunction;
import constants.Keys;
import solvers.ConstrainedSolver;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;
import utilities.CoordinateClosedList;
import utilities.ProblemInstance;

import java.util.HashMap;

/**
 * Class that implements the A* algorithm for a single agent
 */
public class SingleAgentAStar extends MultiAgentAStar {

    /**
     * Constructor that creates a solver object that uses the
     * basic A* algorithm to solve problem instances
     */
    public SingleAgentAStar() { this(null, -1); }

    public SingleAgentAStar(ConstrainedSolver highLevel, int groupToSolve) {
        super(CostFunction.SUM_OF_COSTS, highLevel, groupToSolve);
    }

    public SingleAgentAStar(HashMap<Keys, Object> params) {
        super(CostFunction.SUM_OF_COSTS);
    }

    public State createRoot(ProblemInstance problemInstance) {
        if (problemInstance.getAgents().size() > 1)
            throw new IllegalArgumentException("Passed ProblemInstance has more than one agent!");

        return super.createRoot(problemInstance);
    }

    public String toString() {
        return "Single agent A*";
    }

}
