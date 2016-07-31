package solvers.states;

import solvers.ConstrainedSolver;
import solvers.astar.*;
import utilities.CBSConstraint;
import utilities.Conflict;
import utilities.Coordinate;
import utilities.Path;
import utilities.ProblemInstance;
import utilities.Util;
import utilities.Agent;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Tree node in Conflict-Based search. Represents a set of solutions
 * subject to constraints determined by predecessors in the tree.
 */
public class CBSState extends State {

    private List<Path> solutions;
    private CBSConstraint constraint;
    private GenericAStar solver;
    private Map<Coordinate, Coordinate> constraints;
    private boolean consistent;
    private Conflict conflict;

    private static final int NO_TIME_STEP = Integer.MAX_VALUE;

    public CBSState(CBSState backPointer, CBSConstraint constraint, ProblemInstance problemInstance, GenericAStar solver) {
        super(backPointer);
        this.constraint = constraint;
        this.constraints = new HashMap<>();
        this.solver = solver;
        this.solutions = isRoot() ? new ArrayList<>() : backPointer.solutions;
        if (!isRoot()) rePlan(problemInstance, solver);
        calculateCost(problemInstance);
    }

    public CBSState(ProblemInstance problemInstance, GenericAStar solver) {
        this(null, null, problemInstance, solver);
        for (Agent agent : problemInstance.getAgents()) {
            Agent singletonAgent =  new Agent(agent.position(), agent.goal(), 0);
            consistent = solver.solve(
                    new ProblemInstance(
                            problemInstance.getGraph(), Collections.singletonList(singletonAgent)));
            if (consistent) solutions.add(solver.getPath());
        }
        //solver.addParam(Keys.PREPROCESS, false);
        calculateCost(problemInstance);
    }

    private void findConflict() {
        //System.out.println("Conflict starts out null: " + (conflict == null));
        for (int index = 0; index < solutions.size() && conflict == null; index++) {
            for (int startIndex = index; startIndex < solutions.size() && conflict == null; startIndex++) {
                conflict = Util.conflict(index, startIndex, solutions);
            }
        }


        /*for (int startIndex = 0; startIndex < solutions.size() && conflict == null; startIndex++) {
            for (int index = startIndex; index < solutions.size() && conflict == null; index++) {
                conflict = Util.conflict(index, startIndex, solutions);
            }
        }*/
        if (!isRoot() && conflict != null && conflict.equals(((CBSState) predecessor()).conflict)) System.out.println("RUH ROH");
    }

    private void populateConstraints() {
        CBSState current = this;
        while (current != null) {
            handleAddConstraint(current);
            current = (CBSState) current.predecessor();
        }
    }

    private void handleAddConstraint(CBSState cbsState) {
        if (cbsState.constraint != null) {
            if (cbsState.constraint.constrainedAgent()
                    == constraint.constrainedAgent()) {
                constraints.put(cbsState.constraint.coordinate(), cbsState.constraint.previous());
                //if (!(cbsState==this || cbsState.isRoot()) && cbsState.constraint.equals(constraint)) System.out.println("ruh roh");
            }
        }
    }

    @Override
    public List<State> expand(ProblemInstance problem) {
        List<State> result = new ArrayList<>();
        //System.out.println(gValue());
        Coordinate conflictCoordinateGroup1 = getCoordinateToConstrain(conflict.getGroup2());
        Coordinate conflictCoordinateGroup2 = getCoordinateToConstrain(conflict.getGroup1());

        //System.out.println(conflict);

        CBSState child1 = getChildState(conflict.getGroup1(),
                                        conflictCoordinateGroup2,
                                        problem);

        CBSState child2 = getChildState(conflict.getGroup2(),
                                        conflictCoordinateGroup1,
                                        problem);
        //System.out.println("My constraint: " + constraint);
        //if (!isRoot())System.out.println("Predecessor: " + ((CBSState) predecessor()).constraint);
        //System.out.println("Expand: \n" + child1.constraint + " " + child1.gValue() + "\n" + child2.constraint + " " + child2.gValue() + "\n");
        if (child1.isConsistent()) result.add(child1);
        if (child2.isConsistent()) result.add(child2);
        // this is not goal, so there is a conflict
        // come up with a way to check conflict indices
        // once we have them
        /*
        use same method as in IndependenceDetection
        now we know that there is only one agent in each state
        so we can do the following:
            constraintCoordinate = conflictingState_i.getSingleAgentStates().get(0).coordinate()
            i = index we're checking
            j = index of conflict
            child1 = new CBSState(this, new Constraint(i, constraintCoordinate))
            child2 = new CBSState(this, new Constraint(j, constraintCoordinate))
            return children
         */

        return result;
    }

