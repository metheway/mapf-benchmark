package solvers;

import utilities.Path;
import utilities.ProblemInstance;
import utilities.Statistics;

import java.util.List;

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

    /**
     * Gets statistics about the solution and solver
     * @param statistics A list of enum telling the solver what statistics are requested. A solver should be
     *      smart about handling a requested statistic that does not apply to it
     * @return Returns a string containing the requested statistics. This string will be placed in a CSV file and
     *      should be formatted appropriately.
     */
    // String getStatistics(List<Statistics> statistics);
}
