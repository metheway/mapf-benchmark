package utilities;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import constants.Terrain;

/**
 * Class that represents a map on which a problem must be solved
 */
public class ProblemMap {
	
	private String mapType;
	private String mapTitle;
	private int height;
	private int width;
	private String content;

    /**
     * Constructor that creates a map such that
     * a character in the map is an obstacle with
     * probability obstacleProbability and given
     * dimensions
     * @param obstacleProbability probability that a character is an obstacle
     * @param height height of the map
     * @param width width of the maps
     */
    public ProblemMap(double obstacleProbability, int height, int width) {
		mapType = "rand";
		mapTitle = "random_map";
		this.height = height;
		this.width = width;
		generateMap(obstacleProbability);
		
	}

    /**
     * Parses a map from a file
     * @param mapFile the file to get the map from
     * @throws FileNotFoundException
     */
	public ProblemMap(File mapFile) throws FileNotFoundException{
		mapTitle = parseMapTitle(mapFile.getAbsolutePath());
		Scanner mapScanner = new Scanner(mapFile);
		mapScanner.next(); mapType = mapScanner.next();
		mapScanner.next(); height = mapScanner.nextInt();
		mapScanner.next(); width = mapScanner.nextInt();
		mapScanner.next();
		parse(mapScanner);
	}

	private String parseMapTitle(String path) {
		return path.substring(path.lastIndexOf('/') + 1);
	}

	// returns the map
	private void generateMap(double obstacleProbability) {
		StringBuilder processed = new StringBuilder();
        int maxObstacles = (int) Math.ceil(obstacleProbability * height * width);
        int numObstacles = 0;
		int bufferedLineWidth = width + 2;
		for(int i = 0; i < bufferedLineWidth; i++) processed.append(Terrain.OBSTACLE);
		for(int row = 0; row < height; row++) {
			processed.append(Terrain.OBSTACLE);
			for(int col = 0; col < width; col++) {
				if(Util.random.nextDouble() < obstacleProbability && numObstacles < maxObstacles) {
                    numObstacles++;
					processed.append(Terrain.OBSTACLE);
				} else {
					processed.append(Terrain.PATH);
				}
			}
			processed.append(Terrain.OBSTACLE);
		}
		for(int i = 0; i < bufferedLineWidth; i++) processed.append(Terrain.OBSTACLE);
		
		content = processed.toString();
	}

    /**
     * Returns the type of the map
     * @return the type of the map
     */
	public String getMapType() {
		return mapType;
	}

	/**
	 * Returns the title of the map
	 * @return Returns the title of the map
     */
	public String getMapTitle() { return mapTitle; }

    /**
     * Returns the height of the map
     * @return the height of the map
     */
	public int getHeight() {
		return height;
	}

    /**
     * Returns the width of the map
     * @return the width of the map
     */
    public int getWidth() {
		return width;
	}

    /**
     * Returns a string with the raw content
     * of the file this map was created from
     * @return a string with this map's content
     */
	public String getContent() {
		return content;
	}
	
	// assumes fields already assigned, map starts at next line
	private void parse(Scanner fileScanner) {
		StringBuilder processed = new StringBuilder();
		int bufferedWidth = width + 2;
		// adds a buffer on the top, bottom, and sides to avoid ArrayIndexOutOfBoundsException
		// when we add neighbors later
		for (int i = 0; i < bufferedWidth; i++) processed.append(Terrain.OBSTACLE);
		while(fileScanner.hasNext()) {
			String next = fileScanner.next();
			processed.append(Terrain.OBSTACLE);
			processed.append(next);
			processed.append(Terrain.OBSTACLE);
		}
		for (int i = 0; i < bufferedWidth; i++) processed.append(Terrain.OBSTACLE);
		content = processed.toString();
	}
	
	// returns whether the character is on the border of the map
	private boolean onBorder(int iterator, int lineWidth) {
		return (iterator%lineWidth == 0 || (iterator + 1)%lineWidth == 0);
	}

    /**
     * Print the map in a nice way
     */
	public void prettyPrintMap() {
		int bufferedWidth = width + 2;
		// skips the obstacles put in at the beginning for convenience
		for(int i = bufferedWidth; i < content.length() - bufferedWidth; i++) {
			if(!onBorder(i, bufferedWidth)) System.out.print(content.charAt(i));
			else if ((i+1)%bufferedWidth == 0) System.out.print("\n");
		}
	}

}
