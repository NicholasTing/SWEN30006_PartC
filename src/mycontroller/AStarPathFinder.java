package mycontroller;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

import utilities.Coordinate;

/**
 * SWEN30006 Project Part C
 * Semester 1, 2018
 * Group 55
 * Jing Kun Ting 792886, Dimosthenis Goulas 762684, Yangxuan Cho 847369
 * 
 * https://codereview.stackexchange.com/questions/38376/a-search-algorithm
 * Dr. Philip Dart said it was okay to use code for path finding according
 * to the post in the discussion forum titled "PathFinding algorithms"
 */


public class AStarPathFinder implements PathFinder {
	
	@SuppressWarnings("hiding")
	final class NodeData<Coordinate> { 

	    private final Coordinate nodeId;
	    private final HashMap<Coordinate, Double> heuristic;

	    private double distance;  // g is distance from the source
	    private double h;  // h is the heuristic of destination.
	    private double f;  // f = g + h 

	    public NodeData (Coordinate nodeId, HashMap<Coordinate, Double> heuristic) {
	        this.nodeId = nodeId;
	        this.distance = Double.MAX_VALUE; 
	        this.heuristic = heuristic;
	    }

	    public Coordinate getNodeId() {
	        return nodeId;
	    }

	    public double getG() {
	        return distance;
	    }

	    public void setG(double g) {
	        this.distance = g;
	    }

	    public void calcF(Coordinate destination) {
	        this.h = heuristic.get(destination);
	        this.f = distance + h;
	    } 

	    public double getH() {
	        return h;
	    }

	    public double getF() {
	        return f;
	    }
	 }

	/**
	 * The graph represents an undirected graph. 
	 * 
	 * @author SERVICE-NOW\ameya.patil
	 *
	 * @param <Coordinate>
	 */
	@SuppressWarnings("hiding")
	final class GraphAStar<Coordinate> implements Iterable<Coordinate> {
	    /*
	     * A map from the nodeId to outgoing edge.
	     * An outgoing edge is represented as a tuple of NodeData and the edge length
	     */
	    private final HashMap<Coordinate, HashMap<NodeData<Coordinate>, Double>> graph;
	    /*
	     * A map of heuristic from a node to each other node in the graph.
	     */
	    private final HashMap<Coordinate, HashMap<Coordinate, Double>> heuristicMap;
	    /*
	     * A map between nodeId and nodedata.
	     */
	    private final HashMap<Coordinate, NodeData<Coordinate>> nodeIdNodeData;

	    public GraphAStar(HashMap<Coordinate, HashMap<Coordinate, Double>> heuristicMap) {
	        if (heuristicMap == null) throw new NullPointerException("The huerisic map should not be null");
	        graph = new HashMap<Coordinate, HashMap<NodeData<Coordinate>, Double>>();
	        nodeIdNodeData = new HashMap<Coordinate, NodeData<Coordinate>>();
	        this.heuristicMap = heuristicMap;
	    } 

	    /**
	     * Adds a new node to the graph.
	     * Internally it creates the nodeData and populates the heuristic map concerning input node into node data.
	     * 
	     * @param nodeId the node to be added
	     */
	    public void addNode(Coordinate nodeId) {
	        if (nodeId == null) throw new NullPointerException("The node cannot be null");
	        if (!heuristicMap.containsKey(nodeId)) throw new NoSuchElementException("This node is not a part of hueristic map");

	        graph.put(nodeId, new HashMap<NodeData<Coordinate>, Double>());
	        nodeIdNodeData.put(nodeId, new NodeData<Coordinate>(nodeId, heuristicMap.get(nodeId)));
	    }

	    /**
	     * Adds an edge from source node to destination node.
	     * There can only be a single edge from source to node.
	     * Adding additional edge would overwrite the value
	     * 
	     * @param nodeIdFirst   the first node to be in the edge
	     * @param nodeIdSecond  the second node to be second node in the edge
	     * @param length        the length of the edge.
	     */
	    public void addEdge(Coordinate nodeIdFirst, Coordinate nodeIdSecond, double length) {
	        if (nodeIdFirst == null || nodeIdSecond == null) throw new NullPointerException("The first nor second node can be null.");

	        if (!heuristicMap.containsKey(nodeIdFirst) || !heuristicMap.containsKey(nodeIdSecond)) {
	            throw new NoSuchElementException("Source and Destination both should be part of the part of hueristic map");
	        }
	        if (!graph.containsKey(nodeIdFirst) || !graph.containsKey(nodeIdSecond)) {
	            throw new NoSuchElementException("Source and Destination both should be part of the part of graph");
	        }

	        graph.get(nodeIdFirst).put(nodeIdNodeData.get(nodeIdSecond), length);
	        graph.get(nodeIdSecond).put(nodeIdNodeData.get(nodeIdFirst), length);
	    }