    private Coordinate getCoordinateToConstrain(int agentID) {
        Path path = solutions.get(agentID);
        int adjustedTimeStep = conflict.getTimeStep() < path.size() ? conflict.getTimeStep() : path.size() - 1;
        MultiAgentState conflicting = (MultiAgentState) path.get(adjustedTimeStep);
        Coordinate fromState = conflicting.getSingleAgentStates().get(0).coordinate();

        return new Coordinate(conflict.getTimeStep(), fromState.getNode());
    }

    private CBSState getChildState(int index, Coordinate conflictCoordinate, ProblemInstance problemInstance) {
        CBSConstraint newConstraint = new CBSConstraint(index, conflictCoordinate, prevCoordinate(conflictCoordinate, index));
        //System.out.println("new constraint: " + newConstraint);
        return new CBSState(this, newConstraint, problemInstance, solver);
    }

    private Coordinate prevCoordinate(Coordinate coordinate, int index) {
        Path solution = solutions.get(index);
        int adjustedTime = (coordinate.getTimeStep() < solution.size()) ? coordinate.getTimeStep() : solution.size();
        SingleAgentState singleAgentState = ((MultiAgentState)solution.get(adjustedTime - 1))
                .getSingleAgentStates().get(0);
        Coordinate fromState = singleAgentState.coordinate();
        return new Coordinate(coordinate.getTimeStep() - 1, fromState.getNode());
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
        return NO_TIME_STEP;
    }

    @Override
    public boolean goalTest(ProblemInstance problemInstance) {
        findConflict();
        return conflict == null;
    }

    public void rePlan(ProblemInstance problemInstance, ConstrainedSolver solver) {
        populateConstraints();
        //System.out.println(constraints);
        Agent agent = problemInstance.getAgents().get(constraint.constrainedAgent());
        Agent constrained = new Agent(agent.position(), agent.goal(), 0);
        //System.out.println("Replanning for agent: " + constraint.constrainedAgent());
        ProblemInstance singleton = new ProblemInstance(problemInstance.getGraph(),
                                                        Collections.singletonList(constrained));
        solver.getReservation().clear();
        for (Coordinate coordinate : constraints.keySet()) {
            solver.getReservation().reserveCoordinate(coordinate, constraints.get(coordinate));
        }

        //System.out.println("reservations: " + solver.getReservation().reservedCoordinates.keySet());
        consistent = solver.solve(singleton);
        if (consistent) {
            solutions.set(constraint.constrainedAgent(), solver.getPath());
        }
        constraints.clear(); // save memory
    }

    public boolean isConsistent() {
        return consistent;
    }

    @Override
    public boolean belongsInClosedList() {
        return false;
    }

    public List<Path> getSolutions() {
        return solutions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CBSState cbsState = (CBSState) o;

        if (solutions != null ? !solutions.equals(cbsState.solutions) : cbsState.solutions != null) return false;
        return !(constraint != null ? !constraint.equals(cbsState.constraint) : cbsState.constraint != null);

    }

    @Override
    public int hashCode() {
        int result = solutions != null ? solutions.hashCode() : 0;
        result = 31 * result + (constraint != null ? constraint.hashCode() : 0);
        return result;
    }
    // a constraint: coordinate (edge or vertex) + agent constrained
    // set of solutions from planning for agents

    // root constructor - pass the start and goal locations of all agents
    //                  set constraints to null
    //                  lack of constraints is unique to root, also null back pointer
    //                  solve each single-agent problem optimally
    //                      when solving each single agent, tie-break to avoid previously-assigned agents
    //                  calculate and set cost
    // conflict field - constraint to add

    // copy constructor receives one new constraint
    // replan for constrained agent
    // update cost

    // goaltest: new constraint is null

    // calculateHeuristic always sets hValue to 0

    // setConflict function

}
