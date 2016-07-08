package utilities;

import java.util.HashSet;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Set;

import constants.Terrain;

public class ProblemMap {
	
	private String mapType;
	private int height;
	private int width;
	private String content;
	private Random rand;
	
	// generates a random map with a probability of obstacles occurring
	public ProblemMap(double obstacleProbability, int height, int width) {
		rand = new Random();
		mapType = "rand";
		this.height = height;
		this.width = width;
		generateMap(obstacleProbability);
		
	}

	// creates a map from a benchmark file
	public ProblemMap(File mapFile) throws FileNotFoundException{
		Scanner mapScanner = new Scanner(mapFile);
		mapScanner.next(); mapType = mapScanner.next();
		mapScanner.next(); height = mapScanner.nextInt();
		mapScanner.next(); width = mapScanner.nextInt();
		mapScanner.next();
		parse(mapScanner);
	}
	
	public ProblemMap(String mapContent, int width, int height) {
		content = mapContent;
		this.width = width;
		this.height = height;
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
				if(rand.nextDouble() < obstacleProbability && numObstacles < maxObstacles) {
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

	// returns the type of the map
	public String getMapType() {
		return mapType;
	}
	
	// returns the height of the map
	public int getHeight() {
		return height;
	}
	
	// returns the width of the map
	public int getWidth() {
		return width;
	}
	
	// returns the content of the map (including buffer)
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
	
	// prints the original map
	public void prettyPrintMap() {
		int bufferedWidth = width + 2;
		// skips the obstacles put in at the beginning for convenience
		for(int i = bufferedWidth; i < content.length() - bufferedWidth; i++) {
			if(!onBorder(i, bufferedWidth)) System.out.print(content.charAt(i));
			else if ((i+1)%bufferedWidth == 0) System.out.print("\n");
		}
	}

}
