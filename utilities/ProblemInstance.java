package utilities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;

public class ProblemInstance {
	private Graph graph;
	private List<Agent> agents;
	private List<Node> goalPositions;
    private String mapTitle;

	/**
	 * Read the agents from a file of serialized agents
	 * @param agentsFile The file with serialized agent objects
	 */
	public ProblemInstance(Graph gr, File agentsFile){
		agents = deserializeAgents(agentsFile);
        graph = gr;
        if (!mapTitle.equals(graph.getMapTitle()))
            throw new IllegalArgumentException("Map " + mapTitle + " not compatible with problem instance!\n " +
                                                "Expected " + graph.getMapTitle());
	}
	
	public ProblemInstance(Graph graph, List<Agent> agents) {
		this.graph = graph;
		this.agents = agents;
		goalPositions = agentGoals();
		if (duplicateGoalsOrStarts(agents)) throw new IllegalArgumentException("Agents share goals or start positions!"
                                                                                + agents);
	}

	public ProblemInstance(Graph graph, int nAgents) {
        this.graph = graph;
        this.agents = graph.generateRandomAgents(nAgents);
        while (duplicateGoalsOrStarts(agents)) {
            agents = graph.generateRandomAgents(nAgents);
        }
		goalPositions = agentGoals();
	}

    public void addAgent(Agent newAgent) {
        List<Agent> current = new ArrayList<>(agents);
        current.add(new Agent(newAgent.position(), newAgent.goal(), current.size()));
        if (duplicateGoalsOrStarts(current)) throw new IllegalArgumentException("Agent with this goal or start location" +
                "already exists!");
        this.agents = current;
		updateGoalPositions();
    }

    public ProblemInstance join(ProblemInstance other) {
        List<Agent> joinAgents = new ArrayList<>(agents);
        for (Agent agent : other.agents) {
            Agent newAgent = new Agent(agent.position(), agent.goal(), joinAgents.size());
            joinAgents.add(newAgent);
        }
        return new ProblemInstance(graph, joinAgents);
    }

	public ProblemMap getMap() {
		return graph.getMap();
	}
	
	public List<Agent> getAgents() {
		return agents;
	}
	
	public void printMap() {
		graph.printMap();
	}
	
	public int getGraphSize() {
		return graph.getSize();
	}
	
	public void setAgents(List<Agent> agents) {
		this.agents = agents;
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	public List<Node> getGoal() {
		return goalPositions;
	}
	
	private List<Node> agentGoals() {
		List<Node> goalList = new ArrayList<Node>();
		for (Agent agent : this.agents) {
			goalList.add(graph.getNodes().get(agent.goal()));
		}
		return goalList;
	}
	
	public void serialize(String path, String fileName) {
		try {
			FileOutputStream fileOut = new FileOutputStream(path + fileName + ".bin");
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(graph.getMapTitle());
			objectOut.writeObject(agents.size());
			for (Agent a : agents)
				objectOut.writeObject(a);
			fileOut.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<Agent> deserializeAgents(File agentsFile) {
		List<Agent> agentList = new ArrayList<Agent>();
		Integer numAgents = 0;
		try {
			FileInputStream fileIn = new FileInputStream(agentsFile);
			BufferedInputStream buffer = new BufferedInputStream(fileIn);
			ObjectInputStream objectIn = new ObjectInputStream(buffer);
            mapTitle = (String) objectIn.readObject();
            numAgents = (Integer) objectIn.readObject();

            for (int i = 0; i < numAgents; i++)
                agentList.add((Agent) objectIn.readObject());
		}
		catch(ClassNotFoundException e) {
            e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println(numAgents);
		return agentList;
	}
	
	private boolean duplicateGoalsOrStarts(List<Agent> agentList) {
		HashSet<Integer> goals = new HashSet<Integer>();
		HashSet<Integer> starts = new HashSet<Integer>();
		for (Agent a : agentList) {
			goals.add(a.goal());
			starts.add(a.position());
		}
		return !(goals.size() == agentList.size()
				&& starts.size() == agentList.size());
	}

	private void updateGoalPositions() {
		goalPositions.clear();
		for (Agent agent : agents) {
			goalPositions.add(graph.getNodes().get(agent.goal()));
		}
	}

}
