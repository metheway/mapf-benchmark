package solvers.astar;

import java.util.List;

import utilities.ProblemInstance;

public abstract class State implements Comparable<State> {

    // TODO

    protected double gValue;
    protected double hValue;
    private State backPointer;

    /**
     * Constructor.
     *
     * @param backPointer   This state's predecessor
     */
    public State(State backPointer) {
        this.backPointer = backPointer;
    }

    @Override
    /**
     * Compare states by their gValues and hValues.
     * 
     * @param other The State to be compared with
     */
    public int compareTo(State other) {
        if (other == null)
            throw new IllegalArgumentException("Cannot compare to null state.");

        double res = gValue - other.gValue + hValue - other.hValue;
    	return (int) Math.signum(res);
    }

    /**
     * Return the g-value of the state
     * @return The g-value of the state
     */
    public double gValue() {
        return gValue;
    }
    
    /**
     * Return the h-value of the state
     *
     * @return The h-value of the state
     */
    public double hValue() {
        return hValue;
    }

    /**
     * Return whether the state is the root state
     *
     * @return true if the state is root, false otherwise
     */
    public boolean isRoot() {
        return backPointer == null;
    }

    /**
     * Return the predecessor to this state
     *
     * @return the predecessor to this state
     */
    public State predecessor() {
        return backPointer;
    }

    /**
     * Return a list of legal states reachable from this state
     *
     * @return a list of legal states reachable from this state
     */
    public abstract List<State> expand(ProblemInstance problem);


    /**
     * Set the g-value of a state
     * @param problemInstance the problem instance the state is tied to
     */
    protected abstract void calculateCost(ProblemInstance problemInstance);


    /**
     * Set hValue based on a true-distance heuristic
     * 
     */
    protected abstract void setHeuristic(TDHeuristic heuristic);

    /**
     * Set h-value for the state directly
     * @param heuristic the h-value to set
     */
    public void setHeuristic(double heuristic) {
        hValue = heuristic;
    }

    /**
     * Returns the time-step of this state
     * @return the time-step of this state
     */
    public abstract int timeStep();

    /**
     * Override Object.equals(Object)
     */
    public abstract boolean equals(Object obj);
    
    /**
     * Override Object.hashCode()
     */
    public abstract int hashCode();

    /**
     * Returns whether the state should be added to the closed list in
     * an A* based solver
     * @return true if the state should be added to a closed list, false otherwise
     */
    public boolean belongsInClosedList() {
        return true;
    }

    /**
     * Output for debugging
     */
    public void printIndices() {

    }

    /**
     * Returns whether this state is the goal for a given problem instance
     * @param problemInstance the problem instance to test against
     * @return true if this state is the goal, false otherwise
     */
    public abstract boolean goalTest(ProblemInstance problemInstance);

    public String toString() { return "g: " + gValue + " h: " + hValue + " SUM: " + (gValue + hValue); }
}
