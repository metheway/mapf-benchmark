package solvers.sat;

import org.sat4j.core.VecInt;
import utilities.*;
import utilities.Node;

import java.util.*;

/**
 * This class represents a time expansion graph of a problem instance.
 * A time expansion graph is a directed acyclic graph that represents all
 * possible moves within a problem instance. The set of original vertices
 * is duplicated for all time steps within the given makespan, then possible
 * moves are represented as edges between successive time steps.
 */
public class TimeExpansionGraph {
    // TODO: set up this class so that you can add a function to increase the makespan by 1
    // List of all coordinates in each timestep, outer list is timestep, inner is coordinates
    private List<List<Coordinate>> coordinates;
    // List of all possible moves in problem
    private List<List<EdgeCoordinate>> edges;
    // List of initial nodes passed to constructor
    private List<Node> initialNodes;
    // The makespan used to create this time expansion graph
    private int makespan;
    // Map used to store mapping of objects to propositional variables when creating the CNF encoding
    private Map<Object, Integer> propVarMapping;

    // The number of vertex propositional variables, used to offset edge and agent propositional variables
    private int vertexOffset;
    // The number of edge propositional variables, used to offset agent propositional variables
    private int edgeOffset;
    // The number of elements in the boolean vectors used to represent agent propositional variables
    private int numBinaryPropVars;

    /**
     * Constructor that takes no makespan. Calls other constructor with a makespan of zero.
     * @param nodes The list of nodes to be included in the time expansion graph
     */
    public TimeExpansionGraph(List<Node> nodes) {
        this(nodes, 0);
    }

    /**
     * Constructor. Populates coordinates and edges according to
     * the list of initialNodes and the makespan
     * @param nodes The list of initial nodes to be included in the time expansion graph
     * @param makespan The number of timesteps to expand to
     */
    public TimeExpansionGraph(List<Node> nodes, int makespan) {
        this.coordinates = new ArrayList<List<Coordinate>>();
        this.edges = new ArrayList<List<EdgeCoordinate>>();
        this.initialNodes = nodes;
        this.makespan = 0;
        this.propVarMapping = new HashMap<Object, Integer>();
        increaseMakespan(makespan);
    }

    /**
     * Returns the makespan of this time expansion graph
     * @return the makespan of this time expansion graph
     */
    public int getMakespan() {
        return makespan;
    }

    /**
     * Returns the number of coordinates in this time expansion graph
     * @return the number of coordinates in this time expansion graph
     */
    public int getNumCoordinates() {
        int numCoordinates = 0;
        for (List<Coordinate> coordinatesInTimeStep : coordinates)
            numCoordinates += coordinatesInTimeStep.size();
        return numCoordinates;
    }
    /**
     * Returns the number of edges in this time expansion graph
     * @return the number of edges in this time expansion graph
     */
    public int getNumEdges() {
        int numEdges = 0;
        for (List<EdgeCoordinate> edgesInTimeStep : edges)
            numEdges += edgesInTimeStep.size();
        return numEdges;
    }

    /**
     * Expands the list of initialNodes to fill up value timesteps. Only adds nodes that don't yet exist yet
     * @param value The number of timestep to expand into
     */
    private void expandCoordinates(int value) {
        for (int timeStep = makespan; timeStep < makespan + value; timeStep++) {
            List<Coordinate> coordinatesInTimestep = new ArrayList<>(initialNodes.size());
            for (Node node : initialNodes) {
                coordinatesInTimestep.add(new Coordinate(timeStep, node));
            }
            coordinates.add(coordinatesInTimestep);
        }
    }

    /**
     * Generates a list of possible edges(moves) between coordinates and their neighbors in successive time steps
     * @param coordinates The list of coordinates to generate edges for
     */
    private void addEdges(List<List<Coordinate>> coordinates) {
        for (List<Coordinate> coordinatesInTimeStep : coordinates) {
            List<EdgeCoordinate> newEdgesInTimeStep = new ArrayList<EdgeCoordinate>();
            for (Coordinate coordinate : coordinatesInTimeStep) {
                Node prevNode = coordinate.getNode();
                EdgeCoordinate waitEdge = new EdgeCoordinate(prevNode, prevNode,
                        coordinate.getTimeStep());
                newEdgesInTimeStep.add(waitEdge);
                for (int nextNodeIndex = 0; nextNodeIndex < prevNode.getNeighbors().length; nextNodeIndex++) {
                    Node nextNode = coordinate.getNode().getNeighbors()[nextNodeIndex];
                    // If coordinate.getNode() is on the edge of the map, then it's neighbor will be null
                    if (nextNode != null) {
                        EdgeCoordinate moveEdge = new EdgeCoordinate(prevNode, nextNode,
                                coordinate.getTimeStep());
                        newEdgesInTimeStep.add(moveEdge);
                    }
                }
            }
            edges.add(newEdgesInTimeStep);
        }
    }

