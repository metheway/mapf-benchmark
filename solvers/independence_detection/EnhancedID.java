package solvers.independence_detection;

import solvers.ConstrainedSolver;
import utilities.Path;
import utilities.ProblemInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that implements Trevor Standley's Enhanced Independence Detection
 * algorithm with any constraint-capable A*-based solver
 */
public class EnhancedID extends IndependenceDetection {

    private List<List<Boolean>> conflictedInPast;

    public EnhancedID(ConstrainedSolver solver) {
        super(solver);
    }

    @Override
    protected boolean resolveConflict(int index, int indexOfConflict) {
        // check whether the problems have conflicted before
        // if not, plan for p1 with p2's path reserved
        // solved?
        // if not, free previous reservation
        //         plan for p2 with p1's path reserved
        // solved?
        // if not, remove all reservations
        //         merge problems, solve together
        if (conflictedInPast.get(index).get(indexOfConflict)) {
            updateConflictRecord(index, indexOfConflict);
            System.out.println("updated conflict record.");
            return super.resolveConflict(index, indexOfConflict);
        } else {
            conflictedInPast.get(index).set(indexOfConflict, true);
            conflictedInPast.get(indexOfConflict).set(index, true);

            // reserve first path, try to solve second
            ProblemInstance current = problems().get(index);
            ProblemInstance conflict = problems().get(indexOfConflict);
            boolean finished = false;
            double costLimit = paths().get(indexOfConflict).cost();
            solver().getReservation().reservePath(paths().get(index));
            if (solver().solve(conflict)) {
                Path newPath = solver().getPath();
                if (newPath.cost() == costLimit) {
                    paths().set(indexOfConflict, newPath);
                    System.out.println("new optimal path found");
                    System.out.println(newPath);
                    finished = true;
                }
            }

            solver().getReservation().clear();

            // reserve second path, try to solve first
            if (!finished) {
                System.out.println("first pass failed");
                solver().getReservation().reservePath(paths().get(indexOfConflict));
                costLimit = paths().get(index).cost();
                if (solver().solve(current)) {
                    Path newPath = solver().getPath();
                    if (newPath.cost() == costLimit) {
                        paths().set(index, newPath);
                        finished = true;
                    }
                }
            }

            solver().getReservation().clear();

            if (!finished) {
                System.out.println("second pass failed... merging problems.");
                return resolveConflict(index, indexOfConflict);
            } else {
                return true;
            }
        }
    }

    private void updateConflictRecord(int index, int indexOfConflict) {
        // replace first group with union of the two
        // e.g., replace the list at index with a list of only false
        conflictedInPast.set(index, new ArrayList<>());
        paths().forEach(p -> conflictedInPast.get(index).add(false));

        // remove the conflicting group from the matrix
        conflictedInPast.forEach(conflictedList -> conflictedList.remove(indexOfConflict));

        // update list of problems
        //handleProblemMerge(index, indexOfConflict); // may be unnecessary; keep an open mind
    }

    @Override
    protected void init() {
        super.init();
        conflictedInPast = new ArrayList<>();
    }

    @Override
    protected boolean populatePaths(ProblemInstance problemInstance) {
        boolean populated = super.populatePaths(problemInstance);
        if (populated) { // initialize conflict record
            List<Boolean> defaultValues = new ArrayList<>();
            paths().forEach(p -> defaultValues.add(false));
            paths().forEach(p -> conflictedInPast.add(new ArrayList<>(defaultValues)));
        }
        return populated;
    }
}
