package application;

public class ReversiBoard {

	/*
	 * This class contains the 2x2 matrix containing board data,
	 * and the methods which can manipulate that.
	 */

	/*
	 * COORDINATE SYSTEM:
	 * The bottom left square of the board is (0, 0)
	 * +X = going right
	 * +Y = going up
	 * The top right square of the board is (7, 7)
	 * The square at coordinate (x, y) is board[x][y]
	 */
	private int[][] board; //the board for the pieces. 0 = empty, 1 = white, -1 = black

	/*
	 * COORDINATE SYSTEM FOR LEGAL BOARD:
	 * 8 1 2
	 * 7   3
	 * 6 5 4
	 * Each number above represents a direction a move will take AND an index in the third dimension of legalboard,
	 * corresponding to the coordinates determined by the other two dimensions.
	 * Each index is 1 if and only if a piece placed at that coordinate can influence into that direction
	 * Index 0 is 1 if at least one of the other 8 are >0.
	 * If Index 0 is 0, the other 8 indices do not matter.
	 * 
	 * NUMBER SYSTEM FOR LEGAL BOARD:
	 * For the first layer, 0 = illegal placement, 1 = legal placement
	 * For the other 8 layers, the number will be the number of pieces that will be flipped, and 0 means illegal move.
	 */
	private int[][][] legalboard; //the board that keeps track of which moves are legal

	private int numLegal;

	public ReversiBoard() {

		board = new int[8][8];

		//sets up standard Reversi opening
		board[3][3] = -1;
		board[3][4] = 1;
		board[4][3] = 1;
		board[4][4] = -1;

		//sets up legalboard
		legalboard = new int[8][8][9];
		numLegal = 64;

	}

	//prints board in terminal. For testing purposes only.
	public void print() {
		for (int y = 7; y >= 0; y--) {
			for (int x = 0; x < 8; x++) {
				if (board[x][y] == 1)
					System.out.print("W ");
				else if (board[x][y] == -1)
					System.out.print("B ");
				else
					System.out.print("O ");
			}
			System.out.println();
		}
	}

	//prints first layer of legalboard in terminal. For testing purposes only.
	public void printlegal() {
		for (int y = 7; y >= 0; y--) {
			for (int x = 0; x < 8; x++) {
				System.out.print(legalboard[x][y][0] + " ");
			}
			System.out.println();
		}
	}

	//returns the number at (x, y)
	public int getSpace(int x, int y) {

		return board[x][y];

	}

	//returns the number at coords, which is a tuple
	public int getSpace(int[] coords) {

		return board[coords[0]][coords[1]];

	}

	//returns the 3-d matrix that determines legality of moves
	public int[][][] getLegal() {

		int[][][] newlegal = new int[8][8][9];
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
				for (int k = 0; k < 9; k++)
					newlegal[i][j][k] = legalboard[i][j][k];
		return newlegal;

	}
	
	public int[][] getBoard() {
		
		int[][] newboard = new int[8][8];
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
				newboard[i][j] = board[i][j];
		return newboard;
		
	}

	//sets a piece in accordance with move, assuming board legality is already updated and is consistent with move's color,
	//and assumes that move is legal (will end in a same color piece)
	public void setPiece(int color, int x, int y) {
		
		boolean placed = false;

		//follows legalboard coordinate system
		int[] xDirList = {0, 1, 1, 1, 0, -1, -1, -1};
		int[] yDirList = {1, 1, 0, -1, -1, -1, 0, 1};

		for (int dir = 0; dir < 8; dir++) {
			int step = 0;
			if (legalboard[x][y][dir + 1] > 0) {
				for (int i = 0; i <= legalboard[x][y][dir + 1]; i++) {
					board[x + step * xDirList[dir]][y + step * yDirList[dir]] = color;
					step++;
				}
				placed = true;
			}
		}

	}

	//updates the legality board if the color is placing and updates the number of legal positions, returns true if there are no legal moves left
	public boolean updateLegal(int color) {

		numLegal = 0; //resets number of legal moves

		//follows legalboard coordinate system
		int[] xDirList = {0, 1, 1, 1, 0, -1, -1, -1};
		int[] yDirList = {1, 1, 0, -1, -1, -1, 0, 1};

		//loops through the entire board
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {

				//first assume it's illegal until proven otherwise
				legalboard[x][y][0] = 0;
				//first checks if the coordinate already has a piece or not
				if (board[x][y] != 0)
					continue;

				int totalTaken = 0;

				//now checks all the possible directions
				for (int i = 0; i < 8; i++) {
					int numTaken = isLegal(color, x, y, xDirList[i], yDirList[i]);
					legalboard[x][y][i+1] = numTaken; //becomes the number of pieces that will be flipped
					totalTaken += numTaken;
				}

				legalboard[x][y][0] = totalTaken;
				if (legalboard[x][y][0] > 0)
					numLegal++;

			}
		}

		return numLegal == 0;

	}

	//assumes the starting square is empty
	//returns 0 if the move is illegal, else returns the number of pieces that will be flipped
	private int isLegal(int color, int x, int y, int xdir, int ydir) {
		
		//keeps going down the line while pieces are opposite colored,
		//makes sure that it ends in an opposite colored piece and not in a border or blank space
		int step = 1;
		int xNext = x + xdir;
		int yNext = y + ydir;
		int[] nextCoord = {xNext, yNext};
		if (!outOfBounds(nextCoord))
			while (board[nextCoord[0]][nextCoord[1]] == -1 * color) {
				step++;
				xNext = x + step * xdir;
				yNext = y + step * ydir;
				nextCoord[0] = xNext;
				nextCoord[1] = yNext;
				if (outOfBounds(nextCoord))
					break;
			}
		if (outOfBounds(nextCoord) || board[nextCoord[0]][nextCoord[1]] == 0) //illegal if a boundary or an empty space comes up before the same color
			return 0;

		return step - 1;

	}

	//takes in a tuple, return true if out of bounds
	public boolean outOfBounds(int[] coords) {

		return coords[0] < 0 || coords[0] > 7 || coords[1] < 0 || coords[1] > 7;

	}
	
	//returns color of piece who won
	public int findWinner() {
		
		int sum = 0;
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
				sum += board[i][j];
		
		if (sum > 0)
			return 1;
		if (sum < 0)
			return -1;
		return 0;
		
	}

}
