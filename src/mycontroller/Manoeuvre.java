package mycontroller;

/**
 * Project Part C
 * Group 105
 * Andrew Roche (638338), David Barrell (520704), Hugh Edwards (584183)
 * Software Modelling and Design - University of Melbourne 2017
 *  Class which dictates the way the car behaves
 */
public abstract class Manoeuvre {

	protected MyAIController controller;
	
	public Manoeuvre(MyAIController controller) {
		this.controller = controller;
	}
	
	public abstract void update(float delta);
	
}
