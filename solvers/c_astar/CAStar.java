package solvers.c_astar;

import solvers.ConstrainedSolver;
import solvers.Reservation;
import solvers.astar.MultiAgentAStar;
import solvers.astar.SingleAgentAStar;
import solvers.astar.State;
import solvers.astar.TDHeuristic;
import solvers.states.MultiAgentState;
import utilities.Agent;
import utilities.Path;
import utilities.ProblemInstance;
import constants.Keys;

import utilities.Util;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class implementing the Cooperative A* algorithm
 */
public class CAStar extends ConstrainedSolver {
    
    private Reservation reservation;
    private HashMap<Keys, Object> params;
    private List<Path> paths;
    private ProblemInstance problemInstance;

	public CAStar() {

	}

    @Override
    public boolean solve(ProblemInstance problem) {
        this.problemInstance = problem;
        init();
        List<Agent> agents = problem.getAgents();
        SingleAgentAStar solver = new SingleAgentAStar(params);
        for (Agent a : agents) {
            Agent singleton = new Agent(a.position(), a.goal(), 0);
            ProblemInstance agentProblem = new ProblemInstance(problemInstance.getGraph(), Collections.singletonList(singleton));
            if (!solver.solve(agentProblem)) return false;
            Path solverPath = solver.getPath();
            reservation.reservePath(solverPath);
            paths.add(solverPath);
            for (State s : solverPath) {
                s.printIndices();
            }
        }
        return true;
    }

    private void init() {
        params = new HashMap<>();
        reservation = new Reservation();
        paths = new ArrayList<>();
        params.put(Keys.RESERVATIONS, reservation);
    }

    @Override
    public Path getPath() {
        return Util.mergePaths(paths, problemInstance);
    }

    public String toString() {
    	return "Cooperative A*";
	}

}