	    /**
	     * Returns immutable view of the edges
	     * 
	     * @param nodeId    the nodeId whose outgoing edge needs to be returned
	     * @return          An immutable view of edges leaving that node
	     */
	    public HashMap<NodeData<Coordinate>, Double> edgesFrom (Coordinate nodeId) {
	        if (nodeId == null) throw new NullPointerException("The input node should not be null.");
	        if (!heuristicMap.containsKey(nodeId)) throw new NoSuchElementException("This node is not a part of hueristic map");
	        if (!graph.containsKey(nodeId)) throw new NoSuchElementException("The node should not be null.");

	        return (HashMap<NodeData<Coordinate>, Double>) Collections.unmodifiableMap(graph.get(nodeId));
	    }

	    /**
	     * The nodedata corresponding to the current nodeId.
	     * 
	     * @param nodeId    the nodeId to be returned
	     * @return          the nodeData from the 
	     */ 
	    public NodeData<Coordinate> getNodeData (Coordinate nodeId) {
	        if (nodeId == null) { throw new NullPointerException("The nodeid should not be empty"); }
	        if (!nodeIdNodeData.containsKey(nodeId))  { throw new NoSuchElementException("The nodeId does not exist"); }
	        return nodeIdNodeData.get(nodeId);
	    }

	    /**
	     * Returns an iterator that can traverse the nodes of the graph
	     * 
	     * @return an Iterator.
	     */
	    @Override public Iterator<Coordinate> iterator() {
	        return graph.keySet().iterator();
	    }
	}

	@SuppressWarnings("hiding")
	public class AStar<Coordinate> {

	    private final GraphAStar<Coordinate> graph;


	    public AStar (GraphAStar<Coordinate> graphAStar) {
	        this.graph = graphAStar;
	    }

	    // extend comparator.
	    public class NodeComparator implements Comparator<NodeData<Coordinate>> {
	        public int compare(NodeData<Coordinate> nodeFirst, NodeData<Coordinate> nodeSecond) {
	            if (nodeFirst.getF() > nodeSecond.getF()) return 1;
	            if (nodeSecond.getF() > nodeFirst.getF()) return -1;
	            return 0;
	        }
	    } 

	    /**
	     * Implements the A-star algorithm and returns the path from source to destination
	     * 
	     * @param source        the source nodeid
	     * @param destination   the destination nodeid
	     * @return              the path from source to destination
	     */
	    public ArrayList<Coordinate> astar(Coordinate source, Coordinate destination) {
	        /**
	         * http://stackoverflow.com/questions/20344041/why-does-priority-queue-has-default-initial-capacity-of-11
	         */
	        final PriorityQueue<NodeData<Coordinate>> openQueue = new PriorityQueue<NodeData<Coordinate>>(11, new NodeComparator()); 

	        NodeData<Coordinate> sourceNodeData = graph.getNodeData(source);
	        sourceNodeData.setG(0);
	        sourceNodeData.calcF(destination);
	        openQueue.add(sourceNodeData);

	        final HashMap<Coordinate, Coordinate> path = new HashMap<Coordinate, Coordinate>();
	        final HashSet<NodeData<Coordinate>> closedList = new HashSet<NodeData<Coordinate>>();

	        while (!openQueue.isEmpty()) {
	            final NodeData<Coordinate> nodeData = openQueue.poll();

	            if (nodeData.getNodeId().equals(destination)) { 
	                return path(path, destination);
	            }

	            closedList.add(nodeData);

	            for (Entry<NodeData<Coordinate>, Double> neighborEntry : graph.edgesFrom(nodeData.getNodeId()).entrySet()) {
	                NodeData<Coordinate> neighbor = neighborEntry.getKey();

	                if (closedList.contains(neighbor)) continue;

	                double distanceBetweenTwoNodes = neighborEntry.getValue();
	                double tentativeG = distanceBetweenTwoNodes + nodeData.getG();

	                if (tentativeG < neighbor.getG()) {
	                    neighbor.setG(tentativeG);
	                    neighbor.calcF(destination);

	                    path.put(neighbor.getNodeId(), nodeData.getNodeId());
	                    if (!openQueue.contains(neighbor)) {
	                        openQueue.add(neighbor);
	                    }
	                }
	            }
	        }

	        return null;
	    }
	    
	    
	    /**
	     * All hope fails when trying to code this part. It just doesnt seem to work at all
	     * Future improvements would be trying to get this part to work
	     * @param from
	     * @return
	     */
	    public ArrayList<Coordinate> lowestCostExit(Coordinate from) {
			// TODO Auto-generated method stub
			return null;
		}

	    private ArrayList<Coordinate> path(HashMap<Coordinate, Coordinate> path, Coordinate destination) {
	        assert path != null;
	        assert destination != null;

	        final ArrayList<Coordinate> pathList = new ArrayList<Coordinate>();
	        pathList.add(destination);
	        while (path.containsKey(destination)) {
	            destination = path.get(destination);
	            pathList.add(destination);
	        }
	        Collections.reverse(pathList);
	        return pathList;
	    }
	}
	
	/**
	 * Future improvements would be to get this part to work.
	 * At this stage, its too late to change and it would not work.
	 */
	@Override
	public ArrayList<utilities.Coordinate> lowestCostExit(utilities.Coordinate from) {
		// TODO Auto-generated method stub
		return null;
	}

}
