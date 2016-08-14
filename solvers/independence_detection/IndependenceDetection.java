package solvers.independence_detection;

import solvers.ConstrainedSolver;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;
import utilities.*;

import java.util.*;

/**
 * Class that implements Trevor Standley's simple independence
 * detection algorithm. Each time two groups of agents conflict, they
 * are merged and planned cooperatively as a single group using any
 * multi-agent solver.
 */
public class IndependenceDetection extends ConstrainedSolver {

    private ProblemInstance initialProblem;
    private List<ProblemInstance> problemList;
    private List<Path> pathList;
    private ConstrainedSolver solver;

    public IndependenceDetection(ConstrainedSolver solver) {
        this.solver = solver;
    }

    @Override
    public boolean solve(ProblemInstance problemInstance) {

        // initialize a set of multiagent problems
        // for each agent in each problem instance, make a new problem and insert it into the set
        // solve all subproblems
        // validate solutions: check for conflicts
        // if paths conflict, extract both, unite subproblems, solve the new problem, validate solution, repeat until no conflicts are found
        // (simple independence detection)
        initialProblem = problemInstance;
        if (!populatePaths(problemInstance)) return false;

        int index = 0;
        //System.out.println("problems at start: " + problemList.size());
        while (index < pathList.size()) {
            System.out.println("checking path " + index + " for conflicts...");
            Conflict conflict = Util.conflict(index, 0, this.pathList);
            if (conflict != null) {
                int numProblemsBefore = problemList.size();
                if (!resolveConflict(conflict.getGroup1(), conflict.getGroup2())) return false;
                if (problemList.size() < numProblemsBefore) {
                    if (conflict.getGroup2() < index) index--;
                } else {
                    index = Math.min(conflict.getGroup1(), conflict.getGroup2());
                }
            } else {
                index++;
            }
        }

        return true;
    }

    protected boolean resolveConflict(int index, int indexOfConflict) {
        ProblemInstance joined = handleProblemMerge(index, indexOfConflict);

        boolean solved = solver.solve(joined);

        if (!solved) return false;
        System.out.println("conflict resolved");
        Path joinedPath = solver.getPath();
        int offset = (indexOfConflict < index) ? -1 : 0;
        pathList.set(index + offset, joinedPath);
        return true;
    }

    protected ProblemInstance handleProblemMerge(int index, int indexOfConflict) {
        ProblemInstance problem = problemList.get(index);
        ProblemInstance conflicting = problemList.get(indexOfConflict);
        ProblemInstance joined = problem.join(conflicting);
        problemList.set(index, joined);
        problemList.remove(indexOfConflict);
        pathList.remove(indexOfConflict);
        return joined;
    }

    protected boolean populatePaths(ProblemInstance problemInstance) {
        init();
        for (Agent agent : problemInstance.getAgents()) {
            ProblemInstance newProblem = new ProblemInstance(problemInstance.getGraph(), new ArrayList<>());
            newProblem.addAgent(agent);
            problemList.add(newProblem);
        }
        for (ProblemInstance problem : problemList) {
            boolean solved = solver.solve(problem);
            if (!solved) return false;
            pathList.add(solver.getPath());
        }
        return true;
    }

    protected void init() {
        problemList = new ArrayList<>();
        pathList = new ArrayList<>();
    }

    public Path getPath() {
        return Util.mergePaths(pathList, initialProblem);
    }

    public List<Path> paths() {
        return pathList;
    }

    public List<ProblemInstance> problems() {
        return problemList;
    }

    public ConstrainedSolver solver() {
        return solver;
    }

    // get a list of the paths
    //
    //  for 0 <= i < length of longest path
    //      for 0 <= j < numPaths
    //          look at the jth path
    //          add all single agents from the ith state in the path (if i >= path size, return the last one)
    //

    // think of a way with less overhead :o
    // get list of solutions
    // for 0 <= i < numSols
    //      for 0 <= j < ith path length
    //          for i + 1 <= k < numSols
    //              check if path k has a conflict at time step j
    // still O(numSols^2 * <pathlength>) bleh


}
