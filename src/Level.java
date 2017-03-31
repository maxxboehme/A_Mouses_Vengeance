import java.awt.Point;


public class Level {
	
	private TileType[] level;
	
	private final int ROW_COUNT;
	
	private final int COL_COUNT;
	
	private int timePerRound;
	
	private int numOfWallsToGenerate;
	
	private int numOfMouseTrapsToGenerate;
	
	private int numOfHolesToGenerate;
	
	private int numOfBlocksToGenerate;
	
	private int[] numOfCatsPerRound;
	
	private int framesForYarnBallGeneration;
	
	private Point startingLocationOfMouse;
	
	Level(int r, int c){
		ROW_COUNT = r;
		COL_COUNT = c;
		level = new TileType[ROW_COUNT * COL_COUNT];
		this.setBorder();
		
		this.timePerRound = 1;
		this.numOfWallsToGenerate = 0;
		this.numOfHolesToGenerate = 0;
		this.numOfMouseTrapsToGenerate = 0;
		this.numOfBlocksToGenerate = 0;
		this.setFramesForYarnBallGeneration(-1);
		this.numOfCatsPerRound = new int[0];
		this.startingLocationOfMouse = new Point(BoardPanel.COL_COUNT/2, BoardPanel.ROW_COUNT/2);
	}
	
	public void setTimePerRound(int minutes){
		this.timePerRound = minutes;
	}
	
	public int getTimePerRound(){
		return this.timePerRound;
	}
	
	
	public int getNumOfWallsToGenerate() {
		return numOfWallsToGenerate;
	}

	public void setNumOfWallsToGenerate(int numOfWallsToGenerate) {
		this.numOfWallsToGenerate = numOfWallsToGenerate;
	}

	public int getNumOfMouseTrapsToGenerate() {
		return numOfMouseTrapsToGenerate;
	}

	public void setNumOfMouseTrapsToGenerate(int numOfMouseTrapsToGenerate) {
		this.numOfMouseTrapsToGenerate = numOfMouseTrapsToGenerate;
	}

	public int getNumOfHolesToGenerate() {
		return numOfHolesToGenerate;
	}

	public void setNumOfHolesToGenerate(int numOfHolesToGenerate) {
		this.numOfHolesToGenerate = numOfHolesToGenerate;
	}

	public int getNumOfCatsForRound(int round) {
		if(numOfCatsPerRound.length == 0){
			return 0;
		}
		return numOfCatsPerRound[round % numOfCatsPerRound.length];
	}

	public void setNumOfCatsPerRound(int[] numOfCatsPerRound) {
		this.numOfCatsPerRound = numOfCatsPerRound;
	}

	public int getNumOfBlocksToGenerate() {
		return numOfBlocksToGenerate;
	}

	public void setNumOfBlocksToGenerate(int numOfBlocksToGenerate) {
		this.numOfBlocksToGenerate = numOfBlocksToGenerate;
	}
	
	public void setMouseLocation(int x, int y){
		this.startingLocationOfMouse = new Point(x, y);
	}
	
	public void setMouseLocation(Point p){
		this.startingLocationOfMouse = p;
	}
	
	public Point getMousePosition(){
		return this.startingLocationOfMouse;
	}

	/**
	 * Sets the tile at the desired coordinate.
	 * @param point The coordinate of the tile.
	 * @param type The type to set the tile to.
	 */
	public void setTile(Point point, TileType type) {
		setTile(point.x, point.y, type);
	}
	
	/**
	 * Sets the tile at the desired coordinate.
	 * @param x The x coordinate of the tile.
	 * @param y The y coordinate of the tile.
	 * @param type The type to set the tile to.
	 */
	public void setTile(int x, int y, TileType type) {
		level[y * ROW_COUNT + x] = type;
	}
	
	/**
	 * Gets the tile at the desired coordinate.
	 * @param x The x coordinate of the tile.
	 * @param y The y coordinate of the tile.
	 * @return
	 */
	public TileType getTile(int x, int y) {
		return level[y * ROW_COUNT + x];
	}
	
	public int getRowCount(){
		return this.ROW_COUNT;
	}
	
	public int getColCount(){
		return this.COL_COUNT;
	}
	
	public void setBorder(){
		for(int i = 0; i < this.COL_COUNT; i++){
			this.setTile(new Point(i, 0), TileType.Wall);
			this.setTile(new Point(i, this.ROW_COUNT-1), TileType.Wall);
		}
		
		for(int i = 0; i < this.ROW_COUNT; i++){
			this.setTile(new Point(0, i), TileType.Wall);
			this.setTile(new Point(this.COL_COUNT-1, i), TileType.Wall);
		}
	}

	public int getFramesForYarnBallGeneration() {
		return framesForYarnBallGeneration;
	}

	public void setFramesForYarnBallGeneration(int framesForYarnBallGeneration) {
		this.framesForYarnBallGeneration = framesForYarnBallGeneration;
	}

}
