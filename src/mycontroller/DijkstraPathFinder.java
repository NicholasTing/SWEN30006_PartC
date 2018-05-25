package mycontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import mycontroller.Tile.TileType;
import utilities.Coordinate;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * 
 * Dijkstra path finding algorithm with code sources online.
 */

public class DijkstraPathFinder implements PathFinder{
	
	private Map map;

	private static final int LAVA_COST = 999;
	
	// Creates a hash map for section explored in the map.
    private HashMap<Integer, Boolean> sectionExplored = new HashMap<Integer,Boolean>();
    
	public DijkstraPathFinder(Map map) {
		this.map = map;
	}
	
	/**
	 * If the map contains the coordinate, return the tile cost, else
	 * returns integer max value if the map does not contain the coordinate.
	 * 
	 * @param coordinate
	 * @return the cost
	 */
	private int getCostAtCoordinate(Coordinate coordinate) {
		if (!map.getMap().containsKey(coordinate)) {
			return Integer.MAX_VALUE;
		}
		TileType tileType = map.getMap().get(coordinate).getType();
		return Tile.getTileCost(tileType);
	}
	
	/**
	 * Calculates the lowest cost to exit from the coordinate given to us.
	 * Updates pathToExit along the way
	 * 
	 * @param from the coordinates to calculate the path from
	 * @return the coordinates of the destination tile in the new section
	 */
	public ArrayList<Coordinate> calculateBestPath(Coordinate from) {
		
		// Create the first node.
		CoordinateNode startNode = new CoordinateNode(from, null);
	
		// Initialise queue
		ArrayList<CoordinateNode> queue = new ArrayList<CoordinateNode>();

		queue.add(startNode);
		
		HashMap<Coordinate,Boolean> expanded = new HashMap<Coordinate,Boolean>();

		// Get the section that the car is in
		Integer carSection = map.getMap().get(from).getSection();
		
		sectionExplored.put(carSection, false);
		
		Integer minCost = null;
		Integer minCostReturn = null;
		ArrayList<Coordinate> exits = new ArrayList<Coordinate>();
		ArrayList<Coordinate> exitsReturn = new ArrayList<Coordinate>();

		// Apply Min-cost-search algorithm
		while (queue.size() > 0) {
			
			// Sorts the queue in ascending order
			Collections.sort(queue);
			
			// Return the lowest cost node
			CoordinateNode node = queue.get(0);
			queue.remove(0);
			
			// If the node is in a new and unexplored section
			if (map.getMap().containsKey(node.coordinate)) {
				Integer newSection = map.getMap().get(node.coordinate).getSection();
				if (newSection!=null && !newSection.equals(carSection)
						&& !sectionExplored.containsKey(newSection) && !isLavaTile(node)) {
					
					if (minCost == null) {
						minCost = node.cost;
					}
					
					if (node.cost == minCost) {
						getExitPath(node, exits);
					} else if (node.cost > minCost) {
						return exits;
					}
					
				} else if (newSection != null && !newSection.equals(carSection)
						&& sectionExplored.containsKey(newSection) && !sectionExplored.get(newSection)
						&& !isLavaTile(node)) {
					if (minCostReturn == null) {
						minCostReturn = node.cost;
					}
					
					if (node.cost == minCostReturn) {
						getExitPath(node, exitsReturn);
					}
					
				} else if (!(newSection != null && !newSection.equals(carSection)
						&& sectionExplored.containsKey(newSection) && sectionExplored.get(newSection)
						&& !isLavaTile(node))) {

					// Adds all the neighbours of the current node if it is valid to the queue
					int x = node.coordinate.x;
					int y = node.coordinate.y;
					enqueueIfValid(new CoordinateNode(x+1, y, node), queue, expanded);
					enqueueIfValid(new CoordinateNode(x-1, y, node), queue, expanded);
					enqueueIfValid(new CoordinateNode(x, y+1, node), queue, expanded);
					enqueueIfValid(new CoordinateNode(x, y-1, node), queue, expanded);
				}
			}
		}
		
		if (exits.isEmpty()) {
			
			sectionExplored.put(carSection, true);
			return exitsReturn;
		}
		
		return !exits.isEmpty() ? exits : exitsReturn;
		
	}
	
