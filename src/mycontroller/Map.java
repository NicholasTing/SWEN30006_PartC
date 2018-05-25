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
 * This class manages the map of the maze which is constantly updated by the AI controller.
 * This class uses a path finder to help calculate the path from one section to another.
 * Also, the map is updated and the location of the keys are added in a hash map for it to be used
 * at a later stage.
 *
 */
public class Map {
		
	private PathFinder pathFinder;
	private Sensor sensor;
	
	private Integer maxX = null;
	private Integer maxY = null;
	private Integer minX = null;
	private Integer minY = null;
	
	// Initialise car current coordinate
	private Coordinate carCoords = null;
	
	// Car's position in the previous update
	private Coordinate prevCoords = null;

	// Position of the start of a section
	private Coordinate startSectionCoords = null;

	private int currentSection = -1;
	private List<Tile> visited;
	
	private Integer nextSection = null;
	
	// Maps coordinates on exitPath if they are included in the path to exit
	private ArrayList<Coordinate> exitPath = new ArrayList<Coordinate>();
	
	// Dead ends
	private ArrayList<Coordinate> deadEnds = new ArrayList<Coordinate>();
	
	// Location of the keys
	private HashMap<Integer, Coordinate> keysLocation = new HashMap<Integer, Coordinate>();

	private HashMap<Coordinate, Tile> map = new HashMap<Coordinate, Tile>();
	
	
	public Map(Sensor sensor) {
		this.sensor = sensor;
		this.pathFinder = new DijkstraPathFinder(this);
	}
	
	/**
	 *  Explores the map and is updated as the car travels around the map
	 *  Updated through the radar in the update method of myAIController
	 */
	public void exploreMap() {
		
		updateCarDetails();
		updateKeysLocation();
		
		// Updates sections
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
		
		// if the car has reached a new cell
		if (!carCoords.equals(prevCoords)) {
			
			// If car returns to the starting position of the map
			if (carCoords.equals(startSectionCoords)) {
			
				// Find the best path
				exitPath = pathFinder.calculateBestPath(carCoords);
				
				if (exitPath.size()==0) {
					exitPath=allMapCoordinates();
				}
				else {
					nextSection = map.get(exitPath.get(0)).getSection();
				}
				sensor.getController().realign();
			}
			
			// If the map contains the key
			if (map.containsKey(carCoords)) {
				Integer thisSection = map.get(carCoords).getSection();	
				
				//If this section is the same as the next section
				if (nextSection != null && thisSection != null && thisSection == nextSection) {
					
					startSectionCoords = carCoords;
					
					//New exit path created
					exitPath = new ArrayList<Coordinate>();
					
					// Remove any dead-end
					deadEnds = new ArrayList<Coordinate>();
					nextSection = null;
				}
			}
		}
		prevCoords = carCoords;
	}

	/**
	 * Returns all the coordinates of all tiles in an ArrayList of coordinates
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
	 * Calls the section calculating algorithm
	 * @param t
	 * @return
	 */
	private Integer calcSection(Tile t) {
		visited = new ArrayList<Tile>();		
		return calcCost(t, 0, Integer.MAX_VALUE);
	}
	
	/**
	 * 
	 * This function is used to calculate the cost and help detect infinite loops, i.e. an island wall
	 * so that the car does not cycle around and follow the same wall an infinite number of times.
	 * 
	 * @param t
	 * @param depth
	 * @param currSection
	 * @return
	 */
	private Integer calcCost(Tile t, int depth, Integer currSection) {
		
		// If tile is a  wall, return
		try {
			if (!t.getType().equals(TileType.ROAD)&&!t.getType().equals(TileType.START)&&!t.getType().equals(TileType.EXIT)) {
				return null;
			}
		}
		catch(Exception e) {
			
			return null;
		}
		
		// Visited tiles
		visited.add(t);
		
		// If the tile has not previously been given a section, set as Int.MAX
		int section;
		if (t.getSection() == null) {
			section = Integer.MAX_VALUE;
		} else {
			section = t.getSection();
		}
		
		// Sets the section
		section = Math.min(section, currSection);
		
		// Expand the node to up, down, left and right
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
		
		// Increment the next available section number
		if (section == Integer.MAX_VALUE) {
			currentSection++;
			section = currentSection;
		}
		
		// Sets the section of the current tile
		t.setSection(section);
		return section;
		
	}

	/**
	 * Set our start section to current car position
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
	
	// Returns the shortest path to exit according to the path finding algorithm used.
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
