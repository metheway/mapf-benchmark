package solvers.states;

import constants.CostFunction;
import solvers.astar.State;
import utilities.ProblemInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guni on 12/14/16.
 */
public class EPEAState extends MultiAgentState {
    private double currentF;
    private double nextF;

    public EPEAState(State backPointer, CostFunction costFunction, ProblemInstance problemInstance,
                   List<SingleAgentState> singleStates) {
        super(backPointer, costFunction, singleStates, problemInstance);
    }

    public EPEAState(CostFunction costFunction, ProblemInstance problemInstance) {
        super(costFunction, problemInstance);
    }

//    @Override
//    private List<List<SingleAgentState>> generateNeighbors(ProblemInstance problem) {
//        List<List<SingleAgentState>> prelim = new ArrayList<>();
//        for (SingleAgentState sa : singleStates) {
//            process(sa, problem, prelim);
//        }
//        filter(prelim, singleStates);
//        return prelim;
//    }
//
//    private List<List<SingleAgentState>> generateNeighbor(ProblemInstance problem, List<List<SingleAgentState>> prelim, )
//
//    private static void process(SingleAgentState sa, ProblemInstance problem, List<List<SingleAgentState>> processed) {
//        int limit = processed.size(); // processed.size() will change during this method
//        List<State> exp = sa.expand(problem); // get the list of states to add
//        if (processed.size() > 0) {
//            for (List<SingleAgentState> ls : processed) ls.add((SingleAgentState) exp.get(0)); // add the first state to all of the processed lists
//            for (int i = 1; i < exp.size(); i++) {
//                SingleAgentState current = (SingleAgentState) exp.get(i); // cast next state
//                for (int j = 0; j < limit; j++) {
//                    List<SingleAgentState> clone = new ArrayList<>(processed.get(j)); // make a copy of each list already processed
//                    clone.set(clone.size() - 1, current); // set the last state to the current one
//                    processed.add(clone); // add this one to the end
//                }
//            }
//        } else {
//            for (State s : exp) {
//                List<SingleAgentState> newList = new ArrayList<>();
//                newList.add((SingleAgentState) s);
//                processed.add(newList);
//            }
//        }
//    }

}
