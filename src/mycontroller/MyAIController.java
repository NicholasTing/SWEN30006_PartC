package mycontroller;

import java.util.ArrayList;

import controller.CarController;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 *  Class providing an AI implementation of CarController to escape a maze
 *  Keeps an internal map (Map), checks Radar, and uses Maneuvers
 *  
 *  Difference to Design Notes:
 *  We decided to not use any timers as was in the original design because 
 *  it was a temporary solution as opposed to fixing the actual problem.
 *  We have decided to never use the three point turn and the u-turn.
 *  The u-turn is not used because there is no point, our car makes the right
 *  decisions to navigate a dead-end with more than 2 wide anyway.
 *  The three point turn is the same. 
 */
public class MyAIController extends CarController {

	private Car car;
	private Macro macro;
	private Radar radar;
	
	//Car health
	private float carPrevHealth;
	
	private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
	private WorldSpatial.Direction previousOrientation = null; // Keeps track of the previous state
	
	// Car Speed to move at
	final float MAX_SPEED = (float) 4.0;
	final float LEFT_SPEED = (float) 1.5;
	final float RIGHT_SPEED = (float) 0.8;
	
	final float LAVA_SPEED = (float) 5.0;

	final int SLOW_DISTANCE = 3;
	float targetSpeed = MAX_SPEED;

	// orientation of the car
	WorldSpatial.Direction orientation;
	
	// a coordinate to determine where the last left turn started
	// Coordinate to determine last left turn
	private Coordinate turningCoordinate;
	
	//Healing for the car
	private boolean isHealing = false;
	
	//Approaching lava
	private boolean approachingLava = false;

	/** 
	 * Constructor for MyAIController
	 * @param car the car
	 */
	public MyAIController(Car car) {
		super(car);
		this.car = car;
		this.radar = new Radar(this);
		setMacro(DriveStraight.class);
		previousOrientation = orientation;
		turningCoordinate = new Coordinate(0,0);
		carPrevHealth = car.getHealth();
	}

	/** 
	 * This changes the car's current manoeuvre to the manoeuvre class passed in as a parameter
	 * @param manoeuvreClass the manoeuvre you want to change to
	 */
	public <T extends Macro> void setMacro(Class<T> macroClass) {

		if (!macroClass.isInstance(macro)) {	
			try {
				if (macro != null)
					System.out.println("New manoeuvre: " + macroClass.getSimpleName());
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
		return isFollowingWall && !radar.isFollowingBoundary(orientation) && !(macro instanceof TurnRight);
	}
	
	/**
	 * Checks to see if there is a left turn available that the car will get to
	 * @param tilesAhead how many tiles ahead it's checking for a left turn
	 * @return true if there is a left turn
	 */
	private boolean checkLeftTurnAhead(int tilesAhead) {
		return isFollowingWall &&  radar.isLeftOpenAhead(tilesAhead, orientation) && !(macro instanceof TurnRight);
	}
	
	/**
	 * Checks to see if there is a left turn available that the car will get to
	 * @param tilesAhead how many tiles ahead it's checking for a left turn
	 * @return true if there is a left turn
	 */
	private boolean checkLavaAhead(int tilesAhead) {
		return isFollowingWall &&  radar.isLavaAhead(tilesAhead, orientation) && !(macro instanceof TurnRight);
	}
	
	/**
	 * Setting the speed of the car
	 */
	private void setSpeed() {
		
		if (approachingLava) {
			targetSpeed = LAVA_SPEED;
		}
		
		else if (macro instanceof TurnLeft || checkLeftTurnAhead(SLOW_DISTANCE)) {
			targetSpeed = LEFT_SPEED;
		}
		else if (macro instanceof TurnRight || radar.isDirectionBlocked(SLOW_DISTANCE, orientation)) {
			targetSpeed = RIGHT_SPEED;
		}
		
		else {
			targetSpeed = MAX_SPEED;
		}
	}
	
	private int carGoingInCirclesLava = 0;
	private boolean reversing = false;
	@Override
	/**
	 * Update method for the AI controller
	 * Governs all its behaviour every delta milliseconds
	 */
	public void update(float delta) {
		// gets orientation
		orientation = getOrientation();
		radar.updateMap();	
		checkOrientationChange();	
		
		boolean leftTurnAvailable = checkLeftTurn();
		Integer isDeadEnd = radar.isDeadEnd(orientation);
		boolean isFrontBlocked = radar.isBlockedAhead(orientation);
		
		//If it is on health trap, stop and heal for a bit.
		boolean isOnHealthTrap = radar.isHealthTrap(orientation);
		
		//Finish tile
		boolean travelledTheMap = radar.endTile();
		
		boolean isLavaAhead = radar.isLavaAhead(SLOW_DISTANCE, orientation);
		
		if (isHandlingDeadend()) {
			// If you're currently handling a dead end, keep doing what you were doing
		}
		
		else if(isLavaAhead) {
			car.applyForwardAcceleration();
		}
		
		else if(isOnHealthTrap && car.getHealth() < 100) {
			car.brake();
			car.applyReverseAcceleration();
			System.out.println("health trap");
			isHealing = true;
		}
		
		else if(car.getHealth() == 100 && isHealing) {
			car.applyForwardAcceleration();
			isHealing = false;
		}
		
		// if you're not in the coordinate where you last turned left, and you can turn left, then turn left
		else if ((!turningCoordinate.equals(getPositionCoords())) && leftTurnAvailable) {			
			setMacro(TurnLeft.class);
		}
		
		// if you shouldn't turn left, and you can go forward, then go forward
		else if(!isTurning() && !isFrontBlocked){
			setMacro(DriveStraight.class);
		}
		
		else if (!isTurning() && isDeadEnd != null && isDeadEnd == 1) {
			setMacro(ReverseOut.class); 
		}
		// if you can't turn left and the front is blocked ahead, turn right
		else if (!isTurning() && isFrontBlocked){
			isFollowingWall = true;
			radar.updateSectionStart();
			setMacro(TurnRight.class);
		}

		setSpeed();
		//actually do what you've chosen
		macro.update(delta);
	}
	
	/**
	 * Checks to see if the car is currently turning
	 * @return true if it is turning
	 */
	private boolean isTurning() {
		return (macro instanceof TurnLeft || macro instanceof TurnRight);
	}

	/**
	 * Checks whether the car's state has changed or not, if it has, set the previous orientation
	 * to the current orientation. Additionally, if it's turning left, take the turning coordinate
	 * to be the current coordinate
	 */
	private void checkOrientationChange() {
		if (previousOrientation != orientation) {
			if (macro instanceof TurnLeft) {
				turningCoordinate = getPositionCoords();
			}
			setMacro(DriveStraight.class);
			previousOrientation = orientation;
		}
	}
	
	/**
	 * Checks to see if the car is currently handling a dead end
	 * @return true if it is
	 */
	private boolean isHandlingDeadend() {
		return (macro instanceof ThreePointTurn || macro instanceof ReverseOut);
	}
	
	/**
	 * Gets the car's current position
	 * @return the car's coordinates
	 */
	public Coordinate getPositionCoords() {
		return new Coordinate(Math.round(car.getX()),Math.round(car.getY()));
	}
	
	/**
	 * reset the currently left turning coordinate so that it can be reused
	 */
	public void resetTurningCoords() {
		turningCoordinate = new Coordinate(0,0);
	}
	
	public Radar getRadar() {
		return radar;
	}
}
