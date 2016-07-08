package solvers.states;

public class CBSState {
    private double cost;
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
