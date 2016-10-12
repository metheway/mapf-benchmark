package utilities;

public class EdgeCoordinate {

    private Node source;
    private Node destination;
    private int timeStep;

    public EdgeCoordinate(Node source, Node destination, int timeStep) {
        this.source = source;
        this.destination = destination;
        // The time step at the beginning of the edge
        // Ie. An agent is occupying source at timeStep and destination at timeStep + 1
        this.timeStep = timeStep;
    }

    public void reverse() {
        Node temp = source;
        source = destination;
        destination = temp;
    }

    public int getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(int timeStep) {
        this.timeStep = timeStep;
    }

    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EdgeCoordinate that = (EdgeCoordinate) o;

        if (timeStep != that.timeStep) return false;
        return !(destination != null ? !destination.equals(that.destination) : that.destination != null);

    }

    @Override
    public int hashCode() {
        int result = destination != null ? destination.hashCode() : 0;
        result = 31 * result + timeStep;
        return result;
    }

    @Override
    public String toString() {
        return "(t=" + getTimeStep() + ", src=" + getSource() + ", dest=" + getDestination() + ")";
    }
}
