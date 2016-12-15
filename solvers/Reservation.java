package solvers;

import utilities.Coordinate;
import utilities.Node;
import utilities.Path;

import java.util.List;
import java.util.Map;

public class Reservation extends ConflictAvoidanceTable {

    private static final int NO_GROUP = 1;

    public Reservation() {
        super();
    }

    public Reservation(Map<Coordinate, List<Coordinate>> coordinateTable,
                                  Map<Coordinate, List<Integer>> groupOccupantTable,
                                  Map<Node, int[]> agentDestinations,
                                  int lastTimeStep,
                                  Map<Integer, Integer> agentGroups) {
        super(coordinateTable, groupOccupantTable, agentDestinations, lastTimeStep, agentGroups);
    }

    public void reserveCoordinate(Coordinate coordinate, Coordinate previous) {
        addCoordinate(coordinate, previous, NO_GROUP);
        lastTimeStep = Math.max(lastTimeStep, coordinate.getTimeStep());
    }

    public void reserveDestination(Coordinate coordinate) {
        addDestination(coordinate, NO_GROUP);
    }

    public void reservePath(Path path) {
        super.addPath(path);
        lastTimeStep = Math.max(lastTimeStep, path.getLast().timeStep());
    }

    public int getLastTimeStep() {
        return lastTimeStep;
    }

    public void clear() {
        super.clear();
        lastTimeStep = 0;
    }

    public Reservation deepCopy() {
        ConflictAvoidanceTable baseCAT = super.deepCopy();
        return new Reservation( baseCAT.coordinateTable,
                                baseCAT.groupOccupantTable,
                                baseCAT.agentDestinations,
                                baseCAT.lastTimeStep,
                                baseCAT.agentGroups);
    }
}
