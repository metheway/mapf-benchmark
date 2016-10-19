package solvers.sat;

/**
 * Created by jswiatek on 10/18/16.
 */

import com.sun.javafx.css.StyleCache;
import com.sun.javafx.geom.Edge;
import javafx.util.Pair;
import org.omg.CORBA.INTERNAL;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import utilities.Agent;
import utilities.Coordinate;
import utilities.EdgeCoordinate;
import utilities.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to encapsulate the creation of CNF encodings for a time expansion graph,
 * as well as mapping of encodings back to usable objects
 */
public class CnfEncoder {

    // Maps an object to an encoding
    Map<Object, Integer> encodings;
    // Maps encoding to objects
    Map<Integer, Object> backwardsEncodings;

    // The number of binary variables per agent variable
    int numPropVarsPerAgent;

    /**
     * Constructor that accepts a list of nodes, a list of agents, and a makespan
     * @param vertices
     * @param edges
     * @param agents
     * @param makespan
     */
    public CnfEncoder(
            List<List<Coordinate>> vertices, List<List<EdgeCoordinate>> edges, List<Agent> agents, int makespan) {
        this.numPropVarsPerAgent = (int)Math.ceil(Math.log(agents.size() + 1) / Math.log(2));
        this.encodings = new HashMap<>();
        int nextEncoding = 1;

        System.out.println("Encoding Vertices: ");
        // Add an entry for each vertex in the time expansion graph.
        // A vertex is made up of a node at a timestep, aka a coordinate
        for (List<Coordinate> verticesInTimeStep : vertices) {
            System.out.println("Encoding Vertices in this timestep: ");
            for (Coordinate vertex : verticesInTimeStep) {
                encodings.put(vertex, nextEncoding);
                System.out.println(nextEncoding);
                nextEncoding++;
            }
        }

        System.out.println("Encoding Edges: ");
        // Add an entry for each edge in the time expansion graph.
        // An edge is made up of a source node, a destination node, and a timestep
        for (List<EdgeCoordinate> edgesInTimeStep : edges) {
            System.out.println("Encoding Edges in this timestep: ");
            for (EdgeCoordinate edge : edgesInTimeStep) {
                encodings.put(edge, nextEncoding);
                System.out.println(nextEncoding);
                nextEncoding++;
            }
        }

        System.out.println("Encoding Agents: ");
        // Add an entry for each agent's propositional variable
        // Each entry is identified by a node, a timestep, and an index into the binary variables
        for (Coordinate vertex : vertices.get(0)) {
            Node node = vertex.getNode();
            for (int timeStep = 0; timeStep < makespan; timeStep++) {
                System.out.println("Binary vars for one agent: ");
                for (int binaryIndex = 0; binaryIndex < numPropVarsPerAgent; binaryIndex++) {
                    Pair<Object, Pair<Integer, Integer>> entry =
                            new Pair<>(node, new Pair<>(timeStep, binaryIndex));
                    encodings.put(entry, nextEncoding);
                    System.out.println(nextEncoding);
                    nextEncoding++;
                }
            }
        }

        // Generate the mapping of encodings to objects
        this.backwardsEncodings = new HashMap<>();
        for (Object key : encodings.keySet()) {
            backwardsEncodings.put(encodings.get(key), key);
        }
    }

    /**
     * Gets the number of variables encoded by this CnfEncoder
     * @return Returns the number of variables encoded by this CnfEncoder
     */
    public int getNumberOfVariables() {
        return encodings.size();
    }

    /**
     * Gets the number of binary propositional variables per agent variable
     * @return Returns the number of binary propositional variables per agent variable
     */
    public int getNumberOfBinaryVariablesPerAgent() {
        return numPropVarsPerAgent;
    }

    /**
     * Gets the encoding for any object
     * @param key The object to get the encoding for
     * @return Returns the encoding for the given object.
     *     Will return null if the object is not in this CnfEncoder
     */
    private Integer getEncoding(Object key) {
        return encodings.get(key);
    }

    public Integer getEncoding(Coordinate vertex) {
        return getEncoding((Object)vertex);
    }

    public Integer getEncoding(EdgeCoordinate edge) {
        return getEncoding((Object)edge);
    }

    public Integer getEncoding(Node node, int timestep) {
        return getEncoding(new Coordinate(timestep, node));
    }

    /**
     * Overloaded method for getting the encoding of a variable that has two additional integer qualifiers.
     * Used to get individual agent variables in this implementation
     * @param node The node object the agent variable is specified by
     * @param timestep The timestep to use
     * @param binaryIndex The binary index to use
     * @return Returns the encoding for the given agent at the given timestep and binary index
     */
    public Integer getEncoding(Node node, int timestep, int binaryIndex) {
        return getEncoding(new Pair<Node, Pair<Integer, Integer>>(
                node, new Pair<Integer, Integer>(timestep, binaryIndex)));
    }

    // get agent variable based on coordinate and binary index
    public Integer getEncoding(Coordinate coordinate, int binaryIndex) {
        return getEncoding(coordinate.getNode(), coordinate.getTimeStep(), binaryIndex);
    }

    /**
     * Gets the object that maps to the given encoding.
     * @param value The value to get the object for
     * @return Returns the object that maps to the given encoding
     */
    public Object getObject(Integer value) {
        return backwardsEncodings.get(value);
    }
}
