package solvers.states;

import constants.ConflictType;
import solvers.ConstrainedSolver;
import solvers.MultiLevelCAT;
import solvers.astar.State;
import solvers.astar.TDHeuristic;
import solvers.cbs.MACBS;
import utilities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MACBSNode extends State {

    private MACBS highLevel;
    private CBSConstraint constraint;
    private List<Path> solutions;
    private List<ProblemInstance> subProblems;
    private boolean consistent;
    private Map<Coordinate, Coordinate> constraints;
    private Conflict conflict;

    private List<List<Integer>> metaAgents;
    private int[][] conflictCounters;

    private static final ProblemInstance IRRELEVANT = null;


    // Root constructor
    public MACBSNode(ProblemInstance problemInstance, List<List<Integer>> metaAgents, int[][] conflictCounters,
                     MACBS highLevel, ConstrainedSolver lowLevel) {
        super(null);
        solutions = new ArrayList<>();
        this.metaAgents = metaAgents;
        this.conflictCounters = conflictCounters;
        this.subProblems = getSubProblems(problemInstance);
        this.solutions = getSolutions(lowLevel);
        this.highLevel = highLevel;
        populateCAT();
        conflict = highLevel.getConflictAvoidanceTable().getEarliestConflict();
        calculateCost(IRRELEVANT);
    }

    // Descendant constructor
    public MACBSNode(MACBSNode backPointer, ConstrainedSolver lowLevel) {
        super(backPointer);

        metaAgents = backPointer.metaAgents;
        conflictCounters = backPointer.conflictCounters;
        subProblems = backPointer.subProblems;

        solutions = new ArrayList<>(backPointer.solutions);
        highLevel = backPointer.highLevel;
        populateCAT();
        replan(lowLevel);
        calculateCost(IRRELEVANT);
    }

    private void populateCAT() {
        int index = constraint != null ? constraint.constrainedAgent() : -1;
        highLevel.getConflictAvoidanceTable().clear();
        for (int i = 0; i < metaAgents.size(); i++) {
            if (i != index) {
                Path path = solutions.get(i);
                highLevel.getConflictAvoidanceTable().addPath(path);
            }
        }
    }

    private List<Path> getSolutions(ConstrainedSolver lowLevel) {
        List<Path> result = new ArrayList<>();
        lowLevel.setConflictAvoidanceTable(highLevel.getConflictAvoidanceTable());
        lowLevel.setReservation(highLevel.getReservation());
        for (ProblemInstance problemInstance : subProblems) {
            lowLevel.solve(problemInstance);
            result.add(lowLevel.getPath());
        }
        return result;
    }

    private List<ProblemInstance> getSubProblems(ProblemInstance original) {
        List<ProblemInstance> result = new ArrayList<>();
        List<List<Agent>> agentLists = getAgentLists(original, metaAgents);
        agentLists.forEach(agents -> result.add(new ProblemInstance(original.getGraph(), agents,
                                                        original.getTrueDistanceHeuristic())));
        return result;
    }

    private List<List<Agent>> getAgentLists(ProblemInstance problemInstance, List<List<Integer>> indicesList) {
        List<List<Agent>> result = new ArrayList<>();
        for (List<Integer> indices : indicesList) {
            result.add(getMetaAgent(problemInstance, indices));
        }
        return result;
    }

    private List<Agent> getMetaAgent(ProblemInstance problemInstance, List<Integer> indices) {
        List<Agent> result = new ArrayList<>();
        int trueIndex = 0;
        for (int idx : indices) {
            Agent toAdd = problemInstance.getAgents().get(idx);
            result.add(new Agent(toAdd.position(), toAdd.goal(), trueIndex));
            trueIndex++;
        }
        return result;
    }

    public void replan(ConstrainedSolver lowLevel) {
        lowLevel.setConflictAvoidanceTable(highLevel.getConflictAvoidanceTable());
        lowLevel.setReservation(highLevel.getReservation());
        lowLevel.solve(subProblems.get(constraint.constrainedAgent()));
        conflict = lowLevel.getConflictAvoidanceTable().simulatePath(lowLevel.getPath(), constraint.constrainedAgent());
        incrementCounters(conflict.getGroup1(), conflict.getGroup2());
        solutions.set(constraint.constrainedAgent(), lowLevel.getPath());
    }

    public void incrementCounters(int i, int j) {
        conflictCounters[i][j]++;
        conflictCounters[j][i]++;
    }

    @Override
    protected void updateCATViolations(MultiLevelCAT cat) {
        throw new NoSuchMethodError("MACBS Nodes do not keep track of CAT violations.");
    }

    @Override
    public List<State> expand(ProblemInstance problem) {
        List<State> result = new ArrayList<>();

        Map<Integer, Integer> destToGroup = highLevel.getConflictAvoidanceTable().getAgentGroups();

        Coordinate conflictCoordinate1 = conflict.getConflictCoordinate(conflict.getGroup1());
        //Coordinate conflictPrevious1 = conflictPrevCoordinate(problem, conflict, conflict.getGroup1());
        int constrained1  = destToGroup.get(conflict.getGroup1());
        //CBSConstraint constraint1 = new CBSConstraint(constrained1, conflictCoordinate1, conflictPrevious1);

        Coordinate conflictCoordinate2 = conflict.getConflictCoordinate(conflict.getGroup2());
        //Coordinate confl

        return null;
    }

    private Coordinate conflictPrevCoordinate(ProblemInstance problemInstance, Conflict conflict, int group, int goal) {
        Path thisPath = solutions.get(group);
        int adjustedTime = Math.min(conflict.getTimeStep() - 1, thisPath.size() - 1);
        MultiAgentState multiAgentState = (MultiAgentState) thisPath.get(adjustedTime);
        int otherGroup = highLevel.getConflictAvoidanceTable().getAgentGroups().get(conflict.getGroup2());
        Path otherPath = solutions.get(otherGroup);
        assert otherGroup != group;

        if (conflict.getType() == ConflictType.COLLISION) {
            for (SingleAgentState singleAgentState : multiAgentState.getSingleAgentStates()) {
                if (singleAgentState.getAgentGoal() == conflict.getGroup1()) {
                    return new Coordinate(conflict.getTimeStep() - 1, singleAgentState.coordinate().getNode());
                }
            }
        }

        // get the coordinate the conflicting agent is at during the current time step
        if (conflict.getType() == ConflictType.TRANSPOSITION) {
            MultiAgentState conflicting = (MultiAgentState) otherPath.get(Math.min(conflict.getTimeStep(), otherPath.size() - 1));
            for (SingleAgentState singleAgentState : conflicting.getSingleAgentStates()) {
                if (singleAgentState.getAgentGoal() == conflict.getGroup2()) {
                    return new Coordinate(conflict.getTimeStep() - 1, singleAgentState.coordinate().getNode());
                }
            }
        }

        if (conflict.getType() == ConflictType.DESTINATION) {
            Node node = problemInstance.getGraph().getNodes().get(conflict.getGroup2());
            return new Coordinate(conflict.getTimeStep(), node);
        }

        return null;
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
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean goalTest(ProblemInstance problemInstance) {
        return conflict == null;
    }
}
