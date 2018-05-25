package mycontroller;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * 
 * Class that provides the macro for the car to stop.
 */

public class Stop extends Macro{

	public Stop(MyAIController controller) {
		super(controller);
	}
	
	public void update(float delta) {
		
		//Allows the car to come to a stop
		controller.applyBrake();
	
	}

}
