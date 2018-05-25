package mycontroller;

import controller.CarController;

import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * 
 * Class providing an AI implementation of CarController to escape a maze
 * Keeps an internal map (Map), checks Sensors to determine what is in its
 * surround locations, and uses Macros to move around.
 *  
 */
public class MyAIController extends CarController {

	private Car car;
	private Macro macro;
	private Sensor sensor;
	
	// This is initialized when the car sticks to a wall.
	private boolean isFollowingWall = false; 
	// Keeps track of the previous state
	private WorldSpatial.Direction previousOrientation = null; 
	
	// Car Speed to move at
	final float MAX_SPEED = (float) 4.0;
	final float LEFT_SPEED = (float) 1.5;
	final float RIGHT_SPEED = (float) 1.5;
	
	final float LAVA_SPEED = (float) 5.0;

	final int SLOW_DISTANCE = 3;
	float targetSpeed = MAX_SPEED;

	// orientation of the car
	WorldSpatial.Direction orientation;
	
	// Coordinate to determine the last left turn
	private Coordinate turningCoordinate;
	
	// Check if the car is approaching lava
	private boolean approachingLava = false;

	/** 
	 * Constructor for MyAIController
	 * @param car the car
	 */
	public MyAIController(Car car) {
		super(car);
		this.car = car;
		this.sensor = new Sensor(this);
		previousOrientation = orientation;
		turningCoordinate = new Coordinate(0,0);
		
		setMacro(Forward.class);
		
	}

	/**
	 * Sets the car's current macro to the macro class passed in as a parameter in relevant updates.
	 * 
	 * @param macroClass the macro you want to change to
	 */
	public <T extends Macro> void setMacro(Class<T> macroClass) {

		if (!macroClass.isInstance(macro)) {	
			try {
				macro = macroClass.getConstructor(this.getClass()).newInstance(this);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks to see if there is a left turn available
	 * @return true if there is a left turn available
	 */
	private boolean checkLeftTurn() {
		return isFollowingWall && !sensor.isFollowingWall(orientation) && !(macro instanceof RightTurn);
	}
	
	/**
	 * Checks to see if there is a left turn available that the car will get to
	 * 
	 * @param tilesAhead how many tiles ahead it's checking for a left turn
	 * @return true if there is a left turn
	 */
	private boolean checkLeftTurnAhead(int tilesAhead) {
		return isFollowingWall &&  sensor.isLeftOpenAhead(tilesAhead, orientation) && !(macro instanceof RightTurn);
	}
	
//	/**
//	 * Checks to see if there is a lava ahead, the car will slow down
//	 * @param tilesAhead 
//	 * @return true if there is lava ahead
//	 */
//	private boolean checkLavaAhead(int tilesAhead) {
//		return isFollowingWall &&  sensor.isLavaAhead(orientation));
//	}
//	
	/**
	 * Set speed of the car according to what is doing or about to do
	 */
	private void setSpeed() {
		
		if (approachingLava) {
			targetSpeed = LAVA_SPEED;
		}
		
		else if (macro instanceof LeftTurn || checkLeftTurnAhead(SLOW_DISTANCE)) {
			targetSpeed = LEFT_SPEED;
		}
		else if (macro instanceof RightTurn || sensor.isDirectionBlocked(SLOW_DISTANCE, orientation)) {
			targetSpeed = RIGHT_SPEED;
		}
		
		else {
			targetSpeed = MAX_SPEED;
		}
	}
	
	@Override
	/**
	 * Update method for the AI controller
	 * 
	 * According to specific scenarios that the car facem relevant updates will be called.
	 */
	public void update(float delta) {
		// gets orientation
		orientation = getOrientation();
		
		sensor.exploreMap();
		
		checkOrientationChange();	
		
		boolean leftTurnAvailable = checkLeftTurn();
		Integer isDeadEnd = sensor.isDeadEnd(orientation);
		boolean isFrontBlocked = sensor.isWallAhead(orientation);
		
		//If it is on health trap, stop and heal for a bit.
		boolean isOnHealthTrap = sensor.isHealthTrap(orientation);
		
		// Check if there is lava ahead
		//boolean isLavaAhead = sensor.isLavaAhead(orientation);
		
		// Collected all the keys
		boolean allKeysCollected = collectedAllKeys();
		
		// If the car health is less than 80, stop on a health trap to regenerate
		if(car.getHealth() < 80 && isOnHealthTrap) {
			setMacro(Stop.class);
			System.out.println("Car is healing. Please wait.");
		}
		
		else if (isHandlingDeadend()) {
			// If car is handling dead-end, continue.
		}
		
		// If the car has collected all the keys in the map, tries to exit the map.
		else if(allKeysCollected) {
			System.out.println("Done");
			sensor.exitMap();
		}
		
		// If there is a left turn available, turn left
		else if ((!turningCoordinate.equals(getCarCoords())) && leftTurnAvailable) {			
			setMacro(LeftTurn.class);
		}
		
		// If you aren't turning and you can move forward, drive forward
		else if(!isTurning() && !isFrontBlocked){
			setMacro(Forward.class);
		}
		
		// If its not turning and it is a dead end, reverse.
		else if (!isTurning() && isDeadEnd != null && isDeadEnd == 1) {
			setMacro(Reverse.class); 
		}
		
		// If it is  not turning and the front is blocked, turn right instead
		else if (!isTurning() && isFrontBlocked){
			isFollowingWall = true;
			sensor.updateSectionStart();
			setMacro(RightTurn.class);
		}
	
		setSpeed();
		
		// Calls macro update to apply what the car is supposed to do
		macro.update(delta);
	}

	/**
	 * Returns if the car has collected all the keys in the map
	 * @return
	 */
	private boolean collectedAllKeys() {
		
		return car.getKey() == 1;
	}

	/**
	 * Returns if the car is turning left or right
	 * 
	 * @return true if it is turning
	 */
	private boolean isTurning() {
		return (macro instanceof LeftTurn || macro instanceof RightTurn);
	}

	/**
	 * 
	 * Check whether the car state has changed from the previous orientation. If it has changed,
	 * update the car's current orientation. 
	 * 
	 */
	private void checkOrientationChange() {
		
		
		if (previousOrientation != orientation) {
			if (macro instanceof LeftTurn) {
				turningCoordinate = getCarCoords();
			}
			setMacro(Forward.class);
			previousOrientation = orientation;
		}
	}
	
	/**
	 * Returns true if the car is currently handling a dead-end in the form of Three Point Turn or Reverse
	 */
	private boolean isHandlingDeadend() {
		return (macro instanceof ThreePointTurn || macro instanceof Reverse);
	}
	
	/**
	 * Gets the car's current position in coordinate form.
	 * 
	 * @return the car's coordinates
	 */
	public Coordinate getCarCoords() {
		return new Coordinate(Math.round(car.getX()),Math.round(car.getY()));
	}
	
	/**
	 * Realigns the turning coordinate of the car
	 */
	public void realign() {
		turningCoordinate = new Coordinate(0,0);
	}

	public Sensor getSensor() {
		return sensor;
	}
}
