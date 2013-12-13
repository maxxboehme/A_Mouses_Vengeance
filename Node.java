import java.awt.*;
import java.util.*;
import java.util.List;

/**
  The AStarNode class, along with the AStarSearch class,
  implements a generic A* search algorithm. The AStarNode
  class should be subclassed to provide searching capability.
 */
public class Node implements Comparable<Node>, Comparator<Node>{

	private Node pathParent;
	private int costFromStart;
	private double estimatedCostToGoal;
	private Point location;
	private TreeSet<Node> neighbors;
	private TileType t;
	private boolean visited;

	Node(Point p){
		location = p;
		neighbors = new TreeSet<Node>();
		costFromStart = 0;
		estimatedCostToGoal = 0;
	}

	Node(int x, int y){
		this(new Point(x, y));
	}

	public double getCost() {
		return costFromStart + estimatedCostToGoal;
	}

	public int getX(){
		return location.x;
	}

	public int getY(){
		return location.y;
	}

	public TileType getTileType() {
		return t;
	}

	public void setTileType(TileType t) {
		this.t = t;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public Node getPathParent() {
		return pathParent;
	}

	public void setPathParent(Node pathParent) {
		this.pathParent = pathParent;
	}

	public boolean addNaughbor(Node n){
		return this.neighbors.add(n);
	}


	public int compareTo(Node other) {
		if(this.getX() < other.getX()){
			return -1;
		} else if(this.getX() > other.getX()){
			return 1;
		} else {
			if(this.getY() < other.getY()){
				return -1;
			} else if(this.getY() > other.getY()){
				return 1;
			} else {
				return 0;
			}
		}
	}
	
//	public int compareTo(Node other) {
//		if(this.getEstimatedCost() < other.getEstimatedCost()){
//			return -1;
//		} else if(this.getEstimatedCost() > other.getEstimatedCost()){
//			return 1;
//		} else {
//			return 0;
//		}
//	}

	public int getCostFromStart(){
		return this.costFromStart;
	}

	public void setCostFromStart(int n){
		this.costFromStart = n;
	}

	public double getEstimatedCost(){
		return this.estimatedCostToGoal;
	}

	public void setEstimatedCost(double e){
		this.estimatedCostToGoal = e;
	}


	/**
    Gets the estimated cost between this node and the
    specified node. The estimated cost should never exceed
    the true cost. The better the estimate, the more
    effecient the search.
	 */
//	public int getEstimatedCost(Node node){
//		return Math.abs(node.getX()- this.getX()) + Math.abs(node.getY() - this.getY());
//	}

	public double getEstimatedCost(Node node){
		return this.location.distance(node.location);
	}

	/**
    Gets the children (AKA "neighbors" or "adjacent nodes")
    of this node.
	 */
	public TreeSet<Node> getNeighbors(){
		return this.neighbors;
	}

	public boolean equals(Object other){
		if(other instanceof Node){
			Node n = (Node)other;
			if(this.location.x == n.location.x && this.location.y == n.location.y){
				if(this.t == n.t){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int compare(Node n1, Node n2) {
		double thisValue = n1.getCost();
		double otherValue = n2.getCost();

		double v = thisValue - otherValue;
		return (v>0)?1:(v<0)?-1:0; // sign function
	}
	
	public String toString(){
		String result = "Node: "+this.t+" "+this.location.x+", "+this.location.y+" cost: "+this.getCost() +" Parent: ";
		if(this.pathParent != null){
			result += this.pathParent.location;
		}
		result += " Neighbors "+neighbors.size()+" : ";
		for(Node n: neighbors){
			result += n.t+" "+n.location.x+", "+n.location.y+"   ";
		}
		return result;
	}
} 
