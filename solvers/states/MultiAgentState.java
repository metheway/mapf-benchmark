package solvers.states;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import constants.CostFunction;
import solvers.ConflictAvoidanceTable;
import solvers.MultiLevelCAT;
import solvers.astar.State;
import solvers.astar.TDHeuristic;
import utilities.Agent;
import utilities.ProblemInstance;

/**
 * Representation of a multi-agent state as a list of single agent
 * states
 */
public class MultiAgentState extends State {

    private CostFunction costFunction;
    protected List<SingleAgentState> singleStates;

    /**
     * Constructor that creates a multi-agent state with the specified back pointer,
     * list of single agent states, and cost function from the specified problem
     * instance
     * @param backPointer the state's back pointer
     * @param costFunction the cost function the algorithm should minimize
     * @param singleStates the list of single agent states
     * @param problem the problem instance
     */
    public MultiAgentState(State backPointer, CostFunction costFunction,
                            List<SingleAgentState> singleStates, ProblemInstance problem) {
        super(backPointer);
        this.costFunction = costFunction;
        this.singleStates = singleStates;
        calculateCost(problem);
    }

    /**
     * Constructor that creates a root state from the specified problem instance
     * using the given cost function
     * @param costFunction the cost function to minimize
     * @param problemInstance the problem instance
     */
    public MultiAgentState(CostFunction costFunction, ProblemInstance problemInstance) {
        super(null);
        this.costFunction = costFunction;
        singleStates = new ArrayList<>();
        for (Agent a : problemInstance.getAgents())
            singleStates.add(new SingleAgentState(a.id(), problemInstance));
    }

    @Override
    public List<State> expand(ProblemInstance problem) {
        List<State> answer = new ArrayList<>();
        generateNeighbors(problem, 0, new ArrayList<SingleAgentState>(), answer);
        return answer;
    }

    public void updateCATViolations(MultiLevelCAT conflictAvoidanceTable) {
        this.conflictViolations = 0;
        for (SingleAgentState singleAgentState : getSingleAgentStates()) {
            singleAgentState.updateCATViolations(conflictAvoidanceTable);
            this.conflictViolations += singleAgentState.numCATViolations();
        }
    }

    private List<List<SingleAgentState>> generateNeighbors(ProblemInstance problem) {
        int branchFactor = 0;
        if (!singleStates.isEmpty()) branchFactor = problem.getGraph().getConnectednessNumber() + 1;
        int initialCapacity = (int) Math.pow(branchFactor, singleStates.size());
        List<List<SingleAgentState>> prelim = new ArrayList<>(initialCapacity);
        for (SingleAgentState sa : singleStates) {
            process(sa, problem, prelim);
        }
        filter(prelim, singleStates);
        return prelim;
    }

    private void generateNeighbors(ProblemInstance problem, int agentIndex,
                                                           List<SingleAgentState> currentBranch, List<State> answer) {
        // If all agents are set. Build a new state from the current branch and add it to the returned list
        if(agentIndex == singleStates.size()){
            answer.add(new MultiAgentState(this, costFunction, new ArrayList<SingleAgentState>(currentBranch), problem));
            return;
        }
        SingleAgentState current = singleStates.get(agentIndex);
        // for each legal move of agentIndex:
        List<State> currentMoves = current.expand(problem);
        for(State move : currentMoves) {
            // If move does not conflict with any previous assigned moves in current branch then:
            if (isLegalMove(currentBranch, (SingleAgentState)move)) {
                if (shouldContinue(current, (SingleAgentState)move, problem)) {
                    // add this move to the current branch and call
                    currentBranch.add((SingleAgentState) move);
                    //recursive call
                    generateNeighbors(problem, agentIndex + 1, currentBranch, answer);
                    //remove last entry in the branch
                    currentBranch.remove(currentBranch.size() - 1);
                    revert(current, move);
                }
            }
        }
    }

    protected boolean shouldContinue(SingleAgentState current, SingleAgentState next, ProblemInstance problem){
        return true;
    }

    protected void revert(State current, State next){}

