package solvers.astar;

import constants.CostFunction;
import constants.Keys;
import solvers.states.MultiAgentState;
import utilities.CoordinateClosedList;
import utilities.ProblemInstance;

import java.util.HashMap;

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
        super();
        this.costFunction = costFunction;
    }

    /**
     * Constructor that creates a search object with the specified
     * parameters
     * @param params a map containing parameters to alter solver behavior
     */
    public MultiAgentAStar(HashMap<Keys, Object> params) {
        super(params);
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
}
