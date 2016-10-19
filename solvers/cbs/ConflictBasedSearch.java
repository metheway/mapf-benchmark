package solvers.cbs;

import constants.CostFunction;
import solvers.ConflictAvoidanceTable;
import solvers.Solver;
import solvers.astar.GenericAStar;
import solvers.astar.MultiAgentAStar;
import solvers.astar.SingleAgentAStar;
import solvers.astar.State;
import solvers.states.CBSNode;
import utilities.Agent;
import utilities.Path;
import utilities.ProblemInstance;
import utilities.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class ConflictBasedSearch implements Solver {

    private List<GenericAStar> solvers;
    private State goal;
    private PriorityQueue<State> openList;
    private ProblemInstance problemInstance;


    public ConflictBasedSearch() {
        solvers = new ArrayList<>();
        openList = new PriorityQueue<>();
    }

    @Override
    public boolean solve(ProblemInstance problemInstance) {
        init(problemInstance);

        CBSNode root = new CBSNode(problemInstance, solvers);
        getInitialConflict(root);

        State current = root;
        openList.add(current);
        while (!openList.isEmpty()){
            current = openList.remove();

            if (current.goalTest(problemInstance)) {
                goal = current;
                return true;
            }

            // otherwise, we know a conflict has occurred
            List<State> neighbors = current.expand(problemInstance);
            //System.out.println("expand reached.");
            for (State child : neighbors) {
                CBSNode childNode = (CBSNode) child;
                int constrainedAgent = childNode.constraint().constrainedAgent();
                childNode.replan(solvers.get(constrainedAgent), problemInstance);
                //System.out.println("child replanned.");
                if (childNode.isConsistent()) {
                    openList.add(childNode);
                    //System.out.println(childNode.constraint().equals(((CBSNode) childNode.predecessor()).constraint()));
                }
            }
        }
        return false;
    }

    private void init(ProblemInstance problemInstance) {
        this.problemInstance = problemInstance;
        solvers.clear();
        openList.clear();
        for (Agent agent : problemInstance.getAgents()) {
            solvers.add(new SingleAgentAStar());
        }
    }

    private void getInitialConflict(CBSNode root) {
        ConflictAvoidanceTable cat = new ConflictAvoidanceTable();
        int group = 0;
        for (Path rootPath : root.solutions()) {
            cat.addPath(rootPath, group);
            group++;
        }
        root.setConflict(cat.getEarliestConflict());
    }

    @Override
    public Path getPath() {
        CBSNode goalNode = (CBSNode) goal;
        return Util.mergePaths(goalNode.solutions(), problemInstance);
    }

    public String toString() {
        return "CBS";
    }
}
