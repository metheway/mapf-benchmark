package solvers.astar;

import solvers.states.SingleAgentState;
import utilities.Agent;
import utilities.ProblemInstance;
import utilities.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Object used to find an abbreviated all-pairs-shortest-paths lookup table
 * to compute a true-distance heuristic
 */

public class TDHeuristic {

    // table to look up costs
    private double[][] lookupTable;

    public TDHeuristic(ProblemInstance problem) {
        initLookup(problem);
    }

    public double trueDistance(Node pos, int agentId) {
        return lookupTable[agentId][pos.getIndexInGraph()];
    }

    private void initLookup(ProblemInstance problem) {
        lookupTable = new double[problem.getAgents().size()][problem.getGraph().getSize()];
        // run UCS
        // fill lookupTable with values from the closed list when UCS finishes
        List<ProblemInstance> roots = rootStates(problem);
        populateLookup(roots, problem);
    }

    private List<ProblemInstance> rootStates(ProblemInstance problem) {
        List<ProblemInstance> roots = new ArrayList<>();
        for (Agent a : problem.getAgents()) {
            ProblemInstance root = new ProblemInstance(problem.getGraph(), Collections.singletonList(new Agent(a.goal(), a.position(), a.id())));
            roots.add(root);
        }
        return roots;
    }

    private void populateLookup(List<ProblemInstance> rootList, ProblemInstance problem) {
        BreadthFirstSearch search = new BreadthFirstSearch();
        int agentId = 0;
        for (ProblemInstance root : rootList) {
            search.solve(root);
            for (State end : search.finalList()) {
                SingleAgentState endState = (SingleAgentState) end;
                Node pos = endState.coordinate().getNode();
                lookupTable[agentId][pos.getIndexInGraph()] = endState.gValue();
            }
            agentId++;
        }
    }


    public double[][] getLookupTable() { return lookupTable; }

}
