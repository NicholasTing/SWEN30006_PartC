package mycontroller;

import world.WorldSpatial;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * 
 * Class that provides the macro for the car to go forward in a straight manner.
 */
public class Forward extends Macro {

	public Forward(MyAIController controller) {
		super(controller);
	}

	/**
	 * Get's the direction and speed right given it's current trajectory
	 * Makes sure that the car does not speed. 
	 */
	@Override
	public void update(float delta) {
		
		if(controller.getSpeed() < controller.targetSpeed){
			controller.applyForwardAcceleration();
		}
		else if (controller.getSpeed() > controller.targetSpeed + 0.5) {
			controller.applyReverseAcceleration();
		}
		adjustRight(controller.getOrientation(),delta);
		adjustLeft(controller.getOrientation(),delta);

	}
	
	/**
	 * Adjust the car slightly to the left if it's not moving in a straight manner
	 * 
	 * @param orientation
	 * @param delta
	 */
	private void adjustLeft(WorldSpatial.Direction orientation, float delta) {
		
		// Offset used to differentiate between 0 and 360 degrees
		int EAST_THRESHOLD = 3;
		
		switch(orientation){
		case EAST:
			if(controller.getAngle() > WorldSpatial.EAST_DEGREE_MIN + EAST_THRESHOLD){
				controller.turnRight(delta);
			}
			break;
		case NORTH:
			if(controller.getAngle() > WorldSpatial.NORTH_DEGREE){
				controller.turnRight(delta);
			}
			break;
		case SOUTH:
			if(controller.getAngle() > WorldSpatial.SOUTH_DEGREE){
				controller.turnRight(delta);
			}
			break;
		case WEST:
			if(controller.getAngle() > WorldSpatial.WEST_DEGREE){
				controller.turnRight(delta);
			}
			break;
			
		default:
			break;
		}
		
	}

	/**
	 * Adjust the car slightly to the right if it's not moving in a straight manner
	 * 
	 * @param orientation the direction it wants to travel
	 * @param delta
	 */
	private void adjustRight(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(controller.getAngle() > WorldSpatial.SOUTH_DEGREE && controller.getAngle() < WorldSpatial.EAST_DEGREE_MAX){
				controller.turnLeft(delta);
			}
			break;
		case NORTH:
			if(controller.getAngle() < WorldSpatial.NORTH_DEGREE){
				controller.turnLeft(delta);
			}
			break;
		case SOUTH:
			if(controller.getAngle() < WorldSpatial.SOUTH_DEGREE){
				controller.turnLeft(delta);
			}
			break;
		case WEST:
			if(controller.getAngle() < WorldSpatial.WEST_DEGREE){
				controller.turnLeft(delta);
			}
			break;
			
		default:
			break;
		}
		
	}
	
	

}
