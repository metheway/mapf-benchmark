package solvers;

import solvers.astar.TDHeuristic;

public abstract class ConstrainedSolver implements Solver {

    private MultiLevelReservation reservation;
    private MultiLevelCAT conflictAvoidanceTable;
    private ConstrainedSolver parentSolver;
    private int groupToSolve;

    private static final int NO_GROUP = -1;

    public ConstrainedSolver() {
        this(null, NO_GROUP);
    }

    public ConstrainedSolver(ConstrainedSolver parentSolver, int groupToSolve) {
        this.parentSolver = parentSolver;
        this.conflictAvoidanceTable = parentSolver == null ? new MultiLevelCAT() : parentSolver.getConflictAvoidanceTable();
        this.reservation = parentSolver == null ? new MultiLevelReservation() :
                                                  parentSolver.getReservation();
        this.groupToSolve = groupToSolve;
    }

    public MultiLevelReservation getReservation() {
        return reservation;
    }

    public MultiLevelCAT getConflictAvoidanceTable() {
        return conflictAvoidanceTable;
    }

    public ConstrainedSolver parentSolver() {
        return parentSolver;
    }

    public void setConflictAvoidanceTable(MultiLevelCAT conflictAvoidanceTable) {
        this.conflictAvoidanceTable = conflictAvoidanceTable;
    }

    public void setReservation(MultiLevelReservation reservation) {
        this.reservation = reservation;
    }
}