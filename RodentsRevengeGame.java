/**
 *  @author Maxx Boehme
 */

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;

import javax.swing.JPanel;


public class RodentsRevengeGame extends JPanel {

	/**
	 * The Serial Version UID.
	 */
	private static final long serialVersionUID = 1890317935063860966L;


	public static final int FRAMES_PER_SECOND = 24;
	/**
	 * The number of milliseconds that should pass between each frame.
	 */
	private static final long FRAME_TIME = 1000L / FRAMES_PER_SECOND;


	/**
	 * The maximum number of directions that we can have polled in the
	 * direction list.
	 */
	private static final int MAX_DIRECTIONS = 1;

	/**
	 * The BoardPanel instance.
	 */
	private BoardPanel board;

	/**
	 * The TopPanel instance.
	 */
	private TopPanel top;

	/**
	 * The random number generator (used for spawning fruits).
	 */
	private Random random;

	/**
	 * The Updater instance for handling the game logic.
	 */
	private GameState currentState;

	/**
	 * The list that contains the queued directions.
	 */
	private LinkedList<Direction> directions;

	/**
	 * The current score.
	 */
	private int score;

	private int lifeScore;

	/*
	 * The number of fruits that we've eaten.
	 */
	private int fruitsEaten;

	private ArrayList<Level> levels;

	private int levelTimer;

	public static final int FRUITS_PER_LEVEL = 10;

	private static final int LIVES_TO_START = 3;

	private int livesLeft;

	private int setUpTimer;

	private static final int SETUP_TIME = FRAMES_PER_SECOND;

	public static final int TIME_PER_LEVEL = FRAMES_PER_SECOND * 60 * 2;

	private static int catTime = FRAMES_PER_SECOND;

	private int catUpdateClock;

	private Point mouse;

	private final int TIME_MOUSE_HOLE = FRAMES_PER_SECOND*6;

	private int mouseInHole;

	private static final int NUMBER_OF_CATS = 3;

	private LinkedList<Point> cats;

	private LinkedList<YarnBall> yarnBalls;

	private LinkedList<Point> mouseTraps;

	private LinkedList<Point> sinkHoles;

	private int timeTillYarnBallGeneration;

	private PathFinder pf;

	private int deadTime;
	
	private boolean repeatLevel;
	
	private int startingLevel;

	/**
	 * !Important
	 * This variable controles what level the player is on by the hours
	 * and what round we are in using minutes and timer.
	 */
	private GameClock gameClock;

	private int currentLevel;

	private int currentRound;


	/*
	 * Creates a new SnakeGame instance. Creates a new window,
	 * and sets up the controller input.
	 */
	RodentsRevengeGame() {
		setLayout(new BorderLayout());
		this.setBorder(null);
		pf = new PathFinder();
		pf.generateBoard();

		this.gameClock = new GameClock(RodentsRevengeGame.FRAMES_PER_SECOND);
		this.gameClock.setTime(0, 0, 0);
		/*
		 * Initialize the game's panels and add them to the window.
		 */
		this.board = new BoardPanel(this);
		this.board.setVisible(true);
		this.top = new TopPanel(this);
		this.top.setVisible(true);
		this.random = new Random();
		this.timeTillYarnBallGeneration = -1;
		this.directions = new LinkedList<Direction>();
		currentState = GameState.NewGame;
		this.deadTime = board.NUM_DEAD_FRAMES;
		this.score = 0;

		this.mouseTraps = new LinkedList<Point>();
		this.sinkHoles = new LinkedList<Point>();
		this.repeatLevel = false;
		this.startingLevel = 0;

		fruitsEaten = 0;
		currentLevel = 0;
		levels = this.generateLevels();
		levelTimer = RodentsRevengeGame.TIME_PER_LEVEL;

		add(board, BorderLayout.CENTER);
		add(top, BorderLayout.NORTH);
		this.cats = new LinkedList<Point>();
		this.yarnBalls = new LinkedList<YarnBall>();
		setVisible(true);
	}

	public void pause(){
		if(currentState == GameState.Running) {
			currentState = GameState.Paused;
		} else if(currentState == GameState.Paused){
			currentState = GameState.Running;
		}
	}

	public void enter() {
		if(currentState == GameState.NewGame || currentState == GameState.Won
				|| currentState == GameState.GameOver) {
			resetGame();
		}
	}
	
	public void newGame() {
		resetGame();
		currentState = GameState.NewGame;
	}

	public void turn(Direction d){
		if(currentState == GameState.Running) {
			if(directions.size() < MAX_DIRECTIONS) {
				directions.add(d);
			}
		}
	}

