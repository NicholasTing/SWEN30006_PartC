package mycontroller;

import world.WorldSpatial;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * Class that drives straight
 */
public class DriveStraight extends Macro {

	public DriveStraight(MyAIController controller) {
		super(controller);
	}

	/**
	 * Get's the direction and speed right given it's current trajectory
	 */
	@Override
	public void update(float delta) {
		
		if(controller.getSpeed() < controller.targetSpeed){
			controller.applyForwardAcceleration();
		}
		else if (controller.getSpeed() > controller.targetSpeed) {
			controller.applyReverseAcceleration();
		}
		adjustRight(controller.getOrientation(),delta);
		adjustLeft(controller.getOrientation(),delta);

	}
	
	/**
	 * Try to orient myself to a degree that I was supposed to be at if I am
	 * misaligned.
	 * @param orientation the direction it wants to travel
	 * @param delta
	 */
	private void adjustLeft(WorldSpatial.Direction orientation, float delta) {
		
		switch(orientation){
		case EAST:
			if(controller.getAngle() > WorldSpatial.EAST_DEGREE_MIN && controller.getAngle() < WorldSpatial.NORTH_DEGREE){
				controller.turnRight(delta);
			}
			break;
		case NORTH:
			if(controller.getAngle() > WorldSpatial.NORTH_DEGREE && controller.getAngle() < WorldSpatial.WEST_DEGREE){
				controller.turnRight(delta);
			}
			break;
		case SOUTH:
			if(controller.getAngle() > WorldSpatial.SOUTH_DEGREE && controller.getAngle() < WorldSpatial.EAST_DEGREE_MAX){
				controller.turnRight(delta);
			}
			break;
		case WEST:
			if(controller.getAngle() > WorldSpatial.WEST_DEGREE && controller.getAngle() < WorldSpatial.SOUTH_DEGREE){
				controller.turnRight(delta);
			}
			break;
			
		default:
			break;
		}
		
	}

	/**
	 * Adjust the car slightly right if it's not travelling straight
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
			if(controller.getAngle() < WorldSpatial.NORTH_DEGREE && controller.getAngle() > WorldSpatial.EAST_DEGREE_MIN){
				controller.turnLeft(delta);
			}
			break;
		case SOUTH:
			if(controller.getAngle() < WorldSpatial.SOUTH_DEGREE && controller.getAngle() > WorldSpatial.WEST_DEGREE){
				controller.turnLeft(delta);
			}
			break;
		case WEST:
			if(controller.getAngle() < WorldSpatial.WEST_DEGREE && controller.getAngle() > WorldSpatial.NORTH_DEGREE){
				controller.turnLeft(delta);
			}
			break;
			
		default:
			break;
		}
		
	}
	

}
