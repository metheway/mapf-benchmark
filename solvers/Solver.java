package solvers;

import utilities.Path;
import utilities.ProblemInstance;

/**
 * Interface that specifies methods for multi-agent solvers
 */
public interface Solver {

    /**
     * Solve a given problem instance
     * @param problemInstance the problem instance
     * @return true if a solution was found, false otherwise
     */
    boolean solve(ProblemInstance problemInstance);

    /**
     * Construct and return the path found by the solver
     * @return the path found by the solver
     */
    Path getPath();

}
