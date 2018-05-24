package mycontroller;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 *  Class that turns left
 */
public class TurnLeft extends Macro {

	public TurnLeft(MyAIController controller) {
		super(controller);
	}

	/**
	 * Updates the car to turn left at the right speed
	 */
	public void update(float delta) {
		if(controller.getSpeed() < controller.targetSpeed){
			controller.applyForwardAcceleration();
			controller.turnLeft(delta);
		}
		else if (controller.getSpeed() > controller.targetSpeed) {
			controller.applyReverseAcceleration();
			controller.turnRight(delta);
		}
		
	}
	
}
