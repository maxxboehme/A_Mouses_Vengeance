import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * The {@code BoardPanel} class is responsible for managing and displaying the
 * contents of the game board.
 * @author Maxx Boehme
 *
 */
public class BoardPanel extends JPanel {
	
	/*
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 2513019940077893899L;

	/*
	 * The number of columns on the board. (Should be odd so we can start in
	 * the center).
	 */
	public static final int COL_COUNT = 23;
	
	/*
	 * The number of rows on the board. (Should be odd so we can start in
	 * the center).
	 */
	public static final int ROW_COUNT = 23;
	
	/*
	 * The size of each tile in pixels.
	 */
	public static final int TILE_SIZE = 19;
	
	/*
	 * The number of pixels to offset the eyes from the sides.
	 */
	public static final int EYE_LARGE_INSET = TILE_SIZE / 3;
	
	/*
	 * The number of pixels to offset the eyes from the front.
	 */
	public static final int EYE_SMALL_INSET = TILE_SIZE / 6;
	
	/*
	 * The length of the eyes from the base (small inset).
	 */
	public static final int EYE_LENGTH = TILE_SIZE / 5;
	
	/*
	 * The font to draw the text with.
	 */
	private static final Font FONT = new Font("Tahoma", Font.BOLD, 25);
	
	private static final Font NEW_LEVEL_FONT = new Font("Tahoma", Font.BOLD, 55);
		
	/*
	 * The SnakeGame instance.
	 */
	private RodentsRevengeGame game;
	
	/*
	 * The array of tiles that make up this board.
	 */
	private TileType[] tiles;
	
	private int numberOfEmptySpaces;
	
	private int numberOfWallTiles;
	
	private int numberOfBlockTiles;
	
	private BufferedImage mouse;
	
	private BufferedImage wall;
	
	private BufferedImage cat;
	
	private BufferedImage block;
	
	private BufferedImage sleepingCat;
	
	private BufferedImage sinkHole;
	
	private BufferedImage MouseTrap;
	
	private BufferedImage mouseInHole;
	
	private BufferedImage Cheese;
	
	private BufferedImage YarnBall;
	
	private BufferedImage[] dead;
	
	private int deadFrames;
	
	public final int NUM_DEAD_FRAMES;
		
	/**
	 * Creates a new BoardPanel instance.
	 * @param game The SnakeGame instance.
	 */
	public BoardPanel(RodentsRevengeGame game) {
		this.game = game;
		this.tiles = new TileType[ROW_COUNT * COL_COUNT];
		
		setPreferredSize(new Dimension(COL_COUNT * TILE_SIZE, ROW_COUNT * TILE_SIZE));
		setBackground(new Color(127, 128, 1));
		try {
			this.mouse = ImageIO.read(getClass().getResource("Images/Mouse2.png"));
			this.wall = ImageIO.read(getClass().getResource("Images/Wall.png"));
			this.cat = ImageIO.read(getClass().getResource("Images/Cat.png"));
			this.block = ImageIO.read(getClass().getResource("Images/block.png"));
			this.sleepingCat  = ImageIO.read(getClass().getResource("Images/SleepingCat.png"));
			this.sinkHole = ImageIO.read(getClass().getResource("Images/SinkHole.png"));
			this.MouseTrap = ImageIO.read(getClass().getResource("Images/MouseTrap.png"));
			this.mouseInHole = ImageIO.read(getClass().getResource("Images/MouseInHole.png"));
			this.Cheese = ImageIO.read(getClass().getResource("Images/Cheese.png"));
			this.YarnBall = ImageIO.read(getClass().getResource("Images/YarnBall.png"));
			String fileName = "Images/MouseDeadFrames/deadFrame";
			File f = new File(fileName+"1.png");
			int num = 1;
			ArrayList<BufferedImage> a =new ArrayList<BufferedImage>();
			while(f.exists()){
				for(int i = 0; i < 4; i++){
					a.add(ImageIO.read(getClass().getResource(fileName+num+".png")));
				}
				num++;
				f = new File(fileName+num+".png");
			}
			dead = new BufferedImage[a.size()];
			a.toArray(dead);
		} catch (IOException e) {
			mouse = null;
			wall = null;
			cat = null;
			block = null;
			sleepingCat = null;
		}
		this.numberOfEmptySpaces = BoardPanel.COL_COUNT * BoardPanel.ROW_COUNT;
		this.numberOfBlockTiles = 0;
		numberOfWallTiles = 0;
		this.deadFrames = 0;
		NUM_DEAD_FRAMES = dead.length;
	}
	
	/**
	 * Clears all of the tiles on the board and sets their values to null.
	 */
	public void clearBoard() {
		for(int i = 0; i < tiles.length; i++) {
			tiles[i] = null;
		}
		this.numberOfEmptySpaces = BoardPanel.ROW_COUNT * BoardPanel.COL_COUNT;
		numberOfWallTiles = 0;
		numberOfBlockTiles = 0;
	}
	
