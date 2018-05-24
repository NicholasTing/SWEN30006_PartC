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
//https://codereview.stackexchange.com/questions/38376/a-search-algorithm
public class AStarPathFinder implements PathFinder {

	@Override
	public ArrayList<Coordinate> lowestCostExit(Coordinate from) {
		// TODO Auto-generated method stub
		return null;
	}
	final class NodeData<T> { 

	    private final T nodeId;
	    private final HashMap<T, Double> heuristic;

	    private double g;  // g is distance from the source
	    private double h;  // h is the heuristic of destination.
	    private double f;  // f = g + h 

	    public NodeData (T nodeId, HashMap<T, Double> heuristic) {
	        this.nodeId = nodeId;
	        this.g = Double.MAX_VALUE; 
	        this.heuristic = heuristic;
	    }

	    public T getNodeId() {
	        return nodeId;
	    }

	    public double getG() {
	        return g;
	    }

	    public void setG(double g) {
	        this.g = g;
	    }

	    public void calcF(T destination) {
	        this.h = heuristic.get(destination);
	        this.f = g + h;
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
	 * @param <T>
	 */
	final class GraphAStar<T> implements Iterable<T> {
	    /*
	     * A map from the nodeId to outgoing edge.
	     * An outgoing edge is represented as a tuple of NodeData and the edge length
	     */
	    private final HashMap<T, HashMap<NodeData<T>, Double>> graph;
	    /*
	     * A map of heuristic from a node to each other node in the graph.
	     */
	    private final HashMap<T, HashMap<T, Double>> heuristicMap;
	    /*
	     * A map between nodeId and nodedata.
	     */
	    private final HashMap<T, NodeData<T>> nodeIdNodeData;

	    public GraphAStar(HashMap<T, HashMap<T, Double>> heuristicMap) {
	        if (heuristicMap == null) throw new NullPointerException("The huerisic map should not be null");
	        graph = new HashMap<T, HashMap<NodeData<T>, Double>>();
	        nodeIdNodeData = new HashMap<T, NodeData<T>>();
	        this.heuristicMap = heuristicMap;
	    } 

	    /**
	     * Adds a new node to the graph.
	     * Internally it creates the nodeData and populates the heuristic map concerning input node into node data.
	     * 
	     * @param nodeId the node to be added
	     */
	    public void addNode(T nodeId) {
	        if (nodeId == null) throw new NullPointerException("The node cannot be null");
	        if (!heuristicMap.containsKey(nodeId)) throw new NoSuchElementException("This node is not a part of hueristic map");

	        graph.put(nodeId, new HashMap<NodeData<T>, Double>());
	        nodeIdNodeData.put(nodeId, new NodeData<T>(nodeId, heuristicMap.get(nodeId)));
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
	    public void addEdge(T nodeIdFirst, T nodeIdSecond, double length) {
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
	    public HashMap<NodeData<T>, Double> edgesFrom (T nodeId) {
	        if (nodeId == null) throw new NullPointerException("The input node should not be null.");
	        if (!heuristicMap.containsKey(nodeId)) throw new NoSuchElementException("This node is not a part of hueristic map");
	        if (!graph.containsKey(nodeId)) throw new NoSuchElementException("The node should not be null.");

	        return (HashMap<NodeData<T>, Double>) Collections.unmodifiableMap(graph.get(nodeId));
	    }

	    /**
	     * The nodedata corresponding to the current nodeId.
	     * 
	     * @param nodeId    the nodeId to be returned
	     * @return          the nodeData from the 
	     */ 
	    public NodeData<T> getNodeData (T nodeId) {
	        if (nodeId == null) { throw new NullPointerException("The nodeid should not be empty"); }
	        if (!nodeIdNodeData.containsKey(nodeId))  { throw new NoSuchElementException("The nodeId does not exist"); }
	        return nodeIdNodeData.get(nodeId);
	    }

	    /**
	     * Returns an iterator that can traverse the nodes of the graph
	     * 
	     * @return an Iterator.
	     */
	    @Override public Iterator<T> iterator() {
	        return graph.keySet().iterator();
	    }
	}

	public class AStar<T> {

	    private final GraphAStar<T> graph;


	    public AStar (GraphAStar<T> graphAStar) {
	        this.graph = graphAStar;
	    }

	    // extend comparator.
	    public class NodeComparator implements Comparator<NodeData<T>> {
	        public int compare(NodeData<T> nodeFirst, NodeData<T> nodeSecond) {
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
	    public ArrayList<T> astar(T source, T destination) {
	        /**
	         * http://stackoverflow.com/questions/20344041/why-does-priority-queue-has-default-initial-capacity-of-11
	         */
	        final PriorityQueue<NodeData<T>> openQueue = new PriorityQueue<NodeData<T>>(11, new NodeComparator()); 

	        NodeData<T> sourceNodeData = graph.getNodeData(source);
	        sourceNodeData.setG(0);
	        sourceNodeData.calcF(destination);
	        openQueue.add(sourceNodeData);

	        final HashMap<T, T> path = new HashMap<T, T>();
	        final HashSet<NodeData<T>> closedList = new HashSet<NodeData<T>>();

	        while (!openQueue.isEmpty()) {
	            final NodeData<T> nodeData = openQueue.poll();

	            if (nodeData.getNodeId().equals(destination)) { 
	                return path(path, destination);
	            }

	            closedList.add(nodeData);

	            for (Entry<NodeData<T>, Double> neighborEntry : graph.edgesFrom(nodeData.getNodeId()).entrySet()) {
	                NodeData<T> neighbor = neighborEntry.getKey();

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


	    private ArrayList<T> path(HashMap<T, T> path, T destination) {
	        assert path != null;
	        assert destination != null;

	        final ArrayList<T> pathList = new ArrayList<T>();
	        pathList.add(destination);
	        while (path.containsKey(destination)) {
	            destination = path.get(destination);
	            pathList.add(destination);
	        }
	        Collections.reverse(pathList);
	        return pathList;
	    }
	}

}
