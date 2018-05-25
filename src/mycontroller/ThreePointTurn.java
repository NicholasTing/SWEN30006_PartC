/**
 * 
 */
package mycontroller;

import world.WorldSpatial;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * 
 * Class that provides the macro for the car to perform a three point turn
 */
public class ThreePointTurn extends Macro {

	private boolean turning = true;
	private boolean forward = false;
	private boolean moveForward = false;
	private Sensor sensor;
	private WorldSpatial.Direction initOrientation;
	
	private static final int CAR_SPEED = 1;
	
	public ThreePointTurn(MyAIController controller) {
		super(controller);
		this.sensor = controller.getSensor();
	}

	/**
	 * Updates the car to do the right turn given the point at which
	 * it is in with respects to the current macro
	 */
	@Override
	public void update(float delta) {
		if (initOrientation == null) {
			initOrientation = controller.getOrientation();
		}
		
		boolean leftBlocked = sensor.isDirectionBlocked(2, WorldSpatial.RelativeDirection.LEFT);
		boolean rightBlocked = sensor.isDirectionBlocked(2, WorldSpatial.RelativeDirection.RIGHT);
		
		if (turning) {
			turn(delta);
		} 
		
		else if (forward) {
			controller.realign();
			controller.setMacro(Forward.class);
		} 
		
		else if (leftBlocked && rightBlocked) {
			if (!sensor.getMap().getDeadEnds().contains(controller.getCarCoords()))
				sensor.getMap().getDeadEnds().add(controller.getCarCoords());
			if (controller.getSpeed() < CAR_SPEED)
				controller.applyForwardAcceleration();
			
		} 
		else {
			leftTurn(delta);
		}
		
	}
	
	/**
	 * Checks to see if the front of the car is empty
	 * 
	 * @return
	 */
	private boolean clearFront() {
		return sensor.isDirectionBlocked(1, controller.getOrientation());
	}
	
	/**
	 * Checks if the area behind the car is empty
	 * 
	 * @return
	 */
	private boolean clearRear() {
		
		switch(controller.getOrientation()){
		case EAST:
			return sensor.isDirectionBlocked(1, WorldSpatial.Direction.WEST);
		case NORTH:
			return sensor.isDirectionBlocked(1, WorldSpatial.Direction.SOUTH);
		case SOUTH:
			return sensor.isDirectionBlocked(1, WorldSpatial.Direction.NORTH);
		case WEST:
			return sensor.isDirectionBlocked(1, WorldSpatial.Direction.EAST);
		default:
			return false;
		}
	}
	
	/**
	 * Turns in the right direction given where it is in with respect to the current macro
	 * 
	 * @param delta
	 */
	private void applyTurn(float delta) {	
		
		if (!moveForward) {
			if (!clearRear()) {
				moveForward = true;
			} else {
				controller.applyReverseAcceleration();
				controller.turnLeft(delta);
			}
		} else {
			if (!clearFront()) {
				moveForward = false;
			} else {
				controller.applyForwardAcceleration();
				controller.turnLeft(delta);
			}
		}
	}
	
	/**
	 * Turn according to the direction you are facing.
	 * 
	 * @param delta
	 */
	private void turn(float delta) {
		
		switch(initOrientation){
		case EAST:
			if (!controller.getOrientation().equals(WorldSpatial.Direction.WEST))
				applyTurn(delta);
			else {
				turning = false;
			}
			break;
			
		case NORTH:
			if (!controller.getOrientation().equals(WorldSpatial.Direction.SOUTH))
				applyTurn(delta);
			else {
				turning = false;
			}
			break;
			
		case SOUTH:
			if (!controller.getOrientation().equals(WorldSpatial.Direction.NORTH))
				applyTurn(delta);
			else {
				turning = false;
			}
			break;
			
		case WEST:
			if (!controller.getOrientation().equals(WorldSpatial.Direction.EAST))
				applyTurn(delta);
			else {
				turning = false;
			}
			break;
		default:
			break;
		
		}
	}
	
	/**
	 * Make a left turn if possible
	 * 
	 * @param delta
	 */
	private void leftTurn(float delta) {
		
		switch(initOrientation){
		case EAST:
			if (!controller.getOrientation().equals(WorldSpatial.Direction.SOUTH))
				controller.turnRight(delta);
			else {
				forward = true;
			}
			break;
		case NORTH:
			if (!controller.getOrientation().equals(WorldSpatial.Direction.EAST))
				controller.turnRight(delta);
			else {
				forward = true;
			}
			break;
		case SOUTH:
			if (!controller.getOrientation().equals(WorldSpatial.Direction.WEST))
				controller.turnRight(delta);
			else {
				forward = true;
			}
			break;
		case WEST:
			if (!controller.getOrientation().equals(WorldSpatial.Direction.NORTH))
				controller.turnRight(delta);
			else {
				forward = true;
			}
			break;
		default:
			break;
		
		}
	}

}
