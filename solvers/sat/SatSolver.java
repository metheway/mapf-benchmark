package solvers.sat;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import solvers.Solver;
import utilities.*;

import java.util.*;

/**
 * A solver that uses the SAT approach. A ProblemInstance is converted
 * into a SAT problem, and then solved using a SAT Solver.
 */
public class SatSolver implements Solver {

    ProblemInstance problemInstance;
    TimeExpansionGraph timeExpansionGraph;

    private void init() {
        timeExpansionGraph = new TimeExpansionGraph(problemInstance.getGraph().getNodes());
    }

    @Override
    /**
     * Attempts to solve the given problem instance
     * @param problemInstance The problem instance to solve
     * @return Returns true if a solution was found, false otherwise
     */
    public boolean solve(ProblemInstance problemInstance) {
        this.problemInstance = problemInstance;
        init();

        timeExpansionGraph.increaseMakespan();

        List<VecInt> cnfEncoding = timeExpansionGraph.getCnfEncoding(problemInstance.getAgents());
        for (VecInt v : cnfEncoding) {
            System.out.println(v);
        }
        System.out.println();

        System.out.println(passToExternalSatSolver());
        // TODO: test with external SAT solver, if fails, increase the size of the makespan in the TEG
        return false;
    }

    private boolean passToExternalSatSolver() {
        ISolver sat4jSolver = SolverFactory.newDefault();

        List<VecInt> clauses = timeExpansionGraph.getCnfEncoding(problemInstance.getAgents());

        Iterator<VecInt> clauseIterator = clauses.iterator();
        VecInt thisClause = clauseIterator.next();

        // The first VecInt is special and contains meta data
        sat4jSolver.newVar(thisClause.get(0));
        sat4jSolver.setExpectedNumberOfClauses(thisClause.get(1));

        try {
            // Add all the clauses to the solver
            while(clauseIterator.hasNext()) {
                thisClause = clauseIterator.next();
                System.out.println(thisClause);
                sat4jSolver.addClause(thisClause);
            }
            IProblem sat4jProblem = sat4jSolver;
            return sat4jProblem.isSatisfiable();
        } catch (ContradictionException e) {
            System.out.println("Contradiction Exception thrown");
            System.out.println(e.getMessage());
        } catch (TimeoutException e) {
            System.out.println("Timeout");
        }
        return false;
    }

    @Override
    /**
     * Constructs and returns the path found as a solution by the solver
     * @return Returns the path found by the solver
     */
    public Path getPath() {
        return null;
    }

}
