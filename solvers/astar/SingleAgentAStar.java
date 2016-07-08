package solvers.astar;

import constants.CostFunction;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;
import utilities.ProblemInstance;

import java.util.HashMap;

/**
 * Class that implements the A* algorithm for a single agent
 */
public class SingleAgentAStar extends GenericAStar {

    /**
     * Constructor that creates a solver object that uses the
     * basic A* algorithm to solve problem instances
     */
    public SingleAgentAStar() { super(); }

    public SingleAgentAStar(HashMap<String, ?> params) {
        super(params);
    }

    public State createRoot(ProblemInstance problemInstance) {
        if (problemInstance.getAgents().size() > 1)
            throw new IllegalArgumentException("Passed ProblemInstance has more than one agent!");

        return new MultiAgentState(CostFunction.SUM_OF_COSTS, problemInstance);
    }
}
