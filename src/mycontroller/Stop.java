package mycontroller;

public class Stop extends Macro{

	public Stop(MyAIController controller) {
		super(controller);
		// TODO Auto-generated constructor stub
	}
	
	public void update(float delta) {
		
//		controller.targetSpeed = (float)-1.0;
//		controller.applyReverseAcceleration();
//		controller.applyForwardAcceleration();
		controller.applyBrake();
	
	}

}
