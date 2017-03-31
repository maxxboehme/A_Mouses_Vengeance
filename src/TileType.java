/**
 * 
 * @author Maxx Boehme
 *
 */
public enum TileType {
	Mouse, MouseInHole, Cat, Block, Wall, Cheese, SleepingCat, MouseTrap, SinkHole, YarnBall;
	
	public boolean isBlock(){
		if(this != null){
			return this == Block;
		}
		return false;
	}
	
	public boolean isWall(){
		return this == Wall;
	}
	
	public boolean isCat(){
		return this == Cat || this == SleepingCat;
	}
	
	public boolean canMoveThrough(){
		return !this.isBlock() || !this.isWall() || !this.isCat();
	}
	
	public boolean isMouse(){
		return this == Mouse || this == MouseInHole;
	}
	
	public boolean canYarnBallMoveThrough(){
		return this == SinkHole || this == YarnBall || this == MouseTrap;
	}
}
