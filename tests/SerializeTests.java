package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import utilities.Agent;
import utilities.Connected;
import utilities.Graph;
import utilities.ProblemInstance;

public class SerializeTests {
	public static void main(String[] args) throws FileNotFoundException {
		Graph gr1 = new Graph(Connected.FOUR, new File("MAPF/src/maps/arena.map"));
        Graph gr2 = new Graph(Connected.FOUR, new File("MAPF/src/maps/maze512-1-0.map"));
        ProblemInstance p1 = new ProblemInstance(gr1, 3);
        ProblemInstance p2 = new ProblemInstance(gr2, 3);

        p1.serialize("MAPF/src/test_bin/", "test1");
        p2.serialize("MAPF/src/test_bin/", "test2");

        try {
            File x = new File("MAPF/src/test_bin/test1.bin");
            System.out.println(x);
            ProblemInstance p3 = new ProblemInstance(gr1, new File("MAPF/src/test_bin/test1.bin"));
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.toString());
        }
    }
}
