package mycontroller;

import java.util.HashMap;

import tiles.GrassTrap;
import tiles.HealthTrap;
import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.WorldSpatial;
import world.WorldSpatial.Direction;
/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 *  Class which provides information about the car's surroundings to the AI controller
 */
public class Radar {
	
	private MyAIController controller;
	private Map map;
	
	// How many minimum units the wall is away from the player.
	private double sideWallSensitivity = 1;
	private double frontWallSensitivity = 1;

	
	public Radar(MyAIController controller) {
		this.controller = controller;
		this.map = new Map(this);
	}

	/**
	 * Check if you have a wall in front of you!
	 * @param orientation the orientation we are in based on WorldSpatial
	 * @param currentView what the car can currently see
	 * @return
	 */
	public boolean isBlockedAhead(WorldSpatial.Direction orientation){
		return isDirectionBlocked(frontWallSensitivity, orientation);
	}
	
	
	/**
	 * Check if the wall is on your left hand side given your orientation
	 * @param orientation the direction you want to check
	 * @return true if it is following the boundary
	 */
	public boolean isFollowingBoundary(WorldSpatial.Direction orientation) {
		switch(orientation){
		case EAST:
			return isDirectionBlocked(sideWallSensitivity, WorldSpatial.Direction.NORTH);
		case NORTH:
			return isDirectionBlocked(sideWallSensitivity, WorldSpatial.Direction.WEST);
		case SOUTH:
			return isDirectionBlocked(sideWallSensitivity, WorldSpatial.Direction.EAST);
		case WEST:
			return isDirectionBlocked(sideWallSensitivity, WorldSpatial.Direction.SOUTH);
		default:
			return false;
		}
		
	}
	
	/**
	 * Checks to see if the given direction is blocked
	 * @param sensitivity how far in the direction you want to check
	 * @param direction the direction you want to check
	 * @return true if it is blocked
	 */
	public boolean isDirectionBlocked(double sensitivity, WorldSpatial.RelativeDirection direction) {
		WorldSpatial.Direction orientation = controller.getOrientation();
		switch(orientation){
		case EAST:
			if (direction.equals(WorldSpatial.RelativeDirection.LEFT))
				return isDirectionBlocked(sensitivity, WorldSpatial.Direction.NORTH);
			else
				return isDirectionBlocked(sensitivity, WorldSpatial.Direction.SOUTH);
		case NORTH:
			if (direction.equals(WorldSpatial.RelativeDirection.LEFT))
				return isDirectionBlocked(sensitivity, WorldSpatial.Direction.WEST);
			else
				return isDirectionBlocked(sensitivity, WorldSpatial.Direction.EAST);
		case SOUTH:
			if (direction.equals(WorldSpatial.RelativeDirection.LEFT))
				return isDirectionBlocked(sensitivity, WorldSpatial.Direction.EAST);
			else
				return isDirectionBlocked(sensitivity, WorldSpatial.Direction.WEST);
		case WEST:
			if (direction.equals(WorldSpatial.RelativeDirection.LEFT))
				return isDirectionBlocked(sensitivity, WorldSpatial.Direction.SOUTH);
			else
				return isDirectionBlocked(sensitivity, WorldSpatial.Direction.NORTH);
		default:
			return false;
		}
	}
	

	/**
	 * Checks to see if the given direction is blocked
	 * @param sensitivity how many tiles ahead you want to check
	 * @param direction what direction you want to check
	 * @return true if the direction is blocked
	 */
	public boolean isDirectionBlocked(double sensitivity, WorldSpatial.Direction direction) {

		int xModifier = 0;
		int yModifier = 0;
		
		switch (direction) {
			case EAST: {
				xModifier = 1;
				break;
			}
			case WEST: {
				xModifier = -1;
				break;
			}
			case NORTH: {
				yModifier = 1;
				break;
			}
			case SOUTH: {
				yModifier = -1;
				break;
			}
		}
		
		// Loop through the tiles up to sensitivity to check if the car can drive through them
		Coordinate currentPosition = controller.getPositionCoords();
			for(int i = 0; i <= sensitivity; i++){
				
				Coordinate coordinate = new Coordinate(currentPosition.x+i*xModifier, currentPosition.y+i*yModifier);
				
				MapTile tile = getView().get(coordinate);
				if(!isDriveable(tile, coordinate)) {
					return true;
				}
			}
			return false;
		}
	
