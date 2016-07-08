package utilities;

/**
 * Represents a location and associated time-step
 */
public class Coordinate {
	
	private int timeStep;
	private Node node;

    /**
     * Constructor that creates a coordinate with the given time-step
     * and location (node)
     * @param timeStep the time step of the coordinate
     * @param node the location of the coordinate
     */
	public Coordinate(int timeStep, Node node) {
		this.timeStep = timeStep;
		this.node = node;
	}

    /**
     * Accessor for the time step of the coordinate
     * @return the time step of the coordinate
     */
	public int getTimeStep() {
		return timeStep;
	}

    /**
     * Mutator for the time step of the coordinate
     * @param timeStep the updated time step of the coordinate
     */
	public void setTimeStep(int timeStep) {
        this.timeStep = timeStep;
    }

    /**
     * Accessor for the location of the coordinate
     * @return the location of the coordinate
     */
    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "(t=" + timeStep + " n=" + node.getIndexInGraph() +")";
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result + timeStep;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coordinate other = (Coordinate) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (timeStep != other.timeStep)
			return false;
		return true;
	}

}
