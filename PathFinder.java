import java.util.*;
import java.io.*;
import java.awt.*;

public class PathFinder {

	private TreeMap<Point, Node> board;
	
	
	PathFinder(){
		Comparator<Point> c = new Comparator<Point>(){
			public int compare(Point p1, Point p2) {
				if(p1.x < p2.x){
					return -1;
				} else if(p1.x > p2.x){
					return 1;
				} else {
					if(p1.y < p2.y){
						return -1;
					} else if(p1.y > p2.y){
						return 1;
					} else {
						return 0;
					}
				}
			}
		};
		board = new TreeMap<Point, Node>(c);
	}
	public Node getNode(int x, int y){
		return board.get(new Point(x, y));
	}
	public void parseBoard(BoardPanel bp){
		Comparator<Point> c = new Comparator<Point>(){
			public int compare(Point p1, Point p2) {
				if(p1.x < p2.x){
					return -1;
				} else if(p1.x > p2.x){
					return 1;
				} else {
					if(p1.y < p2.y){
						return -1;
					} else if(p1.y > p2.y){
						return 1;
					} else {
						return 0;
					}
				}
			}
		};
		board = new TreeMap<Point, Node>(c);
		this.generateBoard();
		for(int x = 0; x < BoardPanel.COL_COUNT; x++){
			for(int y = 0; y < BoardPanel.ROW_COUNT; y++){
				Node n = board.get(new Point(x, y));
				n.setTileType(bp.getTile(x, y));
			}
		}
	}
	
	public LinkedList<Node> findPath(Point start, Point goal){
		LinkedList<Node> result = new LinkedList<Node>();
		if(isPath(start, goal)){
			Node n = this.getNode(goal);
			while(n != null){
				result.addFirst(n);
				n = n.getPathParent();
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}
		return result;
	}
	
	public boolean isPath(Point start, Point goal){
		Node startNode = this.getNode(start);
		Node goalNode = this.getNode(goal);
//		System.out.println(startNode);
//		System.out.println(goalNode);
		startNode.setCostFromStart(0);
		startNode.setEstimatedCost(startNode.getEstimatedCost(goalNode));
		startNode.setPathParent(null);
		Comparator<Node> c = new Comparator<Node>(){
			public int compare(Node n1, Node n2){
				double thisValue = n1.getCost();
				double otherValue = n2.getCost();

				double v = thisValue - otherValue;
				return (v>0)?1:(v<0)?-1:0; // sign function
			}
		};
		PriorityQueue<Node> fringe = new PriorityQueue<Node>(20, c);
		fringe.add(startNode);
		boolean found = false;
		while(!fringe.isEmpty() && !found){
			Node n = fringe.remove();
			if(n.equals(goalNode)){
				found = true;
			}else if(!n.isVisited()){
				for(Node neighbor: n.getNeighbors()){
					TileType neighborT = neighbor.getTileType();
					if(neighborT == null || neighborT == TileType.Mouse || neighborT == TileType.MouseInHole){
						if(!neighbor.isVisited()){
							int newCost = n.getCostFromStart()+1;
							if(newCost < neighbor.getCostFromStart()){
								neighbor.setCostFromStart(newCost);
								neighbor.setPathParent(n);
							}
							neighbor.setEstimatedCost(neighbor.getEstimatedCost(goalNode));
							fringe.add(neighbor);
						}
					}
				}
			}
//			try {
//				Thread.sleep(0);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			n.setVisited(true);
		}
		//System.out.println("DONE");
		return found;
	}
	
	public TreeMap<Point, Node> getboard(){
		return board;
	}
	
	public void generateBoard(){
		for(int x = 0; x < BoardPanel.COL_COUNT; x++){
			for(int y = 0; y < BoardPanel.ROW_COUNT; y++){
				Node n = new Node(x, y);
				n.setCostFromStart(Integer.MAX_VALUE);
				n.setEstimatedCost(Integer.MAX_VALUE);
				board.put(new Point(x, y), n);
			}
		}
		
		for(int x = 0; x < BoardPanel.COL_COUNT; x++){
			for(int y = 0; y < BoardPanel.ROW_COUNT; y++){
				Node n = board.get(new Point(x, y));
				if(y > 0){
					n.addNaughbor(board.get(new Point(n.getX(), n.getY()-1)));
				}
				if(y < (BoardPanel.ROW_COUNT-1)){
					n.addNaughbor(board.get(new Point(n.getX(), n.getY()+1)));
				}
				if(x > 0){
					n.addNaughbor(board.get(new Point(n.getX()-1, n.getY())));
				}
				if(x < BoardPanel.ROW_COUNT -1){
					n.addNaughbor(board.get(new Point(n.getX()+1, n.getY())));
				}
			}
		}
		
		for(int x = 0; x < BoardPanel.COL_COUNT; x++){
			for(int y = 0; y < BoardPanel.ROW_COUNT; y++){
				Node n = board.get(new Point(x, y));
				if(y > 0  && x > 0){
					n.addNaughbor(board.get(new Point(n.getX()-1, n.getY()-1)));
				}
				if(y < (BoardPanel.ROW_COUNT-1) && x > 0){
					n.addNaughbor(board.get(new Point(n.getX()-1, n.getY()+1)));
				}
				if(y > 0  && x < (BoardPanel.ROW_COUNT -1)){
					n.addNaughbor(board.get(new Point(n.getX()+1, n.getY()-1)));
				}
				if(y < (BoardPanel.ROW_COUNT-1) && x < (BoardPanel.ROW_COUNT -1)){
					n.addNaughbor(board.get(new Point(n.getX()+1, n.getY()+1)));
				}
			}
		}
		
		
	}
	
	public Node getNode(Point p){
		return board.get(new Point(p.x, p.y));
	}
	
}
