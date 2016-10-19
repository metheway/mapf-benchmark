package tests;

import visuals.MapPanel;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.*;

import utilities.ProblemMap;
import visuals.SolverGUI;

public class GUITest {
	
	public static void main(String[] args) throws FileNotFoundException {
//		testMapPanel();
        SolverGUI solverGUI = new SolverGUI();
	}
	
	public static void testMapPanel() throws FileNotFoundException {
		JFrame mapFrame = new JFrame();
		File mapFile = new File("src/maps/arena.map");
		ProblemMap map = new ProblemMap(mapFile);
		MapPanel panel = new MapPanel(map);
		mapFrame.setSize(panel.getWidth(), panel.getHeight() + 20);
		mapFrame.setVisible(true);
		mapFrame.getContentPane().add(panel);
		panel.paint(panel.getGraphics());
	}
	
}
