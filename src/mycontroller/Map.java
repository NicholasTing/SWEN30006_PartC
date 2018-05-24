package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mycontroller.Tiles.TileType;
import utilities.Coordinate;
/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 *  Class for managing an internal map of the maze maintained by the AI controller
 *  The map is divided into sections, and visited/unvisited sections are recorded
 *  The class also has methods for calculating the path from one section to another
 *
 */
public class Map {
		
	private PathFinder pathFinder;
	private Sensor sensor;
	
	private Integer maxX = null;
	private Integer maxY = null;
	private Integer minX = null;
	private Integer minY = null;
	
	// Car's current position
	private Coordinate carCoords = null;
	// Car's position in the previous update
	// Used to tell if the car has moved to a new cell
	private Coordinate prevCoords = null;

	// Position of the start of a section loop (always next to a wall)
	private Coordinate startSectionCoords = null;

	private int currentSection = -1;
	private List<Tiles> visited;
	
	private Integer nextSection = null;
	
	// Maps coordinates to true if they are on the path to the exit and false otherwise
	private ArrayList<Coordinate> pathToExit = new ArrayList<Coordinate>();
	
	private ArrayList<Coordinate> deadEnds = new ArrayList<Coordinate>();
	
	private HashMap<Coordinate, Tiles> map = new HashMap<Coordinate, Tiles>();
	private HashMap<Coordinate, Tiles> prevPrintedMap;
	
	public Map(Sensor sensor) {
		this.sensor = sensor;
		this.pathFinder = new DijkstraPathFinder(this);
	}
	
	/**
	 *  Updates the internal map
	 *  This is called at the update method of the AI controller, through the radar
	 */
	public void updateMap() {
		
		updateCarInformation();
		
		// updates sections
		sensor.getView().forEach((k,v) -> {
			if (!map.containsKey(k)) {				
				Tiles t = new Tiles(k,v);
				calcSection(t);
				
				map.put(k, t);
				if (k.x >= 0 && k.y >= 0) {					
					if (maxX == null || k.x > maxX) {
						maxX = k.x;
					}
					if (minX == null || k.x < minX) {
						minX = k.x;
					}
					if (maxY == null || k.y > maxY) {
						maxY = k.y;
					}
					if (minY == null || k.y < minY) {
						minY = k.y;
					}
				}
			}
		});
	}
	
	/**
	 *  Updates the car's information
	 */
	private void updateCarInformation() {
		carCoords = sensor.getPositionCoords();
		
		// if the car has reached a new cell in this delta time frame
		if (!carCoords.equals(prevCoords)) {
			
			// if we're back at the start
			if (carCoords.equals(startSectionCoords)) {
				// Remove any deadends from the first time around
				deadEnds = new ArrayList<Coordinate>();
				
				// Find path to next section
				pathToExit = pathFinder.lowestCostExit(carCoords);
				
				// If there is no path, set all traps as passable, by setting the path as including all tiles on the map
				if (pathToExit.size()==0) {
					System.out.println("box");
					pathToExit=allMapCoordinates();
				}
				else {
					nextSection = map.get(pathToExit.get(0)).getSection();
				}
				sensor.getController().realign();
			}
			
			// if we've reached an exit (and are thus in a new section)
			if (map.containsKey(carCoords)) {
				Integer thisSection = map.get(carCoords).getSection();
				if (nextSection != null && thisSection != null && thisSection == nextSection) {
					
					// reset our start section to the exit node
					startSectionCoords = carCoords;
					System.out.println("new start section: "+ startSectionCoords);
					
					// reset the path to the exit to empty (so the car doesn't go back through the old traps on the previous path
					pathToExit = new ArrayList<Coordinate>();
					
					// Remove any deadends
					deadEnds = new ArrayList<Coordinate>();
					nextSection = null;
				}
			}
		}
		prevCoords = carCoords;
	}

	/**
	 * Returns all the coordinates of all tiles in an arraylist
	 * @return
	 */
	private ArrayList<Coordinate> allMapCoordinates() {
		ArrayList<Coordinate> tiles = new ArrayList<Coordinate>();
		for (int i=minX;i<maxX;i++) {
			for (int j = minY;j<maxY;j++) {
				tiles.add(new Coordinate(i,j));
			}
		}
		return tiles;
	}

