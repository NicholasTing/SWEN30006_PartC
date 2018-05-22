package mycontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import mycontroller.TileWrap.TileType;
import utilities.Coordinate;

/**
 * Project Part C
 * Group 105
 * Andrew Roche (638338), David Barrell (520704), Hugh Edwards (584183)
 * Software Modelling and Design - University of Melbourne 2017
 * Class that finds the best way to go to get out of a section
 */
public class PathFinder {
	
	private Map map;
	
	private static final int MUD_OVERLOAD_COST = 999;

	private static final int GRASS_CORNER_COST = 999;
	
    private HashMap<Integer, Boolean> sectionExplored = new HashMap<Integer,Boolean>();
    
	public PathFinder(Map map) {
		this.map = map;
	}
	
	/**
	 *  Returns the cost of the tile at a given coordinate
	 * @param coordinate the coordinate
	 * @return the cost
	 */
	private int getCostAtCoordinate(Coordinate coordinate) {
		if (!map.getMap().containsKey(coordinate)) {
			return Integer.MAX_VALUE;
		}
		TileType tileType = map.getMap().get(coordinate).getType();
		return TileWrap.getTileCost(tileType);
	}
	
	/**
	 *  Calculates the lowestCostExit from the current section
	 *  Updates the pathToExit (all traps on the path are treated as passable by the car)
	 *  
	 * TO DO: 
	 * 1 	needs to deal with situation where the only exit is over 3 tiles of traps
	 * 2	need to deal with impassable combinations (ie can't turn on grass, or some shit)
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
						&& !sectionExplored.containsKey(newSection) && !grassCorner(node)) {
					
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
						&& !grassCorner(node)) {
					if (minCostReturn == null) {
						minCostReturn = node.cost;
					}
					
					if (node.cost == minCostReturn) {
						getPathToExit(node, exitsReturn);
					}
					
				} else if (!(newSection != null && !newSection.equals(carSection)
						&& sectionExplored.containsKey(newSection) && sectionExplored.get(newSection)
						&& !grassCorner(node))) {

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
	private boolean grassCorner(CoordinateNode node) {
		
		if (node.prev !=null) {
			
			// looking at this tile and the previous two, for trap situations
			if (node.prev.prev != null && node.prev.type != null && node.type != null && node.prev.prev.type != null)
				{
				
				// If the last three nodes are a corner, and the middle one is grass
				// that is, grass needs to be turned on
				if (node.prev.type.equals(TileWrap.TileType.LAVA)&&isCorner(node, node.prev, node.prev.prev))
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
	private void getPathToExit(CoordinateNode node, ArrayList<Coordinate> exits) {
		
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
		private TileWrap.TileType type;
		
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
		 *  Constructor for CoordinateNode
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
					
					// If 3 muds in a row, impassable
					if (prev.type.equals(TileWrap.TileType.MUD) && type.equals(prev.type)&& type.equals(prev.prev.type)) {
						cost += MUD_OVERLOAD_COST;
					}
					// If the last three nodes are a corner, and the middle one is grass
					// that is, grass needs to be turned on
					if (grassCorner(this))
					{
						cost += GRASS_CORNER_COST;
					}
					
				}
			}
			
		}
		
		

		/**
		 * CoordinateNodes are compared (ie sorted) based on their cost
		 */
		@Override
		public int compareTo(CoordinateNode other) {
			return cost - other.cost;
		}
	}
	/** 
	 *  Returns true if the three coordinate nodes form a corner
	 * @param node1
	 * @param node2
	 * @param node3
	 * @return
	 */
	private boolean isCorner(CoordinateNode node1, CoordinateNode node2, CoordinateNode node3) {
		
		// coordinates are all in a line
		if ((node2.coordinate.x==node1.coordinate.x && node3.coordinate.x==node1.coordinate.x) ||
				(node2.coordinate.y==node1.coordinate.y && node3.coordinate.y==node1.coordinate.y))
			{
			return false;
			}
		return true;
	}
}
