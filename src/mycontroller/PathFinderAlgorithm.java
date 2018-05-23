package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;

import tiles.MapTile;
import utilities.Coordinate;

public class PathFinderAlgorithm {
	
	private ArrayList<Coordinate> exit_tiles = new ArrayList<Coordinate>();

	public PathFinderAlgorithm(HashMap<Coordinate, MapTile> world) {
		// TODO Auto-generated constructor stub
		for (Coordinate name: world.keySet()){
			
			if(world.get(name).getType() == MapTile.Type.FINISH) {
				System.out.println("EXIT FOUND");
				exit_tiles.add(name);
			}
			if(world.get(name).getType() == MapTile.Type.START) {
				System.out.println("START TILE");
			}

		}
	}
	
	

	
}
