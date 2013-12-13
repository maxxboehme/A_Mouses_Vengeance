import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;


public class TopPanel extends JPanel{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 4921110690270257412L;

	/**
	 * Font used for the title.
	 */
	private static final Font TITLE_FONT = new Font("Tahoma", Font.BOLD, 22);
	
	private static final Font LARGE_FONT = new Font("Tahoma", Font.PLAIN, 30);
	
	/**
	 * The medium font to draw with.
	 */
	private static final Font MEDIUM_FONT = new Font("Tahoma", Font.BOLD, 16);

	/**
	 * The small font to draw with.
	 */
	private static final Font SMALL_FONT = new Font("Tahoma", Font.BOLD, 12);
	
	private final int IMAGE_SIZE = 38;
	
	private BufferedImage mouse;
	
	/**
	 * The SnakeGame instance.
	 */
	private RodentsRevengeGame game;
	
	private GameClock clock;
	
	private static final int HIGHT = 80;
	
	/**
	 * Creates a new SidePanel instance.
	 * @param game The SnakeGame instance.
	 */
	TopPanel(RodentsRevengeGame game) {
		this.game = game;
		
		this.setSize(new Dimension(BoardPanel.COL_COUNT * BoardPanel.TILE_SIZE, HIGHT));
		setPreferredSize(new Dimension(BoardPanel.COL_COUNT * BoardPanel.TILE_SIZE, HIGHT));
		setBackground(Color.LIGHT_GRAY);
		
		try {
			this.mouse = ImageIO.read(new File("Images/MouseSitting.png"));
		} catch (IOException e) {
			this.mouse = null;
		}
		this.clock = game.getGameClock();
		this.clock.setSize(50);
		int clockRadious = this.clock.getSize()/2;
		this.clock.setLocation(this.getWidth()/2+10-clockRadious, this.getHeight()/2+10-this.clock.getSize()/2);
	}
	

	
	private static final int STATISTICS_OFFSET = 30;
	
	private static final int SCORE_OFFSET = BoardPanel.ROW_COUNT * BoardPanel.TILE_SIZE - 10;
	
	private static final int LIVES_OFFSET = 30;
	
	private static final int MESSAGE_STRIDE = 30;
	
	private static final int SMALL_OFFSET = 10;
	
	private static final int LARGE_OFFSET = 20;
	
	private static final int TIMER_HEIGHT = 10;
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(1.1f));
		
		/*
		 * Draw the game name onto the window.
		 */
		g.setFont(TITLE_FONT);
		g.setColor(Color.WHITE);
		int borderWidth = 1;
		g.drawString("A Mouse's Vengence", getWidth() / 2 - g.getFontMetrics().stringWidth("A Mouse's Vengence") / 2-borderWidth, 20-borderWidth);
		g.drawString("A Mouse's Vengence", getWidth() / 2 - g.getFontMetrics().stringWidth("A Mouse's Vengence") / 2+borderWidth, 20+borderWidth);
		g.drawString("A Mouse's Vengence", getWidth() / 2 - g.getFontMetrics().stringWidth("A Mouse's Vengence") / 2-borderWidth, 20+borderWidth);
		g.drawString("A Mouse's Vengence", getWidth() / 2 - g.getFontMetrics().stringWidth("A Mouse's Vengence") / 2+borderWidth, 20-borderWidth);
		g.setColor(Color.BLACK);
		g.drawString("A Mouse's Vengence", getWidth() / 2 - g.getFontMetrics().stringWidth("A Mouse's Vengence") / 2, 20);
		
		/*
		 * Draw the categories onto the window.
		 */
		g.setFont(MEDIUM_FONT);
		//g.drawString("Statistics", SMALL_OFFSET, STATISTICS_OFFSET);
		
		//g.drawString("Lives: ", SMALL_OFFSET, LIVES_OFFSET);
		
		int xlives = LARGE_OFFSET;
		g.setColor(Color.YELLOW);
		for(int i = 0; i < game.getLivesLeft(); i++){
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(xlives, LIVES_OFFSET, BoardPanel.TILE_SIZE, BoardPanel.TILE_SIZE);
			g.setColor(Color.BLACK);
			int baseY = LIVES_OFFSET + BoardPanel.EYE_SMALL_INSET;
			g.drawLine(xlives + BoardPanel.EYE_LARGE_INSET, baseY, xlives + BoardPanel.EYE_LARGE_INSET, baseY + BoardPanel.EYE_LENGTH);
			g.drawLine(xlives + BoardPanel.TILE_SIZE - BoardPanel.EYE_LARGE_INSET, baseY, xlives + BoardPanel.TILE_SIZE - BoardPanel.EYE_LARGE_INSET, baseY + BoardPanel.EYE_LENGTH);
			g.drawImage(this.mouse, xlives, LIVES_OFFSET, xlives+IMAGE_SIZE, LIVES_OFFSET+IMAGE_SIZE, 0, 0, this.mouse.getWidth(), this.mouse.getHeight(), this.getBackground(), null);
			xlives += IMAGE_SIZE+5;
		}
				
		/*
		 * Draw the category content onto the window.
		 */
		g.setFont(LARGE_FONT);
		g.setColor(Color.BLACK);
		
		//Draw the content for the statistics category.
		int drawY = STATISTICS_OFFSET;
		g.drawString(game.getScore()+"", SCORE_OFFSET-g.getFontMetrics().stringWidth(game.getScore()+""), drawY += MESSAGE_STRIDE);
//		g.drawString("Fruit Eaten: " + game.getFruitsEaten(), LARGE_OFFSET, drawY += MESSAGE_STRIDE);
//		g.drawString("Fruit Score: " + game.getNextFruitScore(), LARGE_OFFSET, drawY += MESSAGE_STRIDE);
//		g.drawString("Current Level: " + game.getLevel(), LARGE_OFFSET, drawY += MESSAGE_STRIDE);
		
		if(this.clock != null){
			this.clock.paint(g);
		}
	}
	
	public void incrementClock(){
		this.clock.increment();
	}
}