    /**
     * Public facing method for increasing the makespan of this Time Expansion Graph
     * @return Returns the new makespan
     */
    public int increaseMakespan() {
        return this.increaseMakespan(1);
    }

    /**
     * Increases the makespan by the given value. Creates more coordinates and adds the necessary edges
     * @param value The value to increase the makespan by
     * @return Returns the new makespan
     */
    private int increaseMakespan(int value) {
        // Do not allow negative values or zero, only increase the makespan.
        if (value <= 0) {
            return makespan;
        }
        // Expand the coordinates
        expandCoordinates(value);
        // Add edges for new coordinates
        addEdges(coordinates.subList(makespan, makespan + value));
        return makespan += value;
    }

    /**
     * Returns a list of VecInt that represents the CNF encoding of the time expansion graph and the provided agents
     * @param agents A list of agents to use when generating the encoding
     * @return Returns a list of VecInt that represents the CNF encoding of the time expansion graph and passed agents
     * Each VectInt object represents a clause. The first VecInt object is special in that it contains meta data:
     * the total number of clauses and the total number propositional variables, which are required by Sat4j
     */
    public List<VecInt> getCnfEncoding(List<Agent> agents) {
        /*
        Use a matching encoding scheme as described in:
        "Compact Representations of Cooperative Path-Finding as SAT
            Based on Matchings in Bipartite Graphs", Pavel Surynek

        List of things to input:
        Induced Submodel:
        4)
        (~E(u,v,i) or M(u,i)) and (~E(u,v,i) or M(v,i+1))

        5)
        (~E(u,i) or M(u,i)) and (~E(u,i) or M(u,i+1))

        6)
        (~E(u,v,i) or ~(E(u,w,i)) where E(u,v) and E(u,w) are valid edges and v != w
        (~E(u,v,i) or ~E(u,i)) where E(u,v) is a valid edge

        7)
        // TODO: I think this is true
        (contained in encoding for 6)

        8)
        (~M(u,i) or E(u,i)) and (~M(u,i) or E(u,v,i))

        9)
        // TODO: clarify this equation, possible error in paper
        (~M(v,i+1) or E(v,i)) and (~M(v,i+1) or E(u,v,i))

        10)
        (~E(u,v,i) or ~M(v,i))

        Mapping Submodel:
        11)
        (~E(u,v,i) or ~A(u,i)[0] or A(v, i+1)[0]) and
        (~E(u,v,i) or A(u,i)[0] or ~A(v, i+1)[0]) and
        (~E(u,v,i) or ~A(u,i)[1] or A(v, i+1)[1]) and
        (~E(u,v,i) or A(u,i)[1] or ~A(v, i+1)[1]) and
        ...
        (~E(u,v,i) or ~A(u,i)[log2(|A|+1) or A(v, i+1)[log2(|A|+1]) and
        (~E(u,v,i) or A(u,i)[log2(|A|+1]] or ~A(v, i+1)[log2(|A|+1]])

        12)
        (~A(u,i)[0] or M(u,i)) and
        (~A(u,i)[1] or M(u,i)) and
        ...
        (~A(u,i)[log2(|A|+1]] or M(u,i))

        13)
        Encoding initial states
        if agent a(j) starts in node n(i), then
        A(v(i),0)[x] == (j[x] and M(v(i), 0))
        14)
        Encoding goal states

        Add constraints for agents numbered higher than the actual number of agents
         */

        // TODO: Remove
        // Update the mapping of objects to propositional variable indices
//        updatePropVarMapping(agents);

        CnfEncoder encoder = new CnfEncoder(coordinates, edges, agents, makespan);
        this.numBinaryPropVars = encoder.getNumberOfBinaryVariablesPerAgent();
        /*
        System.out.print("Vertices: ");
        for (List<Coordinate> coordinatesTimestep : coordinates)
            for (Coordinate coordinate : coordinatesTimestep)
                System.out.print(encoder.getEncoding(coordinate) + " ");
        System.out.println();
        System.out.print("Edges: ");
        for (List<EdgeCoordinate> edgesTime : edges)
            for (EdgeCoordinate edge : edgesTime)
                System.out.print(encoder.getEncoding(edge) + " ");
        System.out.println();
        System.out.print("Agents: ");
        for (Node node : initialNodes)
            for (int timestep = 0; timestep < makespan; timestep++)
                for (int binaryIndex = 0; binaryIndex < encoder.getNumberOfBinaryVariablesPerAgent(); binaryIndex++) {
                    System.out.print(encoder.getEncoding(node, timestep, binaryIndex) + " ");
                }
        System.out.println();
        System.out.println(encoder.getNumberOfBinaryVariablesPerAgent());
        */

        List<VecInt> results = new ArrayList<VecInt>();

        // Declare the initial special clauseTwoVars for meta data
        int[] metaDataClause = new int[2];

        // number of unique encodings our encoder generated
        metaDataClause[0] = encoder.getNumberOfVariables();

        // number of clauses in induced submodel
        metaDataClause[1] = (getMakespan() * (3 * initialNodes.size() + 2 * edges.get(0).size())) +
                (getMakespan() *
                        (initialNodes.get(0).getNeighbors().length *
                        (initialNodes.get(0).getNeighbors().length + 1)));
        // add number of clauses in mapping submodel
        metaDataClause[1] += getMakespan() * ((2 * initialNodes.size()) + edges.get(0).size()) *
                Math.ceil(Math.log(agents.size() + 1));
        // Add to result
        results.add(new VecInt(metaDataClause));

//        updateOffsetsAndNumBinaryPropVars(agents.size());

        // INDUCED SUBMODEL:
        // Equation 4 and 5:
        // (~E(u,v,i) or M(u,i)) and (~E(u,v,i) or M(v,i+1))
        // (~E(u,i) or M(u,i)) and (~E(u,i) or M(u,i+1))
        for (int timeStep = 0; timeStep < edges.size()-1; timeStep++) {
            List<EdgeCoordinate> edgesInTimeStep = edges.get(timeStep);
            for (int edgeIndex = 0; edgeIndex < edgesInTimeStep.size(); edgeIndex++) {
                int[] clauseOne = new int[2];
                EdgeCoordinate thisEdgeCoordinate = edgesInTimeStep.get(edgeIndex);
                // ~E(u,v,i)
                clauseOne[0] = -1 * encoder.getEncoding(thisEdgeCoordinate);
                        //getEdgeEncoding(thisEdgeCoordinate, edgeIndex);
                // M(u,i)
                clauseOne[1] = encoder.getEncoding(thisEdgeCoordinate.getSource(), thisEdgeCoordinate.getTimeStep());
                        //getVertexEncoding(thisEdgeCoordinate.getSource(), thisEdgeCoordinate.getTimeStep());
                results.add(new VecInt(clauseOne));
                int[] clauseTwo = new int[2];
                clauseTwo[0] = clauseOne[0];
                // M(v,i+1)
                clauseTwo[1] =
                        encoder.getEncoding(thisEdgeCoordinate.getDestination(), thisEdgeCoordinate.getTimeStep() + 1);
                        //getVertexEncoding(thisEdgeCoordinate.getDestination(), thisEdgeCoordinate.getTimeStep() + 1);
                results.add(new VecInt(clauseTwo));
            }
        }

        // Equation 6 and 7:
        // (~E(u,v,i) or ~(E(u,w,i)) where E(u,v) and E(u,w) are valid edges and v != w
        // (~E(u,v,i) or ~E(u,i)) where E(u,v) is a valid edge
        List<EdgeCoordinate> edgesInFirstTimeStep = edges.get(0);
        for (int edgeIndex = 0; edgeIndex < edgesInFirstTimeStep.size(); edgeIndex++) {
            EdgeCoordinate thisEdgeCoordinate = edgesInFirstTimeStep.get(edgeIndex);
            // Find all edges with same source node and different destinations as this edge
            // If this edge is wait edge (source == destination), find all edges with same source
            for (int otherEdgeIndex = 0; otherEdgeIndex < edgesInFirstTimeStep.size(); otherEdgeIndex++) {
                EdgeCoordinate otherEdgeCoordinate = edgesInFirstTimeStep.get(otherEdgeIndex);
                // TODO: this logic may be incorrect
                if (otherEdgeCoordinate.getSource() == thisEdgeCoordinate.getSource() &&
                        (otherEdgeCoordinate.getDestination() != thisEdgeCoordinate.getDestination() ||
                        thisEdgeCoordinate.getSource() == thisEdgeCoordinate.getDestination())) {
                    // Add a clause for this pair of edges for each timestep
                    for (int timeStep = 0; timeStep < edges.size(); timeStep++) {
                        int[] clause = new int[2];
                        // ~E(u,v,i)
                        clause[0] = -1 * encoder.getEncoding(edges.get(timeStep).get(edgeIndex));
                                //getEdgeEncoding(timeStep, edgeIndex);
                        // ~E(u,w,i)
                        clause[1] = -1 * encoder.getEncoding(edges.get(timeStep).get(otherEdgeIndex));
                                //getEdgeEncoding(timeStep, otherEdgeIndex);
                        results.add(new VecInt(clause));
                    }
                }
            }
        }

        // Equation 8:
        // (~M(u,i) or E(u,i)) and (~M(u,i) or E(u,v,i))
        for (int timeStep = 0; timeStep < coordinates.size(); timeStep++) {
            List<Coordinate> coordinatesInTimeStep = coordinates.get(timeStep);
            for (int coordinateIndex = 0; coordinateIndex < coordinatesInTimeStep.size(); coordinateIndex++) {
                Coordinate thisCoordinate = coordinatesInTimeStep.get(coordinateIndex);
                // ~M(u,i)
                int propVar1 = -1 * encoder.getEncoding(thisCoordinate);
                        //getVertexEncoding(thisCoordinate);
                // Find all edges that have same timestep and source of thisCoordinate.getNode()
                List<EdgeCoordinate> edgesInSameTimeStep = edges.get(timeStep);
                for (int edgeIndex = 0; edgeIndex < edgesInFirstTimeStep.size(); edgeIndex++) {
                    EdgeCoordinate thisEdgeCoordinate = edgesInFirstTimeStep.get(edgeIndex);
                    if (thisEdgeCoordinate.getSource() == thisCoordinate.getNode()) {
                        int[] clause = new int[2];
                        clause[0] = propVar1;
                        // E(u,i) or E(u,v,i)
                        clause[1] = encoder.getEncoding(thisEdgeCoordinate);
                                //getEdgeEncoding(thisEdgeCoordinate, edgeIndex);
                        results.add(new VecInt(clause));
                    }
                }
            }
        }

        // Equation 9:
        // (~M(v,i+1) or E(v,i)) and (~M(v,i+1) or E(u,v,i))
        for (int timeStep = 1; timeStep < coordinates.size(); timeStep++) {
            List<Coordinate> coordinatesInTimeStep = coordinates.get(timeStep);
            for (int coordinateIndex = 0; coordinateIndex < coordinatesInTimeStep.size(); coordinateIndex++) {
                Coordinate thisCoordinate = coordinatesInTimeStep.get(coordinateIndex);
                // ~M(v, i+1). Consider timestep of coordinate as k = i+1
                int propVar1 = -1 * encoder.getEncoding(thisCoordinate);
                        //getVertexEncoding(thisCoordinate);
//                        -1 * ((thisCoordinate.getNode().getIndexInGraph() * makespan) +
//                        thisCoordinate.getTimeStep());
                // Find all edges that have timestep-1 and destination of thisCoordinate.getNode()
                List<EdgeCoordinate> edgesInPreviousTimeStep = edges.get(timeStep - 1);
                for (int edgeIndex = 0; edgeIndex < edgesInPreviousTimeStep.size(); edgeIndex++) {
                    EdgeCoordinate thisEdgeCoordinate = edgesInPreviousTimeStep.get(edgeIndex);
                    if (thisEdgeCoordinate.getDestination() == thisCoordinate.getNode()) {
                        int[] clause = new int[2];
                        clause[0] = propVar1;
                        // E(v,i) or E(u,v,i). where i = k-1
                        clause[1] = encoder.getEncoding(thisEdgeCoordinate);
                                //getEdgeEncoding(thisEdgeCoordinate, edgeIndex);
//                                vertexOffset + (timeStep * edgesInPreviousTimeStep.size()) + edgeIndex;
                        results.add(new VecInt(clause));
                    }
                }
            }
        }

        // Equation 10:
        // (~E(u,v,i) or ~M(v,i))
        for (int timeStep = 0; timeStep < edges.size()-1; timeStep++) {
            List<EdgeCoordinate> edgesInTimeStep = edges.get(timeStep);
            for (int edgeIndex = 0; edgeIndex < edgesInTimeStep.size(); edgeIndex++) {
                int[] clause = new int[2];
                EdgeCoordinate thisEdgeCoordinate = edgesInTimeStep.get(edgeIndex);
                // ~E(u,v,i)
                clause[0] = -1 * encoder.getEncoding(thisEdgeCoordinate);
                        //getEdgeEncoding(thisEdgeCoordinate, edgeIndex);
//                        -1 * (vertexOffset + (timeStep * edgesInTimeStep.size()) + edgeIndex);
                // ~M(v,i)
                clause[1] = -1 * encoder.getEncoding(
                        thisEdgeCoordinate.getDestination(), thisEdgeCoordinate.getTimeStep() + 1);
                        //getVertexEncoding(thisEdgeCoordinate.getDestination(), thisEdgeCoordinate.getTimeStep() + 1);
//                        -1 * ((thisEdgeCoordinate.getDestination().getIndexInGraph() * makespan) +
//                        thisEdgeCoordinate.getTimeStep() + 1);
                results.add(new VecInt(clause));
            }
        }

        // MAPPING SUBMODEL:
        // Equation 11:
        // (~E(u,v,i) or ~A(u,i)[0] or A(v, i+1)[0]) and
        // (~E(u,v,i) or A(u,i)[0] or ~A(v, i+1)[0]) and
        // (~E(u,v,i) or ~A(u,i)[1] or A(v, i+1)[1]) and
        // (~E(u,v,i) or A(u,i)[1] or ~A(v, i+1)[1]) and
        // ...
        // (~E(u,v,i) or ~A(u,i)[log2(|A|+1) or A(v, i+1)[log2(|A|+1]) and
        // (~E(u,v,i) or A(u,i)[log2(|A|+1]] or ~A(v, i+1)[log2(|A|+1]])
        for (int timeStep = 0; timeStep < edges.size()-1; timeStep++) {
            List<EdgeCoordinate> edgesInTimestep = edges.get(timeStep);
            for (int edgeIndex = 0; edgeIndex < edgesInTimestep.size(); edgeIndex++) {
                EdgeCoordinate thisEdgeCoordinate = edgesInTimestep.get(edgeIndex);
                // ~E(u,v,i)
                int propVar1 = -1 * encoder.getEncoding(thisEdgeCoordinate);
                    //getEdgeEncoding(thisEdgeCoordinate, edgeIndex);
//                        -1 * (vertexOffset + (timeStep * edgesInFirstTimeStep.size()) + edgeIndex);
                for (int binaryIndex = 0; binaryIndex < numBinaryPropVars; binaryIndex++) {
                    int[] clauseOne = new int[3];
                    clauseOne[0] = propVar1;
                    // ~A(u,i)[binaryIndex]
                    clauseOne[1] = -1 * encoder.getEncoding(
                            thisEdgeCoordinate.getSource(), thisEdgeCoordinate.getTimeStep(), binaryIndex);
                            //getAgentEncoding(thisEdgeCoordinate.getSource(), thisEdgeCoordinate.getTimeStep(), binaryIndex);
//                            -1 * (vertexOffset + edgeOffset +
//                            (makespan * thisEdgeCoordinate.getSource().getIndexInGraph() * numBinaryPropVars) +
//                            ((thisEdgeCoordinate.getTimeStep() - 1) * numBinaryPropVars) +
//                            binaryIndex);
                    // A(v,i+1)[binaryIndex]
                    clauseOne[2] = encoder.getEncoding(
                            thisEdgeCoordinate.getDestination(), thisEdgeCoordinate.getTimeStep() + 1, binaryIndex);
                            //getAgentEncoding(thisEdgeCoordinate.getDestination(), thisEdgeCoordinate.getTimeStep() + 1, binaryIndex);
//                            vertexOffset + edgeOffset +
//                            (makespan * thisEdgeCoordinate.getDestination().getIndexInGraph() * numBinaryPropVars) +
//                            ((thisEdgeCoordinate.getTimeStep()) * numBinaryPropVars) +
//                            binaryIndex;
                    results.add(new VecInt(clauseOne));
                    int[] clauseTwo = new int[3];
                    clauseTwo[0] = propVar1;
                    // A(u,i)[binaryIndex]
                    clauseTwo[1] = clauseOne[1] * -1;
                    // ~A(v,i+1)[binaryIndex]
                    clauseTwo[2] = clauseOne[2] * -1;
                    results.add(new VecInt(clauseTwo));
                }
            }
        }

        // Equation 12:
        // (~A(u,i)[0] or M(u,i)) and
        // (~A(u,i)[1] or M(u,i)) and
        // ...
        // (~A(u,i)[log2(|A|+1]] or M(u,i))
        for (int timeStep = 0; timeStep < coordinates.size(); timeStep++) {
            List<Coordinate> coordinatesInTimeStep = coordinates.get(timeStep);
            for (int coordinateIndex = 0; coordinateIndex < coordinatesInTimeStep.size(); coordinateIndex++) {
                Coordinate thisCoordinate = coordinatesInTimeStep.get(coordinateIndex);
                // M(u,i)
                int propVar2 = encoder.getEncoding(thisCoordinate);
                    //getVertexEncoding(thisCoordinate);
//                        (thisCoordinate.getNode().getIndexInGraph() * makespan) + thisCoordinate.getTimeStep();
                for (int binaryIndex = 0; binaryIndex < numBinaryPropVars; binaryIndex++) {
                    int[] clause = new int[2];
                    clause[1] = propVar2;
                    // ~A(u,i)[binaryIndex]
                    clause[0] = -1 * encoder.getEncoding(thisCoordinate, binaryIndex);
                            //getAgentEncoding(thisCoordinate.getNode(), thisCoordinate.getTimeStep(), binaryIndex);
//                            -1 * (vertexOffset + edgeOffset +
//                            (makespan * thisCoordinate.getNode().getIndexInGraph() * numBinaryPropVars) +
//                            ((thisCoordinate.getTimeStep() - 1) * numBinaryPropVars) +
//                            binaryIndex);
                    results.add(new VecInt(clause));
                }
            }
        }

        // Equation 13:
        // Encoding initial states

        // TODO: I am not encoding iff, I am just only adding the applicable constraint in each scenario
        List<Coordinate> coordinatesInFirstTimeStep = coordinates.get(0);
        for (int coordinateIndex = 0; coordinateIndex < coordinatesInFirstTimeStep.size(); coordinateIndex++) {
            Coordinate thisCoordinate = coordinatesInFirstTimeStep.get(coordinateIndex);
            // Check if an agent starts at this coordinate
            boolean found = false;
            for (int agentIndex = 0; agentIndex < agents.size() && !found; agentIndex++) {
                Agent thisAgent = agents.get(agentIndex);
                if (thisAgent.position() == thisCoordinate.getNode().getIndexInGraph()) {
                    found = true;
                    boolean[] agentNumBinary = getBinaryEncoding(agentIndex + 1,
                            encoder.getNumberOfBinaryVariablesPerAgent());
                    // this agent starts in this coordinate
                    for (int binaryIndex = 0; binaryIndex < numBinaryPropVars; binaryIndex++) {
                        if (agentNumBinary[binaryIndex]) {
                            int[] clauseOne = new int[2];
                            // A(v(i),0)[binaryIndex]
                            clauseOne[0] = encoder.getEncoding(thisCoordinate, binaryIndex);
                                    //getAgentEncoding(thisCoordinate.getNode(), thisCoordinate.getTimeStep(), binaryIndex);
//                                    vertexOffset + edgeOffset +
//                                    (makespan * thisCoordinate.getNode().getIndexInGraph() * numBinaryPropVars) +
//                                    ((thisCoordinate.getTimeStep() - 1) * numBinaryPropVars) +
//                                    binaryIndex;
                            // ~M(v(i),0)
                            clauseOne[1] = -1 * encoder.getEncoding(thisCoordinate);
                                    //getVertexEncoding(thisCoordinate);
//                                    -1 * ((thisCoordinate.getNode().getIndexInGraph() * makespan) +
//                                    thisCoordinate.getTimeStep());
                            results.add(new VecInt(clauseOne));
                            int[] clauseTwo = new int[2];
                            // ~A(v(i),0)[binaryIndex]
                            clauseTwo[0] = clauseOne[0] * -1;
                            // M(v(i),0)
                            clauseTwo[1] = clauseOne[1] * -1;
                            results.add(new VecInt(clauseTwo));
                        } else {
                            int[] clause = new int[1];
                            // ~A(v(i),0)[binaryIndex]
                            clause[0] = -1 * encoder.getEncoding(thisCoordinate, binaryIndex);
                                    //getAgentEncoding(thisCoordinate.getNode(), thisCoordinate.getTimeStep(), binaryIndex);
//                                    -1 * (vertexOffset + edgeOffset +
//                                    (makespan * thisCoordinate.getNode().getIndexInGraph() * numBinaryPropVars) +
//                                    ((thisCoordinate.getTimeStep() - 1) * numBinaryPropVars) +
//                                    binaryIndex);
                            results.add(new VecInt(clause));
                        }
                    }
                }
            }

            // If no agent starts in this node, then add constraint to make node empty at first timestep
            if (!found) {
                for (int binaryIndex = 0; binaryIndex < numBinaryPropVars; binaryIndex++) {
                    int[] clause = new int[1];
                    // ~A(v(i),0)[binaryIndex]
                    clause[0] = -1 * encoder.getEncoding(thisCoordinate, binaryIndex);
                            //getAgentEncoding(thisCoordinate.getNode(), thisCoordinate.getTimeStep(), binaryIndex);
//                            -1 * (vertexOffset + edgeOffset +
//                            (makespan * thisCoordinate.getNode().getIndexInGraph() * numBinaryPropVars) +
//                            ((thisCoordinate.getTimeStep() - 1) * numBinaryPropVars) +
//                            binaryIndex);
                    results.add(new VecInt(clause));
                }
            }
        }

        // Equation 14:
        // Encoding goal positions
        // TODO: I am not encoding iff, I am just only adding the applicable constraint in each scenario
        List<Coordinate> coordinatesInLastTimeStep = coordinates.get(makespan - 1);
        for (int coordinateIndex = 0; coordinateIndex < coordinatesInLastTimeStep.size(); coordinateIndex++) {
            Coordinate thisCoordinate = coordinatesInLastTimeStep.get(coordinateIndex);
            // Check if an agent ends at this coordinate
            boolean found = false;
            for (int agentIndex = 0; agentIndex < agents.size() && !found; agentIndex++) {
                Agent thisAgent = agents.get(agentIndex);
                if (thisAgent.goal() == thisCoordinate.getNode().getIndexInGraph()) {
                    found = true;
                    boolean[] agentNumBinary = getBinaryEncoding(agentIndex + 1,
                            encoder.getNumberOfBinaryVariablesPerAgent());
                    // this agent ends in this coordinate
                    for (int binaryIndex = 0; binaryIndex < numBinaryPropVars; binaryIndex++) {
                        if (agentNumBinary[binaryIndex]) {
                            int[] clauseOne = new int[2];
                            // A(v(i),0)[binaryIndex]
                            clauseOne[0] = encoder.getEncoding(thisCoordinate, binaryIndex);
                                    //getAgentEncoding(thisCoordinate.getNode(), thisCoordinate.getTimeStep(), binaryIndex);
//                                    vertexOffset + edgeOffset +
//                                    (makespan * thisCoordinate.getNode().getIndexInGraph() * numBinaryPropVars) +
//                                    ((thisCoordinate.getTimeStep() - 1) * numBinaryPropVars) +
//                                    binaryIndex;
                            // ~M(v(i),0)
                            clauseOne[1] = -1 * encoder.getEncoding(thisCoordinate);
                                    //getVertexEncoding(thisCoordinate);
//                                    -1 * ((thisCoordinate.getNode().getIndexInGraph() * makespan) +
//                                    thisCoordinate.getTimeStep());
                            results.add(new VecInt(clauseOne));
                            int[] clauseTwo = new int[2];
                            // ~A(v(i),0)[binaryIndex]
                            clauseTwo[0] = clauseOne[0] * -1;
                            // M(v(i),0)
                            clauseTwo[1] = clauseOne[1] * -1;
                            results.add(new VecInt(clauseTwo));
                        } else {
                            int[] clause = new int[1];
                            // ~A(v(i),0)[binaryIndex]
                            clause[0] = -1 * encoder.getEncoding(thisCoordinate, binaryIndex);
                                    //getAgentEncoding(thisCoordinate.getNode(), thisCoordinate.getTimeStep(), binaryIndex);
//                                    -1 * (vertexOffset + edgeOffset +
//                                    (makespan * thisCoordinate.getNode().getIndexInGraph() * numBinaryPropVars) +
//                                    ((thisCoordinate.getTimeStep() - 1) * numBinaryPropVars) +
//                                    binaryIndex);
                            results.add(new VecInt(clause));
                        }
                    }
                }
            }


            // If no agent ends in this node, then add constraint to make node empty at last timestep
            if (!found) {
                for (int binaryIndex = 0; binaryIndex < numBinaryPropVars; binaryIndex++) {
                    int[] clause = new int[1];
                    // ~A(v(i),0)[binaryIndex]
                    clause[0] = -1 * encoder.getEncoding(thisCoordinate, binaryIndex);
                            //getAgentEncoding(thisCoordinate.getNode(), thisCoordinate.getTimeStep(), binaryIndex);
//                            -1 * (vertexOffset + edgeOffset +
//                            (makespan * thisCoordinate.getNode().getIndexInGraph() * numBinaryPropVars) +
//                            ((thisCoordinate.getTimeStep() - 1) * numBinaryPropVars) +
//                            binaryIndex);
                    results.add(new VecInt(clause));
                }
            }
        }

        return results;
    }


