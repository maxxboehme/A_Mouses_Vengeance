import java.awt.Point;


public class YarnBall {
	
	private Point position;
	private Direction direction;
	private int timeTillUpdate;
	private int startingUpdateTime;
	private boolean justCreated;
	
	YarnBall(Point p, Direction d, int sut){
		this.position = p;
		this.direction = d;
		this.startingUpdateTime = sut;
		this.timeTillUpdate = this.startingUpdateTime;
		this.justCreated = true;
	}
	
	YarnBall(int x, int y, Direction d, int sut){
		this(new Point(x, y), d, sut);
	}
	
	YarnBall(){
		this(new Point(0, 0), null, 0);
	}

	public Point getPosition(){
		return this.position;
	}
	
	public void setPosition(Point p){
		this.position = p;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction d) {
		this.direction = d;
	}
	
	public boolean isMoving(){
		if(this.direction == null){
			return false;
		}
		return true;
	}

	public int getTimeTillUpdate() {
		return timeTillUpdate;
	}

	public void setTimeTillUpdate(int timeTillUpdate) {
		this.timeTillUpdate = timeTillUpdate;
	}
	
	public int getStartingUpdateTime(){
		return this.startingUpdateTime;
	}

	public boolean isJustCreated() {
		return justCreated;
	}

	public void setJustCreated(boolean justCreated) {
		this.justCreated = justCreated;
	}
}
