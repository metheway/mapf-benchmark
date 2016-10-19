package solvers;

import utilities.Coordinate;
import utilities.Path;

public class Reservation extends ConflictAvoidanceTable {

    private int lastTimeStep;
    private static final int NO_GROUP = -1;

    public void reserveCoordinate(Coordinate coordinate, Coordinate previous) {
        addCoordinate(coordinate, previous, NO_GROUP);
        lastTimeStep = Math.max(lastTimeStep, coordinate.getTimeStep());
    }

    public void reserveDestination(Coordinate coordinate) {
        addDestination(coordinate, NO_GROUP);
    }

    public void reservePath(Path path) {
        super.addPath(path, NO_GROUP);
        lastTimeStep = Math.max(lastTimeStep, path.getLast().timeStep());
    }

    public int getLastTimeStep() {
        return lastTimeStep;
    }

    public void clear() {
        super.clear();
        lastTimeStep = 0;
    }
}
