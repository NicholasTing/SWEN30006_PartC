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
	
	// Check if the car is healing
	private boolean isHealing = false;
	
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
		setMacro(Forward.class);
		previousOrientation = orientation;
		turningCoordinate = new Coordinate(0,0);
		prevHealth = car.getHealth();
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
	 * @param tilesAhead how many tiles ahead it's checking for a left turn
	 * @return true if there is a left turn
	 */
	private boolean checkLeftTurnAhead(int tilesAhead) {
		return isFollowingWall &&  sensor.isLeftOpenAhead(tilesAhead, orientation) && !(macro instanceof RightTurn);
	}
	
//	/**
//	 * Checks to see if there is a lava ahead, the car will slow down
//	 * @param tilesAhead 
//	 * @return true if there is a left turn
//	 */
//	private boolean checkLavaAhead(int tilesAhead) {
//		return isFollowingWall &&  sensor.isLavaAhead(orientation));
//	}
//	
	/**
	 * Setting the speed of the car
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

	private float prevHealth;
	@Override
	/**
	 * Update method for the AI controller
	 * Relevant cases included inside this method.
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
		
		//boolean isLavaAhead = sensor.isLavaAhead(orientation);
		
		// Collected all the keys
		boolean allKeysCollected = collectedAllKeys();
		
		if(car.getHealth() < 80 && isOnHealthTrap) {
			
			isHealing = true;
			setMacro(Stop.class);
			System.out.println("Car is healing. Please wait.");
			
		}
		
		else if (isHandlingDeadend()) {
			// If you're currently handling a dead end, keep doing what you were doing
		}
		
		else if(car.getHealth() == 100 && isHealing) {
			isHealing = false;
			setMacro(Forward.class);
		}
		
		else if(car.getSpeed() == 0 && car.getHealth() < prevHealth) {
			if(macro instanceof Forward) {
				setMacro(Reverse.class);
			}
			
		}
		
		else if(allKeysCollected) {
			System.out.println("Done");
			sensor.exitMap();
			
		}
		
		// if you're not in the coordinate where you last turned left, and you can turn left, then turn left
		else if ((!turningCoordinate.equals(getCarCoords())) && leftTurnAvailable) {			
			setMacro(LeftTurn.class);
		}
		
		// if you shouldn't turn left, and you can go forward, then go forward
		else if(!isTurning() && !isFrontBlocked){
			setMacro(Forward.class);
		}
		
		else if (!isTurning() && isDeadEnd != null && isDeadEnd == 1) {
			setMacro(Reverse.class); 
		}
		// if you can't turn left and the front is blocked ahead, turn right
		else if (!isTurning() && isFrontBlocked){
			isFollowingWall = true;
			sensor.updateSectionStart();
			setMacro(RightTurn.class);
		}
	
		setSpeed();
		prevHealth = car.getHealth();
		//actually do what you've chosen
		macro.update(delta);
	}

	private boolean collectedAllKeys() {
		// TODO Auto-generated method stub
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
	 * Checks whether the car's state has changed or not, if it has, set the previous orientation
	 * to the current orientation. Additionally, if it's turning left, take the turning coordinate
	 * to be the current coordinate
	 * 
	 * Check whether the car state has changed 
	 * 
	 * If the car 
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
