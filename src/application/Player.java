package application;

public class Player {
	
	/*
	 * This class defines a human player
	 * Can be client player or opponent player
	 */
	
	private String name; //name of player
	private int color; //color of piece
	private ReversiBoard rboard; //shares the board with anyone else playing, and also with Main
	
	public Player(String name, ReversiBoard playBoard, int c) {
		
		this.name = name;
		color = c;
		rboard = playBoard;
		
	}
	
	//updates rboard's legalboard
	public boolean updateLegal() {
		
		return rboard.updateLegal(color);
		
	}
	
	//assumes that move is legal
	public void makeMove(int x, int y) {
		
		rboard.setPiece(color, x, y);
		
	}
	
	public void setName(String n) {
		name = n;
	}
	
	public String getName() {
		
		return name;
		
	}
	
	public int getColor() {
		
		return color;
		
	}

}