    /**
     * Gets the unique CNF encoding value of the vertex propositional variable
     * associated with the given coordinate
     * @param coordinate The coordinate to get the encoding for
     * @return Returns the unique CNF encoding value of the vertex propositional variable
     * associated with the given coordinate
     */
//    private int getVertexEncoding(Coordinate coordinate) {
//        return getVertexEncoding(coordinate.getNode(), coordinate.getTimeStep());
//    }

    /**
     * Gets the unique CNF encoding value of the vertex propositional variable
     * associated with the given node at the given timestep
     * @param node The node to use
     * @param timeStep The timestep to use
     * @return Returns the unique CNF encoding value of the vertex propositional variable
     * associated with the given node at the given timestep
     */
//    private int getVertexEncoding(Node node, int timeStep) {
//         The index of the node times the makespan, plus the given timestep
//        return node.getIndexInGraph() * makespan + (timeStep + 1);
//    }

    /**
     * Gets the unique CNF encoding value for the edge propositional variable
     * associated with the given edge and edge index
     * @param edge The edge to use
     * @param edgeIndex The edge index to use
     * @return Returns the unique CNF encoding value for the edge propositional variable
     * associated with the given edge and edge index
     */
//    private int getEdgeEncoding(EdgeCoordinate edge, int edgeIndex) {
//         The number of vertex propositional variables, plus the timestep of the edge times
//         the number of edges per timestep, plus the index of edge within each timestep
//        return getEdgeEncoding(edge.getTimeStep(), edgeIndex);
//    }

