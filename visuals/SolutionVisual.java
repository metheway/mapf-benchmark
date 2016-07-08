package visuals;

import utilities.ProblemMap;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by maxgray on 4/17/16.
 */
public class SolutionVisual {
    private JPanel panel1;
    private JButton button1;
    private JButton button2;
    private MapPanel mapPanel;
    private JPanel panel2;

    public SolutionVisual() {
        mapPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                waitFor(1000);
                mapPanel.paintComponent(mapPanel.getGraphics());
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SolutionVisual");
        frame.setContentPane(new SolutionVisual().panel1);
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void createUIComponents() throws FileNotFoundException {
        mapPanel = new MapPanel(new ProblemMap(new File("MAPF/src/maps/arena.map")));
    }

    private void waitFor(long t) {
        long ctime = System.currentTimeMillis();
        while (System.currentTimeMillis() < ctime + t) {}
    }


}
