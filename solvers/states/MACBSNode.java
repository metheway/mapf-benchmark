package solvers.states;

import solvers.ConflictAvoidanceTable;
import solvers.ConstrainedSolver;
import solvers.astar.MultiAgentAStar;
import solvers.astar.State;
import solvers.astar.TDHeuristic;
import utilities.*;

import java.util.*;

public class MACBSNode extends State {

    /*
    List<List<Integer>> metaAgents

    needs all the functionality of the normal CBS node
    plus the ability to handle meta-agents
    constraints should be the same

    how to store meta agents?
    2D list of integers; metaAgents.get(0) == list of agents merged into meta-agent 0


     */

    private List<List<Integer>> metaAgents;
    private CBSConstraint constraint;
    private List<Path> solutions;
    private boolean consistent;
    private Conflict conflict;
    private Map<Coordinate, Coordinate> constraints;

    private static final ProblemInstance IRRELEVANT = null;

    public MACBSNode(State backPointer, CBSConstraint constraint, List<List<Integer>> metaAgents) {
        super(backPointer);
        this.metaAgents = metaAgents;
        this.constraint = constraint;
        this.solutions = new ArrayList<>(((MACBSNode) predecessor()).solutions);
    }

    public MACBSNode(ProblemInstance problemInstance, List<ConstrainedSolver> solvers, List<List<Integer>> metaAgents) {
        super(null);
        this.solutions = new ArrayList<>();
        this.metaAgents = metaAgents;
        for (int metaAgent = 0; metaAgent < metaAgents.size(); metaAgent++) {
            ProblemInstance meta = metaProblem(metaAgent, problemInstance);
            ConstrainedSolver solver = solvers.get(metaAgent);
            consistent = solver.solve(meta);
            Path newPath = consistent ? solver.getPath() : null;
            solutions.add(newPath);
        }
        calculateCost(IRRELEVANT);
    }

    private ProblemInstance metaProblem(int group, ProblemInstance problemInstance) {
        List<Integer> grouping = metaAgents.get(group);
        List<Agent> newAgents = new ArrayList<>();
        for (int agent = 0; agent < grouping.size(); agent++) {
            Agent oldAgent = problemInstance.getAgents().get(grouping.get(agent));
            newAgents.add(new Agent(oldAgent.position(), oldAgent.goal(), agent));
        }
        return new ProblemInstance(problemInstance.getGraph(), newAgents);
    }
/*
    private Coordinate conflictCoordinate(int index) {
        Path path = solutions.get(index);
        int adjustedTime = Math.min(path.size() - 1, conflict.getTimeStep());
        MultiAgentState multiAgentState = (MultiAgentState) path.get(adjustedTime);
        SingleAgentState conflictState = singleAgentWithCoordinate()
    }
*/

    public void replan(ConstrainedSolver solver, ProblemInstance problemInstance) {
        populateConstraints();
        solver.getReservation().clear();
        solver.getConflictAvoidanceTable().clear();

        for (Coordinate coordinate : constraints.keySet()) {
            solver.getReservation().reserveCoordinate(coordinate, constraints.get(coordinate));
        }

        for (int group = 0; group < solutions.size(); group++) {
            if (group != constraint.constrainedAgent()) {
                Path otherPath = solutions.get(group);
                solver.getConflictAvoidanceTable().addPath(otherPath, group);
            }
        }

        solveMetaProblem(solver, problemInstance);
        if (consistent) {
            solutions.set(constraint.constrainedAgent(), solver.getPath());
        }

        calculateCost(IRRELEVANT);
        constraints.clear();

        Path newPath = solutions.get(constraint.constrainedAgent());
        conflict = solver.getConflictAvoidanceTable().simulatePath(newPath, constraint.constrainedAgent());
    }

    private void solveMetaProblem(ConstrainedSolver solver, ProblemInstance problemInstance) {
        ProblemInstance meta = metaProblem(constraint.constrainedAgent(), problemInstance);
        consistent = solver.solve(meta);
    }

