package solvers;

public abstract class ConstrainedSolver implements Solver {

    private Reservation reservation;
    private ConflictAvoidanceTable conflictAvoidanceTable;

    public ConstrainedSolver() {
        reservation = new Reservation();
        conflictAvoidanceTable = new ConflictAvoidanceTable();
    }

    public Reservation getReservation() {
        return reservation;
    }

    public ConflictAvoidanceTable getConflictAvoidanceTable() {
        return conflictAvoidanceTable;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void setConflictAvoidanceTable(ConflictAvoidanceTable conflictAvoidanceTable) {
        this.conflictAvoidanceTable = conflictAvoidanceTable;
    }
}