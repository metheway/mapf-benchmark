package utilities;

/**
 * Class that represents a constraint on agents a_i and a_j
 * in a CBS solver
 */
public class CBSConstraint {

    private Coordinate coordinate;
    private Coordinate previous;
    private int constrainedAgent;

    /**
     * Constructor that creates a CBS constraint on agent constrainedAgent
     * at the location and time step specified by coordinate
     * @param constrainedAgent the constrained agent's id
     * @param coordinate the coordinate at which the agent is constrained
     * @param previous the previous coordinate
     */
    public CBSConstraint(int constrainedAgent, Coordinate coordinate, Coordinate previous) {
        this.constrainedAgent = constrainedAgent;
        this.coordinate = coordinate;
        this.previous = previous;
    }

    /**
     * Returns the id of the agent constrained by
     * this CBS constraint
     * @return the id of the constrained agent
     */
    public int constrainedAgent() {
        return constrainedAgent;
    }

    /**
     * Returns the coordinate at which the agent is constrained
     * @return the coordinate at which the agent is constrained
     */
    public Coordinate coordinate() {
        return coordinate;
    }

    /**
     * Returns the coordinate previously occupied by the constrained agent
     * @return the coordinate previously occupied by the constrained agent
     */
    public Coordinate previous() {
        return previous;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CBSConstraint that = (CBSConstraint) o;

        if (constrainedAgent != that.constrainedAgent) return false;
        return !(coordinate != null ? !coordinate.equals(that.coordinate) : that.coordinate != null);

    }

    @Override
    public int hashCode() {
        int result = coordinate != null ? coordinate.hashCode() : 0;
        result = 31 * result + constrainedAgent;
        return result;
    }

    @Override
    public String toString() {
        return "constrained: " + constrainedAgent + ", coordinate: " + coordinate + ", previous: " + previous;

    }

}