	/**
	 * Starts the game running.
	 */
	public void startGame() {

		board.boardFromLevel(this.levels.get(currentLevel));
		this.resetGame();
		this.currentState = GameState.NewGame;
		/*
		 * This is the game loop. It will update and render the game and will
		 * continue to run until the game window is closed.
		 */
		while(true) {
			//Get the current frame's start time.
			long start = System.currentTimeMillis();

			/*
			 * If a cycle has elapsed on the logic timer, then update the game.
			 */
			//if(updateClock.hasElapsedCycle()) {
			if(currentState == GameState.SetUp){
				this.setUpTimer--;
				if(this.setUpTimer < 0){
					currentState = GameState.Running;
				}
			} else if(currentState.canUpdateGame()){
				updateGame();
			}
			//}

			//System.out.println("CurrentState: "+currentState+" setting up: "+this.isSettingUp());
			//Repaint the board and side panel with the new content.
			board.repaint();
			top.repaint();

			/*
			 * Calculate the delta time between since the start of the frame
			 * and sleep for the excess time to cap the frame rate. While not
			 * incredibly accurate, it is sufficient for our purposes.
			 */
			long delta = (System.currentTimeMillis() - start);
			//System.out.println("Delta: "+delta);
			if(delta < FRAME_TIME) {
				try {
					Thread.sleep(FRAME_TIME - delta);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Updates the game's logic.
	 */
	private void updateGame() {
		if(currentState == GameState.Dead){
			if(--this.deadTime == 0){
				this.deadTime = board.NUM_DEAD_FRAMES;
				if(this.livesLeft <= 0){
					this.currentState = GameState.GameOver;
				} else {
					this.resetRound();
				}
			}
		} else {
			int minBefore = this.gameClock.getMinutes();
			if(this.cats.size() == 0){
				this.gameClock.increment(this.FRAMES_PER_SECOND*10);
			} else {
				this.gameClock.increment();
			}
			int minAfter = this.gameClock.getMinutes();
			if(minBefore != minAfter){
				score++;
			}
			Level l = this.levels.get(this.currentLevel);
			int numOfRoundsInLevel = 30/l.getTimePerRound();
			boolean needToMoveOn = this.currentRound == numOfRoundsInLevel;
			boolean nextLevel = needToMoveOn && this.cats.size() == 0;
			boolean newRound = false;


			//System.out.println("currentRound: "+this.currentRound+" numRounds: "+numOfRoundsInLevel);
			//System.out.println("needToMoveOn: "+needToMoveOn+" NextLeve: "+nextLevel);

			if(this.gameClock.getMinutes() == this.gameClock.getTimer() && !needToMoveOn){
				newRound = true;
				System.out.println("increastedRounds");
				this.currentRound++;
				needToMoveOn = this.currentRound == numOfRoundsInLevel;
				nextLevel = needToMoveOn && this.cats.size() == 0;
				if(!needToMoveOn){
					this.gameClock.setTimer(this.gameClock.getTimer()+l.getTimePerRound());
				}
			}

			if(needToMoveOn && !nextLevel &&  newRound){
				this.gameClock.setTime(this.gameClock.getSeconds(), 0, this.currentLevel);
			}

			if(needToMoveOn && !nextLevel &&  this.gameClock.getMinutes() > 0){
				this.score--;
				this.gameClock.setTime(this.gameClock.getSeconds(), 0, this.currentLevel);
			}

			if(nextLevel){
				this.nextLevel();
			} else if(newRound && !needToMoveOn){
				this.gameClock.setTime(0, 0, this.gameClock.getMinutes(), this.gameClock.getHours());
				this.generateCats(l.getNumOfCatsForRound(this.currentRound));
				this.catUpdateClock = 0;
			}else {
				/*
				 * Gets the type of tile that the head of the snake collided with. If 
				 * the snake hit a wall, SnakeBody will be returned, as both conditions
				 * are handled identically.
				 */
				if(this.mouseInHole > 0){
					this.mouseInHole--;
					if(directions.size() > 0){
						directions.clear();
					}
				} else {
					TileType collision = updateMouse();
					/*
					 * Here we handle the different possible collisions.
					 * 
					 * Fruit: If we collided with a fruit, we increment the number of
					 * fruits that we've eaten, update the score, and spawn a new fruit.
					 * 
					 * SnakeBody: If we collided with our tail (or a wall), we flag that
					 * the game is over and pause the game.
					 * 
					 * If no collision occurred, we simply decrement the number of points
					 * that the next fruit will give us if it's high enough. This adds a
					 * bit of skill to the game as collecting fruits more quickly will
					 * yield a higher score.
					 */
					if(collision == TileType.Cat || collision == TileType.MouseTrap) {
						this.livesLeft--;
						this.mouseTraps.remove(this.mouse);
						//					if(this.livesLeft <= 0){
						//						this.currentState = GameState.GameOver;
						//					} else {
						//						this.resetRound();
						//					}
						this.currentState = GameState.Dead;this.currentState = GameState.Dead;
					} else if(collision == TileType.SinkHole){
						this.sinkHoles.remove(this.mouse);
						this.mouseInHole = this.TIME_MOUSE_HOLE;
					} else if(collision == TileType.Cheese){
						this.score += 100;
					}
				}

				this.catUpdateClock++;
				//System.out.println("catUpdateClock; "+this.catUpdateClock);
				if(this.catUpdateClock >= this.catTime){
					int sleepingCats = 0;
					for(Point p: this.cats){
						TileType catCollision = updateCat(p);
						if(catCollision == TileType.Mouse || catCollision == TileType.MouseInHole){
							this.livesLeft--;
							this.currentState = GameState.Dead;
						} else if(catCollision == TileType.Cat){
							sleepingCats++;
						}
					}
					this.catUpdateClock = 0;
					if(sleepingCats == this.cats.size()){
						for(Point p: this.cats){
							this.board.setTile(p, TileType.Cheese);
						}
						this.cats.clear();
					}
				}

				if(this.timeTillYarnBallGeneration == 0){
					this.generateYarnBall();
					int time = this.levels.get(currentLevel).getFramesForYarnBallGeneration();
					this.timeTillYarnBallGeneration = random.nextInt(time)+time;
				} else if(this.timeTillYarnBallGeneration > 0){
					this.timeTillYarnBallGeneration--;
				}

				Iterator<YarnBall> it = this.yarnBalls.iterator();
				while(it.hasNext()){
					YarnBall yb = it.next();
					TileType yarnCollision = this.updateYarnBall(yb);
					if(yarnCollision == TileType.Mouse || yarnCollision == TileType.MouseInHole){
						it.remove();
						this.livesLeft--;
						this.currentState = GameState.Dead;
					} else if(yarnCollision == TileType.YarnBall){
						it.remove();
					}
				}

				for(Point p: this.mouseTraps){
					if(board.getTile(p.x, p.y) == null){
						board.setTile(p, TileType.MouseTrap);
					}
				}
				for(Point p: this.sinkHoles){
					if(board.getTile(p.x, p.y) == null){
						board.setTile(p, TileType.SinkHole);
					}
				}
			}
		}
	}

	private void generateYarnBall(){
		this.yarnBalls.addLast(new YarnBall(this.getRandomYarnPosition(), null, this.FRAMES_PER_SECOND));
	}

	private Point getRandomYarnPosition(){
		int maxPositions = BoardPanel.ROW_COUNT*2 + (BoardPanel.COL_COUNT-2)*2;
		int index = random.nextInt(maxPositions);

		int freeFound = -1;
		for(int i = 0; i < 2; i++){
			for(int x = 0; x < BoardPanel.COL_COUNT; x++){
				if(++freeFound == index) {
					return new Point(x, (BoardPanel.COL_COUNT-1)*i);
				}
			}
		}

		for(int i = 0; i < 2; i++){
			for(int y = 1; y < BoardPanel.ROW_COUNT-1; y++){
				if(++freeFound == index) {
					return new Point((BoardPanel.ROW_COUNT-1)*i, y);
				}
			}
		}

		return new Point(0, 0);
	}

	private void testScore(int score){
		if(this.lifeScore >= 100){
			//System.out.println("Score: "+this.score+" LIfe: "+this.lifeScore);
			this.livesLeft++;
			this.lifeScore -= 100;
			testScore(this.lifeScore); // for
			//System.out.println("Score: "+this.score);
		}
	}

	private TileType updateYarnBall(YarnBall yb){
		TileType result = null;
		if(!yb.isMoving()){
			int timeTillUpdate = yb.getTimeTillUpdate();
			if(timeTillUpdate == 0){
				int rand = random.nextInt(6);
				//System.out.println("Random: "+rand);
				if(yb.isJustCreated() || rand > 0){
					LinkedList<Point> points = this.freePointsAround(yb.getPosition());
					if(points.isEmpty()){
						result = TileType.YarnBall;
						if(yb.isJustCreated()){
							board.setTile(yb.getPosition(), TileType.Wall);
						} else {
							board.setTile(yb.getPosition(), null);
						}
					} else {
						yb.setDirection(direction(yb.getPosition(), points.get(random.nextInt(points.size()))));
						board.setTile(yb.getPosition(), TileType.YarnBall);
					}
				} else {
					board.setTile(yb.getPosition(), null);
					result = TileType.YarnBall;
				}
			} else {
				yb.setTimeTillUpdate(timeTillUpdate-1);
				board.setTile(yb.getPosition(), TileType.YarnBall);
			}
		} else {
			boolean wasJustCreated = yb.isJustCreated();
			boolean moved = true;
			Direction d = yb.getDirection();
			Point pd = direction(d);
			Point originalLocation = (Point)yb.getPosition().clone();
			Point location = (Point)yb.getPosition().clone();
			location.translate(pd.x, pd.y);
			if(location.x >0 && location.x < BoardPanel.COL_COUNT-1 && location.y > 0 && location.y < BoardPanel.ROW_COUNT-1){
				result = board.getTile(location.x, location.y);
				if(result == null || result.canYarnBallMoveThrough()){
					board.setTile(yb.getPosition(), null);
					yb.setPosition(location);
					board.setTile(yb.getPosition(), TileType.YarnBall);
					result = null;
					yb.setJustCreated(false);
				} else if(result == TileType.Mouse){
					board.setTile(yb.getPosition(), null);
					yb.setPosition(location);
					board.setTile(yb.getPosition(), null);
					yb.setJustCreated(false);
				} else if(result == TileType.YarnBall){
					board.setTile(yb.getPosition(), null);
					yb.setPosition(location);
					yb.setJustCreated(false);
					result = null;
				} else {
					moved = false;
					yb.setDirection(null);
					board.setTile(yb.getPosition(), TileType.YarnBall);
					yb.setTimeTillUpdate(yb.getStartingUpdateTime());
				}
			} else {
				moved = false;
				yb.setDirection(null);
				board.setTile(yb.getPosition(), TileType.YarnBall);
				yb.setTimeTillUpdate(yb.getStartingUpdateTime());
			}
			if(wasJustCreated && moved){
				board.setTile(originalLocation, TileType.Wall);
			}
		}
		return result;
	}

	/**
	 * Updates the snake's position and size.
	 * @return Tile tile that the head moved into.
	 */
	private TileType updateMouse() {
		TileType old = null;
		if(this.directions.size() > 0){

			/*
			 * Here we peek at the next direction rather than polling it. While
			 * not game breaking, polling the direction here causes a small bug
			 * where the snake's direction will change after a game over (though
			 * it will not move).
			 */
			Direction direction = directions.removeFirst();

			/*
			 * Here we calculate the new point that the snake's head will be at
			 * after the update.
			 */		
			Point mouse = (Point) this.mouse.clone();
			Point d = RodentsRevengeGame.direction(direction);
			mouse.translate(d.x, d.y);

			/*
			 * If the snake has moved out of bounds ('hit' a wall), we can just
			 * return that it's collided with itself, as both cases are handled
			 * identically.
			 */
			if(mouse.x < 0 || mouse.x >= BoardPanel.COL_COUNT || mouse.y < 0 || mouse.y >= BoardPanel.ROW_COUNT) {
				return null; //Pretend we collided with our body.
			}

			//		if(board.getTile(head.x, head.y) == TileType.Exit){
			//			return TileType.Exit;
			//		}

			/*
			 * Here we get the tile that was located at the new head position and
			 * remove the tail from of the snake and the board if the snake is
			 * long enough, and the tile it moved onto is not a fruit.
			 * 
			 * If the tail was removed, we need to retrieve the old tile again
			 * incase the tile we hit was the tail piece that was just removed
			 * to prevent a false game over.
			 */
			old = board.getTile(mouse.x, mouse.y);

			if(old == TileType.Cat){
				board.setTile(this.mouse, null);
				this.mouse = mouse;
			} else if(old == TileType.Cheese){
				board.setTile(this.mouse, null);
				this.mouse = mouse;
				board.setTile(this.mouse, TileType.Mouse);
			} else if(old == TileType.SinkHole){
				board.setTile(this.mouse, null);
				this.mouse = mouse;
				board.setTile(this.mouse, TileType.MouseInHole);
			} else if(old == TileType.MouseTrap){
				board.setTile(this.mouse, null);
				this.mouse = mouse;
				board.setTile(mouse, null);
				return TileType.MouseTrap;
			} else if(old == TileType.Block) {
				if(canMoveBlock(this.mouse, direction)){
					board.setTile(this.mouse, null);
					this.mouse = mouse;
					board.setTile(this.mouse, TileType.Mouse);
				}
			} else if(old == null){
				board.setTile(this.mouse, null);
				this.mouse = mouse;
				board.setTile(this.mouse, TileType.Mouse);
			}
		} else {
			board.setTile(this.mouse, TileType.Mouse);
		}

		return old;
	}

	private void replaceMouse(){
		int maxEmpty = board.getNumberOfEmptySpaces()-24*this.cats.size();
		int index = random.nextInt(maxEmpty);

		int freeFound = -1;
		for(int x = 0; x < BoardPanel.COL_COUNT; x++) {
			for(int y = 0; y < BoardPanel.ROW_COUNT; y++) {
				double distance = Double.MAX_VALUE;
				for(int i = 0; i < this.cats.size() && distance>=3; i++){
					distance = Math.min(distance, this.cats.get(i).distance(x, y));
				}
				if(distance >= 3){
					TileType type = board.getTile(x, y);
					if(type == null) {
						if(++freeFound == index) {
							board.setTile(x, y, TileType.Cat);
							this.mouse.setLocation(new Point(x, y));
							break;
						}
					}
				}
			}
		}
	}

	private TileType updateCat2(Point p){
		TileType result = null;
		pf.parseBoard(this.board);
		LinkedList<Node> path = pf.findPath(p, this.mouse);
		//System.out.println("Path: "+path);
		if(path.size() >= 2){
			board.setTile(p, null);
			//System.out.println("MOVE: "+path.get(1));
			p.x = path.get(1).getX();
			p.y = path.get(1).getY();
			result = board.getTile(p.x, p.y);
			board.setTile(p, TileType.Cat);
		} 
		return result;
	}

	private TileType updateCat(Point p){
		TileType result = null;
		LinkedList<Point> points = freePointsAround(p);
		if(points.size() == 0){
			board.setTile(p, TileType.SleepingCat);
			result = TileType.Cat;
		} else {
			pf.parseBoard(this.board);
			LinkedList<Node> path = pf.findPath(p, this.mouse);
			//System.out.println("Path: "+path);
			if(path.size() >= 2){
				board.setTile(p, null);
				//System.out.println("MOVE: "+path.get(1));
				p.x = path.get(1).getX();
				p.y = path.get(1).getY();
				result = board.getTile(p.x, p.y);
				board.setTile(p, TileType.Cat);
			} else {
				int someRand = this.random.nextInt(3);
				if(someRand == 0){
					Point move = points.get(this.random.nextInt(points.size()));
					board.setTile(p, null);
					p.x = move.x;
					p.y = move.y;
					result = board.getTile(p.x, p.y);
					board.setTile(p, TileType.Cat);
				} else {
					Point move = this.moveCat(p, points);
					//System.out.println("Move: "+move.x+", "+move.y);
					//System.out.println("Point: "+p.x+", "+p.y);
					if(move != null){
						board.setTile(p, null);
						p.x = move.x;
						p.y = move.y;
						result = board.getTile(p.x, p.y);
						board.setTile(p, TileType.Cat);
					}
				}
			}
		}
		return result;
	}

	private Point moveCat(Point p, LinkedList<Point> points){
		Point closestPoint = null;
		LinkedList<Point> closestPoints = new LinkedList<Point>();
		double closestDistance = Double.MAX_VALUE;
		for(Point freePoint: points){
			double distance = freePoint.distance(this.mouse);
			if(distance < closestDistance){
				closestPoints.clear();
				closestPoints.add(freePoint);
				closestDistance = distance;
			} else if(distance == closestDistance){
				closestPoints.add(freePoint);
			}
		}
		if(closestPoints.size() == 1){
			closestPoint = closestPoints.removeFirst();
		} else {
			closestPoint = closestPoints.get(this.random.nextInt(closestPoints.size()));
		}
		return closestPoint;
	}

	private LinkedList<Point> freePointsAround(Point p){
		LinkedList<Point> result = new LinkedList<Point>();
		if(p.x > 0){
			TileType t = board.getTile(p.x-1, p.y);
			if(t == null || t== TileType.Mouse){
				result.add(new Point(p.x-1, p.y));
			}
		}
		if(p.x < BoardPanel.COL_COUNT-1){
			TileType t = board.getTile(p.x+1, p.y);
			if(t == null || t== TileType.Mouse){
				result.add(new Point(p.x+1, p.y));
			}
		}
		if(p.y > 0){
			TileType t = board.getTile(p.x, p.y-1);
			if(t == null || t== TileType.Mouse){
				result.add(new Point(p.x, p.y-1));
			}
		}
		if(p.y < BoardPanel.ROW_COUNT-1){
			TileType t = board.getTile(p.x, p.y+1);
			if(t == null || t== TileType.Mouse){
				result.add(new Point(p.x, p.y+1));
			}
		}
		if(p.x > 0 && p.y > 0){
			TileType t = board.getTile(p.x-1, p.y-1);
			if(t == null || t== TileType.Mouse){
				result.add(new Point(p.x-1, p.y-1));
			}
		}
		if(p.x > 0 && p.y < BoardPanel.ROW_COUNT -1){
			TileType t = board.getTile(p.x-1, p.y+1);
			if(t == null || t== TileType.Mouse){
				result.add(new Point(p.x-1, p.y+1));
			}
		}
		if(p.x < BoardPanel.COL_COUNT -1 && p.y > 0){
			TileType t = board.getTile(p.x+1, p.y-1);
			if(t == null || t== TileType.Mouse){
				result.add(new Point(p.x+1, p.y-1));
			}
		}

		if(p.x < BoardPanel.COL_COUNT -1 && p.y < BoardPanel.ROW_COUNT -1){
			TileType t = board.getTile(p.x+1, p.y+1);
			if(t == null || t== TileType.Mouse){
				result.add(new Point(p.x+1, p.y+1));
			}
		}
		return result;
	}

	private boolean canMoveBlock(Point p, Direction direction){
		boolean result = false;
		Point d = RodentsRevengeGame.direction(direction);
		//System.out.println("POINT: "+p+" Direction: "+d);
		Point newPoint = new Point(p.x + d.x, p.y + d.y);
		if(newPoint.x < 0 || newPoint.x >= BoardPanel.COL_COUNT || newPoint.y < 0 || newPoint.y >= BoardPanel.ROW_COUNT){
			return false;
		}
		TileType pt = board.getTile(p.x, p.y);
		TileType newpt = board.getTile(newPoint.x, newPoint.y);
		//System.out.println("Point: "+p+" TileType: "+pt);
		//System.out.println("NEWPOINT: "+newPoint+" TileType: "+newpt);

		if(newpt == TileType.Wall){
			result = false;
		} else if(newpt == TileType.MouseTrap){
			result = false;
		} else if(newpt == TileType.YarnBall){
			result = false;
		} else if(newpt == null || newpt == TileType.Cheese){
			board.setTile(newPoint, pt);
			if(pt == TileType.Cat){
				for(Point cat: this.cats){
					if(cat.x == p.x && cat.y == p.y){
						cat.x = newPoint.x;
						cat.y = newPoint.y;
					}
				}
			}
			result = true;
		} else {
			if(pt == TileType.Cat){
				if(newpt == TileType.Cat){
					if(canMoveBlock(newPoint, direction)){
						//if(pt == TileType.Cat){
						for(Point cat: this.cats){
							if(cat.x == p.x && cat.y == p.y){
								cat.x = newPoint.x;
								cat.y = newPoint.y;
							}
						}
						//}
						board.setTile(newPoint, pt);
						result = true;
					}
				} else {
					result = false;
				}
			} else {
				if(newpt == TileType.Cat){
					if(canMoveBlock(newPoint, direction)){
						if(pt == TileType.Cat){
							for(Point cat: this.cats){
								if(cat.x == p.x && cat.y == p.y){
									cat.x = newPoint.x;
									cat.y = newPoint.y;
								}
							}
						}
						board.setTile(newPoint, pt);
						result = true;;
					}
				} else if(newpt == TileType.Block){
					if(canMoveBlock(newPoint, direction)){
						//System.out.println(pt);
						if(pt == TileType.Cat){
							for(Point cat: this.cats){
								if(cat.x == p.x && cat.y == p.y){
									cat.x = newPoint.x;
									cat.y = newPoint.y;
								}
							}
						}
						board.setTile(newPoint, pt);
						result = true;
					}
				} else if(newpt == TileType.SinkHole){
					return true;
				}
			}
		}
		return result;
	}

	private static Point direction(Direction d){
		Point p = new Point();
		switch(d) {
		case North:
			p.y--;
			break;

		case South:
			p.y++;
			break;

		case West:
			p.x--;
			break;

		case East:
			p.x++;
			break;
		case NorthEast:
			p.y--;
			p.x++;
			break;
		case NorthWest:
			p.y--;
			p.x--;
			break;
		case SouthEast:
			p.y++;
			p.x++;
			break;
		case SouthWest:
			p.y++;
			p.x--;
			break;
		default:
			break;
		}
		return p;
	}

	private static Direction direction(Point at, Point to){
		Direction d = null;
		Point p = new Point(to.x - at.x, to.y - at.y);
		if(p.x < 0){
			if(p.y < 0){
				d = Direction.NorthWest;
			} else if(p.y > 0){
				d = Direction.SouthWest;
			} else {
				d = Direction.West;
			}
		} else if(p.x > 0){
			if(p.y < 0){
				d = Direction.NorthEast;
			} else if(p.y > 0){
				d = Direction.SouthEast;
			} else {
				d = Direction.East;
			}
		} else {
			if(p.y < 0){
				d = Direction.North;
			} else if(p.y > 0){
				d = Direction.South;
			} else {
				d = null;
			}
		}
		return d;
	}

	/**
	 * Resets the game's variables to their default states and starts a new game.
	 */
	private void nextLevel() {

		this.score += 10;
		this.lifeScore += 10;
		this.testScore(this.lifeScore);

		/*
		 * Create the head at the center of the board.
		 */

		currentState = GameState.SetUp;

		this.setUpTimer = RodentsRevengeGame.SETUP_TIME;

		this.mouseInHole = 0;
		
		if(!repeatLevel){
			this.currentLevel++;
		}
		levelTimer = RodentsRevengeGame.TIME_PER_LEVEL;
		if(currentLevel < levels.size()){
			this.mouse = this.levels.get(this.currentLevel).getMousePosition();
			/*
			 * Clear the board and add the head.
			 */
			board.clearBoard();
			board.boardFromLevel(levels.get(currentLevel));
			this.mouseTraps.clear();
			this.sinkHoles.clear();
			this.generateLevel();
			board.setTile(this.mouse, TileType.Mouse);


			this.yarnBalls.clear();
			this.timeTillYarnBallGeneration = this.levels.get(currentLevel).getFramesForYarnBallGeneration();

			this.currentRound = 0;
			this.cats.clear();
			this.generateCats(levels.get(currentLevel).getNumOfCatsForRound(currentRound));
			for(int i = 0; i < cats.size(); i++){
				board.setTile(this.cats.get(i), TileType.Cat);
			}
			catUpdateClock = 0;
			this.gameClock.setTime(0, 0, this.currentLevel);
			this.gameClock.setTimer(levels.get(currentLevel).getTimePerRound());

			/*
			 * Clear the directions and add north as the
			 * default direction.
			 */
			directions.clear();

			for(int i = 0; i < cats.size(); i++){
				board.setTile(this.cats.get(i), TileType.Cat);
			}

		} else {
			currentState = GameState.Won;
		}
	}

	private void generateLevel(){
		Level l = this.levels.get(this.currentLevel);
		this.generateTilesNoneWallSpaces(l.getNumOfWallsToGenerate(), TileType.Wall);
		this.generateTilesNullBlockSpaces(l.getNumOfHolesToGenerate(), TileType.SinkHole, this.sinkHoles);
		this.generateTilesNullBlockSpaces(l.getNumOfMouseTrapsToGenerate(), TileType.MouseTrap, this.mouseTraps);
		this.generateTilesEmptySpaces(l.getNumOfBlocksToGenerate(), TileType.Block);
	}

	private void resetRound(){

		/*
		 * Create the head at the center of the board.
		 */
		this.replaceMouse();
		board.setTile(this.mouse, TileType.Mouse);

		currentState = GameState.SetUp;

		this.setUpTimer = RodentsRevengeGame.SETUP_TIME;
		this.mouseInHole = 0;


		levelTimer = RodentsRevengeGame.TIME_PER_LEVEL;

		/*
		 * Clear the board and add the head.
		 */

		catUpdateClock = 0;
		this.gameClock.setTime(0, gameClock.getSeconds(), gameClock.getMinutes(), gameClock.getHours());

		/*
		 * Clear the directions and add north as the
		 * default direction.
		 */
		directions.clear();
	}

	/**
	 * Resets the game's variables to their default states and starts a new game.
	 */
	private void resetGame() {
		/*
		 * Reset the score statistics. (Note that nextFruitPoints is reset in
		 * the spawnFruit function later on).
		 */
		this.score = 0;
		this.lifeScore = 50;
		this.fruitsEaten = 0;

		/*
		 * Reset both the new game and game over flags.
		 */
		currentState = GameState.SetUp;

		this.setUpTimer = RodentsRevengeGame.SETUP_TIME;

		if(startingLevel > 0){
			this.currentLevel = startingLevel;
			startingLevel = 0;
		} else {
			this.currentLevel = 0;
		}

		this.levelTimer = RodentsRevengeGame.TIME_PER_LEVEL;

		this.livesLeft = RodentsRevengeGame.LIVES_TO_START;

		this.mouseInHole = 0;

		/*
		 * Create the head at the center of the board.
		 */
		this.mouse = this.levels.get(this.currentLevel).getMousePosition();

		this.yarnBalls.clear();

		this.timeTillYarnBallGeneration = this.levels.get(this.currentLevel).getFramesForYarnBallGeneration();

		//		this.yarnBalls.addLast(new YarnBall(0, 0, null, this.FRAMES_PER_SECOND));

		/*
		 * Clear the snake list and add the head.
		 */

		/*
		 * Clear the board and add the head.
		 */
		board.clearBoard();

		board.boardFromLevel(levels.get(currentLevel));
		this.mouseTraps.clear();
		this.sinkHoles.clear();
		this.generateLevel();
		board.setTile(this.mouse, TileType.Mouse);

		this.currentRound = 0;
		this.cats.clear();
		this.generateCats(levels.get(currentLevel).getNumOfCatsForRound(currentRound));
		for(int i = 0; i < cats.size(); i++){
			board.setTile(this.cats.get(i), TileType.Cat);
		}
		catUpdateClock = 0;
		this.gameClock.setTime(0, 0, 0, 0);
		this.gameClock.setTimer(levels.get(currentLevel).getTimePerRound());

		/*
		 * Clear the directions and add north as the
		 * default direction.
		 */
		directions.clear();
	}

	/**
	 * Gets the flag that indicates whether or not we're playing a new game.
	 * @return The new game flag.
	 */
	public boolean isNewGame() {
		return currentState == GameState.NewGame;
	}

	/**
	 * Gets the flag that indicates whether or not the game is over.
	 * @return The game over flag.
	 */
	public boolean hasWon() {
		return currentState == GameState.Won;
	}

	/**
	 * Gets the flag that indicates whether or not the game is over.
	 * @return The game over flag.
	 */
	public boolean isGameOver() {
		return currentState == GameState.GameOver;
	}

	/**
	 * Gets the flag that indicates whether or not the game is over.
	 * @return The game over flag.
	 */
	public boolean isSettingUp() {
		return currentState == GameState.SetUp;
	}

	/**
	 * Gets the flag that indicates whether or not the game is paused.
	 * @return The paused flag.
	 */
	public boolean isPaused() {
		return currentState == GameState.Paused;
	}


	/**
	 * Gets the current score.
	 * @return The score.
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Gets the number of fruits eaten.
	 * @return The fruits eaten.
	 */
	public int getFruitsEaten() {
		return fruitsEaten;
	}

	/**
	 * Gets the current direction of the snake.
	 * @return The current direction.
	 */
	public Direction getDirection() {
		return directions.peek();
	}
	
	public void repeatLevel(boolean b){
		this.repeatLevel = b;
	}


	private ArrayList<Level> generateLevels(){
		ArrayList<Level> result = new ArrayList<Level>();
		Level l1 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l2 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l3 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l4 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l5 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l6 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l7 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l8 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l9 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l10 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l11 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l12 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l13 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l14 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l15 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l16 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l17 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l18 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l19 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);
		Level l20 = new Level(BoardPanel.COL_COUNT, BoardPanel.ROW_COUNT);

		int ygap = BoardPanel.ROW_COUNT/5;
		int xgap = BoardPanel.COL_COUNT/5;

		for(int i = ygap; i < BoardPanel.ROW_COUNT-ygap; i++){
			for(int j = xgap; j < BoardPanel.COL_COUNT-xgap; j++){
				l1.setTile(new Point(i, j), TileType.Block);
			}
		}
		//		l1.setTile(new Point(BoardPanel.COL_COUNT/2, 3), TileType.SinkHole);
		//l1.setTile(new Point(BoardPanel.COL_COUNT/2, 3), TileType.MouseTrap);
		l1.setNumOfCatsPerRound(new int[]{1, 2});
		l1.setTimePerRound(5);

		// Level 2
		for(int i = ygap; i < BoardPanel.ROW_COUNT-ygap; i++){
			for(int j = xgap; j < BoardPanel.COL_COUNT-xgap; j++){
				l2.setTile(new Point(i, j), TileType.Block);
			}
		}
		l2.setNumOfWallsToGenerate(20);
		l2.setNumOfCatsPerRound(new int[]{1, 2});
		l2.setTimePerRound(5);

		// Level 3
		l3.setNumOfWallsToGenerate(20);
		l3.setNumOfBlocksToGenerate(150);
		l3.setNumOfCatsPerRound(new int[]{1, 2});
		l3.setTimePerRound(5);

		// Level 4
		for(int i = 2; i < BoardPanel.COL_COUNT-2; i++){
			for(int j = 2; j < BoardPanel.ROW_COUNT-2; j++){
				int off = j+1%2;
				if((i+off)%2 == 0){
					l4.setTile(new Point(i, j), TileType.Block);
				}
			}
		}
		l4.setNumOfHolesToGenerate(5);
		l4.setNumOfCatsPerRound(new int[]{1, 2});
		l4.setTimePerRound(5);

		// Level 5
		l5.setNumOfWallsToGenerate(64);
		l5.setNumOfBlocksToGenerate(64);
		l5.setNumOfHolesToGenerate(11);
		l5.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*10);
		l5.setNumOfCatsPerRound(new int[]{1, 2});
		l5.setTimePerRound(5);

		// Level 6
		for(int i = 4; i < BoardPanel.COL_COUNT-4; i++){
			for(int j = 4; j < BoardPanel.ROW_COUNT-4; j++){
				int off = j+1%2;
				if((i+off)%2 == 0){
					l6.setTile(new Point(i, j), TileType.Wall);
				}
			}
		}
		l6.setNumOfBlocksToGenerate(64);
		l6.setNumOfHolesToGenerate(16);
		l6.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*10);

		int maxEmpty = 6*(BoardPanel.COL_COUNT-2)+6*(BoardPanel.ROW_COUNT-8);
		//System.out.println("Max empty: "+maxEmpty);
		int index = random.nextInt(maxEmpty);

		int freeFound = -1;
		boolean placed = false;
		for(int x = 0; x < BoardPanel.COL_COUNT && !placed; x++) {
			for(int y = 0; y < BoardPanel.ROW_COUNT && !placed; y++) {
				TileType type = l6.getTile(x, y);
				if((y < 5 || y > BoardPanel.ROW_COUNT-5) || (x < 5 || x > BoardPanel.COL_COUNT-5)){
					if(type == null) {
						if(++freeFound == index) {
							l6.setMouseLocation(x, y);
							//System.out.println("hey: "+x+", "+y+ " :: "+type);
							placed = true;
						}
					}
				}
			}
		}
		l6.setNumOfCatsPerRound(new int[]{1, 2});
		l6.setTimePerRound(5);

		// Level 7
		for(int i = ygap; i < BoardPanel.ROW_COUNT-ygap; i++){
			for(int j = xgap; j < BoardPanel.COL_COUNT-xgap; j++){
				l7.setTile(new Point(i, j), TileType.Block);
			}
		}
		l7.setNumOfHolesToGenerate(5);
		l7.setNumOfMouseTrapsToGenerate(1);
		l7.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*10);
		l7.setNumOfCatsPerRound(new int[]{1, 2});
		l7.setTimePerRound(5);

		// Level 8
		for(int i = ygap; i < BoardPanel.ROW_COUNT-ygap; i++){
			for(int j = xgap; j < BoardPanel.COL_COUNT-xgap; j++){
				l8.setTile(new Point(i, j), TileType.Block);
			}
		}
		l8.setNumOfHolesToGenerate(5);
		l8.setNumOfMouseTrapsToGenerate(1);
		l8.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*8);
		l8.setNumOfCatsPerRound(new int[]{1, 2});
		l8.setTimePerRound(5);

