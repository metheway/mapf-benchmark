package solvers;

import solvers.astar.TDHeuristic;

public abstract class ConstrainedSolver implements Solver {

    private Reservation reservation;
    private ConflictAvoidanceTable conflictAvoidanceTable;
    private ConstrainedSolver parentSolver;
    private int groupToSolve;

    private static final int NO_GROUP = -1;

    public ConstrainedSolver() {
        this(null, NO_GROUP);
    }

    public ConstrainedSolver(ConstrainedSolver parentSolver, int groupToSolve) {
        this.parentSolver = parentSolver;
        this.conflictAvoidanceTable = parentSolver == null ? new ConflictAvoidanceTable() :
                                                             parentSolver.getConflictAvoidanceTable().deepCopy();
        this.reservation = parentSolver == null ? new Reservation() :
                                                  parentSolver.getReservation().deepCopy();
        this.groupToSolve = groupToSolve;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public ConflictAvoidanceTable getConflictAvoidanceTable() {
        return conflictAvoidanceTable;
    }

    public ConstrainedSolver parentSolver() {
        return parentSolver;
    }

    public void setConflictAvoidanceTable(ConflictAvoidanceTable conflictAvoidanceTable) {
        this.conflictAvoidanceTable = conflictAvoidanceTable;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }
}