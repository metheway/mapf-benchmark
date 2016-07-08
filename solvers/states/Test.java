package solvers.states;

import utilities.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Arrays;

/**
 * Created by maxgray on 5/2/16.
 */
public class Test {
    public static void main(String[] args) throws FileNotFoundException {
        Graph pgraph = new Graph(Connected.FOUR, new File("MAPF/src/maps/arena.map"));
        Agent a1 = new Agent(0, 1, 0);
        Agent a2 = new Agent(1, 0, 1);
        List<Agent> agentList = Arrays.asList(a1, a2);
        ProblemInstance pi = new ProblemInstance(pgraph, agentList);
        SingleAgentState sa = new SingleAgentState(0, pi);
        SingleAgentState as = new SingleAgentState(1, pi);
        List<SingleAgentState> list = Arrays.asList(as, sa);
        System.out.println(MultiAgentState.containsTransposition(list, list));
    }
}
