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

/*
TODO: Change solvers so that they keep track of group assignments
TODO: Inherit hard and soft constraints, include a check when finding conflicts
TODO: Conflict avoidance table should now use an agent's goal as its identifier, then group lists should be sent in
TODO: No fast conflict detection in the conflict avoidance table

now conflicts will have the agent goal as the group
so we would have to have the group table as
map: group_num => list<int>
conflict_relevant := solver.parent.group_table[group_num].contains(agent.goal())

solver:
    parent solver (null if solver at highest level)
        in solver interface or in constrained solver?
        constrained solver; solver includes things like SAT, which wouldn't have a CAT anyway
    group partition (unnecessary in low-level solvers; initialize to {[agent0.goal], [agent1.goal], ...})
    group table: group_num => list<int> (we will always construct this)


 */

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
            cat.addPath(rootPath);
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
