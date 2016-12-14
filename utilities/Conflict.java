package utilities;

/**
 * Wrapper for conflict information
 * used in some solvers
 */
public class Conflict {

    private int timeStep;
    private int group1;
    private int group2;
    private Node group1Node;
    private Node group2Node;

    /**
     * Constructor that creates a conflict object
     * with the given fields
     * @param timeStep the time step of the conflict
     * @param group1 the index of the first group of agents
     * @param group2 the index of the second group of agents
     */
    public Conflict(int timeStep, int group1, int group2) {
        this(timeStep, group1, group2, null, null);
    }

    public Conflict(int timeStep, int group1, int group2, Node group1Node, Node group2Node) {
        this.timeStep = timeStep;
        this.group1 = group1;
        this.group2 = group2;
        this.group1Node = group1Node;
        this.group2Node = group2Node;
    }

    /**
     * Returns the time step of the conflict
     * @return the time step of the conflict
     */
    public int getTimeStep() {
        return timeStep;
    }

    /**
     * Returns the index of the first group
     * @return the index of the first group
     */
    public int getGroup1() {
        return group1;
    }

    /**
     * Returns the index of the second group
     * @return the index of the second group
     */
    public int getGroup2() {
        return group2;
    }

    /**
     * Returns the node that group occupies to cause conflict
     * @return the node the group occupies to cause the conflict
     */
    public Node getGroupNode(int group) {
        return group == group1 ? group1Node : group2Node;
    }

    /**
     * Return the coordinate that the conflict happened at with respect to group
     * @param group
     * @return the coordinate that the conflict happened at with respect to group
     */
    public Coordinate getConflictCoordinate(int group) {
        return new Coordinate(getTimeStep(), getGroupNode(group));
    }

    @Override
    public String toString() {
        return "g1: " + getGroup1() + ", g2: " + getGroup2() + ", t: " + getTimeStep();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Conflict conflict = (Conflict) o;

        if (timeStep != conflict.timeStep) return false;
        if (group1 != conflict.group1) return false;
        return group2 == conflict.group2;

    }

    @Override
    public int hashCode() {
        int result = timeStep;
        result = 31 * result + group1;
        result = 31 * result + group2;
        return result;
    }
}
