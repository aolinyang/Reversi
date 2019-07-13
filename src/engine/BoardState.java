package engine;

import java.util.ArrayList;

import application.ReversiBoard;

//state of the board
public class BoardState {

	ReversiBoard rboard;
	int turn;

	//recording how the state got here from previous, so computer knows where to place
	int placedX;
	int placedY;
	int placedColor;
	int change;

	boolean isMax;
	int value;

	BoardState[] nextStates = null;

	public BoardState(ReversiBoard r, int t, int px, int py, int pc, boolean im) {
		rboard = r;
		turn = t;
		placedX = px;
		placedY = py;
		placedColor = pc;
		isMax = im;
		if (isMax)
			value = Integer.MIN_VALUE;
		else
			value = Integer.MAX_VALUE;
	}

	public boolean equals(BoardState other) {

		return rboard.equals(other.rboard) && 
				turn == other.turn &&
				isMax == other.isMax;

	}

	//returns 2 if original turn has a move, 1 if forced to pass to opposite color, 0 if game over
	//must be called before getnextstates
	public int hasNext() {

		boolean outOfMoves = rboard.updateLegal(turn);
		if (outOfMoves) {
			turn *= -1;
			isMax = !isMax;
			value = -value;
			outOfMoves = rboard.updateLegal(turn);
			if (outOfMoves)
				return 0;
			return 1;
		}

		return 2;

	}

	public void findNextStates() {

		nextStates = new BoardState[rboard.numLegal()];

		ArrayList<Integer[]> allLegal = rboard.getAllLegal();

		for (int i = 0; i < allLegal.size(); i++) {
			int x = allLegal.get(i)[0];
			int y = allLegal.get(i)[1];
			ReversiBoard nextBoard = rboard.clone();
			int nextchange = nextBoard.setPiece(turn, x, y);
			BoardState nState = new BoardState(nextBoard, turn * -1, x, y, turn, !isMax);
			nState.change = nextchange;
			nextStates[i] = nState;
		}


	}

	public int findWinner() {
		int winner = rboard.findWinner();
		return winner;
	}

}