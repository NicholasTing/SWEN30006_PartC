package mycontroller;

import java.util.ArrayList;
import utilities.Coordinate;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * Class that finds the best way to go to get out of a section
 */
public abstract interface PathFinder {

	/**
	 *  Calculates the lowestCostExit from the current section
	 *  Updates the pathToExit 
	 *  
	 * TO DO: 
	 * 1 Maps out the whole unexplored section of the map
	 *  
	 * @param from the coordinates to calculate the path from
	 * @return the coordinates of the destination tile in the new section
	 */
	public abstract ArrayList<Coordinate> lowestCostExit(Coordinate from);
	
	
}
