package visuals;

import solvers.Solver;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utilities.ProblemInstance;
import utilities.Statistics;
import visuals.GlobalSolvers;

/**
 * Created by jswiatek on 12/14/16.
 */
public class ProblemSolverVisual {
    private JList solversGuiList;
    private JTextField problemFileField;
    private JPanel panel;
    private JList selectedSolversGuiList;
    private JButton solveProblemFileWithButton;
    private JCheckBox algorithmNameCheckBox;
    private JCheckBox mapTitleCheckBox;
    private JCheckBox uniqueProblemIDCheckBox;
    private JCheckBox costFunctionCheckBox;
    private JCheckBox solutionCostCheckBox;
    private JCheckBox timeToSolveCheckBox;
    private JButton addSelectedSolversButton;
    private JButton removeSelectedSolversButton;
    private JCheckBox saveSolutionFileForCheckBox;

    List<String> solversStringList;
    List<String> selectedSolversStringList;

    public ProblemSolverVisual() {
        solversStringList = new ArrayList<>(GlobalSolvers.solverMap.keySet());
        selectedSolversStringList = new ArrayList<>();

        // Add all the solvers to the solver list
        solversGuiList.setListData(GlobalSolvers.solverMap.keySet().toArray());
        addSelectedSolversButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Add the selected solvers to the selected solvers list
                // Remove the selected solvers from the main list
                for (String solverString : (ArrayList<String>)solversGuiList.getSelectedValuesList()) {
                    selectedSolversStringList.add(solverString);
                    solversStringList.remove(solverString);
                }
                // Update the visual gui lists
                solversGuiList.setListData(solversStringList.toArray());
                selectedSolversGuiList.setListData(selectedSolversStringList.toArray());
            }
        });
        removeSelectedSolversButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Remove the selected solvers from the selected solvers list
                // Add the selected solvers to the main list
                for (String solverString : (ArrayList<String>)selectedSolversGuiList.getSelectedValuesList()) {
                    solversStringList.add(solverString);
                    selectedSolversStringList.remove(solverString);
                }
                // Update the visual gui lists
                solversGuiList.setListData(solversStringList.toArray());
                selectedSolversGuiList.setListData(selectedSolversStringList.toArray());
            }
        });
        solveProblemFileWithButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (!problemFileField.equals("")) {
                    // Process the checkboxes to determine output statistics
                    List<Statistics> outputStatistics = getStatistics();

                    // Generate a list of Solver objects based on the selected solvers
                    List<Solver> solvers = new ArrayList<Solver>();
                    for (String solverString : selectedSolversStringList) {
                        solvers.add(GlobalSolvers.solverMap.get(solverString));
                    }

                    // Generate a list of ProblemInstances based on the problem file
                    File problemFile = new File("src/problem_instances/" + problemFileField.getText() + ".prob");
                    List<ProblemInstance> problems = ProblemInstance.deserializeFile(problemFile);

                    StringBuilder outputStringBuilder = new StringBuilder();
                    // for each problem
                    for (ProblemInstance problem : problems) {
                        // for each solver
                        for (Solver solver : solvers) {
                            System.out.println("Solving" + problem + " with " + solver);
                            // solve the problem with the solver
                            System.out.println(solver.solve(problem));
                            // TODO: uncomment the getStatistics method in the Solver interface
                            // pull the requested statistics
                            //outputStringBuilder.append(solver.getStatistics(outputStatistics));
                            //outputStringBuilder.append("\n");
                            // TODO: if save solution is checked save the solution
                        }
                    }

                    // TODO: save statistics based on selection

                } else {
                    JOptionPane.showMessageDialog(panel, "Please input a file name");
                }
            }
        });
    }

    List<Statistics> getStatistics() {
        List<Statistics> stats = new ArrayList<>();
        if (algorithmNameCheckBox.isSelected())
            stats.add(Statistics.ALGORITHM_NAME);
        if (mapTitleCheckBox.isSelected())
            stats.add(Statistics.MAP_TITLE);
        if (uniqueProblemIDCheckBox.isSelected())
            stats.add(Statistics.UNIQUE_ID);
        if (costFunctionCheckBox.isSelected())
            stats.add(Statistics.COST_FUNCTION);
        if (solutionCostCheckBox.isSelected())
            stats.add(Statistics.SOLUTION_COST);
        if (timeToSolveCheckBox.isSelected())
            stats.add(Statistics.TIME_TO_SOLVE);
        return stats;
    }

    public static void main(String args[]) {
        ProblemSolverVisual visual = new ProblemSolverVisual();

        JFrame frame = new JFrame("ProblemSolverVisual");
        frame.getContentPane().add(visual.panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

}