    private void populateConstraints() {
        MACBSNode current = this;
        constraints = new HashMap<>();
        while (current != null) {
            if (current.constraint != null) {
                if (current.constraint.constrainedAgent()
                        == this.constraint.constrainedAgent()) {
                    constraints.put(current.constraint.coordinate(), current.constraint.previous());
                }
            }
            current = (MACBSNode) current.predecessor();
        }
    }

    private SingleAgentState singleAgentWithCoordinate(Coordinate coordinate, MultiAgentState multiAgentState) {
        for (SingleAgentState singleAgentState : multiAgentState.getSingleAgentStates()) {
            if (singleAgentState.coordinate().equals(coordinate)) {
                return singleAgentState;
            }
        }
        return null;
    }

    public void setConflict(Conflict conflict) {
        this.conflict = conflict;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MACBSNode macbsNode = (MACBSNode) o;

        if (constraint != null ? !constraint.equals(macbsNode.constraint) : macbsNode.constraint != null) return false;
        return solutions != null ? solutions.equals(macbsNode.solutions) : macbsNode.solutions == null;

    }

    @Override
    public int hashCode() {
        int result = constraint != null ? constraint.hashCode() : 0;
        result = 31 * result + (solutions != null ? solutions.hashCode() : 0);
        return result;
    }

    public void updateCATViolations(ConflictAvoidanceTable conflictAvoidanceTable) {
        throw new NoSuchMethodError("CAT violations not recorded in CBSNode");
    }

    @Override
    public List<State> expand(ProblemInstance problem) {
        List<State> neighbors = new ArrayList<>();

        Coordinate conflictCoordinate1 = conflict.getConflictCoordinate(conflict.getGroup1());
        Coordinate conflictPrev1 = conflictPrevCoordinate(conflict.getGroup1());

        Coordinate conflictCoordinate2 = conflict.getConflictCoordinate(conflict.getGroup2());
        Coordinate conflictPrev2 = conflictPrevCoordinate(conflict.getGroup2());

        CBSConstraint constraint1 = new CBSConstraint(conflict.getGroup1(), conflictCoordinate1, conflictPrev1);
        CBSConstraint constraint2 = new CBSConstraint(conflict.getGroup2(), conflictCoordinate2, conflictPrev2);

        neighbors.add(new MACBSNode(this, constraint1, metaAgents));
        neighbors.add(new MACBSNode(this, constraint2, metaAgents));

        return neighbors;
    }

    private Coordinate conflictPrevCoordinate(int group) {
        final int NOT_FOUND = -1;
        Path groupSolution = solutions.get(group);
        int adjustedTime = Math.min(conflict.getTimeStep(), groupSolution.size() - 1);
        MultiAgentState conflictMAState = (MultiAgentState) groupSolution.get(adjustedTime);
        int agentIndex = NOT_FOUND;
        int numAgents = conflictMAState.getSingleAgentStates().size();
        for (int index = 0; index < numAgents && agentIndex == NOT_FOUND; index++) {
            SingleAgentState current = conflictMAState.getSingleAgentStates().get(index);
            if (current.coordinate().getNode().equals(conflict.getGroupNode(group))) {
                agentIndex = index;
            }
        }

        Coordinate result = null;
        if (agentIndex != NOT_FOUND) {
            int offset = conflict.getTimeStep() >= groupSolution.size() ? 0 : -1;
            MultiAgentState prevState = (MultiAgentState) groupSolution.get(adjustedTime + offset);
            Coordinate prelim = prevState.getSingleAgentStates().get(agentIndex).coordinate();
            result = new Coordinate(conflict.getTimeStep() - 1, prelim.getNode());
        }

        return result;
    }

    @Override
    protected void calculateCost(ProblemInstance problemInstance) {
        for(Path path : solutions) {
            gValue += path.cost();
        }
    }

    @Override
    protected void setHeuristic(TDHeuristic heuristic) {
        hValue = 0;
    }

    @Override
    public int timeStep() {
        return 0;
    }

    @Override
    public boolean goalTest(ProblemInstance problemInstance) {
        return conflict == null;
    }

}
