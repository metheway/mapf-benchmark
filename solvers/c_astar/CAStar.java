package solvers.c_astar;

import constants.CostFunction;
import solvers.ConstrainedSolver;
import solvers.Reservation;
import solvers.astar.SingleAgentAStar;
import solvers.states.MultiAgentState;
import utilities.Agent;
import utilities.Path;
import utilities.ProblemInstance;
import constants.Keys;

import solvers.astar.State;
import solvers.states.SingleAgentState;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class CAStar extends ConstrainedSolver {
	
	// needs ProblemInstance, reservation table (HashTable)
	private Reservation reservation;
	private HashMap<String, Reservation> params;
	private List<Path> paths;
	private ProblemInstance problemInstance;
	
	public boolean solve(ProblemInstance problem) {
		this.problemInstance = problem;
		init(problem);
		List<Agent> agents = problem.getAgents();
		SingleAgentAStar solver = new SingleAgentAStar(params);
		for (Agent a : agents) {
			ProblemInstance agentProblem = new ProblemInstance(problemInstance.getGraph(), Collections.singletonList(a));
			if (!solver.solve(agentProblem)) return false;
            Path solverPath = solver.getPath();
            reservation.reservePath(solverPath);
			paths.add(solverPath);
		}
		return true;
	}

	private void init(ProblemInstance problem) {
		params = new HashMap<>();
		reservation = new Reservation();
		paths = new ArrayList<>();
		params.put(Keys.RESERVATIONS, reservation);
	}

	
	public Path getPath() {
		List<State> result = new ArrayList<>();
		List<List<SingleAgentState>> pre = new ArrayList<>();
		int longestLength = lengthOfLongestPath();
		for (int j = 0; j < longestLength; j++){
			List<SingleAgentState> individual = new ArrayList<>();
			for (int i = 0; i < paths.size(); i++) {
				Path path = paths.get(i);
				if (j < path.size()) {
					individual.addAll(((MultiAgentState) path.get(j)).getSingleAgentStates());
				} else {
					individual.addAll(((MultiAgentState) path.get(path.size() - 1)).getSingleAgentStates());
				}
			}
			pre.add(individual);
		}
		result.add(new MultiAgentState(null, CostFunction.SUM_OF_COSTS, pre.get(0), problemInstance));
		for (int state = 1; state < pre.size(); state++) {
			result.add(new MultiAgentState(result.get(state - 1), CostFunction.SUM_OF_COSTS, pre.get(state), problemInstance));
		}
		return new Path(result);
	}

	private int lengthOfLongestPath() {
		int max = 0;
		for (Path path : paths) {
			if (path.size() > max) max = path.size();
		}
		return max;
	}
}
