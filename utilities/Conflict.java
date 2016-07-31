package utilities;

/**
 * Wrapper for conflict information
 * used in some solvers
 */
public class Conflict {

    private int timeStep;
    private int group1;
    private int group2;

    /**
     * Constructor that creates a conflict object
     * with the given fields
     * @param timeStep the time step of the conflict
     * @param group1 the index of the first group of agents
     * @param group2 the index of the second group of agents
     */
    public Conflict(int timeStep, int group1, int group2) {
        this.timeStep = timeStep;
        this.group1 = group1;
        this.group2 = group2;
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
