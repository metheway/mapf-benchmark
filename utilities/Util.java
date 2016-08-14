package utilities;


import constants.CostFunction;
import solvers.astar.State;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Methods that are used often but disparately
 */
public class Util {

    public static Random random;
    static {
        random = new Random(2016);
    }

    /**
     * Return a path that represents a collection of paths
     * merged into a single path of multi-agent states
     * @param paths the paths to merge
     * @param problemInstance the problem instance they came from
     * @return the merged path
     */
    public static Path mergePaths(List<Path> paths, ProblemInstance problemInstance) {
        List<State> result = new ArrayList<>();
        List<List<SingleAgentState>> pre = new ArrayList<>();
        int longestLength = lengthOfLongestPath(paths);
        for (int timeStep = 0; timeStep < longestLength; timeStep++) {
            List<SingleAgentState> individual = new ArrayList<>();
            for (int pathIndex = 0; pathIndex < paths.size(); pathIndex++) {
                Path path = paths.get(pathIndex);
                if (timeStep < path.size()) {
                    State current = path.getStateList().get(timeStep);
                    if (current instanceof SingleAgentState) individual.add((SingleAgentState) current);
                    else individual.addAll(((MultiAgentState) current).getSingleAgentStates());
                } else {
                    State current = path.getLast();
                    if (current instanceof SingleAgentState) individual.add((SingleAgentState) current);
                    else individual.addAll(((MultiAgentState) current).getSingleAgentStates());
                }
            }
            pre.add(individual);
        }
        result.add(new MultiAgentState(null, CostFunction.SUM_OF_COSTS, pre.get(0), problemInstance));
        for (int state = 1; state < pre.size(); state++) {
            result.add(new MultiAgentState(result.get(state - 1), CostFunction.SUM_OF_COSTS, pre.get(state), problemInstance));
        }
        return new Path(result);
    }

    private static int lengthOfLongestPath(List<Path> paths) {
        int max = 0;
        for (Path path : paths) {
            if (path.size() > max) max = path.size();
        }
        return max;
    }

}