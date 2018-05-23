package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;

import controller.CarController;
import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.World;
import world.WorldSpatial;

public class TeamAIController extends CarController {
	
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
	float MAX_SPEED = (float) 2.4;
	final float LEFT_SPEED = (float) 1.5;
	final float RIGHT_SPEED = (float) 0.8;

	final int SLOW_DISTANCE = 3;
	float targetSpeed = MAX_SPEED;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	private Car car;
	
	//One array list for keys found and another for health traps
	private HashMap<Coordinate, Integer> keys_found = new HashMap<Coordinate, Integer>();
	private ArrayList<Coordinate> health_trap = new ArrayList<Coordinate>();
	
	private HashMap<Coordinate, MapTile> wholeMap = new HashMap<Coordinate, MapTile>();
	
	
	private HashMap<Coordinate, MapTile> not_mapped_out = new HashMap<Coordinate,MapTile>();
	
	//Looking for health traps set to false
	private boolean lookingForHealth = false;
	
	public TeamAIController(Car car) {
		super(car);
		this.car = car;
	}
	
	Coordinate initialGuess;
	boolean notSouth = true;
	@Override
	public void update(float delta) {
		
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		
		boolean finishTile = isAtEnd(currentView);
		
		boolean isAtHealth = isAtHealth();
		
		checkStateChange();

//		 If you are not following a wall initially, find a wall to stick to!
		if (!lookingForHealth && car.getHealth() < 70) {
//			lookingForHealth = true;
			lookForHealthArea(currentView);
			Coordinate carCurrent = new Coordinate((int)car.getX(),(int)car.getY());
			Coordinate carCurrentNext = new Coordinate((int)car.getX()+1,(int)car.getY());
			carMoveTo(carCurrent,carCurrentNext,delta);
			System.out.println("Look for health area");
			applyRightTurn(getOrientation(),delta);
			applyRightTurn(getOrientation(),delta);
//			lookingForHealth = false;
		} 
		
		else if (isAtHealth) {
			
		}
		
		
//		if(finishTile) {
////			ArrayList<Coordinate> search_keys = driveToKeys();
//			System.out.println("Here end");
//			for (Coordinate coord : search_keys) {
//				System.out.println(coord.toString());
//			}
//		}
		
//		else if(lookingForHealth) {
//			System.out.println("Go to health area");
//			driveToHealthArea();
//		}
//		
		else if(!isFollowingWall){
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
		lookForKeys(currentView);
		addToMap(currentView);

	}
	
	private boolean isAtHealth() {
		// TODO Auto-generated method stub
		Coordinate current = new Coordinate((int)car.getX(),(int)car.getY());
		if(World.getMap().get(current).getType() == MapTile.Type.TRAP) {
			((HealthTrap) World.getMap().get(current)).getTrap().equals("health");
			MAX_SPEED = (float) 0.2;
			return true;
		}
		return false;
	}
	
	private void mapOutMap() {
		HashMap<Coordinate,MapTile> map = World.getMap();
		Coordinate start = new Coordinate((int)car.getX(), (int)car.getY());
		
		
	}
	
	private void carMoveTo(Coordinate c1, Coordinate c2, float delta) {
		
		
		int move = differenceBetweenCoords(c1,c2);
		switch(move){
			case 1:
				if(getAngle() > WorldSpatial.EAST_DEGREE_MIN+EAST_THRESHOLD){
					turnRight(delta);
				}
				break;
			case 3:
				if(getAngle() > WorldSpatial.NORTH_DEGREE){
					turnRight(delta);
				}
				break;
			case 4:
				if(getAngle() > WorldSpatial.SOUTH_DEGREE){
					turnRight(delta);
				}
				break;
			case 2:
				if(getAngle() > WorldSpatial.WEST_DEGREE){
					turnRight(delta);
				}
				break;
			
			default:
				car.applyForwardAcceleration();
				
		}
	}

	private void driveToHealthArea() {
		// TODO Auto-generated method stub
		Coordinate current = new Coordinate((int)car.getX(),(int)car.getY());
		if(World.getMap().get(current).getType() == MapTile.Type.TRAP) {
			((HealthTrap) World.getMap().get(current)).getTrap().equals("health");
			MAX_SPEED = (float) 0.2;
		}
	}

	private boolean isAtEnd(HashMap<Coordinate, MapTile> currentView) {
		// TODO Auto-generated method stub
		Coordinate current = new Coordinate((int)car.getX(),(int)car.getY());
		if(World.getMap().get(current).getType() == MapTile.Type.FINISH) {
			return true;
		}
		return false;
	}

	private void addToMap(HashMap<Coordinate, MapTile> currentView) {
		// TODO Auto-generated method stub
		for(Coordinate coordinate : currentView.keySet()) {
			if(!wholeMap.containsKey(coordinate)) {
				wholeMap.put(coordinate, currentView.get(coordinate));
			}
		}
	}

	private void lookForKeys(HashMap<Coordinate, MapTile> currentView) {
		// TODO Auto-generated method stub
		for (Coordinate name: currentView.keySet()){

            String key = name.toString();
            MapTile currentTile = currentView.get(name);
            if(currentTile instanceof LavaTrap) {
            		LavaTrap lavaTrap = (LavaTrap) currentTile;
            		for(int i = car.getKey(); i > 0; i--) {
            			if(lavaTrap.getKey() == i && !keys_found.containsKey(name)) {
                			System.out.println("Key found");
                			keys_found.put(name, lavaTrap.getKey());
                		}
            		}
            		
            }
            String value = currentView.get(name).toString();  
//            System.out.println(key + " " + value);  


		} 
		
	}
	
//	private ArrayList<Coordinate> driveToKeys() {
//		Coordinate carCurrCoord = new Coordinate((int)car.getX(), (int)car.getY());
//		//Directions up, down ,left ,right
//		//(0, +1), (0,-1), (-1, 0), (1,0 )
//		
//		ArrayList<Coordinate> path = new ArrayList<Coordinate>();
//		
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
			
			Coordinate keys_location = null;
			
			
			for(Coordinate coords : keys_found.keySet()) {
				
				if(keys_found.get(coords) == car.getKey()) {
					keys_location = coords;
					path.add(coords);
					break;
				}
			}
			
			while(keys_location != carCurrCoord) {
				path.add(previous.get(keys_location));
				keys_location = previous.get(keys_location);
			}
			
			return path;
			
			
		}
		return path;
//	}
	
	
	private void lookForHealthArea(HashMap<Coordinate, MapTile> currentView) {
		
		Coordinate carCurrentCoord = new Coordinate((int)car.getX(),(int)car.getY());
		
		
		for (Coordinate name: currentView.keySet()){

            String key = name.toString();
            MapTile currentTile = currentView.get(name);
            if(currentTile instanceof HealthTrap) {
            		
        			System.out.println("Health Traps found");
        			
				health_trap.add(name);
            	}
           
		}
		
		
	}
	
	public int differenceBetweenCoords(Coordinate coord1, Coordinate coord2) {
		int x_diff,y_diff;
		
		
		x_diff = coord1.x - coord2.x;
		y_diff = coord1.y - coord2.y;
		
		if(x_diff == 0 && y_diff == 0) {
			return 0;
		}
		
		else if(x_diff == 0 && y_diff > 0){
			return 1;
		}
		
		else if(x_diff == 0 && y_diff < 0) {
			return 3;
		}
		
		else if(x_diff > 0 && y_diff == 0) {
			return 4;
		}
		
		else if(x_diff < 0 && y_diff == 0) {
			return 2;
		}
		return 0;
	}
	
	private void healCar() {
		if(car.getHealth() < 50) {
			
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