    private boolean isLegalMove(List<SingleAgentState> currentBranch, SingleAgentState state){
        for(SingleAgentState other : currentBranch){
            if(state.isLegal(other) == false){
                return false;
            }
        }
        return true;
    }

    private static void process(SingleAgentState sa, ProblemInstance problem, List<List<SingleAgentState>> processed) {
        int limit = processed.size(); // processed.size() will change during this method
        List<State> exp = sa.expand(problem); // get the list of states to add
        if (processed.size() > 0) {
            for (List<SingleAgentState> ls : processed) ls.add((SingleAgentState) exp.get(0)); // add the first state to all of the processed lists
            for (int i = 1; i < exp.size(); i++) {
                SingleAgentState current = (SingleAgentState) exp.get(i); // cast next state
                for (int j = 0; j < limit; j++) {
                    List<SingleAgentState> clone = new ArrayList<>(processed.get(j)); // make a copy of each list already processed
                    clone.set(clone.size() - 1, current); // set the last state to the current one
                    processed.add(clone); // add this one to the end
                }
            }
        } else {
            for (State s : exp) {
                List<SingleAgentState> newList = new ArrayList<>();
                newList.add((SingleAgentState) s);
                processed.add(newList);
            }
        }
    }

    protected void filter(List<List<SingleAgentState>> list, List<SingleAgentState> states) {
        Iterator<List<SingleAgentState>> lit = list.iterator();
        List<SingleAgentState> current;
        Set<SingleAgentState> duplicates;
        while (lit.hasNext()) {
            current = lit.next();
            duplicates = new HashSet<>(current);
            boolean hasDuplicates = duplicates.size() != current.size();
            if (hasDuplicates || containsTransposition(current, states)) lit.remove();
        }
    }


    protected static boolean containsTransposition(List<SingleAgentState> singleAgentStates, List<SingleAgentState> baseList) {
        Set<SingleAgentState> intersection = new HashSet<>(baseList);
        intersection.retainAll(singleAgentStates);
        if (!intersection.isEmpty()) {
            for (SingleAgentState singleAgentState : intersection) {
                int thisIndex = baseList.indexOf(singleAgentState);
                int otherIndex = singleAgentStates.indexOf(singleAgentState);
                if (thisIndex != otherIndex) {
                    if (baseList.get(otherIndex).equals(singleAgentStates.get(thisIndex))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<SingleAgentState> getSingleAgentStates() {
        return singleStates;
    }

    @Override
    public int timeStep() {
        return singleStates.get(0).coordinate().getTimeStep();
    }

    @Override
    public void setHeuristic(TDHeuristic heuristic) {
        for (SingleAgentState state : singleStates) {
            state.setHeuristic(heuristic);
            hValue += state.hValue();
        }
    }

    @Override
    protected void calculateCost(ProblemInstance problem) {
        if (isRoot()) return;
        else if (costFunction == CostFunction.MAKESPAN) gValue = predecessor().gValue() + 1;
        else if (costFunction == CostFunction.SUM_OF_COSTS) {
            for (SingleAgentState state : singleStates) {
                gValue += state.gValue();
            }
        }
    }

    @Override
    public void printIndices() {
        System.out.print("(");
        for (int i = 0; i < singleStates.size(); i++) {
            System.out.print(singleStates.get(i).coordinate().getNode().getIndexInGraph());
            if (i < singleStates.size() - 1) System.out.print(", ");
        }
        System.out.print(")");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MultiAgentState other = (MultiAgentState) obj;
        if (singleStates == null) {
            if (other.singleStates != null)
                return false;
        } else {
            return singleStates.equals(other.singleStates);
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 23;
        int result = 1;
        for (SingleAgentState singleAgentState : singleStates) {
            result = prime * result + ((singleAgentState == null) ? 0 : singleAgentState.hashCode());
        }
        return result;
    }

    @Override
    public boolean goalTest(ProblemInstance problemInstance) {
        for (SingleAgentState state : singleStates)
            if (!state.goalTest(problemInstance))
                return false;
        return true;
    }

    /**
     * Returns the cost function associated to this state
     * @return the cost function associated to this state
     */
    public CostFunction getCostFunction() {
        return costFunction;
    }

}
