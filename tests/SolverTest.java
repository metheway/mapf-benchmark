package tests;

import solvers.c_astar.CAStar;
import solvers.cbs.ConflictBasedSearch;
import solvers.astar.*;
import solvers.independence_detection.EnhancedID;
import solvers.independence_detection.IndependenceDetection;
import utilities.*;

import java.io.*;
import java.util.*;

import constants.CostFunction;

public class SolverTest {

	public static void main(String[] args) throws FileNotFoundException {
		//testSingleAgent();
        //testMultiAgent();
        //testIndependenceDetection();
        //testReservation();
        testCBS();
        //testConflict();
	}

	public static void testSingleAgent() throws FileNotFoundException {
		ProblemMap problemMap = new ProblemMap(new File("MAPF/src/maps/test.map"));
		Graph graph = new Graph(Connected.EIGHT, problemMap);
		ProblemInstance problemInstance = new ProblemInstance(graph, Collections.singletonList(new Agent(0, 4, 0)));
		SingleAgentAStar solver = new SingleAgentAStar();
		if (solver.solve(problemInstance)) {
			System.out.println(solver.getPath());
		} else {
			System.out.println("Failure");
		}
	}

	public static void testMultiAgent() throws FileNotFoundException {
        ProblemMap problemMap = new ProblemMap(new File("MAPF/src/maps/test.map"));
        Graph graph = new Graph(Connected.EIGHT, problemMap);
        Agent a1 = new Agent(0, 4, 0);
        Agent a2 = new Agent(5, 9, 1);
        Agent a3 = new Agent(10, 14, 2);
        ProblemInstance problemInstance = new ProblemInstance(graph, Arrays.asList(a1, a2, a3));
        MultiAgentAStar solver = new MultiAgentAStar(CostFunction.SUM_OF_COSTS);
        if (solver.solve(problemInstance)) {
            System.out.println(solver.getPath());
        } else {
            System.out.println("Failure");
        }
	}

    public static void testIndependenceDetection() throws FileNotFoundException {
        ProblemMap problemMap = new ProblemMap(new File("MAPF/src/maps/arena.map"));
        Graph graph = new Graph(Connected.EIGHT, problemMap);
        Agent a1 = new Agent(0, 11, 0);
        Agent a2 = new Agent(5, 5, 1);
        Agent a3 = new Agent(10, 12, 2);
        ProblemInstance problemInstance = new ProblemInstance(graph,50);
        IndependenceDetection solver = new EnhancedID(new OperatorDecomposition());
        long start = System.currentTimeMillis();
        if (solver.solve(problemInstance)) {
            long enhancedTime = System.currentTimeMillis() - start;
            System.out.printf("Enhanced:\n\tTime: %dms\n\tCost: %5.2f",
                    enhancedTime, solver.getPath().cost());
        } else {
            System.out.println("Failure");
        }
    }

    public static void testReservation() throws FileNotFoundException {
        ProblemMap problemMap = new ProblemMap(new File("MAPF/src/maps/reservation_test.map"));
        Graph graph = new Graph(Connected.EIGHT, problemMap);
        ProblemInstance problemInstance = new ProblemInstance(graph, Collections.singletonList(new Agent(0, 1, 0)));
        MultiAgentAStar solver = new MultiAgentAStar(CostFunction.SUM_OF_COSTS);
        solver.getReservation().reserveCoordinate(new Coordinate(1, graph.getNodes().get(1)), null);
        System.out.println("Last time step of reservation = " + solver.getReservation().getLastTimeStep());
        System.out.println(solver.solve(problemInstance));
        System.out.println(solver.getPath().cost());
    }

    public static void testCBS() throws FileNotFoundException {
        ProblemMap problemMap = new ProblemMap(new File("MAPF/src/maps/arena.map"));
        Graph graph = new Graph(Connected.EIGHT, problemMap);
        ProblemInstance problemInstance = new ProblemInstance(graph, 30);
        ConflictBasedSearch c = new ConflictBasedSearch();
        IndependenceDetection e = new EnhancedID(new OperatorDecomposition());
        long t = System.currentTimeMillis();
        System.out.println(e.solve(problemInstance));
        System.out.println((System.currentTimeMillis() - t)/1000.);
        for (State s : e.getPath()) {
            s.printIndices();
        }
        System.out.println();

        t = System.currentTimeMillis();
        System.out.println(c.solve(problemInstance));
        System.out.println((System.currentTimeMillis() - t)/1000.);

        System.out.println(e.getPath().cost() + " " + c.getPath().cost());
    }

    public static void testCAStar() throws FileNotFoundException {
        CAStar solver = new CAStar();
        ProblemMap problemMap = new ProblemMap(new File("MAPF/src/maps/test.map"));
        Graph graph = new Graph(Connected.EIGHT, problemMap);
        ProblemInstance problemInstance = new ProblemInstance(graph, new ArrayList<Agent>(
                Arrays.asList(
                        new Agent(0, 18, 0),
                        new Agent(18, 0, 1)
                )
        ));
        solver.solve(problemInstance);
        System.out.println(solver.getPath().cost());
        for (State s : solver.getPath()) {
            s.printIndices();
        }
    }

    public static void testConflict() throws FileNotFoundException {
        SingleAgentAStar solver1 = new SingleAgentAStar();
        SingleAgentAStar solver2 = new SingleAgentAStar();
        SingleAgentAStar solver3 = new SingleAgentAStar();

        ProblemMap problemMap = new ProblemMap(new File("MAPF/src/maps/test.map"));
        Graph graph = new Graph(Connected.EIGHT, problemMap);

        ProblemInstance problemInstance1 = new ProblemInstance(graph, Collections.singletonList(new Agent(0, 18, 0)));
        ProblemInstance problemInstance2 = new ProblemInstance(graph, Collections.singletonList(new Agent(18, 0, 0)));
        ProblemInstance problemInstance3 = new ProblemInstance(graph, Collections.singletonList(new Agent(6,  6, 0)));

        solver1.solve(problemInstance1);
        solver2.solve(problemInstance2);
        solver3.solve(problemInstance3);

        List<Path> solutions = new ArrayList<>(Arrays.asList(solver1.getPath(), solver2.getPath(), solver3.getPath()));

        Conflict conflict1 = Util.conflict(0, 1, solutions);
        System.out.println(conflict1);

        Conflict conflict2 = Util.conflict(0, 2, solutions);
        System.out.println(conflict2);
    }

}