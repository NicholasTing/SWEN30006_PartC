package mycontroller;

import java.util.ArrayList;
import utilities.Coordinate;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * 
 * An interface PathFinder which implements strategy patterns, where there are multiple
 * different path finding strategies including Dijkstra and A Star. 
 */
public abstract interface PathFinder {

	/**
	 * Calculates the lowestCostExit from the current section
	 * Updates the pathToExit 
	 *  
	 * By using the currentView of the car, explores the map and draws its own map
	 * Updates the path to exit and then applies its specific path finding algorithm to find the 
	 * lowest Cost to exit.
	 *
	 * @param from the coordinate you want to calculate the shortest path from
	 * @return the coordinates of the destination tile
	 */
	public abstract ArrayList<Coordinate> calculateBestPath(Coordinate from);
	
	
}
