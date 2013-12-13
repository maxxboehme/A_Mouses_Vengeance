import java.awt.*;

public class GameClock {

	private int framesPerSecond;
	private int changeFramesPerSecond;
	private int size;
	private int positionx;
	private int positiony;

	private int frames;
	private int seconds;
	private int minutes;
	private int hours;
	private int timer;

	GameClock(int framesPerSec, int size, int x, int y){
		this.framesPerSecond = framesPerSec;
		this.size = size;
		this.positionx = x;
		this.positiony = y;
		this.timer = -1;
		
		this.frames = this.seconds = this.minutes = this.hours = 0;
	}
	
	GameClock(int framesPerSec){
		this(framesPerSec, 25, 0, 0);
	}
	
	public void setLocation(int x, int y){
		this.positionx = x;
		this.positiony = y;
	}
	
	public void setSize(int s){
		this.size = s;
	}
	
	public int getSize(){
		return this.size;
	}

	public void setTime(int sec, int min, int hour){
		this.seconds = sec;
		this.minutes = min;
		this.hours = hour;
	}
	
	public void setTime(int frame, int sec, int min, int hour){
		this.frames = frame;
		this.seconds = sec;
		this.minutes = min;
		this.hours = hour;
	}
	
	public void setSeconds(int s){
		this.seconds = s;
	}
	
	public int getSeconds(){
		return this.seconds;
	}
	
	public void setMinutes(int m){
		this.minutes = m;
	}
	
	public int getMinutes(){
		return this.minutes;
	}
	
	public void setHours(int h){
		this.hours = h;
	}
	
	public int getHours(){
		return this.hours;
	}
	
	public void setTimer(int t){
		this.timer = t;
	}
	
	public int getTimer(){
		return this.timer % 30;
	}
	
	public void drawTic(double angle, int radious, int radious2, Graphics g){
		angle -= 0.5 * Math.PI;
		int x = (int)(radious*Math.cos(angle) );
		int y = (int)(radious*Math.sin(angle) );
		int x2 = (int)(radious2*Math.cos(angle) );
		int y2 = (int)(radious2*Math.sin(angle) );
		g.drawLine(positionx+size/2+x2, positiony+size/2+y2, positionx+size/2 + x, positiony+size/2 + y);
	}

	public void drawHand(double angle, int radius, Graphics g) {
		angle -= 0.5 * Math.PI;
		int x = (int)( radius*Math.cos(angle) );
		int y = (int)( radius*Math.sin(angle) );
		g.drawLine( positionx+size/2, positiony+size/2, positionx+size/2 + x, positiony+size/2 + y );
	}
	
	public void incrementSeconds(){
		this.increment(this.framesPerSecond);
	}
	
	public void increment(int f){
		this.frames += f;
		if(frames >= framesPerSecond){
			int s = this.frames/framesPerSecond;
			this.frames -= s*framesPerSecond;
			this.seconds += s;
			if(this.seconds >= 60){
				int m = this.seconds/60;
				this.seconds -= m*60;
				this.minutes += m;
				if(this.minutes >= 30){
					int h = this.minutes/30;
					this.minutes -= h*30;
					this.hours +=h;
				}
			}
		}
	}
	
	public void increment(){
		frames++;
		//System.out.println("Frames: "+frames);
		if(frames >= framesPerSecond){
			frames = 0;
			seconds++;
			if(seconds == 60){
				seconds = 0;
				minutes++;
				if(minutes == 30){
					minutes = 0;
					hours++;
					if(hours == 12){
						hours = 0;
					}
				}
			}
		}
		//System.out.println("f:"+this.frames+" s: "+this.seconds+" M: "+this.minutes+" H: "+this.hours);
	}
	
	public void changeFramesPerSecond(int framesPerSecond){
		this.framesPerSecond = framesPerSecond;
	}

	public void paint(Graphics g){
		g.setColor(Color.LIGHT_GRAY);
		g.fillOval(positionx, positiony, size, size);
		g.setColor(Color.BLACK);
		g.drawOval(positionx, positiony, size, size);
		int boarderThickness = (this.size/2)/8;
		int clockFacex = this.positionx+boarderThickness;
		int clockFacey = this.positiony+boarderThickness;
		int clickFaceSize = size-(boarderThickness*2);
		g.setColor(Color.WHITE);
		g.fillOval(clockFacex, clockFacey, clickFaceSize, clickFaceSize);
		g.setColor(Color.BLACK);
		g.drawOval(clockFacex, clockFacey, clickFaceSize, clickFaceSize);
		g.setColor(Color.BLACK);
		for(int i = 0; i < 12; i++){
			drawTic(2*Math.PI * i / 12, clickFaceSize/2*7/8, clickFaceSize/2, g);
		}
		g.setColor(Color.BLUE);
		if(timer != -1){
			drawTic(2*Math.PI * this.timer / 30, (clickFaceSize/2)*3/4, clickFaceSize/2, g);
		}
		//System.out.println(" s: "+seconds+" M: "+minutes+" H: "+hours);
		drawHand(2*Math.PI * hours / 12, clickFaceSize/5, g);
		drawHand(2*Math.PI * minutes / 30, clickFaceSize/3, g);
		g.setColor(Color.RED);
		drawHand(2*Math.PI * seconds / 60, clickFaceSize/2, g);
		g.setColor(Color.BLACK);
		int dotSize = 3;
		g.fillOval(positionx+size/2-dotSize/2, positiony+size/2-dotSize/2, dotSize, dotSize);
	}
}
