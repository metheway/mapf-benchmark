package solvers.states;

// A* with operator decomposition

import constants.CostFunction;
import solvers.astar.State;
import utilities.ProblemInstance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ODState extends MultiAgentState {

    private int moveNext;
    private ODState prev;

    public ODState(State backPointer, State prev, CostFunction costFunction, ProblemInstance problemInstance,
                   List<SingleAgentState> singleStates, int moveNext) {
        super(backPointer, costFunction, singleStates, problemInstance);
        this.moveNext = moveNext;
        this.prev = (ODState) prev;
    }

    public ODState(CostFunction costFunction, ProblemInstance problemInstance) {
        super(costFunction, problemInstance);
    }

    @Override
    public List<State> expand(ProblemInstance problemInstance) {
        List<State> expandSingleAgent = getSingleAgentStates().get(moveNext).expand(problemInstance);
        List<State> result = new ArrayList<>();
        SingleAgentState currentPos = getSingleAgentStates().get(moveNext);
        SingleAgentState restricted = null; // will stay null unless we need to prevent a transposition
        int indexOfCurrent = getSingleAgentStates().indexOf(currentPos);
        if (indexOfCurrent < moveNext) { // != -1 since moveNext will be returned
            restricted = ((ODState) predecessor()).getSingleAgentStates().get(indexOfCurrent);
        }

        for (State state : expandSingleAgent) {
            SingleAgentState s = (SingleAgentState) state;
            int index = getSingleAgentStates().indexOf(s);
            if (!s.equals(restricted) && (index == -1 || index >= moveNext)) {
                List<SingleAgentState> newList = new ArrayList<>(getSingleAgentStates());
                newList.set(moveNext, s);
                State bp = intermediateState() ? predecessor() : this;
                int updateMoveNext = (moveNext + 1) % getSingleAgentStates().size();
                ODState newState = new ODState(bp, this, getCostFunction(), problemInstance, newList, updateMoveNext);
                result.add(newState);
            }
        }
        return result;
    }

    public boolean intermediateState() {
        return moveNext != 0;
    }

    public boolean goalTest(ProblemInstance problemInstance) {
        return !intermediateState() && super.goalTest(problemInstance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ODState odState = (ODState) o;

        return moveNext == odState.moveNext;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + 23 * moveNext;
    }

    @Override
    public void printIndices() {
        super.printIndices();
        System.out.println(" moveNext: " + moveNext + " pred: " + predecessor() + " this: " + this);
    }

    @Override
    public boolean belongsInClosedList() {
        return moveNext == 0;
    }
}
