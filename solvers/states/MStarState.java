package solvers.states;

import solvers.astar.State;
import solvers.astar.TDHeuristic;
import utilities.ProblemInstance;

import java.util.List;

public class MStarState extends State {
/*
//    /*
    fields: set of MultiAgentStates, backpointer
//     */

    // for each agent, call multiagent constructor with single agent
    // store in set of multiagentstates
    // set predecessor to null
    public MStarState() {
        super(null);
    }

    // copy constructor

    /*
    Create the successors call unite if needed. if called unite add all predecessors to the list of returned states
     */
    @Override
    public List<State> expand(ProblemInstance problem) {
        return null;
    }

    //unite two multi-agent states by state index and unite these states in all predecessors up to the root
    private void unite(int index1, int index2){

    }
    @Override
    public void setHeuristic(TDHeuristic heuristic) {

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

    @Override
    protected void calculateCost(ProblemInstance problemInstance) {

    }

    @Override
    public int timeStep() {
        return 0;
    }
}
