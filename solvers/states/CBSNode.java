package solvers.states;

import constants.Keys;
import solvers.ConflictAvoidanceTable;
import solvers.ConstrainedSolver;
import solvers.astar.GenericAStar;
import solvers.astar.State;
import solvers.astar.TDHeuristic;
import utilities.*;

import java.util.*;


public class CBSNode extends State {

    private CBSConstraint constraint;
    private Conflict conflict;
    private List<Path> solutions;
    private boolean consistent;
    private Map<Coordinate, Coordinate> constraints;

    private static final ProblemInstance IRRELEVANT = null;

    public CBSNode(State backPointer, CBSConstraint constraint) {
        super(backPointer);
        this.solutions = new ArrayList<Path>(((CBSNode) predecessor()).solutions);
        this.constraint = constraint;
    }

    public CBSNode(ProblemInstance problemInstance, List<GenericAStar> solvers) {
        super(null);
        this.solutions = new ArrayList<>();
        for (int agent = 0; agent < problemInstance.getAgents().size(); agent++) {
            solveSingleton(solvers.get(agent), problemInstance, agent);
            solvers.get(agent).addParam(Keys.PREPROCESS, false);
            solutions.add(
                    consistent ? solvers.get(agent).getPath() : null
            );
        }
        calculateCost(IRRELEVANT);
    }

    @Override
    public List<State> expand(ProblemInstance problem) {
        List<State> neighbors = new ArrayList<>();

        Coordinate conflictCoordinate1 = conflictCoordinate(conflict.getGroup1());
        Coordinate conflictPrev1 = conflictPrev(conflict.getGroup1());

        Coordinate conflictCoordinate2 = conflictCoordinate(conflict.getGroup2());
        Coordinate conflictPrev2 = conflictPrev(conflict.getGroup2());

        CBSConstraint constraint1 = new CBSConstraint(conflict.getGroup1(), conflictCoordinate1, conflictPrev1);
        CBSConstraint constraint2 = new CBSConstraint(conflict.getGroup2(), conflictCoordinate2, conflictPrev2);

        neighbors.add(new CBSNode(this, constraint1));
        neighbors.add(new CBSNode(this, constraint2));

        return neighbors;
    }

    private Coordinate conflictCoordinate(int index) {
        Path path = solutions.get(index);
        int adjustedTime = Math.min(path.size() - 1, conflict.getTimeStep());
        SingleAgentState conflictState = ((MultiAgentState) path.get(adjustedTime)).getSingleAgentStates().get(0);
        Coordinate fromState = new Coordinate(conflict.getTimeStep(), conflictState.coordinate().getNode());
        return fromState;
    }

    private Coordinate conflictPrev(int index) {
        Path path = solutions.get(index);
        int adjustedTime = Math.min(path.size() - 1, conflict.getTimeStep() - 1);
        SingleAgentState previousState = ((MultiAgentState) path.get(adjustedTime)).getSingleAgentStates().get(0);
        Coordinate fromState = new Coordinate(conflict.getTimeStep() - 1, previousState.coordinate().getNode());
        return fromState;
    }

    public void updateCATViolations(ConflictAvoidanceTable conflictAvoidanceTable) {

    }

    public void replan(GenericAStar solver, ProblemInstance problemInstance) {
        populateConstraints();
        solver.getReservation().clear();

        for (Coordinate coordinate : constraints.keySet()) {
            solver.getReservation().reserveCoordinate(
                    coordinate, constraints.get(coordinate)
            );
        }
        //System.out.println(solver.getReservation().reservedCoordinates);
        //System.out.println("constraints populated.");
        solveSingleton(solver, problemInstance, constraint.constrainedAgent());
        if (consistent) {
            solutions.set(constraint.constrainedAgent(), solver.getPath());
        }
        calculateCost(IRRELEVANT);
        //System.out.println("cost computed as: " + gValue);
        constraints.clear();
    }

    private void populateConstraints() {
        CBSNode current = this;
        constraints = new HashMap<>();
        //System.out.println(constraint);
        while (current != null) {
            if (current.constraint != null) {
                if (current.constraint.constrainedAgent()
                        == this.constraint.constrainedAgent()) {
                    constraints.put(current.constraint.coordinate(), current.constraint.previous());
                }
            }
            current = (CBSNode) current.predecessor();
        }
    }

    public boolean isConsistent() {
        return consistent;
    }

    public CBSConstraint constraint() {
        return constraint;
    }

    public List<Path> solutions() {
        return solutions;
    }

    @Override
    protected void calculateCost(ProblemInstance problemInstance) {
        for (Path path : solutions) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CBSNode cbsNode = (CBSNode) o;

        if (constraint != null ? !constraint.equals(cbsNode.constraint) : cbsNode.constraint != null) return false;
        return !(solutions != null ? !solutions.equals(cbsNode.solutions) : cbsNode.solutions != null);

    }

    @Override
    public int hashCode() {
        int result = constraint != null ? constraint.hashCode() : 0;
        result = 31 * result + (solutions != null ? solutions.hashCode() : 0);
        return result;
    }

    @Override
    public boolean goalTest(ProblemInstance problemInstance) {
        findConflict();
        return conflict == null;
    }

    private void findConflict() {
        for (int indexToCheck = 0; indexToCheck < solutions.size() && conflict == null; indexToCheck++) {
            for (int checkAgainstBeginning = 0; checkAgainstBeginning < solutions.size() && conflict == null; checkAgainstBeginning++) {
                conflict = Util.conflict(indexToCheck, checkAgainstBeginning, solutions);
            }
        }
    }

    private void solveSingleton(ConstrainedSolver solver, ProblemInstance problemInstance, int agent) {
        Agent singleton = problemInstance.getAgents().get(agent);
        Agent problemAgent = new Agent(singleton.position(), singleton.goal(), 0);
        ProblemInstance singletonProblem = new ProblemInstance(problemInstance.getGraph(),
                Collections.singletonList(problemAgent));
        consistent = solver.solve(singletonProblem);
    }
}
