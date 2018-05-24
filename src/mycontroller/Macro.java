package mycontroller;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * 
 * Its a class which makes it easier to control the car, as to how it behaves.
 */
public abstract class Macro {

	protected MyAIController controller;
	
	public Macro(MyAIController controller) {
		this.controller = controller;
	}
	
	public abstract void update(float delta);
	
}
