package mycontroller;

import world.WorldSpatial;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * 
 * Class that provides the macro for the car to reverse out.
 */
public class Reverse extends Macro {
	
	private boolean turning = false;
	private boolean forward = false;
	private Sensor sensor;
	private WorldSpatial.Direction initOrientation;

	public Reverse(MyAIController controller) {
		super(controller);
		this.sensor = controller.getSensor();
	}

	/**
	 * Updates the car in the correct fashion according to where
	 * in the process of reversing it is in
	 */
	@Override
	public void update(float delta) {
		
		if (initOrientation == null) {
			initOrientation = controller.getOrientation();
		}
		
		boolean leftBlocked = sensor.isDirectionBlocked(2, WorldSpatial.RelativeDirection.LEFT);
		boolean rightBlocked = sensor.isDirectionBlocked(2, WorldSpatial.RelativeDirection.RIGHT);
		
		// If the macro reverse is finished, car drives forward
		if (forward) {
			
			controller.realign();
			controller.setMacro(Forward.class);
			
		} 
		
		// If the car is stuck where both the left and right is blocked and a turn can't be made.
		else if (leftBlocked && rightBlocked) {
			
			if (!sensor.getMap().getDeadEnds().contains(controller.getCarCoords()))
				sensor.getMap().getDeadEnds().add(controller.getCarCoords());
			
			controller.applyReverseAcceleration();
			System.out.println(controller.getVelocity());
			
		}
		
		
		else if (!leftBlocked || turning) {
			System.out.println("Turning");
			turning = true;
			reverseRightTurn(delta);
		}
		
		else {
			controller.realign();
			controller.setMacro(Forward.class);
		}
		
	}
	
	/**
	 * Does a reverse right turn if possible
	 * 
	 * @param delta
	 */
	private void reverseRightTurn(float delta) {
		
		switch(initOrientation){
		case EAST:
			
			if (!controller.getOrientation().equals(WorldSpatial.Direction.SOUTH))
				controller.turnRight(delta);
			else {
				turning = false;
				forward = true;
			}
			break;
		case NORTH:
			
			if (!controller.getOrientation().equals(WorldSpatial.Direction.EAST))
				controller.turnRight(delta);
			else {
				turning = false;
				forward = true;
			}
			break;
		case SOUTH:
			
			if (!controller.getOrientation().equals(WorldSpatial.Direction.WEST))
				controller.turnRight(delta);
			else {
				turning = false;
				forward = true;
			}
			break;
			
		case WEST:
			
			if (!controller.getOrientation().equals(WorldSpatial.Direction.NORTH))
				controller.turnRight(delta);
			
			else {
				turning = false;
				forward = true;
			}
			break;
		default:
			break;
		
		}
	}

}
