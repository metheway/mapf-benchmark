
package solvers.states;

import java.util.List;
import java.util.ArrayList;

import constants.Positions;
import solvers.ConflictAvoidanceTable;
import solvers.MultiLevelCAT;
import solvers.astar.TDHeuristic;
import utilities.Agent;
import utilities.Node;
import utilities.Coordinate;
import utilities.ProblemInstance;
import constants.Costs;
import constants.Terrain;

import solvers.astar.State;

/*
 * heuristic class
 * map, probleminstance (agent goals)
 * lookup table n x k, n = graph size, k = number of agents
 * 
 * public function to calculate heuristic (set heuristic)
 * a* should call setHeuristic before adding to openList
 * 
 * change multiagent hashCode to take into account all agents' positions (not just the list)
 * 
 * trueDistanceHeuristic class
 * run UCS from goal with no goal states
 * closed list will become lookup table
 * 
 * Heuristic interface
 * a* should receive an object implementing Heuristic
 * 
 * Graph String - check in ProblemInstance File constructor
 * 
 */

public class SingleAgentState extends State {

    private final int agentId;
    private final int agentGoal;
    private final Coordinate coord;

    // calculate g-value in the state itself instead of passing cost into the 
    // constructor
    
    // pass a cost function into expand()
    
    public SingleAgentState(int agentId, Node currentNode,
                            SingleAgentState backPointer, ProblemInstance problem){
        super(backPointer);
        this.agentId = agentId;
        if (!isRoot()) {
            coord = new Coordinate(backPointer.coord.getTimeStep() + 1, currentNode);
        } else {
            coord = new Coordinate(0, currentNode);
        }
        this.agentGoal = problem.getGoal().get(agentId).getIndexInGraph();

        calculateCost(problem);
        this.conflictViolations = backPointer.conflictViolations;
    }

    public SingleAgentState(int agentId, ProblemInstance problem) {
        super(null);
        Agent a = problem.getAgents().get(agentId);
        Node currentNode = problem.getGraph().getNodes().get(a.position());
        this.agentGoal = a.goal();
        this.agentId = agentId;
        coord = new Coordinate(0, currentNode);
    }

    /**
     * expand() method specified by abstract State
     *
     * @return a list of states reachable from this one
     */
    public List<State> expand(ProblemInstance problem) {
        List<State> neighbors = new ArrayList<>();
        neighbors.add(waitState(problem));

        Node[] neighborNodes = coord.getNode().getNeighbors();
        for (int i = 0; i < neighborNodes.length; i++) {
            Node neighbor = neighborNodes[i];
            if (neighbor != null && neighbor.isReachable(i)){
                State nextState = new SingleAgentState(agentId, neighbor, this, problem);
                neighbors.add(nextState);
            }
        }

        return neighbors;
    }

    public void updateCATViolations(MultiLevelCAT conflictAvoidanceTable) {
        this.conflictViolations += conflictAvoidanceTable.totalViolations(this);
    }

    /**
     * Return the Coordinate object associated with this state
     *
     * @return the Coordinate object associated with this state
     */
    public Coordinate coordinate() {
        return coord;
    }

    /**
     * Generate a state that represents waiting in the current position
     * for the case when the agent must wait.
     * @return the generated state
     */
    private SingleAgentState waitState(ProblemInstance problem) {
        return new SingleAgentState(agentId, coord.getNode(), this, problem);
    }


    /**
     * Helper method to generate costs in the expand() method
     * @param  position  the place in the neighbors array
     * @return  the cost of moving to this location from the current state
     */
    private double costOfNeighbor(int position) {
        double cost = Costs.ADJACENT;
        Node currentNode = coord.getNode();
        Node nextNode = coord.getNode().getNeighbors()[position];
        if (this.equals(predecessor())) return Costs.STAY;

        if (Positions.isDiagonal(position)) cost = Costs.DIAGONAL;
        if (currentNode.getType() == Terrain.WATER || nextNode.getType() == Terrain.WATER) cost *= Costs.WATER_PENALTY;
        else if (currentNode.getType() == Terrain.TREE || nextNode.getType() == Terrain.TREE) cost *= Costs.TREE_PENALTY;

        return cost;
    }

    @Override
    public int timeStep() {
        return coordinate().getTimeStep();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((coord.getNode() == null) ? 0 : coord.getNode().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SingleAgentState other = (SingleAgentState) obj;
        if (coord.getNode() == null) {
            if (other.coord.getNode() != null)
                return false;
        } else if (!(coord.getNode().equals(other.coord.getNode())))
            return false;

        return true;
    }

    public boolean crossConflicts(SingleAgentState other) {
        if (other == null)
            return false;
        if (coord.getNode() == null) {
            if (other.coord.getNode() != null)
                return false;
        } else if (!(coord.getNode().equals(other.coord.getNode())))
            return false;
        if(coord.getTimeStep() == other.coord.getTimeStep() +1) {
            return true;
        }
        if(coord.getTimeStep() == other.coord.getTimeStep() -1) {
            return true;
        }
        return false;
    }

    @Override
    protected void calculateCost(ProblemInstance problem) {
        if (isRoot()) {
            return;
        }

        Node[] neighbors = coord.getNode().getNeighbors();
        SingleAgentState pred = (SingleAgentState) predecessor();

        gValue = pred.gValue();
        if (this.equals(pred)) {
            if (!goalTest(problem)) gValue += Costs.STAY;
            return;
        }

        Node predNode = pred.coord.getNode();
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] != null)
                if (neighbors[i].equals(predNode)) {
                    gValue += costOfNeighbor(i);
                    return;
                }
        }
    }

    public int getAgentId() {
        return agentId;
    }

    public int getAgentGoal() {
        return agentGoal;
    }

    public void setHeuristic(TDHeuristic heuristic) {
        hValue = heuristic.trueDistance(coord.getNode(), agentGoal);
    }

    public boolean isLegal(SingleAgentState other){
        if(this.equals(other)){
            return false;
        }
        if(other.crossConflicts((SingleAgentState)(super.backPointer))){
            if(this.crossConflicts((SingleAgentState)(other.backPointer))){
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean goalTest(ProblemInstance problem) {
        // backpointer irrelevant since only agentId and node are
        // compared by equals()
        return coordinate().getNode() == problem.getGoal().get(agentId);
    }

}
