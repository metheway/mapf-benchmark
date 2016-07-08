package solvers;

import solvers.astar.State;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;
import utilities.Coordinate;
import utilities.Path;

import java.util.HashSet;
import java.util.Set;

public abstract class ConstrainedSolver implements Solver {
    //TODO change CA* to follow this terminology
    private Reservation reservation;

    public ConstrainedSolver() {
        reservation = new Reservation();
    }

    // TODO put this in Util
    // state is valid iff:
    //      state is not reserved
    //      state does not cause transposition

    public Reservation getReservation() {
        return reservation;
    }
}