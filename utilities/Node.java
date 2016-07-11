package utilities;

import constants.Positions;

/**
 * Class that represents a vertex in a graph
 */
public class Node {
	
	private char type;
	private Connected connectedness;
	private Node[] neighbors;
	private int indexInMap;
	private int indexInGraph;

	/**
	 * Constructor that creates a node with the given fields
	 * @param nodeType type of this node
	 * @param c connectedness of the graph
	 * @param indexInMap index in the map
	 * @param indexInGraph index in the graph
	 */
	public Node(char nodeType, Connected c, int indexInMap, int indexInGraph) {
		type = nodeType;
		connectedness = c;
		neighbors = new Node[connNumber(c)];
		this.indexInMap = indexInMap;
		this.indexInGraph = indexInGraph;
	}

    /**
     * Returns an array of nodes that
     * are neighbors with this node
     * @return this node's neighbors
     */
	public Node[] getNeighbors() {
		return neighbors;
	}

    /**
     * Returns the index of this node in the map
     * @return the index of this node in the map
     */
	public int getIndexInMap() {
		return indexInMap;
	}

    /**
     * Returns the index of this node in the graph
     * @return the index of this node in the graph
     */
	public int getIndexInGraph() {
		return indexInGraph;
	}

    /**
     * Returns the type of this node
     * @return the type of this node
     */
	public char getType() {
		return type;
	}

    /**
     * Sets the neighbor at position to the new neighbor
     * @param newNeighbor the new neighbor node
     * @param position the position to add
     */
	public void addNeighbor(Node newNeighbor, int position) {
		neighbors[position] = newNeighbor;
	}

    /**
     * Returns whether a neighbor is reachable
     * @param position relative position of neighbor
     * @return true if the neighbor is reachable, false otherwise
     */
	public boolean isReachable(int position) {
		if (connNumber(connectedness) == 4) return true;
		if (position == Positions.TOP_RIGHT) {
			if (neighbors[Positions.LEFT] == null || neighbors[Positions.BOTTOM] == null) return false;
		} else if (position == Positions.TOP_LEFT) {
			if (neighbors[Positions.RIGHT] == null || neighbors[Positions.BOTTOM] == null) return false;
		} else if (position == Positions.BOTTOM_RIGHT) {
			if (neighbors[Positions.TOP] == null || neighbors[Positions.LEFT] == null) return false;
		} else if (position == Positions.BOTTOM_LEFT) {
			if (neighbors[Positions.TOP] == null || neighbors[Positions.RIGHT] == null) return false;
		}
		return true;
	}

    /**
     * Convenience method for getting neighbor positions in the map
     * @param iterator value of loop control variable
     * @param lineWidth width of a line in the buffered map
     * @return the neighbor positions
     */
    public int[] getNeighborPositions(int iterator, int lineWidth) {
		if (connNumber(connectedness) == 4) {  // four-connected
			return new int[] {
					iterator + 1,         // right
					iterator - 1,		  // left
					iterator - lineWidth, // top
					iterator + lineWidth  // bottom
			};
		} else { // 8 - connected
			return new int[] {
					iterator + 1,			  // right
					iterator - 1,			  // left
					iterator - lineWidth,	  // top
					iterator + lineWidth,	  // bottom
					iterator - lineWidth + 1, // top right
					iterator - lineWidth - 1, // top left
					iterator + lineWidth + 1, // bottom right
					iterator + lineWidth - 1  // bottom left
			};
		}
	}

	private int connNumber(Connected c) {
		if (c == Connected.FOUR) return 4;
		else return 8;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((connectedness == null) ? 0 : connectedness.hashCode());
		result = prime * result + indexInMap;
		result = prime * result + type;
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
		Node other = (Node) obj;
		if (connectedness != other.connectedness)
			return false;
		if (indexInMap != other.indexInMap)
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}