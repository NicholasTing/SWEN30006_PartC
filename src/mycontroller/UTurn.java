package mycontroller;

import world.WorldSpatial;

/**
 * Project Part C
 * Group 105
 * Andrew Roche (638338), David Barrell (520704), Hugh Edwards (584183)
 * Software Modelling and Design - University of Melbourne 2017
 * Class that u-turns
 */
public class UTurn extends Manoeuvre {

	private boolean turning = true;
	private boolean forward = false;
	private Radar radar;
	private WorldSpatial.Direction initOrientation;
	
	private static final int CAR_SPEED = 1;
	
	public UTurn(MyAIController controller) {
		super(controller);
		this.radar = controller.getRadar();
	}

	/**
	 * Updates the car to do the right thing wherever it is in the process of making a u-turn
	 */
	@Override
	public void update(float delta) {
		if (initOrientation == null) {
			initOrientation = controller.getOrientation();
		}
		
		boolean leftBlocked = radar.isDirectionBlocked(2, WorldSpatial.RelativeDirection.LEFT);
		boolean rightBlocked = radar.isDirectionBlocked(2, WorldSpatial.RelativeDirection.RIGHT);
		
		// if it's turning, keep doing the u-turn
		if (turning) {
			uTurn(delta);
		} // if you can now continue (i.e. you're finished doing the u-turn), drive straight
		else if (forward) {
			System.out.println("forward");
			controller.resetTurningCoords();
			controller.setManoeuvre(DriveStraight.class);
		} else if (leftBlocked && rightBlocked) {
			if (!radar.getMap().getDeadEnds().contains(controller.getPositionCoords()))
				radar.getMap().getDeadEnds().add(controller.getPositionCoords());
			if (controller.getSpeed() < CAR_SPEED)
				controller.applyForwardAcceleration();
		} else {
			leftTurn(delta);
		}
		
	}
	
	/**
	 * Turn around (by turning right)
	 * @param delta
	 */
	private void uTurn(float delta) {
		
		switch(initOrientation){
		case EAST:
			if (!controller.getOrientation().equals(WorldSpatial.Direction.WEST))
				controller.turnRight(delta);
			else {
				turning = false;
			}
			break;
		case NORTH:
			if (!controller.getOrientation().equals(WorldSpatial.Direction.SOUTH))
				controller.turnRight(delta);
			else {
				turning = false;
			}
			break;
		case SOUTH:
			if (!controller.getOrientation().equals(WorldSpatial.Direction.NORTH))
				controller.turnRight(delta);
			else {
				turning = false;
			}
			break;
		case WEST:
			if (!controller.getOrientation().equals(WorldSpatial.Direction.EAST))
				controller.turnRight(delta);
			else {
				turning = false;
			}
			break;
		default:
			break;
		
		}
	}
	
	/**
	 * Turn left if you need to
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