	/**
	 * Checks to see if the tiles ahead and to the left (how far ahead based on 
	 * sensitivity)
	 * @param sensitivity how many tiles ahead
	 * @param direction which direction you are checking from
	 * @return true if it is
	 */
	public boolean isLeftOpenAhead(int sensitivity, WorldSpatial.Direction direction) {

		int xModifier = 0;
		int yModifier = 0;
		
		int xOffset = 0;
		int yOffset = 0;
		
		switch (direction) {
			case EAST: {
				xModifier = 1;
				yOffset = (1);
				break;
			}
			case WEST: {
				xModifier = (-1);
				yOffset = (-1);
				break;
			}
			case NORTH: {
				yModifier = 1;
				xOffset = (-1);
				break;
			}
			case SOUTH: {
				yModifier = (-1);
				xOffset = 1;
				break;
			}
		}
		
		Coordinate currentPosition = controller.getPositionCoords();
		for(int i = 0; i <= sensitivity; i++){
			
			Coordinate coordinate = new Coordinate(currentPosition.x+i*xModifier+xOffset, currentPosition.y+i*yModifier+yOffset);

			MapTile tile = map.getMap().get(coordinate).getTile();
			if(isDriveable(tile, coordinate)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks to see if the space ahead is a dead-end or not
	 * @param orientation the direction you are checking
	 * @return how wide the dead-end is (zero if it's not a dead end)
	 */
	public Integer isDeadEnd(WorldSpatial.Direction orientation) {
		int spaceAhead = 0;
		// Checks to see how much further the car can go
		for (int i = 0; i <= 3; i++) {
			if (isDirectionBlocked(i, orientation)) {
				spaceAhead = i - 1;
				break;
			}
		}

		for (int sensitivity = 1; sensitivity <= 1; sensitivity++) {
			int xOffset = 0;
			int yOffset = 0;
			
			switch (orientation) {
				case EAST: {
					xOffset = spaceAhead;
					yOffset = (-sensitivity);
					break;
				}
				case WEST: {
					xOffset = (-spaceAhead);
					yOffset = (sensitivity);
					break;
				}
				case NORTH: {
					xOffset = sensitivity;
					yOffset = spaceAhead;
					break;
				}
				case SOUTH: {
					xOffset = (-sensitivity);
					yOffset = (-spaceAhead);
					break;
				}
			}
			
			Coordinate currentPosition = controller.getPositionCoords();
			
			Coordinate coordinate = new Coordinate(currentPosition.x+xOffset, currentPosition.y+yOffset);
			MapTile tile = map.getMap().get(coordinate).getTile();
			tile = getView().get(coordinate);
			if(!isDriveable(tile, coordinate)) {
				return sensitivity;
			}
		}
		return null;
	}
	
	public double getFrontWallSensitivity() {
		return frontWallSensitivity;
	}
	
	/**
	 *  Returns whether a certain tile at a certain coordinate should be driven over
	 *  Will return false if its a wall
	 *  if its free road will return true
	 *  will return false if its a trap, unless the trap is marked as passable by the map
	 * @param mapTile the type of tile
	 * @param coordinate the position of the tile
	 * @return whether the tile should be driven over
	 */
	public boolean isDriveable(MapTile mapTile, Coordinate coordinate) {
		
//		Type tileType = mapTile.getType();;
		boolean driveable;
		switch(mapTile.getType()) {
		
			case WALL:
				driveable = false;
				break;
			
			default:
				driveable = true;
				
				break;
		}
		
		if(!driveable) {
			return false;
		}
		
		// If it's a dead end, it's not
		if (map.getDeadEnds().contains(coordinate)) {
			return false;
		}
		
		// Traps which are not marked as passable should not be driven over
		if (isImpassableTrap(mapTile, coordinate) )
		{
			return false;
		}
		
		// otherwise return true
		return true;
	}
//	
//	public boolean isOnHealth(WorldSpatial.Direction orientation) {
//		int spaceAhead = 0;
//		for (int sensitivity = 1; sensitivity <= 1; sensitivity++) {
//			int xOffset = 0;
//			int yOffset = 0;
//			
//			switch (orientation) {
//				case EAST: {
//					xOffset = spaceAhead;
//					yOffset = (-sensitivity);
//					break;
//				}
//				case WEST: {
//					xOffset = (-spaceAhead);
//					yOffset = (sensitivity);
//					break;
//				}
//				case NORTH: {
//					xOffset = sensitivity;
//					yOffset = spaceAhead;
//					break;
//				}
//				case SOUTH: {
//					xOffset = (-sensitivity);
//					yOffset = (-spaceAhead);
//					break;
//				}
//			}
//			
//			Coordinate currentPosition = controller.getPositionCoords();
//			
//			Coordinate coordinate = new Coordinate(currentPosition.x+xOffset, currentPosition.y+yOffset);
//			MapTile tile = map.getMap().get(coordinate).getTile();
//			
//			if(tile instanceof HealthTrap) {
//				System.out.println("Health trap here");
//				return true;
//			}
//		}
//		return false;
//		
	public boolean isHealthTrap(WorldSpatial.Direction orientation) {
		// TODO Auto-generated method stub
		return true;
	}
//	}
	
	/**
	 * Checks to see if the given tile is not able to be passed
	 * @param mapTile type of tile
	 * @param coordinate
	 * @return
	 */
	private boolean isImpassableTrap(MapTile mapTile, Coordinate coordinate) {
		return (mapTile instanceof GrassTrap && !map.isPassable(coordinate));
	}

	/**
	 * Updates the map
	 */
	public void updateMap() {
		map.updateMap();
	}
	
	public MyAIController getController() {
		return controller;
	}
	
	public void updateSectionStart() {
		map.updateSectionStart();
	}
	
	public Coordinate getPositionCoords() {
		return controller.getPositionCoords();
	}
	
	public HashMap<Coordinate,MapTile> getView(){
		return controller.getView();
	}
	
	public Map getMap() {
		return map;
	}
	
}
