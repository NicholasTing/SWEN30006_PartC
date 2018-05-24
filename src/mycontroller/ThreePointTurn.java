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
 * Class that three point turns
 */
public class ThreePointTurn extends Macro {

	private boolean turning = true;
	private boolean forward = false;
	private boolean movingFwd = false;
	private Radar radar;
	private WorldSpatial.Direction initOrientation;
	
	private static final int CAR_SPEED = 1;
	
	public ThreePointTurn(MyAIController controller) {
		super(controller);
		this.radar = controller.getRadar();
	}

	/**
	 * Updates the car to do the right point given the point at which
	 * it is in regarding the manpeuvre
	 */
	@Override
	public void update(float delta) {
		if (initOrientation == null) {
			initOrientation = controller.getOrientation();
		}
		
		boolean leftBlocked = radar.isDirectionBlocked(2, WorldSpatial.RelativeDirection.LEFT);
		boolean rightBlocked = radar.isDirectionBlocked(2, WorldSpatial.RelativeDirection.RIGHT);
		
		if (turning) {
			turn(delta);
		} else if (forward) {
			System.out.println("forward");
			controller.resetTurningCoords();
			controller.setMacro(DriveStraight.class);
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
	 * Checks to see if the front is clear ahead
	 * @return
	 */
	private boolean clearFront() {
		return radar.isDirectionBlocked(1, controller.getOrientation());
	}
	
	/**
	 * Checks if the path is clear behind
	 * @return
	 */
	private boolean clearRear() {
		switch(controller.getOrientation()){
		case EAST:
			return radar.isDirectionBlocked(1, WorldSpatial.Direction.WEST);
		case NORTH:
			return radar.isDirectionBlocked(1, WorldSpatial.Direction.SOUTH);
		case SOUTH:
			return radar.isDirectionBlocked(1, WorldSpatial.Direction.NORTH);
		case WEST:
			return radar.isDirectionBlocked(1, WorldSpatial.Direction.EAST);
		default:
			return false;
		}
	}
	
	/**
	 * Turns in the right direction given where it is in the manoeuvre
	 * @param delta
	 */
	private void applyTurn(float delta) {	
		if (!movingFwd) {
			if (!clearRear()) {
				movingFwd = true;
			} else {
				controller.applyReverseAcceleration();
				controller.turnLeft(delta);
			}
		} else {
			if (!clearFront()) {
				movingFwd = false;
			} else {
				controller.applyForwardAcceleration();
				controller.turnLeft(delta);
			}
		}
	}
	
	/**
	 * turn according to the right direction you're travelling in
	 * @param delta
	 */
	private void turn(float delta) {
		System.out.println("Init: " + initOrientation + ", curr: "+ controller.getOrientation());
		
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
	 * Make a left turn at the right time
	 * @param delta
	 */
	private void leftTurn(float delta) {
		System.out.println("Init: " + initOrientation + ", curr: "+ controller.getOrientation());
		
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