	public boolean boardFromLevel(Level l){
		this.clearBoard();
		if(l.getColCount() == BoardPanel.COL_COUNT && l.getRowCount() == BoardPanel.ROW_COUNT){
			for(int x = 0; x < BoardPanel.COL_COUNT; x++){
				for(int y = 0; y < BoardPanel.ROW_COUNT; y++){
					this.setTile(x, y, l.getTile(x, y));
				}
			}		
			return true;
		}
		return false;
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
		if(tiles[y * ROW_COUNT + x] == null){
			this.numberOfEmptySpaces--;
		} else if(tiles[y * ROW_COUNT + x] == TileType.Wall){
			numberOfWallTiles--;
		} else if(tiles[y * ROW_COUNT + x] == TileType.Block){
			this.numberOfBlockTiles--;
		}
		
		tiles[y * ROW_COUNT + x] = type;
		
		if(type == null){
			this.numberOfEmptySpaces++;
		} else if(type == TileType.Wall){
			numberOfWallTiles++;
		} else if(type == TileType.Block){
			this.numberOfBlockTiles++;
		}
	}
	
	public int getNumberOfWallTiles(){
		return this.numberOfWallTiles;
	}
	
	public int getNumberOfEmptySpaces(){
		return this.numberOfEmptySpaces;
	}
	
	/**
	 * Gets the tile at the desired coordinate.
	 * @param x The x coordinate of the tile.
	 * @param y The y coordinate of the tile.
	 * @return
	 */
	public TileType getTile(int x, int y) {
		return tiles[y * ROW_COUNT + x];
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(1.1f));
		/*
		 * Loop through each tile on the board and draw it if it
		 * is not null.
		 */
		for(int x = 0; x < COL_COUNT; x++) {
			for(int y = 0; y < ROW_COUNT; y++) {
				TileType type = getTile(x, y);
				if(type != null) {
					drawTile(x * TILE_SIZE, y * TILE_SIZE, type, g);
				}
			}
		}
		
		/*
		 * Draw the grid on the board. This makes it easier to see exactly
		 * where we in relation to the fruit.
		 * 
		 * The panel is one pixel too small to draw the bottom and right
		 * outlines, so we outline the board with a rectangle separately.
		 */
//		g.setColor(Color.BLACK);
//		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
//		for(int x = 0; x < COL_COUNT; x++) {
//			for(int y = 0; y < ROW_COUNT; y++) {
//				g.drawLine(x * TILE_SIZE, 0, x * TILE_SIZE, getHeight());
//				g.drawLine(0, y * TILE_SIZE, getWidth(), y * TILE_SIZE);
//			}
//		}
		
		
//		for(int x = 0; x < COL_COUNT; x++) {
//			for(int y = 0; y < ROW_COUNT; y++) {
//				TileType type = getTile(x, y);
//				if(type != null && (type == TileType.SnakeBody || type == TileType.SnakeHead)) {
//					drawTile(x * TILE_SIZE, y * TILE_SIZE, type, g);
//				}
//			}
//		}
//		
		/*
		 * Get the center coordinates of the board.
		 */
		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;
		
		String largeMessage = null;
		String smallMessage = null;
		