	/**
	 * Calls the section-calculating algorithm below.
	 * @param t
	 * @return
	 */
	private Integer calcSection(Tiles t) {
		visited = new ArrayList<Tiles>();		
		return calcSection(t, 0, Integer.MAX_VALUE);
	}
	
	/**
	 * Recursive function that assigns each road tile a section number, so that two road tiles that are
	 * connected by other road tiles will have always have the same section number, and two road tiles that
	 * are not connected by other road tiles (i.e. separated by traps or walls) will not have the same section
	 * number.
	 * 
	 * This is called when there are new tiles within the car's internal map, meaning some tiles may change
	 * section if there is now a route of road tiles between them.
	 * @param t
	 * @param depth
	 * @param currSection
	 * @return
	 */
	private Integer calcSection(Tiles t, int depth, Integer currSection) {
		// If tile is a trap or wall, do not expand
		try {
			if (!t.getType().equals(TileType.ROAD)&&!t.getType().equals(TileType.START)&&!t.getType().equals(TileType.EXIT)) {
				return null;
			}
		}
		catch(Exception e) {
			System.out.println("Exception");
			return null;
		}
		
		// Keeps track of expanded nodes
		visited.add(t);
		
		// If the tile has not previously been given a section, set as Int.MAX
		int section;
		if (t.getSection() == null) {
			section = Integer.MAX_VALUE;
		} else {
			section = t.getSection();
		}
		
		// Sets the section to either the current section, or the previously allocated section.
		section = Math.min(section, currSection);
		
		// Expand node in all four directions
		Coordinate coords = t.getCoords();
		Coordinate[] toCheck = new Coordinate[] {
				new Coordinate(coords.x+1, coords.y),
				new Coordinate(coords.x-1, coords.y),
				new Coordinate(coords.x, coords.y+1),
				new Coordinate(coords.x, coords.y-1)
		};
		
		for (Coordinate c : toCheck) {
			
			try {
				if (map.containsKey(c)
						&& map.get(c).getType().equals(TileType.ROAD)
						&& !visited.contains(map.get(c))) {
					System.out.println("Coordinate checking 2");
					System.out.println(map.get(c).getType());
					section = Math.min(section, calcSection(map.get(c),depth+1, section));
				} else if (map.containsKey(c)
						&& visited.contains(map.get(c))) {
					section = Math.min(section, map.get(c).getSection());
				} 
			} catch(Exception e) {
				System.out.println("next");
			}
			
		}
		
		// Increment the next available section number, and allocate
		if (section == Integer.MAX_VALUE) {
			currentSection++;
			section = currentSection;
		}
		
		// Sets the section of the current tile
		t.setSection(section);
		return section;
		
	}
	
	/**
	 * Prints the map, if it has been updated since the last print
	 */
	@SuppressWarnings("unchecked")
	public void printMap() {
		if (prevPrintedMap != null && prevPrintedMap.equals(map)) {
			return;
		}
		
		Coordinate c;
		for (int j = maxY; j >= minY; j--) {
			for (int i = minX; i <= maxX; i++) {
				c = new Coordinate(i,j);
				String v;
				if (carCoords.equals(c)) {
					v = "C";
				}
				else if (map.containsKey(c) && map.get(c).getType().equals(TileType.ROAD)) {
					v = String.valueOf(map.get(c).getSection());
				} else {
					v = map.containsKey(c) ? map.get(c).toString() : " ";
				} 
				
				System.out.print(v + " ");
			}
			System.out.println();
		}
		System.out.println("\n");
		
		prevPrintedMap = (HashMap<Coordinate, Tiles>) map.clone();
	}

	/**
	 * Set our start section to current car position, if it has not yet been set
	 */
	public void updateSectionStart() {
		if (startSectionCoords == null) {
			startSectionCoords = carCoords;
		}		
	}
	
	public HashMap<Coordinate,Tiles> getMap() {
		return map;
	}

	public Integer getMaxX() {
		return maxX;
	}

	public Integer getMaxY() {
		return maxY;
	}

	public Integer getMinX() {
		return minX;
	}

	public Integer getMinY() {
		return minY;
	}
	
	public ArrayList<Coordinate> getPathToExit() {
		return pathToExit;
	}
	
	public ArrayList<Coordinate> getDeadEnds() {
		return deadEnds;
	}
	/**
	 * Checks to see if the current coordinate is able to be driven through
	 * @param coordinate
	 * @return
	 */
	public boolean isPassable(Coordinate coordinate) {
		return pathToExit.contains(coordinate);
	}
	

	
}
