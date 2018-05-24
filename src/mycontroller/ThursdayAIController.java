package mycontroller;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import controller.CarController;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.World;
import world.WorldSpatial;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;

public class ThursdayAIController extends CarController {
	
	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 2;
	
	private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
	private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
	private boolean isTurningLeft = false;
	private boolean isTurningRight = false; 
	private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
	
	// Car Speed to move at
	private final float CAR_SPEED = 3;
	// Car Speed to move at
		final float MAX_SPEED = (float) 2.4;
		final float LEFT_SPEED = (float) 1.5;
		final float RIGHT_SPEED = (float) 0.8;

		final int SLOW_DISTANCE = 3;
		float targetSpeed = MAX_SPEED;
	
	// Offset used to differentiate between 0 and 360 degrees
	private int EAST_THRESHOLD = 3;
	
	// The whole map withoput lava and keys
	private HashMap<utilities.Coordinate, MapTile> worldMap = new HashMap<Coordinate, MapTile>();
	
	// Store map
	private HashMap<Coordinate, MapTile> storedMap = new HashMap<Coordinate, MapTile>();
	
	public ThursdayAIController(Car car) {
		super(car);
		setWholeMap();
	}
	
	Coordinate initialGuess;
	boolean notSouth = true;


	
	@Override
	public void update(float delta) {
		
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();
		
		// Stores the map
		storeMap(currentView);
		
		
		checkStateChange();

		// If you are not following a wall initially, find a wall to stick to!
		if(!isFollowingWall){
			if(getSpeed() < CAR_SPEED){
				applyForwardAcceleration();
			}
			// Turn towards the north
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
				applyLeftTurn(getOrientation(),delta);
			}
			if(checkNorth(currentView)){
				// Turn right until we go back to east!
				if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					applyRightTurn(getOrientation(),delta);
				}
				else{
					isFollowingWall = true;
				}
			}
		}
		// Once the car is already stuck to a wall, apply the following logic
		else{
			
			// Readjust the car if it is misaligned.
			readjust(lastTurnDirection,delta);
			
			if(isTurningRight){
				applyRightTurn(getOrientation(),delta);
			}
			else if(isTurningLeft){
				// Apply the left turn if you are not currently near a wall.
				if(!checkFollowingWall(getOrientation(),currentView)){
					applyLeftTurn(getOrientation(),delta);
				}
				else{
					isTurningLeft = false;
				}
			}
			// Try to determine whether or not the car is next to a wall.
			else if(checkFollowingWall(getOrientation(),currentView)){
				// Maintain some velocity
				if(getSpeed() < CAR_SPEED){
					applyForwardAcceleration();
				}
				// If there is wall ahead, turn right!
				if(checkWallAhead(getOrientation(),currentView)){
					lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
					isTurningRight = true;				
					
				}

			}
			// This indicates that I can do a left turn if I am not turning right
			else{
				lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
				isTurningLeft = true;
			}
		}
		
		

	}
	
	private void storeMap(HashMap<Coordinate, MapTile> currentView) {
		// TODO Auto-generated method stub
		for(Coordinate coord : currentView.keySet()) {
			if(!storedMap.containsKey(coord)) {
				storedMap.put(coord, currentView.get(coord));
			}
		}
	}

	//Dijkstra path finding thingy.
	private void setWholeMap() {
		this.worldMap = World.getMap();	
		Coordinate start = null;
		ArrayList<Coordinate> end = new ArrayList<Coordinate>();
		for(Coordinate coord : worldMap.keySet()) {
			if(worldMap.get(coord).getType() == MapTile.Type.START) {
				start = coord;
			}
			else if(worldMap.get(coord).getType() == MapTile.Type.FINISH) {
				end.add(coord);
			}
		}
		ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
		
	}
	/**
	 * Readjust the car to the orientation we are in.
	 * @param lastTurnDirection
	 * @param delta
	 */
	private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta) {
		if(lastTurnDirection != null){
			if(!isTurningRight && lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)){
				adjustRight(getOrientation(),delta);
			}
			else if(!isTurningLeft && lastTurnDirection.equals(WorldSpatial.RelativeDirection.LEFT)){
				adjustLeft(getOrientation(),delta);
			}
		}
		
	}
	
	/**
	 * Try to orient myself to a degree that I was supposed to be at if I am
	 * misaligned.
	 */
	private void adjustLeft(WorldSpatial.Direction orientation, float delta) {
		
		switch(orientation){
		case EAST:
			if(getAngle() > WorldSpatial.EAST_DEGREE_MIN+EAST_THRESHOLD){
				turnRight(delta);
			}
			break;
		case NORTH:
			if(getAngle() > WorldSpatial.NORTH_DEGREE){
				turnRight(delta);
			}
			break;
		case SOUTH:
			if(getAngle() > WorldSpatial.SOUTH_DEGREE){
				turnRight(delta);
			}
			break;
		case WEST:
			if(getAngle() > WorldSpatial.WEST_DEGREE){
				turnRight(delta);
			}
			break;
			
		default:
			break;
		}
		
	}

	private void adjustRight(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(getAngle() > WorldSpatial.SOUTH_DEGREE && getAngle() < WorldSpatial.EAST_DEGREE_MAX){
				turnLeft(delta);
			}
			break;
		case NORTH:
			if(getAngle() < WorldSpatial.NORTH_DEGREE){
				turnLeft(delta);
			}
			break;
		case SOUTH:
			if(getAngle() < WorldSpatial.SOUTH_DEGREE){
				turnLeft(delta);
			}
			break;
		case WEST:
			if(getAngle() < WorldSpatial.WEST_DEGREE){
				turnLeft(delta);
			}
			break;
			
		default:
			break;
		}
		
	}
	
	/**
	 * Checks whether the car's state has changed or not, stops turning if it
	 *  already has.
	 */
	private void checkStateChange() {
		if(previousState == null){
			previousState = getOrientation();
		}
		else{
			if(previousState != getOrientation()){
				if(isTurningLeft){
					isTurningLeft = false;
				}
				if(isTurningRight){
					isTurningRight = false;
				}
				previousState = getOrientation();
			}
		}
	}
	
	/**
	 * Turn the car counter clock wise (think of a compass going counter clock-wise)
	 */
	private void applyLeftTurn(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				turnLeft(delta);
			}
			break;
		case NORTH:
			if(!getOrientation().equals(WorldSpatial.Direction.WEST)){
				turnLeft(delta);
			}
			break;
		case SOUTH:
			if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
				turnLeft(delta);
			}
			break;
		case WEST:
			if(!getOrientation().equals(WorldSpatial.Direction.SOUTH)){
				turnLeft(delta);
			}
			break;
		default:
			break;
		
		}
		
	}
	
	/**
	 * Turn the car clock wise (think of a compass going clock-wise)
	 */
	private void applyRightTurn(WorldSpatial.Direction orientation, float delta) {
		switch(orientation){
		case EAST:
			if(!getOrientation().equals(WorldSpatial.Direction.SOUTH)){
				turnRight(delta);
			}
			break;
		case NORTH:
			if(!getOrientation().equals(WorldSpatial.Direction.EAST)){
				turnRight(delta);
			}
			break;
		case SOUTH:
			if(!getOrientation().equals(WorldSpatial.Direction.WEST)){
				turnRight(delta);
			}
			break;
		case WEST:
			if(!getOrientation().equals(WorldSpatial.Direction.NORTH)){
				turnRight(delta);
			}
			break;
		default:
			break;
		
		}
		
	}

	/**
	 * Check if you have a wall in front of you!
	 * @param orientation the orientation we are in based on WorldSpatial
	 * @param currentView what the car can currently see
	 * @return
	 */
	private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
		switch(orientation){
		case EAST:
			return checkEast(currentView);
		case NORTH:
			return checkNorth(currentView);
		case SOUTH:
			return checkSouth(currentView);
		case WEST:
			return checkWest(currentView);
		default:
			return false;
		
		}
	}
	
	/**
	 * Check if the wall is on your left hand side given your orientation
	 * @param orientation
	 * @param currentView
	 * @return
	 */
	private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		
		switch(orientation){
		case EAST:
			return checkNorth(currentView);
		case NORTH:
			return checkWest(currentView);
		case SOUTH:
			return checkEast(currentView);
		case WEST:
			return checkSouth(currentView);
		default:
			return false;
		}
		
	}
	

	/**
	 * Method below just iterates through the list and check in the correct coordinates.
	 * i.e. Given your current position is 10,10
	 * checkEast will check up to wallSensitivity amount of tiles to the right.
	 * checkWest will check up to wallSensitivity amount of tiles to the left.
	 * checkNorth will check up to wallSensitivity amount of tiles to the top.
	 * checkSouth will check up to wallSensitivity amount of tiles below.
	 */
	public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
		// Check tiles to my right
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to my left
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to towards the top
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles towards the bottom
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	
//	final class NodeData<Coordinate> { 
//
//	    private final Coordinate nodeId;
//	    private final Map<Coordinate, Double> heuristic;
//
//	    private double g;  // g is distance from the source
//	    private double h;  // h is the heuristic of destination.
//	    private double f;  // f = g + h 
//
//	    public NodeData (Coordinate nodeId, Map<Coordinate, Double> heuristic) {
//	        this.nodeId = nodeId;
//	        this.g = Double.MAX_VALUE; 
//	        this.heuristic = heuristic;
//	    }
//
//	    public Coordinate getNodeId() {
//	        return nodeId;
//	    }
//
//	    public double getG() {
//	        return g;
//	    }
//
//	    public void setG(double g) {
//	        this.g = g;
//	    }
//
//	    public void calcF(Coordinate destination) {
//	        this.h = heuristic.get(destination);
//	        this.f = g + h;
//	    } 
//
//	    public double getH() {
//	        return h;
//	    }
//
//	    public double getF() {
//	        return f;
//	    }
//	 }
//
//	/**
//	 * The graph represents an undirected graph. 
//	 * 
//	 * @author SERVICE-NOW\ameya.patil
//	 *
//	 * @param <T>
//	 */
//	final class GraphAStar<Coordinate> implements Iterable<Coordinate> {
//	    /*
//	     * A map from the nodeId to outgoing edge.
//	     * An outgoing edge is represented as a tuple of NodeData and the edge length
//	     */
//	    private final Map<Coordinate, Map<NodeData<Coordinate>, Double>> graph;
//	    /*
//	     * A map of heuristic from a node to each other node in the graph.
//	     */
//	    private final Map<Coordinate, Map<Coordinate, Double>> heuristicMap;
//	    /*
//	     * A map between nodeId and nodedata.
//	     */
//	    private final Map<Coordinate, NodeData<Coordinate>> nodeIdNodeData;
//
//	    public GraphAStar(Map<Coordinate, Map<Coordinate, Double>> heuristicMap) {
//	        if (heuristicMap == null) throw new NullPointerException("The huerisic map should not be null");
//	        graph = new HashMap<Coordinate, Map<NodeData<Coordinate>, Double>>();
//	        nodeIdNodeData = new HashMap<Coordinate, NodeData<Coordinate>>();
//	        this.heuristicMap = heuristicMap;
//	    } 
//
//	    /**
//	     * Adds a new node to the graph.
//	     * Internally it creates the nodeData and populates the heuristic map concerning input node into node data.
//	     * 
//	     * @param nodeId the node to be added
//	     */
//	    public void addNode(Coordinate nodeId) {
//	        if (nodeId == null) throw new NullPointerException("The node cannot be null");
//	        if (!heuristicMap.containsKey(nodeId)) throw new NoSuchElementException("This node is not a part of hueristic map");
//
//	        graph.put(nodeId, new HashMap<NodeData<Coordinate>, Double>());
//	        nodeIdNodeData.put(nodeId, new NodeData<Coordinate>(nodeId, heuristicMap.get(nodeId)));
//	    }
//
//	    /**
//	     * Adds an edge from source node to destination node.
//	     * There can only be a single edge from source to node.
//	     * Adding additional edge would overwrite the value
//	     * 
//	     * @param nodeIdFirst   the first node to be in the edge
//	     * @param nodeIdSecond  the second node to be second node in the edge
//	     * @param length        the length of the edge.
//	     */
//	    public void addEdge(Coordinate nodeIdFirst, Coordinate nodeIdSecond, double length) {
//	        if (nodeIdFirst == null || nodeIdSecond == null) throw new NullPointerException("The first nor second node can be null.");
//
//	        if (!heuristicMap.containsKey(nodeIdFirst) || !heuristicMap.containsKey(nodeIdSecond)) {
//	            throw new NoSuchElementException("Source and Destination both should be part of the part of hueristic map");
//	        }
//	        if (!graph.containsKey(nodeIdFirst) || !graph.containsKey(nodeIdSecond)) {
//	            throw new NoSuchElementException("Source and Destination both should be part of the part of graph");
//	        }
//
//	        graph.get(nodeIdFirst).put(nodeIdNodeData.get(nodeIdSecond), length);
//	        graph.get(nodeIdSecond).put(nodeIdNodeData.get(nodeIdFirst), length);
//	    }
//
//	    /**
//	     * Returns immutable view of the edges
//	     * 
//	     * @param nodeId    the nodeId whose outgoing edge needs to be returned
//	     * @return          An immutable view of edges leaving that node
//	     */
//	    public Map<NodeData<Coordinate>, Double> edgesFrom (Coordinate nodeId) {
//	        if (nodeId == null) throw new NullPointerException("The input node should not be null.");
//	        if (!heuristicMap.containsKey(nodeId)) throw new NoSuchElementException("This node is not a part of hueristic map");
//	        if (!graph.containsKey(nodeId)) throw new NoSuchElementException("The node should not be null.");
//
//	        return Collections.unmodifiableMap(graph.get(nodeId));
//	    }
//
//	    /**
//	     * The nodedata corresponding to the current nodeId.
//	     * 
//	     * @param nodeId    the nodeId to be returned
//	     * @return          the nodeData from the 
//	     */ 
//	    public NodeData<Coordinate> getNodeData (Coordinate nodeId) {
//	        if (nodeId == null) { throw new NullPointerException("The nodeid should not be empty"); }
//	        if (!nodeIdNodeData.containsKey(nodeId))  { throw new NoSuchElementException("The nodeId does not exist"); }
//	        return nodeIdNodeData.get(nodeId);
//	    }
//
//	    /**
//	     * Returns an iterator that can traverse the nodes of the graph
//	     * 
//	     * @return an Iterator.
//	     */
//	    @Override public Iterator<Coordinate> iterator() {
//	        return graph.keySet().iterator();
//	    }
//	}
//	
////	public void implementAStar() {
////		ArrayList<Coordinate> answer = new ArrayList<Coordinate>();
////		answer = astar()
////	}
//
//	@SuppressWarnings("hiding")
//	public class AStar<Coordinate> {
//
//	    private final GraphAStar<Coordinate> graph;
//
//
//	    public AStar (GraphAStar<Coordinate> graphAStar) {
//	        this.graph = graphAStar;
//	    }
//
//	    // extend comparator.
//	    public class NodeComparator implements Comparator<NodeData<Coordinate>> {
//	        public int compare(NodeData<Coordinate> nodeFirst, NodeData<Coordinate> nodeSecond) {
//	            if (nodeFirst.getF() > nodeSecond.getF()) return 1;
//	            if (nodeSecond.getF() > nodeFirst.getF()) return -1;
//	            return 0;
//	        }
//	    } 
//
//	    /**
//	     * Implements the A-star algorithm and returns the path from source to destination
//	     * 
//	     * @param source        the source nodeid
//	     * @param destination   the destination nodeid
//	     * @return              the path from source to destination
//	     */
//	    public ArrayList<Coordinate> astar(Coordinate source, Coordinate destination) {
//	        /**
//	         * http://stackoverflow.com/questions/20344041/why-does-priority-queue-has-default-initial-capacity-of-11
//	         */
//	        final PriorityQueue<NodeData<Coordinate>> openQueue = new PriorityQueue<NodeData<Coordinate>>(11, new NodeComparator()); 
//
//	        NodeData<Coordinate> sourceNodeData = graph.getNodeData(source);
//	        sourceNodeData.setG(0);
//	        sourceNodeData.calcF(destination);
//	        openQueue.add(sourceNodeData);
//
//	        final Map<Coordinate,Coordinate> path = new HashMap<Coordinate,Coordinate>();
//	        final HashSet<NodeData<Coordinate>> closedList = new HashSet<NodeData<Coordinate>>();
//
//	        while (!openQueue.isEmpty()) {
//	            final NodeData<Coordinate> nodeData = openQueue.poll();
//
//	            if (nodeData.getNodeId().equals(destination)) { 
//	                return path(path, destination);
//	            }
//
//	            closedList.add(nodeData);	
//
//	            for (Map.Entry<NodeData<Coordinate>, Double> neighborEntry : graph.edgesFrom(nodeData.getNodeId()).entrySet()) {
//	                NodeData<Coordinate> neighbor = neighborEntry.getKey();
//
//	                if (closedList.contains(neighbor)) continue;
//
//	                double distanceBetweenTwoNodes = neighborEntry.getValue();
//	                double tentativeG = distanceBetweenTwoNodes + nodeData.getG();
//
//	                if (tentativeG < neighbor.getG()) {
//	                    neighbor.setG(tentativeG);
//	                    neighbor.calcF(destination);
//
//	                    path.put(neighbor.getNodeId(), nodeData.getNodeId());
//	                    if (!openQueue.contains(neighbor)) {
//	                        openQueue.add(neighbor);
//	                    }
//	                }
//	            }
//	        }
//
//	        return null;
//	    }
//
//
//	    private ArrayList<Coordinate> path(Map<Coordinate, Coordinate> path, Coordinate destination) {
//	        assert path != null;
//	        assert destination != null;
//
//	        final ArrayList<Coordinate> pathList = new ArrayList<Coordinate>();
//	        pathList.add(destination);
//	        while (path.containsKey(destination)) {
//	            destination = path.get(destination);
//	            pathList.add(destination);
//	        }
//	        Collections.reverse(pathList);
//	        return pathList;
//	    }
//	}
}