		/*
		 * Show a message on the screen based on the current game state.
		 */
		if(game.isGameOver() || game.isNewGame() || game.isPaused() || game.hasWon()) {
			g.setColor(Color.BLACK);
			
			/*
			 * Allocate the messages for and set their values based on the game
			 * state.
			 */
			if(game.isNewGame()) {
				largeMessage = "New Game!";
				smallMessage = "Press Enter to Start";
			} else if(game.isGameOver()) {
				largeMessage = "Game Over!";
				smallMessage = "Press Enter to Restart";
			} else if(game.isPaused()) {
				largeMessage = "Paused";
				smallMessage = "Press P to Resume";
			} else if(game.hasWon()){
				largeMessage = "You Have Won!";
				smallMessage = "Press Enter to Play Again";
			}
			
			/*
			 * Set the message font and draw the messages in the center of the board.
			 */
			g.setFont(FONT);
			g.drawString(largeMessage, centerX - g.getFontMetrics().stringWidth(largeMessage) / 2, centerY - 50);
			g.drawString(smallMessage, centerX - g.getFontMetrics().stringWidth(smallMessage) / 2, centerY + 50);
		} else if(game.isSettingUp()){
			g.setColor(Color.BLACK);
			largeMessage = "Level "+game.getLevel();
			g.setFont(NEW_LEVEL_FONT);
			g.drawString(largeMessage, centerX - g.getFontMetrics().stringWidth(largeMessage) / 2, centerY - 50);
		} else if(game.isDead()){
			Point d = game.getMousePosition();
			int x = d.x*TILE_SIZE;
			int y = d.y*TILE_SIZE;
			if(this.deadFrames < dead.length){
				g.drawImage(this.dead[deadFrames], x, y, x+TILE_SIZE, y+TILE_SIZE, 0, 0, this.dead[deadFrames].getWidth(), this.dead[deadFrames].getHeight(), this.getBackground(), null);
				this.deadFrames++;
			}
			if(this.deadFrames >= dead.length){
				this.deadFrames = 0;
			}
		}
	}
	
	/**
	 * Draws a tile onto the board.
	 * @param x The x coordinate of the tile (in pixels).
	 * @param y The y coordinate of the tile (in pixels).
	 * @param type The type of tile to draw.
	 * @param g The graphics object to draw to.
	 */
	private void drawTile(int x, int y, TileType type, Graphics g) {
		/*
		 * Because each type of tile is drawn differently, it's easiest
		 * to just run through a switch statement rather than come up with some
		 * overly complex code to handle everything.
		 */
		int borderSize = TILE_SIZE/8;
		switch(type) {
		
		/*
		 * A fruit is depicted as a small red circle that with a bit of padding
		 * on each side.
		 */

			
		case Wall:
			g.setColor(Color.GRAY);
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(x+borderSize, y+borderSize, TILE_SIZE-borderSize*2, TILE_SIZE-borderSize*2);
			g.setColor(Color.GRAY);
			g.fillRect(x+borderSize*2, y+borderSize*2, TILE_SIZE-borderSize*4, TILE_SIZE-borderSize*4);
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(x+borderSize*4, y+borderSize*4, TILE_SIZE-borderSize*8, TILE_SIZE-borderSize*8);
			g.drawImage(this.wall, x, y, x+TILE_SIZE, y+TILE_SIZE, 0, 0, this.wall.getWidth(), this.wall.getHeight(), this.getBackground(), null);
			break;
			
		case Block:
			g.setColor(new Color(0, 202, 0));
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			g.setColor(new Color(0, 22, 0));
			g.fillRect(x+1, y+1, TILE_SIZE-1, TILE_SIZE-1);
			g.setColor(new Color(0, 102, 0));
			g.fillRect(x+1, y+1, TILE_SIZE-2, TILE_SIZE-2);
//			for(int i = 0; i < 2; i++){
//				for(int j = 0; j < 2; j++){
//					g.setColor(Color.LIGHT_GRAY);
//					g.drawOval(x+i*(this.TILE_SIZE/2), y+j*(this.TILE_SIZE/2), this.TILE_SIZE/2, this.TILE_SIZE/2);
//				}
//			}
			g.drawImage(this.block, x, y, x+TILE_SIZE, y+TILE_SIZE, 0, 0, this.block.getWidth(), this.block.getHeight(), this.getBackground(), null);
			break;
			
		/*
		 * The snake body is depicted as a green square that takes up the
		 * entire tile.
		 */
		case Cat:
			g.setColor(Color.YELLOW);
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			g.drawImage(this.cat, x, y, x+TILE_SIZE, y+TILE_SIZE, 0, 0, this.cat.getWidth(), this.cat.getHeight(), this.getBackground(), null);
			break;
			
		case SleepingCat:
			g.setColor(Color.ORANGE);
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			g.drawImage(this.sleepingCat, x, y, x+TILE_SIZE, y+TILE_SIZE, 0, 0, this.sleepingCat.getWidth(), this.sleepingCat.getHeight(), this.getBackground(), null);
			break;
			
		/*
		 * The snake head is depicted similarly to the body, but with two
		 * lines (representing eyes) that indicate it's direction.
		 */
		case Mouse:
			//Fill the tile in with green.
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			g.drawImage(this.mouse, x, y, x+TILE_SIZE, y+TILE_SIZE, 0, 0, this.mouse.getWidth(), this.mouse.getHeight(), this.getBackground(), null);
			break;
		case MouseInHole:
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			g.drawImage(this.mouseInHole, x, y, x+TILE_SIZE, y+TILE_SIZE, 0, 0, this.mouseInHole.getWidth(), this.mouseInHole.getHeight(), this.getBackground(), null);
			break;
		case Cheese:
			g.setColor(Color.ORANGE);
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			g.drawImage(this.Cheese, x, y, x+TILE_SIZE, y+TILE_SIZE, 0, 0, this.Cheese.getWidth(), this.Cheese.getHeight(), this.getBackground(), null);
			break;
		case MouseTrap:
			g.drawImage(this.MouseTrap, x, y, x+TILE_SIZE, y+TILE_SIZE, 0, 0, this.MouseTrap.getWidth(), this.MouseTrap.getHeight(), this.getBackground(), null);
			break;
		case SinkHole:
			g.setColor(Color.BLACK);
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			g.drawImage(this.sinkHole, x, y, x+TILE_SIZE, y+TILE_SIZE, 0, 0, this.sinkHole.getWidth(), this.sinkHole.getHeight(), this.getBackground(), null);
			break;
		case YarnBall:
			g.setColor(Color.PINK);
			g.fillOval(x, y, TILE_SIZE, TILE_SIZE);
			g.drawImage(this.YarnBall, x, y, x+TILE_SIZE, y+TILE_SIZE, 0, 0, this.YarnBall.getWidth(), this.YarnBall.getHeight(), this.getBackground(), null);
		default:
			break;
		}
	}

	public int getNumberOfBlockTiles() {
		return numberOfBlockTiles;
	}
}

