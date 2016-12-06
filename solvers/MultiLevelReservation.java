package solvers;

import solvers.astar.State;
import solvers.states.SingleAgentState;
import utilities.Conflict;
import utilities.Coordinate;
import utilities.Node;
import utilities.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MultiLevelReservation {

    private List<Reservation> reservationList;

    public MultiLevelReservation() {
        reservationList = new ArrayList<>();
    }

    public void addLevel() {
        reservationList.add(new Reservation());
    }

    public void removeLevel() {
        reservationList.remove(reservationList.size() - 1);
    }

    /**
     * Return the first violation found in any level
     * @param state state to check
     * @return the first violation found
     */
    public int violation(SingleAgentState state) {
        for (ConflictAvoidanceTable cat : reservationList) {
            int violation = cat.violation(state);
            if (violation != ConflictAvoidanceTable.NO_CONFLICT) return violation;
        }
        return ConflictAvoidanceTable.NO_CONFLICT;
    }


    public void reserveCoordinate(Coordinate coordinate, Coordinate previous) {
        reservationList.get(reservationList.size() - 1).reserveCoordinate(coordinate, previous);
    }

    public void reserveDestination(Coordinate coordinate) {
        reservationList.get(reservationList.size() - 1).reserveDestination(coordinate);
    }

    public void reservePath(Path path) {
        reservationList.get(reservationList.size() - 1).addPath(path);
    }

    public int getLastTimeStep() {
        return reservationList.get(reservationList.size() - 1).getLastTimeStep();
    }

    public void addPath(Path path) {
        reservationList.get(reservationList.size() - 1).addPath(path);
    }

    public Conflict simulatePath(Path path, int group) {
        return reservationList.get(reservationList.size() - 1).simulatePath(path, group);
    }

    public boolean isValid(State state) {
        return reservationList.get(reservationList.size() - 1).isValid(state);
    }

    public Map<Node, int[]> getAgentDestinations() {
        return reservationList.get(reservationList.size() - 1).getAgentDestinations();
    }

    public Map<Coordinate, List<Integer>> getGroupOccupantTable() {
        return reservationList.get(reservationList.size() - 1).getGroupOccupantTable();
    }

    public Map<Coordinate, List<Coordinate>> getCoordinateTable() {
        return reservationList.get(reservationList.size() - 1).getCoordinateTable();
    }

    public void setRelevantGroups(List<Integer> relevantGroups) {
        reservationList.get(reservationList.size() - 1).setRelevantGroups(relevantGroups);
    }

    public void setAgentGroups(Map<Integer, Integer> agentGroups) {
        reservationList.get(reservationList.size() - 1).setAgentGroups(agentGroups);
    }

    public Map<Integer, Integer> getAgentGroups() {
        return reservationList.get(reservationList.size() - 1).getAgentGroups();
    }

    public List<Integer> getRelevantGroups() {
        return reservationList.get(reservationList.size() - 1).getRelevantGroups();
    }

    public void clear() {
        reservationList.get(reservationList.size() - 1).clear();
    }

}
