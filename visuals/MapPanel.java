package visuals;

import java.awt.*;

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
		int x = tileSize * (index%bufferedWidth);
		int y = tileSize * (index/bufferedWidth);
		System.out.println("Index: " + index + ", x: " + x + ", y: " + y + ", buffered width: " + bufferedWidth);
		g.fillRect(x,y,tileSize,tileSize);
	}

	public Point getCellCenter(int index) {
		int bufferedWidth = map.getWidth() + 2;
		int x = tileSize * (index%bufferedWidth);
		int y = tileSize * (index/bufferedWidth);
		return new Point(x + tileSize/2, y + tileSize/2);
	}

	public int getIndexOfPoint(Point point) {
		int adjustedX = point.x - (point.x % tileSize);
		int adjustedY = point.y - (point.y % tileSize);
		int bufferedWidth = map.getWidth() + 2;
		int index = (adjustedX/tileSize) + ((adjustedY/tileSize)*bufferedWidth);
		return index;
	}
	
	public Color tileColor(char tile) {
		if (tile == Terrain.PATH) return Color.LIGHT_GRAY;
		
		else if (tile == Terrain.TREE) return Color.GREEN;
		
		else if (tile == Terrain.WATER) return Color.BLUE;
		
		else return Color.BLACK;
	}
}
