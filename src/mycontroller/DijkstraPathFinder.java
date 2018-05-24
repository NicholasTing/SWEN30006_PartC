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
 * Dijkstra path finding algorithm
 *
 */

public class DijkstraPathFinder implements PathFinder{
	
	private Map map;

	private static final int LAVA_COST = 999;
	
    private HashMap<Integer, Boolean> sectionExplored = new HashMap<Integer,Boolean>();
    
	public DijkstraPathFinder(Map map) {
		this.map = map;
	}
	
	/**
	 * Get the cost of the tile at the coordinate given
	 * @param coordinate the coordinate
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
	 * Calculates the lowestCostExit from the current section
	 * Updates pathToExit
	 * 
	 * @param from the coordinates to calculate the path from
	 * @return the coordinates of the destination tile in the new section
	 */
	public ArrayList<Coordinate> lowestCostExit(Coordinate from) {
	
		// initialise a priority queue of CoordinateNodes
		ArrayList<CoordinateNode> queue = new ArrayList<CoordinateNode>();
				
		// add the first node in the path
		CoordinateNode first = new CoordinateNode(from, null);
		queue.add(first);
		
		HashMap<Coordinate,Boolean> expanded = new HashMap<Coordinate,Boolean>();
		
		// find the section that the car is in
		Integer carSection = map.getMap().get(from).getSection();
		
		sectionExplored.put(carSection, false);
		
		Integer minCost = null;
		Integer minCostReturn = null;
		ArrayList<Coordinate> exits = new ArrayList<Coordinate>();
		ArrayList<Coordinate> exitsReturn = new ArrayList<Coordinate>();

		// Apply Min-cost-search algorithm
		while (queue.size() > 0) {
			
			// sorts queue by cost (ascending order)
			Collections.sort(queue);
			
			// gets lowest cost node
			CoordinateNode node = queue.get(0);
			queue.remove(0);
			
			// If the given node is in a new and unexplored section
			if (map.getMap().containsKey(node.coordinate)) {
				Integer newSection = map.getMap().get(node.coordinate).getSection();
				if (newSection!=null && !newSection.equals(carSection)
						&& !sectionExplored.containsKey(newSection) && !isLavaTile(node)) {
					
					if (minCost == null) {
						minCost = node.cost;
					}
					
					if (node.cost == minCost) {
						getPathToExit(node, exits);
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
						getPathToExit(node, exitsReturn);
					}
					
				} else if (!(newSection != null && !newSection.equals(carSection)
						&& sectionExplored.containsKey(newSection) && sectionExplored.get(newSection)
						&& !isLavaTile(node))) {

					// Adds all neighbours of the current node to the queue
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
			System.out.println("returning backup");
			sectionExplored.put(carSection, true);
			return exitsReturn;
		}
		
		return !exits.isEmpty() ? exits : exitsReturn;
		
	}
	
	/**
	 *  Returns true if the node is the last node in a corner with grass in the middle
	 *  That is, an impassable corner due to grass
	 * @param node
	 * @return
	 */
	private boolean isLavaTile(CoordinateNode node) {
		
		if (node.prev !=null) {
			
			// looking at this tile and the previous two, for trap situations
			if (node.prev.prev != null && node.prev.type != null && node.type != null && node.prev.prev.type != null)
				{
				
				// If the last three nodes are a corner, and the middle one is grass
				// that is, grass needs to be turned on
				if (node.prev.type.equals(Tile.TileType.LAVA))
				{
					return true;

				}
				
			}
		}
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 *  Given a node in a path, sets the pathToExit as all the nodes in that path
	 *  All nodes in the path will now be passable (ie all traps can be traversed)
	 * @param node the node
	 */
	public void getPathToExit(CoordinateNode node, ArrayList<Coordinate> exits) {
		
		// steps backwards through the path from end to start
		while (true) {
			Coordinate coordinate = node.coordinate;
			
			// adds the coordinates of the node to the pathToExit
			if (!exits.contains(coordinate))
				exits.add(coordinate);
			
			// go to previous node
			if (node.prev != null) {
				node = node.prev;
			}
			else {
				break;
			}
		}
		
	}
	
	/**
	 *  Enqueues nodes in the least cost search queue if they are valid
	 * @param node the node to be enqueued
	 * @param queue the queue to enqueue the node in
	 * @param expanded a 2d array of expanded coordinates
	 */
	private void enqueueIfValid(CoordinateNode node, ArrayList<CoordinateNode> queue, HashMap<Coordinate,Boolean> expanded) {
		int x = node.coordinate.x;
		int y = node.coordinate.y;
		
		// if the coordinate is on the map
		if (x >= map.getMinX() && y >= map.getMinY() && x <= map.getMaxX() && y <= map.getMaxY()) {
			
			// if it is unexpanded
			if (!expanded.containsKey(node.coordinate)) {
				expanded.put(node.coordinate, true);
				
				// if it's not a wall piece, enqueue it
				if (map.getMap().containsKey(node.coordinate) && !node.type.equals(TileType.WALL)) {
					queue.add(node);
				}
			}
		}
	}

	
	/**
	 *  Class for storing nodes in a path
	 *  That is, holds the coordinates of the node, its previous node, and its cost
	 *
	 */
	private class CoordinateNode implements Comparable<CoordinateNode> {
		private Coordinate coordinate;
		private CoordinateNode prev;
		private int cost;
		private Tile.TileType type;
		
		/**
		 *  Constructor for CoordinateNode
		 * @param coordinate coordinate of node
		 * @param prev previous node in path
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
		 * Constructor for the Node
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
			// calculate the cost
			cost = getCostAtCoordinate(coordinate);
			if (prev !=null) {
				cost += prev.cost;
				
				// looking at this tile and the previous two, for trap situations
				if (prev.prev != null && prev.type != null && type != null && prev.prev.type != null)
					{
					// If the tils i made out of lava, add the cost.
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
