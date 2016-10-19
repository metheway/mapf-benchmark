package solvers.astar;

import constants.CostFunction;
import constants.Keys;
import solvers.ConstrainedSolver;
import solvers.states.ODState;
import utilities.ProblemInstance;

import java.util.HashMap;

/**
 * Class that implements Trevor Standley's operator decomposition
 * variant of the A* search algorithm
 */

public class OperatorDecomposition extends GenericAStar {


    /**
     * Constructor that creates a solver that uses
     * operator decomposition A*
     */
    public OperatorDecomposition(){ super(); }

    public OperatorDecomposition(ConstrainedSolver highLevel) {
        super();
    }

    /**
     * Constructor that takes parameters (unused at the moment)
     * @param params a map containing parameters to alter solver behavior
     */
    public OperatorDecomposition(HashMap<Keys, Object> params) {
        super(params);
    }

    public TDHeuristic getHeuristic() {
        return super.getHeuristic();
    }

    @Override
    public State createRoot(ProblemInstance problemInstance) {
        return new ODState(CostFunction.SUM_OF_COSTS, problemInstance);
    }

    public String toString() {
        return "Operator Decomposition";
    }

}
