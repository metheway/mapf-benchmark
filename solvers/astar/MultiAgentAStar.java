package solvers.astar;

import constants.CostFunction;
import constants.Keys;
import solvers.ConstrainedSolver;
import solvers.Solver;
import solvers.states.MultiAgentState;
import utilities.CoordinateClosedList;
import utilities.ProblemInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that implements A* search for multi-agent states
 */
public class MultiAgentAStar extends GenericAStar {

    private CostFunction costFunction;

    /**
     * Constructor that creates a multi-agent A* search object that
     * searches using the specified cost function
     * @param costFunction the cost function to minimize
     */
    public MultiAgentAStar(CostFunction costFunction) {
        this(costFunction, null, -1);
    }

    public MultiAgentAStar(CostFunction costFunction, ConstrainedSolver highLevel, int groupToSolve) {
        this(highLevel, groupToSolve, costFunction, new HashMap<>());
    }

    /**
     * Constructor that creates a search object with the specified
     * parameters
     * @param params a map containing parameters to alter solver behavior
     */
    public MultiAgentAStar(ConstrainedSolver highLevel, int groupToSolve, CostFunction costFunction, Map<Keys, Object> params) {
        super(highLevel, groupToSolve, params);
        this.costFunction = costFunction;
    }

    @Override
    protected void init(ProblemInstance problemInstance) {
        super.init(problemInstance);
        if (problemInstance.getAgents().size() == 1)
            closedList = new CoordinateClosedList(getReservation());
    }

    @Override
    public State createRoot(ProblemInstance problemInstance) {
        return new MultiAgentState(costFunction, problemInstance);
    }

    public String toString() {
        return "Multiagent A* (" + costFunction.name() + ")";
    }
}
