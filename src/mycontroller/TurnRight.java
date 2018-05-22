package mycontroller;

/**
 * Project Part C
 * Group 105
 * Andrew Roche (638338), David Barrell (520704), Hugh Edwards (584183)
 * Software Modelling and Design - University of Melbourne 2017
 * Class that turns right
 */
public class TurnRight extends Manoeuvre{
	
	public TurnRight(MyAIController controller) {
		super(controller);
	}

	/**
	 * Updates the car to turn right at the right speed
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
