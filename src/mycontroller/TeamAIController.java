package mycontroller;

import java.util.HashMap;

import com.badlogic.gdx.math.Vector2;

import controller.CarController;
import tiles.MapTile;
import utilities.Coordinate;
import utilities.PeekTuple;
import world.Car;
import world.World;
import world.WorldSpatial;

public class TeamAIController extends CarController {

	private Car car;
	private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false; 
	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
	
	// Car Speed to move at
	private final float CAR_SPEED = 3;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	
	/**
	 * Instantiates the car
	 * @param car
	 */
	public TeamAIController(Car car){
		super(car);
		this.car = car;
	}

	@Override
	public void update(float delta) {
		// TODO Auto-generated method stub
		
	}
	
}