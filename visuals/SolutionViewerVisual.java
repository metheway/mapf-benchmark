package visuals;

import constants.CostFunction;
import solvers.astar.MultiAgentAStar;
import solvers.*;
import solvers.states.MultiAgentState;
import solvers.states.SingleAgentState;
import utilities.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;

import utilities.ProblemMap;

/**
 * Created by maxgray on 4/17/16.
 */
public class SolutionViewerVisual {
    private JPanel panel1;
    private JButton playButton;
    private JButton pauseButton;
    private MapPanel mapPanel;
    private JPanel panel2;
    private JButton addAgentButton;
    private JButton resetButton;
    private JButton stepForwardButton;
    private JButton stepBackwardButton;
    private JSlider slider1;
    private JSlider slider2;
    private JSplitPane split;

    private final int DEFAULT_STEP_SIZE = 1;
    private final int DEFAULT_STEP_RATE = 75;

    private Path path;
    private int timeStep;
    private int stepSize;
    private int stepRate;
    private ProblemInstance problemInstance;

    public SolutionViewerVisual(int tileSize, ProblemInstance problemInstance, Path path) {
        mapPanel = new MapPanel(problemInstance.getMap(), tileSize);
        timeStep = 0;
        slider1.setValue(DEFAULT_STEP_SIZE);
        stepSize = slider1.getValue();
        // TODO update this with slider
        slider2.setValue(DEFAULT_STEP_RATE);
        stepRate = slider2.getValue();
        this.problemInstance = problemInstance;
        this.path = path;
        split = new JSplitPane();
        split.setLeftComponent(mapPanel);
        split.setRightComponent(panel1);
        mapPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                waitFor(1000);
                mapPanel.paintComponent(mapPanel.getGraphics());
                // Draw agents for the first time
                updateMap();
            }
        });
        playButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                while (timeStep < path.size() - stepSize) {
                    timeStep += stepSize;
                    updateMap();
                    waitFor(stepRate);
                }
                timeStep = path.size() - 1;
                updateMap();
            }
        });
        resetButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                timeStep = 0;
                updateMap();
            }
        });
        stepForwardButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // TODO: Fill with relevant code
                if (timeStep < path.size()-stepSize)
                    timeStep += stepSize;
                else
                    timeStep = path.size() - 1;
                updateMap();
            }
        });
        stepBackwardButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (timeStep - stepSize >= 0)
                    timeStep -= stepSize;
                else
                    timeStep = 0;
                updateMap();
            }
        });
        slider1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                stepSize = slider1.getValue();
            }
        });
        slider2.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                stepRate = slider2.getValue();
            }
        });
    }

    public static void main(String[] args) throws FileNotFoundException {
        ProblemMap problemMap = new ProblemMap(new File("src/maps/arena.map"));
        Graph graph = new Graph(Connected.EIGHT, problemMap);
        ProblemInstance problemInstance = new ProblemInstance(graph, 2);
        Solver solver = new MultiAgentAStar(CostFunction.SUM_OF_COSTS);
//        if (solver.solve(problemInstance)) {
//            System.out.println("Successfully Solved");
//        } else {
//            System.out.println("Failure: not solved");
//        }

//        graph = new Graph(Connected.EIGHT, new ProblemMap(new File("src/maps/arena.map")));
//        problemInstance = new ProblemInstance(graph, new File("src/problems/test_problem.bin"));
        problemInstance = new ProblemInstance(new File("src/problems/test1.prob"));
        solver = new MultiAgentAStar(CostFunction.SUM_OF_COSTS);
        if (solver.solve(problemInstance)) {
            System.out.println("Successfully Solved");
        } else {
            System.out.println("Failure: not solved");
        }

        SolutionViewerVisual solutionViewerVisual = new SolutionViewerVisual(10, problemInstance, solver.getPath());
        JFrame frame = new JFrame("SolutionViewerVisual");
        frame.setSize(solutionViewerVisual.mapPanel.getWidth(), solutionViewerVisual.mapPanel.getHeight() + 20);
        frame.getContentPane().add(solutionViewerVisual.split);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        solutionViewerVisual.updateMap();
    }


    public void updateMap() {
        path.get(timeStep).printIndices();
        // Refresh map to color over all the old agent positions
        mapPanel.paint(mapPanel.getGraphics());
        // Paint all the new agent positions
        Graphics g = mapPanel.getGraphics();
        for (SingleAgentState singleAgentState : ((MultiAgentState) path.get(timeStep)).getSingleAgentStates()) {
            int index = singleAgentState.coordinate().getNode().getIndexInMap();
            if (singleAgentState.goalTest(problemInstance)) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.RED);
            }
            mapPanel.paintCell(g, index);
        }
    }

    private void waitFor(long t) {
        long ctime = System.currentTimeMillis();
        while (System.currentTimeMillis() < ctime + t) {}
    }


}
