package mycontroller;

import world.WorldSpatial;

/**
 * Project Part C
 * Group 105
 * Andrew Roche (638338), David Barrell (520704), Hugh Edwards (584183)
 * Software Modelling and Design - University of Melbourne 2017
 *  Class that reverses out
 */
public class ReverseOut extends Manoeuvre {
	
	private boolean turning = false;
	private boolean forward = false;
	private Radar radar;
	private WorldSpatial.Direction initOrientation;
	
	private final float REV_CAR_SPEED = (float) -1.5;

	public ReverseOut(MyAIController controller) {
		super(controller);
		this.radar = controller.getRadar();
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
		
		boolean leftBlocked = radar.isDirectionBlocked(2, WorldSpatial.RelativeDirection.LEFT);
		boolean rightBlocked = radar.isDirectionBlocked(2, WorldSpatial.RelativeDirection.RIGHT);
		
		// if you have finished the maneuvre, start driving straight again
		if (forward) {
			System.out.println("forward");
			controller.resetTurningCoords();
			controller.setManoeuvre(DriveStraight.class);
		} else if (leftBlocked && rightBlocked) {
			System.out.println("Left and right blocked");
			if (!radar.getMap().getDeadEnds().contains(controller.getPositionCoords()))
				radar.getMap().getDeadEnds().add(controller.getPositionCoords());
			controller.applyReverseAcceleration();
			System.out.println(controller.getVelocity());
		} else if (!leftBlocked || turning) {
			System.out.println("Turning");
			turning = true;
			reverseRightTurn(delta);
		}
		
	}
	
	/**
	 * Implements the right turn when reversing
	 * @param delta
	 */
	private void reverseRightTurn(float delta) {
		System.out.println("Init: " + initOrientation + ", curr: "+ controller.getOrientation());
		
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