	/**
	 * Returns true if it is a lava tile. 
	 * 
	 * @param node
	 * @return
	 */
	private boolean isLavaTile(CoordinateNode node) {
		
		if (node.prev !=null) {
			
			// Look at the tile and previous tiles to check for lava
			if (node.prev.prev != null && node.prev.type != null && node.type != null && node.prev.prev.type != null)
				{
				
				// If the previous tile is a lava tile.
				if (node.prev.type.equals(Tile.TileType.LAVA))
				{
					return true;
				}
				
			}
		}
		return false;
	}

	/**
	 * Gets the optimum exit path for the car to leave the stage.
	 * 
	 * @param node the node
	 */
	public void getExitPath(CoordinateNode node, ArrayList<Coordinate> exits) {
		
		// Traverses from the end path to the start
		while (true) {
			Coordinate coordinate = node.coordinate;
			
			// Adds the coordinates nodes needed to the pathToExit
			if (!exits.contains(coordinate))
				exits.add(coordinate);
			
			// Make sure that the previous node is not null
			if (node.prev != null) {
				node = node.prev;
			}
			else {
				break;
			}
		}
		
	}
	
	/**
	 * Enqueues nodes in the lowest cost search queue if they are valid
	 * 
	 * @param node the node to be enqueued
	 * @param queue the queue to enqueue the node in
	 * @param expanded a 2d array of expanded coordinates
	 */
	private void enqueueIfValid(CoordinateNode node, ArrayList<CoordinateNode> queue, HashMap<Coordinate,Boolean> expanded) {
		int x = node.coordinate.x;
		int y = node.coordinate.y;
		
		// if the coordinate exists on the map
		if (x >= map.getMinX() && y >= map.getMinY() && x <= map.getMaxX() && y <= map.getMaxY()) {
			
			// if unexpanded
			if (!expanded.containsKey(node.coordinate)) {
				expanded.put(node.coordinate, true);
				
				// Enqueue the coordinate if it is not a wall
				if (map.getMap().containsKey(node.coordinate) && !node.type.equals(TileType.WALL)) {
					queue.add(node);
				}
			}
		}
	}

	
	/**
	 *  Class for storing nodes in a path
	 *  
	 *  Contains coordinates of the node, its previous node, and the cost of the node
	 *
	 */
	private class CoordinateNode implements Comparable<CoordinateNode> {
		private Coordinate coordinate;
		private CoordinateNode prev;
		private int cost;
		private Tile.TileType type;
		
		/**
		 * Constructor for CoordinateNode
		 * 
		 * @param coordinate 
		 * @param prev 
		 */
		private CoordinateNode(Coordinate coordinate, CoordinateNode prev) {
			this.coordinate = coordinate;
			this.prev = prev;
			this.type = map.getMap().get(coordinate).getType();
			
			// calculate the cost
			cost = getCostAtCoordinate(coordinate);
			if (prev !=null) {
				cost += prev.cost;
			}
			
		}
		
		/**
		 * Node constructor - CoordinateNode
		 * 
		 * @param x x position
		 * @param y y position
		 * @param prev previous node in path
		 */
		private CoordinateNode(int x, int y, CoordinateNode prev) {
			this.coordinate = new Coordinate(x,y);
			this.prev = prev;
			
			if (map.getMap().get(coordinate)!=null) {
			this.type = map.getMap().get(coordinate).getType();
			}
			setCost();
		}
		
		private void setCost() {
			
			// Calculates the cost
			cost = getCostAtCoordinate(coordinate);
			if (prev !=null) {
				cost += prev.cost;
				
				// Check the previous tiles
				if (prev.prev != null && prev.type != null && type != null && prev.prev.type != null)
					{
					// If the tile is made out of lava, add the cost.
					if (isLavaTile(this))
					{
						cost += LAVA_COST;
					}
					
				}
			}
			
		}
		
		/**
		 * compareTo function to compare nodes based on their cost.
		 */
		@Override
		public int compareTo(CoordinateNode other) {
			return cost - other.cost;
		}
	}
	
}
