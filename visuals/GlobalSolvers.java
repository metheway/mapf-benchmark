package visuals;

import constants.CostFunction;
import solvers.Solver;
import solvers.astar.GenericAStar;
import solvers.astar.MultiAgentAStar;
import solvers.astar.OperatorDecomposition;
import solvers.c_astar.CAStar;
import solvers.cbs.ConflictBasedSearch;
import solvers.independence_detection.EnhancedID;
import solvers.independence_detection.IndependenceDetection;

import java.util.*;

/**
 * Created by maxgray on 4/17/16.
 */
public class GlobalSolvers {
    //public static GenericAStar genericAStar = new GenericAStar();

    public static Map<String, Solver> solverMap;

    static {
        solverMap = mapSolversToStringRepresentations(
                Arrays.asList(
                        new CAStar(),
                        new MultiAgentAStar(CostFunction.MAKESPAN),
                        new MultiAgentAStar(CostFunction.SUM_OF_COSTS),
                        new OperatorDecomposition(),
                        new IndependenceDetection(new MultiAgentAStar(CostFunction.SUM_OF_COSTS)),
                        new IndependenceDetection(new MultiAgentAStar(CostFunction.MAKESPAN)),
                        new EnhancedID(new MultiAgentAStar(CostFunction.SUM_OF_COSTS)),
                        new EnhancedID(new MultiAgentAStar(CostFunction.MAKESPAN)),
                        new ConflictBasedSearch()
                )
        );
    }

    public static CAStar caStar = new CAStar();
    public static MultiAgentAStar aStarMakeSpan = new MultiAgentAStar(CostFunction.MAKESPAN);
    public static MultiAgentAStar aStarSOC = new MultiAgentAStar(CostFunction.SUM_OF_COSTS);
    public static OperatorDecomposition operatorDecomposition = new OperatorDecomposition();
    public static IndependenceDetection idWithAStarSOC = new IndependenceDetection(new MultiAgentAStar(CostFunction.SUM_OF_COSTS));
    public static IndependenceDetection idWithAStarMakespan = new IndependenceDetection(new MultiAgentAStar(CostFunction.MAKESPAN));

    private static Map<String, Solver> mapSolversToStringRepresentations(List<Solver> solvers) {
        Map<String, Solver> result = new TreeMap<>();
        for (Solver solver : solvers) {
            result.put(solver.toString(), solver);
        }
        return result;
    }



    private GlobalSolvers(){};
}
