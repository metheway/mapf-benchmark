package utilities;
// TODO obstacle neighbors should be null or all point to the same object
// neighbor costs
// tree costs 1.5
// separate costs from time steps
// class for static variables (cost, directions)
// serializable class for reading in the agent goals

import constants.Positions;

// a node on the graph of the map
public class Node {
	
	private char type;
	private Connected connectedness;
	private Node[] neighbors;
	private int indexInMap;
	private int indexInGraph;
		
	// creates a node with empty neighbors (added in later) of type nodeType
	public Node(char nodeType, Connected c, int indexInMap, int indexInGraph) {
		type = nodeType;
		connectedness = c;
		neighbors = new Node[connNumber(c)];
		this.indexInMap = indexInMap;
		this.indexInGraph = indexInGraph;
	}
	
	// returns an array of references to the nodes connected to this one
	public Node[] getNeighbors() { 
		return neighbors;
	}
	
	public int getIndexInMap() {
		return indexInMap;
	}

	public int getIndexInGraph() {
		return indexInGraph;
	}
	
	// returns the type of the node
	public char getType() {
		return type;
	}
	
	// adds a neighbor at the specified position (documented at top)
	public void addNeighbor(Node newNeighbor, int position) {
		neighbors[position] = newNeighbor;
	}
	
	// returns whether the node can be reached by a neighbor in the 8-connected case
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
	
	// returns an array of the positions of the node's neighbors
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