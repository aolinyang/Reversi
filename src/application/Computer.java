package application;

import engine.*;

public class Computer {

	/*
	 * This class defines an AI that will make moves based on a set of defined rules for maximizing points.
	 */

	private ReversiBoard rboard;
	private int color;
	private int length;
	private int height;

	public Computer(ReversiBoard playBoard, int color) {

		rboard = playBoard;
		this.color = color;
		length = rboard.getDimensions()[0];
		height = rboard.getDimensions()[1];
		
		Engine.color = color;
		Engine.length = length;
		Engine.height = height;
		int product = length * height;
		if (product > 196) {
			Engine.MAX_DEPTH = 4;
		}
		else if (product > 100){
			Engine.MAX_DEPTH = 5;
		}
		else if (product > 64) {
			Engine.MAX_DEPTH = 6;
		}
		else {
			Engine.MAX_DEPTH = 7;
		}
		
	}

	/*
	 * This method uses the Minimax Alpha-Beta algorithm to determine the best move.
	 * Depending on the board size, the max search depth will be different.
	 */
	public void makeMove() {
		
		BoardState state = new BoardState(rboard, color, 0, 0, 0, true);
		
		int[] data = Engine.minimax(state, true, Integer.MIN_VALUE, Integer.MAX_VALUE, true, 0, 0);
		rboard.setPiece(color, data[0], data[1]);
		
		state = null;
		
	}
	
}
