package mycontroller;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * 
 * Class that provides the macro for the car to turn right.
 */
public class RightTurn extends Macro{
	
	public RightTurn(MyAIController controller) {
		super(controller);
	}

	/**
	 * Car turns right after this update, at a specific speed set in the controller.
	 */
	public void update(float delta) {
		if(controller.getSpeed() < controller.targetSpeed){
			controller.applyForwardAcceleration();
			controller.turnRight(delta);
		}
		else if (controller.getSpeed() > controller.targetSpeed) {
			controller.applyReverseAcceleration();
			controller.turnLeft(delta);
		}
	}

}
