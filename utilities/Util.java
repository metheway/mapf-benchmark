package utilities;


import constants.CostFunction;
import solvers.astar.State;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;

import java.util.*;

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

    public static Conflict conflict(int index, int startIndex, List<Path> pathList) {
        Path thisPath = pathList.get(index);
        for (int i = startIndex; i < pathList.size(); i++) {
            if (i != index) {
                Path path = pathList.get(i);
                //System.out.println("checking path " + i);
                for (int t = 1; t < thisPath.size(); t++) {
                    MultiAgentState odState = (MultiAgentState) thisPath.get(t);
                    MultiAgentState compareWith;
                    if (t < path.size()) compareWith = (MultiAgentState) path.get(t);
                    else compareWith = (MultiAgentState) path.get(path.size() - 1);

                    List<SingleAgentState> maStateSingle = odState.getSingleAgentStates();
                    List<SingleAgentState> compareWithSingle = compareWith.getSingleAgentStates();
                    HashSet<SingleAgentState> filter = new HashSet<>(maStateSingle);
                    filter.retainAll(compareWithSingle);

                    boolean filterEmpty = filter.isEmpty();
                    boolean transpositionOccurred = transposition(index, maStateSingle, i, compareWithSingle, t, pathList);
                    if (!filterEmpty || transpositionOccurred) {
                        //System.out.println("conflict found between path " + index + " and path " + i + " at time step " + t);
                        //System.out.println("conflict type: " + ((!filterEmpty) ? "collision" : "transposition"));
                        return new Conflict(t, index, i);
                    }
                }
            }
        }
        return null;
    }

    public static boolean transposition(int index, List<SingleAgentState> maStateSingle, int otherIndex, List<SingleAgentState> compareWithSingle, int timeStep, List<Path> pathList) {
        List<SingleAgentState> maStatePrevSingle = ((MultiAgentState) pathList.get(index).get(timeStep - 1)).getSingleAgentStates();
        List<SingleAgentState> compareWithPrevSingle;
        if (timeStep - 1 < pathList.get(otherIndex).size()) compareWithPrevSingle = ((MultiAgentState) pathList.get(otherIndex).get(timeStep - 1)).getSingleAgentStates();
        else compareWithPrevSingle = ((MultiAgentState) pathList.get(otherIndex).get(pathList.get(otherIndex).size() - 1)).getSingleAgentStates();

        int indexInMaStatePrev = 0;
        for (SingleAgentState singleAgentState : maStatePrevSingle) {
            int indexInCompareWith = compareWithSingle.indexOf(singleAgentState);
            if (indexInCompareWith != -1) {
                SingleAgentState comp = compareWithPrevSingle.get(indexInCompareWith);
                if (maStateSingle.get(indexInMaStatePrev).equals(comp)) {
                    return true;
                }
            }
            indexInMaStatePrev++;
        }
        return false;
    }

    public static int indexOfEarliest(List<Coordinate> list) {
        int index = 0;
        int currentIndex = 1;
        int min = list.get(0).getTimeStep();
        for (Coordinate coordinate : list.subList(1, list.size())) {
            if (coordinate.getTimeStep() < min) {
                min = coordinate.getTimeStep();
                index = currentIndex;
            }
            ++currentIndex;
        }
        return index;
    }

    public static <E extends Comparable<E>> int argmin(List<E> list) {
        E min = list.get(0);
        int result = 0;
        int currentIndex = 1;
        for (E elem : list.subList(1, list.size())) {
            if (elem.compareTo(min) < 0) {
                min = elem;
                result = currentIndex;
            }
            ++currentIndex;
        }
        return result;
    }

}