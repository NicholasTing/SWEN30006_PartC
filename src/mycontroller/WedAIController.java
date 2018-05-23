package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.sun.corba.se.impl.orbutil.graph.Graph;

import java.util.Set;


import controller.CarController;
import tiles.HealthTrap;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.World;
import world.WorldSpatial;

public class WedAIController extends CarController {
	
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 2;
	
	private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false; 
	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
	
	// Car Speed to move at
	private final float CAR_SPEED = 3;
	// Car Speed to move at
		final float MAX_SPEED = (float) 2.4;
		final float LEFT_SPEED = (float) 1.5;
		final float RIGHT_SPEED = (float) 0.8;

		final int SLOW_DISTANCE = 3;
		float targetSpeed = MAX_SPEED;
	
		private Car car;
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	
	public WedAIController(Car car) {
		super(car);
		this.car = car;
	}
	
	Coordinate initialGuess;
	boolean notSouth = true;
	@Override
	public void update(float delta) {
		
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		
		checkStateChange();

		// If you are not following a wall initially, find a wall to stick to!
		if(!isFollowingWall){
			if(getSpeed() < CAR_SPEED){
				applyForwardAcceleration();
			}
			// Turn towards the north
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
				applyLeftTurn(getOrientation(),delta);
			}
			if(checkNorth(currentView)){
				// Turn right until we go back to east!
				if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
				}
				else{
					isFollowingWall = true;
				}
			}
		}
		// Once the car is already stuck to a wall, apply the following logic
		else{
			
			// Readjust the car if it is misaligned.
			readjust(lastTurnDirection,delta);
			
			if(isTurningRight){
				applyRightTurn(getOrientation(),delta);
			}
			else if(isTurningLeft){
				// Apply the left turn if you are not currently near a wall.
				if(!checkFollowingWall(getOrientation(),currentView)){
					applyLeftTurn(getOrientation(),delta);
				}
				else{
					isTurningLeft = false;
				}
			}
			// Try to determine whether or not the car is next to a wall.
			else if(checkFollowingWall(getOrientation(),currentView)){
				// Maintain some velocity
				if(getSpeed() < CAR_SPEED){
					applyForwardAcceleration();
				}
				// If there is wall ahead, turn right!
				if(checkWallAhead(getOrientation(),currentView)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					isTurningRight = true;				
					
				}

			}
			// This indicates that I can do a left turn if I am not turning right
			else{
				lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
				isTurningLeft = true;
			}
		}
		
		System.out.println("Goes here");
		Coordinate carCur = new Coordinate((int)car.getX(),(int)car.getY());
		HashMap<Coordinate, MapTile> map = findMap();
		ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
		Node node = new Node("Start",carCur);
		HashMap<String, Node> graph = new HashMap<String, Node>();
		ArrayList<Coordinate> test = new ArrayList<Coordinate>();
		
		for(Coordinate coords : test) {
			System.out.println(coords.toString());
		}
		System.out.println("Ends here");

	}
    
	public HashMap<Coordinate,MapTile> findMap() {
		HashMap<Coordinate,MapTile> map = World.getMap();
		Coordinate start = null;
		ArrayList<Coordinate> finish_coords = new ArrayList<Coordinate>();
		for(Coordinate coord : map.keySet()) {
			MapTile currentTile = map.get(coord);
			if(currentTile.getType() == MapTile.Type.START) {
				start = coord;
			}
			else if(currentTile.getType() == MapTile.Type.FINISH) {
				finish_coords.add(coord);
			}
		}
		findShortestPath(map, start, finish_coords.get(0));
		return map;
//		findShortestPath(map, start,finish_coords.get(0));
	}

	private void findShortestPath(HashMap<Coordinate, MapTile> wholeMap, Coordinate start, Coordinate coordinate) {
		// TODO Auto-generated method stub
		Coordinate carCurrCoord = new Coordinate((int)car.getX(), (int)car.getY());
		//Directions up, down ,left ,right
		//(0, +1), (0,-1), (-1, 0), (1,0 )
		
		ArrayList<Coordinate> path = new ArrayList<Coordinate>();
		while(!(wholeMap.get(carCurrCoord) instanceof HealthTrap)) {
			HashMap<Coordinate, Boolean> visited = new HashMap<Coordinate,Boolean>();
			HashMap<Coordinate, Integer> distance = new HashMap<Coordinate,Integer>();
			HashMap<Coordinate, Coordinate> previous = new HashMap<Coordinate,Coordinate>();
			
			int infinity = 9999;
			for(Coordinate coord : wholeMap.keySet()) {
				visited.put(coord, false);
				distance.put(coord, infinity);
				previous.put(coord, null);
			}
			
			distance.put(carCurrCoord,0);
			ArrayList<Coordinate> queue = new ArrayList<Coordinate>();
			
			queue.add(carCurrCoord);
			
			
			while(queue.size() > 0) {
				Coordinate currentCoord = queue.get(0);
				visited.put(currentCoord,true);
				Coordinate newEast = new Coordinate((int)car.getX()+1, (int)car.getY());
				Coordinate newWest = new Coordinate((int)car.getX()-1, (int)car.getY());
				Coordinate newNorth = new Coordinate((int)car.getX(), (int)car.getY()+1);
				Coordinate newSouth = new Coordinate((int)car.getX(), (int)car.getY()-1);
				
				if(!(wholeMap.get(newEast).getType() == MapTile.Type.WALL)) {
					if(visited.get(newEast) == false) {
						queue.add(newEast);
					}
					if(wholeMap.get(newEast) instanceof HealthTrap) {
						int distance1 = distance.get(newEast);
						if(distance.get(currentCoord) + 1 < distance1) {
							distance.put(newEast, distance.get(currentCoord) + 1);
							previous.put(newEast, currentCoord);
						}
					}
				}
				if(!(wholeMap.get(newWest).getType() == MapTile.Type.WALL)) {
					if(visited.get(newWest) == false) {
						queue.add(newWest);
					}
					if(wholeMap.get(newWest) instanceof HealthTrap) {
						int distance1 = distance.get(newWest);
						if(distance.get(currentCoord) + 1 < distance1) {
							distance.put(newWest, distance.get(currentCoord) + 1);
							previous.put(newWest, currentCoord);

						}
					}
				}
				if(!(wholeMap.get(newNorth).getType() == MapTile.Type.WALL)) {
					if(visited.get(newNorth) == false) {
						queue.add(newNorth);
					}
					if(wholeMap.get(newNorth) instanceof HealthTrap) {
						int distance1 = distance.get(newNorth);
						if(distance.get(currentCoord) + 1 < distance1) {
							distance.put(newNorth, distance.get(currentCoord) + 1);
							previous.put(newNorth, currentCoord);
						}
					}
				}
				if(!(wholeMap.get(newSouth).getType() == MapTile.Type.WALL)) {
					if(visited.get(newSouth) == false) {
						queue.add(newSouth);
					}
					if(wholeMap.get(newSouth) instanceof HealthTrap) {
						int distance1 = distance.get(newSouth);
						if(distance.get(currentCoord) + 1 < distance1) {
							distance.put(newSouth, distance.get(currentCoord) + 1);
							previous.put(newSouth, currentCoord);
						}
					}
				}
				
				queue.remove(0);
				
			}
			
//			Coordinate keys_location = null;
			
			
//			for(Coordinate coords : keys_found.keySet()) {
//				
//				if(keys_found.get(coords) == car.getKey()) {
//					keys_location = coords;
//					path.add(coords);
//					break;
//				}
//			}
			
			while(start != coordinate) {
				path.add(previous.get(start));
				start = previous.get(start);
			}
			
			
		}

	}

	/**
	 * Readjust the car to the orientation we are in.
	 * @param lastTurnDirection
	 * @param delta
	 */
	private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta) {
		if(lastTurnDirection != null){
			if(!isTurningRight && lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)){
				adjustRight(getOrientation(),delta);
			}
			else if(!isTurningLeft && lastTurnDirection.equals(WorldSpatial.RelativeDirection.LEFT)){
				adjustLeft(getOrientation(),delta);
			}
		}
		
	}
	
	/**
	 * Try to orient myself to a degree that I was supposed to be at if I am
	 * misaligned.
	 */
	private void adjustLeft(WorldSpatial.Direction orientation, float delta) {
		
		switch(orientation){
		case EAST:
			if(getAngle() > WorldSpatial.EAST_DEGREE_MIN+EAST_THRESHOLD){
				turnRight(delta);
			}
			break;
		case NORTH:
			if(getAngle() > WorldSpatial.NORTH_DEGREE){
				turnRight(delta);
			}
			break;
		case SOUTH:
			if(getAngle() > WorldSpatial.SOUTH_DEGREE){
				turnRight(delta);
			}
			break;
		case WEST:
			if(getAngle() > WorldSpatial.WEST_DEGREE){
				turnRight(delta);
			}
			break;
			
		default:
			break;
		}
		
	}

	private void adjustRight(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(getAngle() > WorldSpatial.SOUTH_DEGREE && getAngle() < WorldSpatial.EAST_DEGREE_MAX){
				turnLeft(delta);
			}
			break;
		case NORTH:
			if(getAngle() < WorldSpatial.NORTH_DEGREE){
				turnLeft(delta);
			}
			break;
		case SOUTH:
			if(getAngle() < WorldSpatial.SOUTH_DEGREE){
				turnLeft(delta);
			}
			break;
		case WEST:
			if(getAngle() < WorldSpatial.WEST_DEGREE){
				turnLeft(delta);
			}
			break;
			
		default:
			break;
		}
		
	}
	
	/**
	 * Checks whether the car's state has changed or not, stops turning if it
	 *  already has.
	 */
	private void checkStateChange() {
		if(previousState == null){
			previousState = getOrientation();
		}
		else{
			if(previousState != getOrientation()){
				if(isTurningLeft){
					isTurningLeft = false;
				}
				if(isTurningRight){
					isTurningRight = false;
				}
				previousState = getOrientation();
			}
		}
	}
	
	/**
	 * Turn the car counter clock wise (think of a compass going counter clock-wise)
	 */
	private void applyLeftTurn(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				turnLeft(delta);
			}
			break;
		case NORTH:
			if(!getOrientation().equals(WorldSpatial.Direction.WEST)){
				turnLeft(delta);
			}
			break;
		case SOUTH:
			if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
				turnLeft(delta);
			}
			break;
		case WEST:
			if(!getOrientation().equals(WorldSpatial.Direction.SOUTH)){
				turnLeft(delta);
			}
			break;
		default:
			break;
		
		}
		
	}
	
	/**
	 * Turn the car clock wise (think of a compass going clock-wise)
	 */
	private void applyRightTurn(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(!getOrientation().equals(WorldSpatial.Direction.SOUTH)){
				turnRight(delta);
			}
			break;
		case NORTH:
			if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
				turnRight(delta);
			}
			break;
		case SOUTH:
			if(!getOrientation().equals(WorldSpatial.Direction.WEST)){
				turnRight(delta);
			}
			break;
		case WEST:
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				turnRight(delta);
			}
			break;
		default:
			break;
		
		}
		
	}

	/**
	 * Check if you have a wall in front of you!
	 * @param orientation the orientation we are in based on WorldSpatial
	 * @param currentView what the car can currently see
	 * @return
	 */
	private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
		switch(orientation){
		case EAST:
			return checkEast(currentView);
		case NORTH:
			return checkNorth(currentView);
		case SOUTH:
			return checkSouth(currentView);
		case WEST:
			return checkWest(currentView);
		default:
			return false;
		
		}
	}
	
	/**
	 * Check if the wall is on your left hand side given your orientation
	 * @param orientation
	 * @param currentView
	 * @return
	 */
	private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		
		switch(orientation){
		case EAST:
			return checkNorth(currentView);
		case NORTH:
			return checkWest(currentView);
		case SOUTH:
			return checkEast(currentView);
		case WEST:
			return checkSouth(currentView);
		default:
			return false;
		}
		
	}
	

	/**
	 * Method below just iterates through the list and check in the correct coordinates.
	 * i.e. Given your current position is 10,10
	 * checkEast will check up to wallSensitivity amount of tiles to the right.
	 * checkWest will check up to wallSensitivity amount of tiles to the left.
	 * checkNorth will check up to wallSensitivity amount of tiles to the top.
	 * checkSouth will check up to wallSensitivity amount of tiles below.
	 */
	public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
		// Check tiles to my right
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to my left
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to towards the top
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles towards the bottom
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
}
