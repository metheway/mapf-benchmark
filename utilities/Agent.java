package utilities;
import java.io.Serializable;

/**
 * Represents an agent with a start location, goal location, and ID
 * for indexing in a list of agents.
 */
public class Agent implements Serializable {
	
	private int position;
	private int goal;
	private int agentID;

    /**
     * Constructor that creates an agent with the specified
     * start location, goal location, and id.
     * @param start The agent's start location
     * @param goal The agent's goal location
     * @param id The agent ID
     */
	public Agent (int start, int goal, int id) {
		position = start;
		this.goal = goal;
		agentID = id;
	}

    /**
     * Accessor for the agent's start location.
     * @return the agent's start location
     */
	public int position() {
		return position;
	}

    /**
     * Accessor for the agent's goal location.
     * @return the agent's goal location
     */
	public int goal() {
		return goal;
	}

    /**
     * Accessor for the agent's ID
     * @return the agent ID
     */
	public int id() {
		return agentID;
	}

    @Override
	public String toString() {
		return "Start: " + position + " Goal: " + goal;
	}
	
	
}
