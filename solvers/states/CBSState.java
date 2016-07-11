package solvers.states;

import solvers.astar.State;
import solvers.astar.TDHeuristic;
import utilities.Coordinate;
import utilities.ProblemInstance;

import java.util.List;


/**
 * Tree node in Conflict-Based search. Represents a set of solutions
 * subject to constraints determined by predecessors in the tree.
 */
public class CBSState extends State {

    private double cost;
    Coordinate constraint;

    public CBSState(CBSState backPointer, Coordinate constraint) {
        super(backPointer);
        this.constraint = constraint;
    }

    public boolean solveSingleProblem(int problemIndex) {
        return false;
    }

    public void populateConstraints() {

    }

    public Coordinate constraint() {
        return constraint;
    }

    @Override
    public List<State> expand(ProblemInstance problem) {
        return null;
    }

    @Override
    protected void calculateCost(ProblemInstance problemInstance) {

    }

    @Override
    protected void setHeuristic(TDHeuristic heuristic) {
        hValue = 0;
    }

    @Override
    public int timeStep() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean goalTest(ProblemInstance problemInstance) {
        return false;
    }
    // a constraint: coordinate (edge or vertex) + agent constrained
    // set of solutions from planning for agents

    // root constructor - pass the start and goal locations of all agents
    //                  set constraints to null
    //                  lack of constraints is unique to root, also null back pointer
    //                  solve each single-agent problem optimally
    //                      when solving each single agent, tie-break to avoid previously-assigned agents
    //                  calculate and set cost
    // conflict field - constraint to add

    // copy constructor receives one new constraint
    // replan for constrained agent
    // update cost

    // goaltest: new constraint is null

    // calculateHeuristic always sets hValue to 0

    // setConflict function

}
