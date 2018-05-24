package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mycontroller.Tile.TileType;
import tiles.MapTile;
import utilities.Coordinate;
/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * 
 * Class for managing an internal map of the maze maintained by the AI controller
 * The map is divided into sections, and visited/unvisited sections are recorded
 * The class also has methods for calculating the path from one section to another
 *
 */
public class Map {
		
	private PathFinder pathFinder;
	private Sensor sensor;
	
	private Integer maxX = null;
	private Integer maxY = null;
	private Integer minX = null;
	private Integer minY = null;
	
	// Get the cars current coordinate
	private Coordinate carCoords = null;
	
	// Car's position in the previous update
	// Used to tell if the car has moved to a new cell
	private Coordinate prevCoords = null;

	// Position of the start of a section loop (always next to a wall)
	private Coordinate startSectionCoords = null;

	private int currentSection = -1;
	private List<Tile> visited;
	
	private Integer nextSection = null;
	
	// Maps coordinates to true if they are on the path to the exit and false otherwise
	private ArrayList<Coordinate> exitPath = new ArrayList<Coordinate>();
	
	private ArrayList<Coordinate> deadEnds = new ArrayList<Coordinate>();
	
	private HashMap<Integer, Coordinate> keysLocation = new HashMap<Integer, Coordinate>();
	
	private HashMap<Coordinate, Tile> map = new HashMap<Coordinate, Tile>();
	
	
	public Map(Sensor sensor) {
		this.sensor = sensor;
		this.pathFinder = new DijkstraPathFinder(this);
	}
	
	/**
	 *  Updates the internal map
	 *  This is called at the update method of the AI controller, through the radar
	 */
	public void exploreMap() {
		
		updateCarDetails();
		updateKeysLocation();
		
		// updates sections
		sensor.getView().forEach((k,v) -> {
			if (!map.containsKey(k)) {				
				Tile t = new Tile(k,v);
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
	 * While traversing the map, when keys are found, add it into the hash map of keys for find path
	 * their location in later stages.
	 */
	private void updateKeysLocation() {
		
		try {
			HashMap<Coordinate, MapTile> view = sensor.getView();
			
			if(view != null) {
				for(Coordinate coord : view.keySet()) {
					if(!(map.get(coord).getTile() instanceof MapTile)) {
						keysLocation.put(1, coord);
					}
				}
			}
		} catch(Exception e) {
			
		}
		
	}

	/**
	 *  Updates the car's details, including coordinates, orientation
	 */
	private void updateCarDetails() {
		carCoords = sensor.getPositionCoords();
		
		// if the car has reached a new cell in this delta time frame
		if (!carCoords.equals(prevCoords)) {
			
			// If the car returns to the starting position
			if (carCoords.equals(startSectionCoords)) {
				
				//Remove the dead ends that were present in the start.
				deadEnds = new ArrayList<Coordinate>();
				
				// Find the best path
				exitPath = pathFinder.calculateBestPath(carCoords);
				
				// If there is no path, set all traps as passable, by setting the path as including all tiles on the map
				if (exitPath.size()==0) {
					
					exitPath=allMapCoordinates();
				}
				else {
					nextSection = map.get(exitPath.get(0)).getSection();
				}
				sensor.getController().realign();
			}
			
			// if we've reached an exit (and are thus in a new section)
			if (map.containsKey(carCoords)) {
				Integer thisSection = map.get(carCoords).getSection();	
				if (nextSection != null && thisSection != null && thisSection == nextSection) {
					
					// reset our start section to the exit node
					startSectionCoords = carCoords;
					
					// reset the path to the exit to empty (so the car doesn't go back through the old traps on the previous path
					exitPath = new ArrayList<Coordinate>();
					
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
	private Integer calcSection(Tile t) {
		visited = new ArrayList<Tile>();		
		return calcCost(t, 0, Integer.MAX_VALUE);
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
	private Integer calcCost(Tile t, int depth, Integer currSection) {
		// If tile is a trap or wall, do not expand
		try {
			if (!t.getType().equals(TileType.ROAD)&&!t.getType().equals(TileType.START)&&!t.getType().equals(TileType.EXIT)) {
				return null;
			}
		}
		catch(Exception e) {
			
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
					
					section = Math.min(section, calcCost(map.get(c),depth+1, section));
				} else if (map.containsKey(c)
						&& visited.contains(map.get(c))) {
					section = Math.min(section, map.get(c).getSection());
				} 
			} catch(Exception e) {
				
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
	 * Set our start section to current car position, if it has not yet been set
	 */
	public void updateSectionStart() {
		if (startSectionCoords == null) {
			startSectionCoords = carCoords;
		}		
	}
	
	public HashMap<Coordinate,Tile> getMap() {
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
	
	// Returns the shortest path to exit according to the algorithm used.
	public ArrayList<Coordinate> getExitPath() {
		if(!keysLocation.isEmpty()) {
			returnPathToKeys();
		}
		return exitPath;
	}
	
	private void returnPathToKeys() {
		// Should implement Dijkstra or A* here but due to the lack of time, we couldnt.:(
	}

	// Returns the array list which contains dead end.
	public ArrayList<Coordinate> getDeadEnds() {
		return deadEnds;
	}
	
	/**
	 * Returns a boolean, checking whether the array list contains the coordinate which 
	 * is inside the shortest path to exit.
	 * 
	 * @param coordinate
	 * @return
	 */
	public boolean isPassable(Coordinate coordinate) {
		return exitPath.contains(coordinate);
	}
	
}