    /**
     * Gets the unique CNF encoding value for the edge propositional variable
     * associated with the given edge and edge index
     * @param timeStep The timestep to use
     * @param edgeIndex The edge index to use
     * @return Returns the unique CNF encoding value for the edge propositional variable
     * associated with the given timestep and edge index
     */
//    private int getEdgeEncoding(int timeStep, int edgeIndex) {
        // The number of vertex propositional variables, plus the timestep of the edge times
        // the number of edges per timestep, plus the index of edge within each timestep
//        return vertexOffset + (timeStep + 1) * edges.size() + edgeIndex;
//    }

    /**
     * Gets the unique CNF encoding value for the first agent propositional variable
     * associated with the given coordinate
     * @param coordinate The coordinate to use
     * @return  Returns the unique CNF encoding value for the first agent propositional variable
     * associated with the given coordinate
     */
//    private int getAgentEncoding(Coordinate coordinate) {
//        return getAgentEncoding(coordinate.getNode(), coordinate.getTimeStep());
//    }

    /**
     * Gets the unique CNF encoding value for the first agent propositional variable
     * associated with the given node and timestep
     * @param node The node to use
     * @param timeStep The timestep to use
     * @return  Returns the unique CNF encoding value for the first agent propositional variable
     * associated with the given node and timestep
     */
//    private int getAgentEncoding(Node node, int timeStep) {
//        return getAgentEncoding(node, timeStep, 0);
//    }

