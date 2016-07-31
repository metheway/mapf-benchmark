package solvers;

public abstract class ConstrainedSolver implements Solver {

    private Reservation reservation;

    public ConstrainedSolver() {
        reservation = new Reservation();
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

}