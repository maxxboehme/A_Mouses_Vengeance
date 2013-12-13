
public enum GameState {
	Paused, GameOver, Won, Running, NewGame, SetUp, Dead;
	
	public boolean canUpdateGame(){
		return this != GameOver && this != Paused && this != NewGame && this != Won;
	}
}
