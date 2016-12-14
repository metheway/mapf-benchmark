package utilities;
import java.io.InputStream;
import java.net.Inet4Address;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * A graph representation of a map specified by a problem instance.
 * This class uses the adjacency list representation.
 */
public class Graph {
	
	/* node neighbors numbering:
	 * 0 - 3: right, left, top, bottom
	 * 4 - 7: top right, top left, bottom left, bottom right
	 * all numbering counterclockwise
	 */

	private List<Node> nodes;
	private ProblemMap map;
	private Connected connectedness;
	private List<Agent> agents;
	private String mapTitle;

	private Map<Integer, Integer> indexInMapToIndexInGraph;
	
	private static final Node OBSTACLE = null;
	
	// constructor given 4- or 8-connectedness and a file with the map inside

    /**
     * Constructor that creates a graph with a given connectedness from
     * a file containing a map.
     * @param c the connectedness of the graph
     * @param mapFile the map to generate the graph from
     * @throws FileNotFoundException
     */
	public Graph(Connected c, File mapFile) throws FileNotFoundException{
		connectedness = c;
		map = new ProblemMap(mapFile);
		nodes = generateGraph();
		indexInMapToIndexInGraph = generateIndexMap();
	}

	public Graph(Connected c, ProblemMap problemMap) {
		connectedness = c;
		map = problemMap;
		nodes = generateGraph();
		indexInMapToIndexInGraph = generateIndexMap();
	}

    /**
     * Constructor that creates a graph with the given connectedness and
     * a certain probability that any node will be an obstacle. The graph
     * is created from a random 32 x 32 map.
     * @param c the connectedness of the graph
     * @param obstacleProbability the probability of obstacles
     */
	public Graph(Connected c, double obstacleProbability) {
		Util.random = new Random();
		connectedness = c;
		map = new ProblemMap(obstacleProbability, 32, 32);
		nodes = generateGraph();
		indexInMapToIndexInGraph = generateIndexMap();
		agents = generateRandomAgents(5);
	}

	// read maps from Nathan Sturtevant's repository 
	private List<Node> generateGraph() {
		List<Node> rawGraphNodes = new ArrayList<Node>();
		String mapContent = map.getContent();
		
		// adds all characters in the map as nodes
		int index = 0;
		for (int i = 0; i < mapContent.length(); i++) {
			if (mapContent.charAt(i) == '@') rawGraphNodes.add(OBSTACLE);
			else {
				rawGraphNodes.add(new Node(mapContent.charAt(i), connectedness, i, index));
				index++;
			}
		}
		
		// adds the neighbors of each node from the graph
		int bufferedLineWidth = map.getWidth() + 2;
		for (int i = bufferedLineWidth; i < rawGraphNodes.size() - bufferedLineWidth; i++) {
			if (!isBorderNode(i,bufferedLineWidth) && rawGraphNodes.get(i) != OBSTACLE) {
				Node thisNode = rawGraphNodes.get(i);
				int[] neighborPositions = thisNode.getNeighborPositions(i, bufferedLineWidth);
				for (int j = 0; j < neighborPositions.length; j++) {
					thisNode.addNeighbor(rawGraphNodes.get(neighborPositions[j]), j);
				}
			}
		}
		
		// remove the obstacles from the graph
		List<Node> cleanGraph = new ArrayList<Node>();
		for (Node node : rawGraphNodes) {
			if (node != OBSTACLE) cleanGraph.add(node);
		}
		
		return cleanGraph;
	}

	private Map<Integer, Integer> generateIndexMap() {
		Map<Integer, Integer> resultMap = new HashMap<>();

		// for every node in the list of nodes
		for (Node node : nodes) {
			// create an entry in the map mapping from the nodes map index to it's graph index
			resultMap.put(node.getIndexInMap(), node.getIndexInGraph());
		}

		return resultMap;
	}
	
	public List<Agent> getAgents() {
		return agents;
	}
	
	public void setAgents(List<Agent> agents) {
		this.agents = agents;
	}
	
