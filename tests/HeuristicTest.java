package tests;

import maps.Maps;
import solvers.astar.TDHeuristic;
import utilities.Agent;
import utilities.Connected;
import utilities.Graph;
import utilities.ProblemInstance;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by maxgray on 4/8/16.
 */
public class HeuristicTest {

    public static void main(String[] args) throws FileNotFoundException {
        testTDH();
    }

    public static void testTDH() throws FileNotFoundException {
        Agent agent = new Agent(0, 4, 0);
        List<Agent> agentList = new ArrayList<>();
        agentList.add(agent);
        ProblemInstance pi = new ProblemInstance(new Graph(Connected.FOUR, new File("MAPF/src/maps/test.map")), agentList);
        TDHeuristic tdh = new TDHeuristic(pi);
        System.out.println(Arrays.toString(tdh.getLookupTable()[0]));
    }

}
