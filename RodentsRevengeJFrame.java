import java.awt.EventQueue;
import java.awt.event.*;

import javax.swing.*;


public class RodentsRevengeJFrame extends JFrame {

	private static final long serialVersionUID = 8583901963397094682L;
	private JPanel contentPane;
	private RodentsRevengeGame game;
	private JMenuBar menubar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RodentsRevengeJFrame frame = new RodentsRevengeJFrame();
					frame.startGame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public RodentsRevengeJFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("A Mouse's Vengence");
		setResizable(false);
		this.game = new RodentsRevengeGame();
		contentPane = this.game;
		contentPane.setBorder(null);
		//contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setVisible(true);
		
		menubar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem newGame = new JMenuItem("New Game");
		newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		newGame.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				game.newGame();
			}
			
		});
		JMenuItem pause = new JMenuItem("Pause");
		pause.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
		pause.addActionListener(new ActionListener(){
			/*
			 * If the game is not over, toggle the paused flag and update
			 * the logicTimer's pause flag accordingly.
			 */
			@Override
			public void actionPerformed(ActionEvent arg0) {
				game.pause();
			}
			
		});
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
			
		});
		file.add(newGame);
		file.add(pause);
		file.addSeparator();
		file.add(exit);
		menubar.add(file);
		
		JMenu options = new JMenu("Options");
		
		JMenu difficulty = new JMenu("Difficulty");
		
		ButtonGroup difficultyGroup = new ButtonGroup();
		JRadioButtonMenuItem slow = new JRadioButtonMenuItem("Slow");
		slow.addItemListener(new ItemListener(){

			@Override
			public void itemStateChanged(ItemEvent a) {
				if(a.getStateChange() == ItemEvent.SELECTED){
					System.out.println("Slow");
					game.changeDifficulty(Difficulty.SLOW);
				}
			}
			
		});
		difficultyGroup.add(slow);
		difficulty.add(slow);
		
		JRadioButtonMenuItem medium = new JRadioButtonMenuItem("Medium");
		medium.addItemListener(new ItemListener(){

			@Override
			public void itemStateChanged(ItemEvent a) {
				if(a.getStateChange() == ItemEvent.SELECTED){
					game.changeDifficulty(Difficulty.MEDIUM);
				}
			}
			
		});
		medium.setSelected(true);
		
		JRadioButtonMenuItem fast = new JRadioButtonMenuItem("Fast");
		fast.addItemListener(new ItemListener(){

			@Override
			public void itemStateChanged(ItemEvent a) {
				if(a.getStateChange() == ItemEvent.SELECTED){
					game.changeDifficulty(Difficulty.FAST);
				}
			}
			
		});
		JCheckBoxMenuItem repeatLevel = new JCheckBoxMenuItem("Repeat Level");
		repeatLevel.addItemListener(new ItemListener(){

			@Override
			public void itemStateChanged(ItemEvent a) {
				if(a.getStateChange() == ItemEvent.SELECTED){
					game.repeatLevel(true);
				} else {
					game.repeatLevel(false);
				}
				
			}
			
		});
		difficultyGroup.add(slow);
		difficultyGroup.add(medium);
		difficultyGroup.add(fast);
		difficulty.add(slow);
		difficulty.add(medium);
		difficulty.add(fast);
		
		JMenu levels = new JMenu("Levels");
		for(int i = 10; i <= 20; i+=10){
			JMenu range = new JMenu((i-9)+"-"+i+"");
			for(int j = i-9; j <= i; j++){
				JMenuItem level = new JMenuItem(j+"");
				level.addActionListener(new LevelActionListener(j-1, game));
				range.add(level);
			}
			levels.add(range);
		}
		
		options.add(difficulty);
		options.add(levels);
		options.addSeparator();
		options.add(repeatLevel);
		
		menubar.add(options);

		this.setJMenuBar(menubar);
		
		/*
		 * Adds a new key listener to the frame to process input. 
		 */
		addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {

				/*
				 * If the game is not paused, and the game is not over...
				 * 
				 * Ensure that the direction list is not full, and that the most
				 * recent direction is adjacent to North before adding the
				 * direction to the list.
				 */
				case KeyEvent.VK_W:
				case KeyEvent.VK_UP:
					game.turn(Direction.North);
					break;

				/*
				 * If the game is not paused, and the game is not over...
				 * 
				 * Ensure that the direction list is not full, and that the most
				 * recent direction is adjacent to South before adding the
				 * direction to the list.
				 */	
				case KeyEvent.VK_S:
				case KeyEvent.VK_DOWN:
					game.turn(Direction.South);
					break;
				
				/*
				 * If the game is not paused, and the game is not over...
				 * 
				 * Ensure that the direction list is not full, and that the most
				 * recent direction is adjacent to West before adding the
				 * direction to the list.
				 */						
				case KeyEvent.VK_A:
				case KeyEvent.VK_LEFT:
					game.turn(Direction.West);
					break;
			
				/*
				 * If the game is not paused, and the game is not over...
				 * 
				 * Ensure that the direction list is not full, and that the most
				 * recent direction is adjacent to East before adding the
				 * direction to the list.
				 */		
				case KeyEvent.VK_D:
				case KeyEvent.VK_RIGHT:
					game.turn(Direction.East);
					break;
					
				
				/*
				 * Reset the game if one is not currently in progress.
				 */
				case KeyEvent.VK_ENTER:
					System.out.println("Entered");
					game.enter();
					break;
				}
			}
			
		});
		
		pack();
		setLocationRelativeTo(null);
	}
	
	private class LevelActionListener implements ActionListener{

		private int l;
		private RodentsRevengeGame game;
		LevelActionListener(int i, RodentsRevengeGame g){
			l = i;
			this.game = g;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			game.setLevel(l);
		}
		
	}
	
	public void startGame(){
		Thread t = new Thread() {
			public void run(){
				game.startGame();
			}
		};
		t.start();
	}

}