    /**
     * Gets the unique CNF encoding value for the agent propositional variable
     * associated with the given node and timestep, and index into the agent variable's
     * binary vector
     * @param node The node to use
     * @param timeStep The timestep to use
     * @return  Returns the unique CNF encoding value for the first agent
     * propositional variable associated with the given node, timestep,
     * and index into the agent variable's binary vector
     */
//    private int getAgentEncoding(Node node, int timeStep, int binaryIndex) {
//        return vertexOffset + edgeOffset +
//                (makespan * node.getIndexInGraph() * numBinaryPropVars) +
//                ((timeStep) * numBinaryPropVars) +
//                binaryIndex;
//    }

    /**
     * Updates the offsets and the number of binary propositional variables used to represent
     * each agent propositional variable
     * @param n The number of agents
     */
//    private void updateOffsetsAndNumBinaryPropVars(int n) {
//        this.vertexOffset = 1 + getNumCoordinates();
//        this.edgeOffset = vertexOffset + getNumEdges();
//        this.numBinaryPropVars = (int)Math.ceil(Math.log(n + 1)/Math.log(2));
//    }

    /**
     * Creates a boolean vector that represents the binary encoding of n.
     * The length of the return vector is the value of numBinaryPropVars.
     * @param n The int to create a binary encoding of
     * @return Returns an boolean vector that represents a binary encoding of n
     */
    private boolean[] getBinaryEncoding(int n, int length) {
        boolean[] result = new boolean[length];
        for (int i = 0; i < length; i++) {
            result[length - 1 - i] = (n & (1 << i)) != 0;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Makespan: " + makespan + ", \n");
        sb.append("Coordinates: ");
        for (List<Coordinate> coordinatesInTimeStep : coordinates) {
            for (Coordinate coordinate : coordinatesInTimeStep) {
                sb.append(coordinate.toString() + ", ");
            }
        }
        sb.append("\n");

        sb.append("Edges: ");
        for (List<EdgeCoordinate> edgeInTimeStep : edges) {
            for (EdgeCoordinate thisEdgeCoordinate : edgeInTimeStep) {
                sb.append(thisEdgeCoordinate.toString() + ", ");
            }
        }

        return sb.toString();
    }

    private class SatEdgeCoordinate extends EdgeCoordinate {
        int identifier;

        SatEdgeCoordinate(Node source, Node dest, int timeStep, int identifier) {
            super(source, dest, timeStep);
            this.identifier = identifier;
        }
    }
}