	// generates a random list of agents with unique start and goal points
	// arrays are String arrays so that we can ensure uniqueness of starts and goals
	public List<Agent> generateRandomAgents(int numAgents) {
		if (numAgents > nodes.size()) 
			throw new IllegalArgumentException("too many agents!");
		List<Agent> agentSet = new ArrayList<Agent>();
		List<Integer> starts = new ArrayList<Integer>();
		List<Integer> goals  = new ArrayList<Integer>();
		int graphSize = nodes.size();
		
		for (int i = 0; i < numAgents; i++) {
			int nextStart = Util.random.nextInt(graphSize);
			while (starts.contains(nextStart)) {
				nextStart = Util.random.nextInt(graphSize);
			}
			starts.add(nextStart);
			Node nextStartNode = nodes.get(nextStart);
			int nextGoal = randomWalk(nextStartNode, 10_000);
			while (goals.contains(nextGoal)) {
				nextGoal = randomWalk(nextStartNode, 10_000);
			}
			goals.add(nextGoal);
		}
		
		for (int i = 0; i < numAgents; i++) {
			int start = starts.get(i);
			int goal = goals.get(i);
			agentSet.add(new Agent(start, goal, i));
		}

		return agentSet;
	}
	
	// returns whether the node is at the border of the map
	private boolean isBorderNode(int iterator, int lineWidth) {
		return (iterator%lineWidth == 0 || (iterator + 1)%lineWidth == 0);
	}

    /**
     * Print the map to console for testing purposes
     */
	public void printMap() {
		map.prettyPrintMap();
	}

    /**
     * Accessor for the map this graph represents
     * @return the map this graph represents
     */
	public ProblemMap getMap() {
		return map;
	}

    /**
     * Returns the number of nodes in the graph
     * @return the number of nodes in the graph
     */
    public int getSize() {
		return nodes.size();
	}

    /**
     * Returns the nodes in the graph
     * @return the nodes in the graph
     */
	public List<Node> getNodes() {
		return nodes;
	}

	/**
	 * Accessor for the connectedness of the graph
	 * @return Returns the connectedness of the graph
     */
	public Connected getConnectedness() {
		return connectedness;
	}
    /**
     * Accessor for the connectedness number of the graph
     * @return the connectedness number of the graph
     */
	public int getConnectednessNumber() {
		return connNumber(connectedness);
	}

	/**
	 * Allows conversion from an index in the map to and index in the graph
	 * @return Returns the indexInGraph for the node with the given indexInMap, or -1 if the requested index does
	 * not exist
     */
	public int convertIndexInMapToGraph(int indexInMap) {
		Integer indexInGraph = indexInMapToIndexInGraph.get(indexInMap);
		if (indexInGraph == null) {
			return -1;
		} else {
			return indexInGraph;
		}
	}

    /**
     * Accessor for the title of the map associated
     * to this graph
     * @return the title of the map associated to this graph
     */
	public String getMapTitle() {
		return map.getMapTitle();
	}

	// random numSteps-length walk beginning at startNode
	private int randomWalk(Node startNode, int numSteps) {
		Node currentNode = startNode;
        if (hasNonNullNeighbor(currentNode)) {
            for (int i = 0; i < numSteps; i++) {
                int nextNode = Util.random.nextInt(connNumber(connectedness));
                Node successor = currentNode.getNeighbors()[nextNode];
                while (successor == OBSTACLE || !successor.isReachable(nextNode)) {
                    nextNode = Util.random.nextInt(connNumber(connectedness));
                    successor = currentNode.getNeighbors()[nextNode];
                }
                currentNode = successor;
            }
        }
		return nodes.indexOf(currentNode);
	}

    private boolean hasNonNullNeighbor(Node n) {
        for (int i = 0; i < connNumber(connectedness); i++)
            if (n.getNeighbors()[i] != null && n.getNeighbors()[i].isReachable(i)) return true;
        return false;
    }
	
	// returns the number associated with the connectedness of the graph
	private int connNumber(Connected c) {
		if (c == Connected.FOUR) return 4;
		else return 8;
	}
	
}
