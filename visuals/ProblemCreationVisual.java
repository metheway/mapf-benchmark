package visuals;

import utilities.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jswiatek on 11/19/16.
 */
public class ProblemCreationVisual {
    private JPanel panel1;
    private JPanel panel2;
    private JButton addAgentButton;
    private JButton addRandomAgentsButton;
    private JTextField numRandomAgentsToAdd;
    private JButton saveProblemInstanceButton;
    private JTextField problemNameField;
    private JTextField startPositionField;
    private JTextField endPositionField;
    private JList agentDisplayList;
    private JButton clearSelectionButton;
    private JButton removeSelectedAgentsButton;
    private JButton reRandomizeRandomAgentsButton;
    private JButton removeAllAgentsButton;
    private JButton removeAllRandomAgentsButton;
    private JButton removeAllManuallySpecifiedButton;
    private JButton clearButton;

    private JSplitPane split;

    private ProblemInstance problemInstance;
    private ProblemMap problemMap;
    private Graph graph;
    private MapPanel mapPanel;
    private List<Agent> addedAgents;
    private List<Agent> randomAgents;
    private int numTotalRandomAgents;

    private Point startPoint;
    private Point endPoint;
    private boolean mousePressed;

    public ProblemCreationVisual(ProblemMap map, Connected con, int tileSize) {
        problemMap = map;
        graph = new Graph(con, map);
        addedAgents = new ArrayList<Agent>();
        randomAgents = new ArrayList<Agent>();
        mapPanel = new MapPanel(map, tileSize);
        split = new JSplitPane();
        split.setLeftComponent(mapPanel);
        split.setRightComponent(panel1);
        mapPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                mapPanel.paintComponent(mapPanel.getGraphics());
                update();
            }
        });
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                startPoint = e.getPoint();
            }
        });
        mapPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                update();
                endPoint = e.getPoint();
                if (startPoint != null) {
                    mapPanel.getGraphics().drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
                }
                // Update the start and end position fields
                endPositionField.setText(Integer.toString(mapPanel.getIndexOfPoint(endPoint)));
                startPositionField.setText(Integer.toString(mapPanel.getIndexOfPoint(startPoint)));
            }
        });
        addAgentButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Agent newAgent = new Agent(Integer.parseInt(startPositionField.getText()),
                        Integer.parseInt(endPositionField.getText()), addedAgents.size());
                // Clear the two text fields
                startPositionField.setText("");
                endPositionField.setText("");
                addedAgents.add(newAgent);
                update();
            }
        });
        addRandomAgentsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Increment the number of random addedAgents to add to the final problem
                numTotalRandomAgents += Integer.parseInt(numRandomAgentsToAdd.getText());
                System.out.println("Total Number of random addedAgents:" + numTotalRandomAgents);
                // update problem instance
                updateProblemInstance(Integer.parseInt(numRandomAgentsToAdd.getText()));
                // display new addedAgents
                updateAgentDisplay();
                // display new addedAgents
                drawAgents();
            }
        });
        saveProblemInstanceButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                updateProblemInstance();
                System.out.println(problemInstance.getAgents());
                // Serialize and save to file
                problemInstance.serialize(System.getProperty("user.dir") + "/src/problems/",
                        problemNameField.getText());
                System.out.println("Problem Saved as " + problemNameField.getText() + ".bin");
            }
        });
        clearSelectionButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Clear Selection
                agentDisplayList.clearSelection();
                // update display
                drawAgents();
            }
        });
        agentDisplayList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Display selected addedAgents
                drawAgents();
            }
        });
        removeSelectedAgentsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // TODO: fix this so it works
                int[] indices = agentDisplayList.getSelectedIndices();
                for (int i = indices.length-1; i >= 0; i--) {
                    Agent removedAgent = problemInstance.getAgents().remove(indices[i]);
                    // Remove the agent, whether it was specific or random
                    addedAgents.remove(removedAgent);
                    if(randomAgents.remove(removedAgent))
                        numTotalRandomAgents--;
                    System.out.println("removing agent at index: " + indices[i]);
                    System.out.println(problemInstance.getAgents());
                }
                update();
            }
        });
        reRandomizeRandomAgentsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Unassign all the random agents
                randomAgents.clear();
                // Update the problem instance, reassigning all the random agents
                updateProblemInstance(numTotalRandomAgents);
                updateAgentDisplay();
                drawAgents();
            }
        });
        removeAllAgentsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                addedAgents.clear();
                randomAgents.clear();
                numTotalRandomAgents = 0;
                update();
            }
        });
        removeAllRandomAgentsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                randomAgents.clear();
                numTotalRandomAgents = 0;
                update();
            }
        });
        removeAllManuallySpecifiedButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                addedAgents.clear();
                update();
            }
        });
    }

    void update() {
        mapPanel.paint(mapPanel.getGraphics());
        updateProblemInstance();
        updateAgentDisplay();
        drawAgents();
    }
    /**
     * Updates the agent display list with the new addedAgents
     */
    void updateAgentDisplay() {
        agentDisplayList.setListData(problemInstance.getAgents().toArray());
    }

    void updateProblemInstance() {
        // Create problem instance with 0 added random addedAgents
        updateProblemInstance(0);
    }

    void updateProblemInstance(int numRandom) {
        // Create a list of all the current determined agents
        List<Agent> allAgents = new ArrayList<Agent>();
        allAgents.addAll(addedAgents);
        allAgents.addAll(randomAgents);
        // Create ProblemInstance of all determined agents
        this.problemInstance = new ProblemInstance(graph, allAgents);
        // TODO: this method will hang if there isn't a likely configuration of agent positions and goals
        this.problemInstance.addRandomAgents(numRandom);
        // Determine the agents that were randomly generated
        List<Agent> newRandomAgents = new ArrayList<Agent>();
        newRandomAgents.addAll(problemInstance.getAgents());
        newRandomAgents.removeAll(allAgents);
        // update the gui's randomAgents so that if we add more random addedAgents,
        // we'll preserve the ones we already had
        this.randomAgents.addAll(newRandomAgents);
    }

    // TODO: discuss issue with agent's drawing over each other.
    // ie agent 1 starts in agent 2's goal
    void drawAgents() {
        // Clear to a blank map
        mapPanel.paint(mapPanel.getGraphics());
        // draw the selected addedAgents
        Graphics g = mapPanel.getGraphics();
        // If there is some selection
        if (agentDisplayList.getSelectedIndices().length != 0) {
            // Draw only the selected addedAgents
            for (Integer agentIndex : agentDisplayList.getSelectedIndices()) {
                drawAgent(g, problemInstance.getAgents().get(agentIndex));
            }
        } else {
            // Otherwise draw all the addedAgents
            for (Agent agent : problemInstance.getAgents()) {
                drawAgent(g, agent);
            }
        }
    }

    void drawAgent(Graphics g, Agent agent) {
        int startIndexInMap = agent.position();
        int goalIndexInMap = agent.goal();
        g.setColor(Color.RED);
        mapPanel.paintCell(g, startIndexInMap);
        // Color goal position blue
        g.setColor(Color.BLUE);
        mapPanel.paintCell(g, goalIndexInMap);
        // Draw line from start to goal
        Point startCellCenter = mapPanel.getCellCenter(startIndexInMap);
        Point endCellCenter = mapPanel.getCellCenter(goalIndexInMap);
        g.drawLine(startCellCenter.x, startCellCenter.y, endCellCenter.x, endCellCenter.y);
    }

    public static void main(String[] args) throws FileNotFoundException {
        ProblemMap problemMap = new ProblemMap(new File("src/maps/arena.map"));
        ProblemCreationVisual problemCreationVisual = new ProblemCreationVisual(problemMap, Connected.EIGHT, 10);
        JFrame frame = new JFrame("SolutionViewerVisual");
        frame.setSize(problemCreationVisual.mapPanel.getWidth(), problemCreationVisual.mapPanel.getHeight() + 20);
        frame.getContentPane().add(problemCreationVisual.split);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