		// Level 9
		l9.setNumOfWallsToGenerate(8);
		l9.setNumOfBlocksToGenerate(96);
		l9.setNumOfHolesToGenerate(10);
		l9.setNumOfMouseTrapsToGenerate(6);
		l9.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*10);
		l9.setNumOfCatsPerRound(new int[]{1, 2});
		l9.setTimePerRound(5);

		// Level 10
		for(int i = 3; i < BoardPanel.COL_COUNT-3; i++){
			for(int j = 3; j < BoardPanel.ROW_COUNT-3; j++){
				int off = j+1%2;
				if((i+off)%2 == 0){
					l10.setTile(new Point(i, j), TileType.Block);
				}
			}
		}
		l10.setNumOfHolesToGenerate(17);
		l10.setNumOfMouseTrapsToGenerate(10);
		l10.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*8);
		l10.setNumOfCatsPerRound(new int[]{1, 2});
		l10.setTimePerRound(5);

		// Level 11
		l11.setNumOfWallsToGenerate(55);
		l11.setNumOfBlocksToGenerate(58);
		l11.setNumOfHolesToGenerate(11);
		l11.setNumOfMouseTrapsToGenerate(11);
		l11.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*5);
		l11.setNumOfCatsPerRound(new int[]{1, 2});
		l11.setTimePerRound(5);

		// Level 12
		for(int i = 4; i < BoardPanel.COL_COUNT-4; i++){
			for(int j = 4; j < BoardPanel.ROW_COUNT-4; j++){
				int off = j+1%2;
				if((i+off)%2 == 0){
					l12.setTile(new Point(i, j), TileType.Wall);
				}
			}
		}

		maxEmpty = 6*(BoardPanel.COL_COUNT-2)+6*(BoardPanel.ROW_COUNT-8);
		//System.out.println("Max empty: "+maxEmpty);
		index = random.nextInt(maxEmpty);

		freeFound = -1;
		placed = false;
		for(int x = 0; x < BoardPanel.COL_COUNT && !placed; x++) {
			for(int y = 0; y < BoardPanel.ROW_COUNT && !placed; y++) {
				TileType type = l12.getTile(x, y);
				if((y < 5 || y > BoardPanel.ROW_COUNT-5) || (x < 5 || x > BoardPanel.COL_COUNT-5)){
					//System.out.println("hey: "+x+", "+y+ " :: "+type);
					if(type == null) {
						if(++freeFound == index) {
							l12.setMouseLocation(x, y);
							placed = true;
						}
					}
				}
			}
		}

		l12.setNumOfBlocksToGenerate(67);
		l12.setNumOfHolesToGenerate(19);
		l12.setNumOfMouseTrapsToGenerate(16);
		l12.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*6);
		l12.setNumOfCatsPerRound(new int[]{1, 2});
		l12.setTimePerRound(5);

		// Level 13
		for(int i = 5; i < BoardPanel.ROW_COUNT-5; i++){
			for(int j = 5; j < BoardPanel.COL_COUNT-5; j++){
				l13.setTile(new Point(i, j), TileType.Block);
			}
		}
		l13.setNumOfHolesToGenerate(6);
		l13.setNumOfMouseTrapsToGenerate(1);
		l13.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*5);
		l13.setNumOfCatsPerRound(new int[]{1, 2});
		l13.setTimePerRound(5);

		// Level 14
		for(int i = 5; i < BoardPanel.ROW_COUNT-5; i++){
			for(int j = 5; j < BoardPanel.COL_COUNT-5; j++){
				l14.setTile(new Point(i, j), TileType.Block);
			}
		}
		l14.setNumOfWallsToGenerate(1);
		l14.setNumOfHolesToGenerate(16);
		l14.setNumOfMouseTrapsToGenerate(11);
		l14.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*4);
		l14.setNumOfCatsPerRound(new int[]{1, 2});
		l14.setTimePerRound(5);

		// Level 15
		l15.setNumOfWallsToGenerate(8);
		l15.setNumOfBlocksToGenerate(95);
		l15.setNumOfHolesToGenerate(12);
		l15.setNumOfMouseTrapsToGenerate(6);
		l15.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*4);
		l15.setNumOfCatsPerRound(new int[]{1, 2});
		l15.setTimePerRound(5);

		// Level 16
		for(int i = 4; i < BoardPanel.COL_COUNT-4; i++){
			for(int j = 4; j < BoardPanel.ROW_COUNT-4; j++){
				int off = j+1%2;
				if((i+off)%2 == 0){
					l16.setTile(new Point(i, j), TileType.Block);
				}
			}
		}

		l16.setNumOfHolesToGenerate(10);
		l16.setNumOfMouseTrapsToGenerate(11);
		l16.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*4);
		l16.setNumOfCatsPerRound(new int[]{1, 2});
		l16.setTimePerRound(5);

		// Level 17
		l17.setNumOfWallsToGenerate(51);
		l17.setNumOfBlocksToGenerate(58);
		l17.setNumOfHolesToGenerate(14);
		l17.setNumOfMouseTrapsToGenerate(11);
		l17.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*4);
		l17.setNumOfCatsPerRound(new int[]{1, 2});
		l17.setTimePerRound(5);

		// Level 18
		for(int i = 5; i < BoardPanel.COL_COUNT-5; i++){
			for(int j = 5; j < BoardPanel.ROW_COUNT-5; j++){
				int off = j+1%2;
				if((i+off)%2 == 0){
					l18.setTile(new Point(i, j), TileType.Wall);
				}
			}
		}
		l18.setNumOfBlocksToGenerate(57);
		l18.setNumOfHolesToGenerate(17);
		l18.setNumOfMouseTrapsToGenerate(16);
		l18.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*6);
		l18.setNumOfCatsPerRound(new int[]{1, 2});
		l18.setTimePerRound(5);
		
		maxEmpty = 6*(BoardPanel.COL_COUNT-2)+6*(BoardPanel.ROW_COUNT-8);
		//System.out.println("Max empty: "+maxEmpty);
		index = random.nextInt(maxEmpty);

		freeFound = -1;
		placed = false;
		for(int x = 0; x < BoardPanel.COL_COUNT && !placed; x++) {
			for(int y = 0; y < BoardPanel.ROW_COUNT && !placed; y++) {
				TileType type = l18.getTile(x, y);
				if((y < 5 || y > BoardPanel.ROW_COUNT-5) || (x < 5 || x > BoardPanel.COL_COUNT-5)){
					//System.out.println("hey: "+x+", "+y+ " :: "+type);
					if(type == null) {
						if(++freeFound == index) {
							l18.setMouseLocation(x, y);
							placed = true;
						}
					}
				}
			}
		}

		// Level 19
		for(int i = 6; i < BoardPanel.ROW_COUNT-6; i++){
			for(int j = 6; j < BoardPanel.COL_COUNT-6; j++){
				l19.setTile(new Point(i, j), TileType.Block);
			}
		}
		l19.setNumOfMouseTrapsToGenerate(1);
		l19.setNumOfHolesToGenerate(17);
		l19.setNumOfMouseTrapsToGenerate(11);
		l19.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*5);
		l19.setNumOfCatsPerRound(new int[]{1, 2});
		l19.setTimePerRound(5);

		// Level 20
		for(int y = 6; y < BoardPanel.ROW_COUNT-6; y++){
			for(int x = 6; x < BoardPanel.COL_COUNT-6; x++){
				l20.setTile(new Point(x, y), TileType.Block);
			}
		}
		l20.setNumOfWallsToGenerate(1);
		l20.setNumOfHolesToGenerate(17);
		l20.setNumOfMouseTrapsToGenerate(11);
		l20.setFramesForYarnBallGeneration(this.FRAMES_PER_SECOND*4);
		l20.setNumOfCatsPerRound(new int[]{1, 2});
		l20.setTimePerRound(5);

		result.add(l1);
		result.add(l2);
		result.add(l3);
		result.add(l4);
		result.add(l5);
		result.add(l6);
		result.add(l7);
		result.add(l8);
		result.add(l9);
		result.add(l10);
		result.add(l11);
		result.add(l12);
		result.add(l13);
		result.add(l14);
		result.add(l15);
		result.add(l16);
		result.add(l17);
		result.add(l18);
		result.add(l19);
		result.add(l20);
		return result;
	}

	private void generateTileTypeNoneWall(int numOf, TileType t){
		if(numOf > 0){
			int maxEmpty = BoardPanel.COL_COUNT*BoardPanel.ROW_COUNT-board.getNumberOfWallTiles();
			int index = random.nextInt(maxEmpty);

			//System.out.println("Level: "+this.currentLevel+" :: "+board.getNumberOfWallTiles());
			int freeFound = -1;
			boolean placed = false;
			for(int x = 0; x < BoardPanel.COL_COUNT && !placed; x++) {
				for(int y = 0; y < BoardPanel.ROW_COUNT && !placed; y++) {
					TileType type = board.getTile(x, y);
					//System.out.print("Type: "+type);
					if(type != TileType.Wall) {
						//System.out.println(" Yes: "+freeFound+" "+index);
						if(++freeFound == index) {
							board.setTile(x, y, t);
							placed = true;
						}
					}
				}
			}
			generateTileTypeNoneWall(--numOf, t);
		}
	}

	private void generateTilesNoneWallSpaces(int numOf, TileType t){
		PriorityQueue<Integer> rands = new PriorityQueue<Integer>();
		int maxEmpty = BoardPanel.COL_COUNT*BoardPanel.ROW_COUNT-board.getNumberOfWallTiles();
		for(int i = 0; i < numOf && maxEmpty!= 0; i++){
			rands.add(random.nextInt(maxEmpty));
			maxEmpty--;
		}
		//System.out.println(t+" "+rands);


		if(!rands.isEmpty()){
			int freeFound = -1;
			boolean done = false;
			int index = rands.remove();
			for(int x = 0; x < BoardPanel.COL_COUNT && !done; x++) {
				for(int y = 0; y < BoardPanel.ROW_COUNT && !done; y++) {
					TileType type = board.getTile(x, y);
					//System.out.print("Type: "+type);
					if(type != TileType.Wall) {
						//System.out.println(" Yes: "+freeFound+" "+index);
						if(++freeFound >= index) {
							board.setTile(x, y, t);
							if(rands.isEmpty()){
								done = true;
							} else {
								index = rands.remove();
							}
						}
					}
				}
			}
		}
	}

	private void generateTilesNullBlockSpaces(int numOf, TileType t, LinkedList<Point> l){
		PriorityQueue<Integer> rands = new PriorityQueue<Integer>();
		int maxEmpty = board.getNumberOfEmptySpaces()+board.getNumberOfBlockTiles();
		for(int i = 0; i < numOf && maxEmpty!= 0; i++){
			rands.add(random.nextInt(maxEmpty));
			maxEmpty--;
		}
		//System.out.println(t+" "+rands);

		if(!rands.isEmpty()){
			int freeFound = -1;
			boolean done = false;
			int index = rands.remove();
			for(int x = 0; x < BoardPanel.COL_COUNT && !done; x++) {
				for(int y = 0; y < BoardPanel.ROW_COUNT && !done; y++) {
					TileType type = board.getTile(x, y);
					//System.out.print("Type: "+type);
					if(type == null || type == TileType.Block) {
						//System.out.println(" Yes: "+freeFound+" "+index);
						if(++freeFound >= index) {
							board.setTile(x, y, t);
							l.add(new Point(x, y));
							if(rands.isEmpty()){
								done = true;
							} else {
								index = rands.remove();
							}
						}
					}
				}
			}
		}
	}

	private void generateTilesEmptySpaces(int numOf, TileType t){
		PriorityQueue<Integer> rands = new PriorityQueue<Integer>();
		int maxEmpty = board.getNumberOfEmptySpaces();
		for(int i = 0; i < numOf && maxEmpty!= 0; i++){
			rands.add(random.nextInt(maxEmpty));
			maxEmpty--;
		}

		if(!rands.isEmpty()){
			int freeFound = -1;
			boolean done = false;
			int index = rands.remove();
			for(int x = 0; x < BoardPanel.COL_COUNT && !done; x++) {
				for(int y = 0; y < BoardPanel.ROW_COUNT && !done; y++) {
					TileType type = board.getTile(x, y);
					//System.out.print("Type: "+type);
					if(type == null) {
						//System.out.println(" Yes: "+freeFound+" "+index);
						if(++freeFound >= index) {
							board.setTile(x, y, t);
							if(rands.isEmpty()){
								done = true;
							} else {
								index = rands.remove();
							}
						}
					}
				}
			}
		}
	}

	private void generateTileTypeEmptySpaces(int numOf, TileType t){
		if(numOf > 0){
			int maxEmpty = board.getNumberOfEmptySpaces();
			int index = random.nextInt(maxEmpty);

			int freeFound = -1;
			boolean placed = false;
			for(int x = 0; x < BoardPanel.COL_COUNT && !placed; x++) {
				for(int y = 0; y < BoardPanel.ROW_COUNT && !placed; y++) {
					TileType type = board.getTile(x, y);
					//System.out.print("Type: "+type);
					if(type == null) {
						//System.out.println(" Yes: "+freeFound+" "+index);
						if(++freeFound == index) {
							board.setTile(x, y, t);
							placed = true;
						}
					}
				}
			}
			generateTileTypeEmptySpaces(--numOf, t);
		}
	}

	private void generateCats(int numOfCats){
		generateCatHelper(0, numOfCats);
	}

	private void generateCatHelper(int start, int end){
		if(start < end){
			int maxEmpty = board.getNumberOfEmptySpaces()-24;
			int index = random.nextInt(maxEmpty);

			int freeFound = -1;
			boolean placed = false;
			for(int x = 0; x < BoardPanel.COL_COUNT && !placed; x++) {
				for(int y = 0; y < BoardPanel.ROW_COUNT && !placed; y++) {
					double distance = this.mouse.distance(x, y);
					if(distance >= 3){
						TileType type = board.getTile(x, y);
						if(type == null) {
							if(++freeFound == index) {
								board.setTile(x, y, TileType.Cat);
								this.cats.addLast(new Point(x, y));
								placed = true;
							}
						}
					}
				}
			}
			generateCatHelper(++start, end);
		}
	}

	public int getLevel(){
		return this.currentLevel+1;
	}

	public int getTimeLeft(){
		return this.levelTimer;
	}

	public int getLivesLeft(){
		return this.livesLeft;
	}

	public GameClock getGameClock(){
		return this.gameClock;
	}

	public boolean isDead(){
		return currentState == GameState.Dead;
	}

	public Point getMousePosition(){
		return (Point)this.mouse.clone();
	}

	public void changeDifficulty(Difficulty d){
		switch(d){
		case SLOW:
			this.catTime = RodentsRevengeGame.FRAMES_PER_SECOND*2;
			this.gameClock.changeFramesPerSecond(RodentsRevengeGame.FRAMES_PER_SECOND*2);
			break;
		case MEDIUM:
			this.catTime = RodentsRevengeGame.FRAMES_PER_SECOND;
			this.gameClock.changeFramesPerSecond(RodentsRevengeGame.FRAMES_PER_SECOND);
			break;
		case FAST:
			this.catTime = RodentsRevengeGame.FRAMES_PER_SECOND/2;
			this.gameClock.changeFramesPerSecond(RodentsRevengeGame.FRAMES_PER_SECOND/2);
			break;
		default:
			break;
		}
	}
	
	public void setLevel(int l){
		if(l < this.levels.size() && l >=0){
			/*
			 * Reset the score statistics. (Note that nextFruitPoints is reset in
			 * the spawnFruit function later on).
			 */
			this.score = 0;
			this.lifeScore = 50;
			this.fruitsEaten = 0;

			/*
			 * Reset both the new game and game over flags.
			 */
			currentState = GameState.NewGame;

			this.startingLevel = l;
			this.currentLevel = l;

			this.levelTimer = RodentsRevengeGame.TIME_PER_LEVEL;

			this.livesLeft = RodentsRevengeGame.LIVES_TO_START;

			this.mouseInHole = 0;

			/*
			 * Create the head at the center of the board.
			 */
			this.mouse = this.levels.get(this.currentLevel).getMousePosition();

			this.yarnBalls.clear();

			this.timeTillYarnBallGeneration = this.levels.get(this.currentLevel).getFramesForYarnBallGeneration();

			//		this.yarnBalls.addLast(new YarnBall(0, 0, null, this.FRAMES_PER_SECOND));

			/*
			 * Clear the snake list and add the head.
			 */

			/*
			 * Clear the board and add the head.
			 */
			board.clearBoard();

			board.boardFromLevel(levels.get(currentLevel));
			this.mouseTraps.clear();
			this.sinkHoles.clear();
			this.generateLevel();
			board.setTile(this.mouse, TileType.Mouse);

			this.currentRound = 0;
			this.cats.clear();
			this.generateCats(levels.get(currentLevel).getNumOfCatsForRound(currentRound));
			for(int i = 0; i < cats.size(); i++){
				board.setTile(this.cats.get(i), TileType.Cat);
			}
			catUpdateClock = 0;
			this.gameClock.setTime(0, 0, 0, 0);
			this.gameClock.setTimer(levels.get(currentLevel).getTimePerRound());

			/*
			 * Clear the directions and add north as the
			 * default direction.
			 */
			directions.clear();
		}
	}
}

