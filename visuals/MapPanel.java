package visuals;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import utilities.ProblemMap;
import constants.Terrain;

@SuppressWarnings("serial")
public class MapPanel extends JPanel {
	
	private static final int DEFAULT_SIZE = 10;
	
	private ProblemMap map;
	private int tileSize;
	
	public MapPanel(ProblemMap map) {
		this(map, DEFAULT_SIZE);
	}
	
	public MapPanel(ProblemMap map, int tileSize) {
		this.map = map;
		this.tileSize = tileSize;
		setSize(tileSize * map.getWidth(), tileSize * map.getHeight());
	}
	
	public void paintComponent(Graphics g) {
		String mc = map.getContent();
		int mapSize = mc.length();

		for(int i = 0; i < mapSize; i++) {
			g.setColor(tileColor(mc.charAt(i)));
			paintCell(g, i);
		}
	}
	
	public void paintCell(Graphics g, int index) {
		int bufferedWidth = map.getWidth() + 2;
		int x = -tileSize + tileSize * (index%bufferedWidth);
		int y = -tileSize + tileSize * (index/bufferedWidth);
		g.fillRect(x,y,tileSize,tileSize);
	}
	
	public Color tileColor(char tile) {
		if (tile == Terrain.PATH) return Color.LIGHT_GRAY;
		
		else if (tile == Terrain.TREE) return Color.GREEN;
		
		else if (tile == Terrain.WATER) return Color.BLUE;
		
		else return Color.BLACK;
	}
	
}
