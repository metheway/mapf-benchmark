package solvers.cbs;

import solvers.ConstrainedSolver;
import solvers.Solver;
import solvers.astar.State;
import utilities.Path;
import utilities.ProblemInstance;

import java.util.List;
import java.util.PriorityQueue;

public class MACBS implements Solver {

    private ConstrainedSolver solver;
    private State goal;
    private PriorityQueue<State> openList;
    private ProblemInstance problemInstance;
    private List<Integer> rootMetaAgents;

    public MACBS(Path parameter){}

    public MACBS(){}

    @Override
    public boolean solve(ProblemInstance problemInstance) {
        return false;
    }

    public void init(ProblemInstance problemInstance) {

    }

    @Override
    public Path getPath() {
        return null;
    }
}
