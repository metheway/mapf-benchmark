package utilities;


import constants.CostFunction;
import solvers.Reservation;
import solvers.astar.State;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Util {
    // TODO generalize transposition check
    public static HashSet<Coordinate> reservedFromPath(Path path) {
        HashSet<Coordinate> reservation = new HashSet<>();
        if (path.getLast() instanceof SingleAgentState) {
            for (State s : path) reservation.add(((SingleAgentState) s).coordinate());
        } else {
            for (State s : path) ((MultiAgentState) s).getSingleAgentStates()
                    .forEach(singleAgentState -> reservation.add(singleAgentState.coordinate()));
        }
        return reservation;
    }

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

    public static int lengthOfLongestPath(List<Path> paths) {
        int max = 0;
        for (Path path : paths) {
            if (path.size() > max) max = path.size();
        }
        return max;
    }

}