package solvers.astar;

import solvers.states.SingleAgentState;
import utilities.Agent;
import utilities.ProblemInstance;
import utilities.Node;

import java.util.*;

//TODO make it possible to use makespan heuristic

/**
 * Object used to find an abbreviated all-pairs-shortest-paths lookup table
 * to compute a true-distance heuristic
 */

public class TDHeuristic {

    // table to look up costs
    private double[][] lookupTable;
    private Map<Integer, double[]> lookup;

    public TDHeuristic(ProblemInstance problem) {
        initLookup(problem);
    }

    public double trueDistance(Node pos, int agentId) {
        return lookupTable[agentId][pos.getIndexInGraph()];
    }

    private void initLookup(ProblemInstance problem) {
        lookupTable = new double[problem.getAgents().size()][problem.getGraph().getSize()];
        lookup = new HashMap<>();
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
            lookup.put(problem.getAgents().get(0).position(), new double[problem.getGraph().getSize()]);
            for (State end : search.finalList()) {
                SingleAgentState endState = (SingleAgentState) end;
                Node pos = endState.coordinate().getNode();
                lookup.get(problem.getAgents().get(0).position())[pos.getIndexInGraph()] = endState.gValue();
                lookupTable[agentId][pos.getIndexInGraph()] = endState.gValue();
            }
            agentId++;
        }
    }


    public double[][] getLookupTable() { return lookupTable; }

    public Map<Integer, double[]> getLookup() {
        return lookup;
    }

}
