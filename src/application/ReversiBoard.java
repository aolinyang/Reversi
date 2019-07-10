package application;

import java.util.ArrayList;
import java.util.Random;

public class ReversiBoard {

	/*
	 * This class contains the 8x8 matrix containing board data,
	 * and the methods which can manipulate that.
	 */

	/*
	 * COORDINATE SYSTEM:
	 * The top left square of the board is (0, 0)
	 * +X = going right
	 * +Y = going down
	 * The bottom right square of the board is (7, 7)
	 * The square at coordinate (x, y) is board[x][y]
	 */
	private int[][] board; //the board for the pieces. 0 = empty, 1 = white, -1 = black, 2 = blocked
	private int length;
	private int height;
	private ArrayList<Integer[]> allBlocked = new ArrayList<Integer[]>();

	/*
	 * COORDINATE SYSTEM FOR LEGAL BOARD:
	 * 6 5 4
	 * 7   3
	 * 8 1 2
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
	private String[][] legalboard; //the board that keeps track of which moves are legal
	private int numLegal; //number of legal spaces
	private ArrayList<Integer[]> allLegal = new ArrayList<Integer[]>();

	public ReversiBoard() {

	}

	public ReversiBoard(int len, int hei, ArrayList<Integer[]> blockedSpaces) {
		board = new int[len][hei];
		length = len;
		height = hei;

		//sets up standard Reversi opening
		board[len/2 - 1][hei/2 - 1] = -1;
		board[len/2 - 1][hei/2] = 1;
		board[len/2][hei/2 - 1] = 1;
		board[len/2][hei/2] = -1;

		//sets up legalboard
		legalboard = new String[len][hei];
		numLegal = len * hei - blockedSpaces.size();

		for (int i = 0; i < blockedSpaces.size(); i++) {
			int tX = blockedSpaces.get(i)[0];
			int tY = blockedSpaces.get(i)[1];
			board[tX][tY] = 2;
		}

		allBlocked = blockedSpaces;
	}

	public ReversiBoard(int len, int hei, int numBlocked) {

		board = new int[len][hei];
		length = len;
		height = hei;

		//sets up standard Reversi opening
		board[len/2 - 1][hei/2 - 1] = -1;
		board[len/2 - 1][hei/2] = 1;
		board[len/2][hei/2 - 1] = 1;
		board[len/2][hei/2] = -1;

		//sets up legalboard
		legalboard = new String[len][hei];
		numLegal = len * hei - numBlocked;

		ArrayList<Integer> bCoords = new ArrayList<Integer>();
		Random rand = new Random();
		int counter = 0;
		while (counter < numBlocked) {
			int tNum;
			int tX;
			int tY;
			do {
				tNum = rand.nextInt(len * hei);
				tX = tNum%len;
				tY = tNum/len;
			} while(bCoords.contains(tNum) || 
					(tX >= len/2 - 2 && tX <= len/2 + 1 && tY >= hei/2 - 2 && tY <= hei/2 + 1));
			bCoords.add(tNum);
			board[tX][tY] = 2;
			Integer[] thisspace = {tX, tY};
			allBlocked.add(thisspace);
			counter++;
		}

	}

	//prints board in terminal. For testing purposes only.
	public void print() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < length; x++) {
				if (board[x][y] == 1)
					System.out.print("W ");
				else if (board[x][y] == -1)
					System.out.print("B ");
				else if (board[x][y] == 0)
					System.out.print("O ");
				else
					System.out.print("X ");
			}
			System.out.println();
		}
	}

	//prints first layer of legalboard in terminal. For testing purposes only.
	public void printlegal() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < length; x++) {
				String toPrint = legalboard[x][y].split(" ")[0];
				System.out.print(toPrint + " ");
			}
			System.out.println();
		}
	}

	//returns new reversiboard with same pieces but empty legalboard
	@Override
	public ReversiBoard clone() {
		ReversiBoard newBoard = new ReversiBoard();
		newBoard.board = new int[length][height];
		newBoard.legalboard = new String[length][height];
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < height; j++) {
				newBoard.board[i][j] = this.board[i][j];
				for (int k = 0; k < 9; k++)
					newBoard.legalboard[i][j] = this.legalboard[i][j];
			}
		}

		newBoard.length = this.length;
		newBoard.height = this.height;

		return newBoard;
	}

	public int[][] getBlockedSpaces() {
		int[][] blocked = new int[allBlocked.size()][2];
		for (int i = 0; i < allBlocked.size(); i++)
			for (int j = 0; j < 2; j++)
				blocked[i][j] = allBlocked.get(i)[j];
		return blocked;
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
	public String[][] getLegal() {

		String[][] newlegal = new String[length][height];
		for (int i = 0; i < length; i++)
			for (int j = 0; j < height; j++) {
				newlegal[i][j] = legalboard[i][j];
			}
		return newlegal;

	}

	public int[][] getBoard() {

		int[][] newboard = new int[length][height];
		for (int i = 0; i < length; i++)
			for (int j = 0; j < height; j++)
				newboard[i][j] = board[i][j];
		return newboard;

	}

	//sets a piece in accordance with move, assuming board legality is already updated and is consistent with move's color,
	//and assumes that move is legal (will end in a same color piece)
	//returns the net change for Engine
	public int setPiece(int color, int x, int y) {

		//follows legalboard coordinate system
		int[] xDirList = {0, 1, 1, 1, 0, -1, -1, -1};
		int[] yDirList = {1, 1, 0, -1, -1, -1, 0, 1};

		int change = 0;
		board[x][y] = color;
		change += color;
		for (int dir = 0; dir < xDirList.length; dir++) {
			int step = 1;
			if (Integer.parseInt(legalboard[x][y].split(" ")[dir+1]) > 0) {
				for (int i = 1; i <= Integer.parseInt(legalboard[x][y].split(" ")[dir+1]); i++) {
					board[x + step * xDirList[dir]][y + step * yDirList[dir]] = color;
					change += color * 2;
					step++;
				}
			}
		}

		return change;

	}

	//updates the legality board if the color is placing and updates the number of legal positions, returns true if there are no legal moves left
	public boolean updateLegal(int color) {

		numLegal = 0; //resets number of legal moves
		allLegal.clear();

		//follows legalboard coordinate system
		int[] xDirList = {0, 1, 1, 1, 0, -1, -1, -1};
		int[] yDirList = {1, 1, 0, -1, -1, -1, 0, 1};

		//loops through the entire board
		for (int x = 0; x < length; x++) {
			for (int y = 0; y < height; y++) {

				//first assume it's illegal until proven otherwise
				legalboard[x][y] = "0";
				//make sure the coordinate is an empty space
				if (board[x][y] != 0)
					continue;

				int totalTaken = 0;

				//now checks all the possible directions
				String takendata = "";
				for (int i = 0; i < 8; i++) {
					int numTaken = isLegal(color, x, y, xDirList[i], yDirList[i]);
					takendata += " " + numTaken; //becomes the number of pieces that will be flipped
					totalTaken += numTaken;
				}

				legalboard[x][y] = totalTaken + takendata;
				if (totalTaken > 0) {
					numLegal++;
					Integer[] curcoords = {x, y};
					allLegal.add(curcoords);
				}

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
		if (outOfBounds(nextCoord) || board[nextCoord[0]][nextCoord[1]] == 0 || board[nextCoord[0]][nextCoord[1]] == 2) //illegal if a boundary or an empty or blocked space comes up before the same color
			return 0;

		return step - 1;

	}

	//takes in a tuple, return true if out of bounds
	public boolean outOfBounds(int[] coords) {

		return coords[0] < 0 || coords[0] > length-1 || coords[1] < 0 || coords[1] > height-1;

	}

	public int[] getDimensions() {
		int[] dims = {length, height};
		return dims;
	}

	public int numLegal() {
		return numLegal;
	}

	//returns color of piece who won
	public int findWinner() {

		int sum = 0;
		for (int i = 0; i < length; i++)
			for (int j = 0; j < height; j++) {
				if (board[i][j] != 2)
					sum += board[i][j];
			}

		if (sum > 0)
			return 1;
		if (sum < 0)
			return -1;
		return 0;

	}

	//only used by boardstate for finding previously computed states
	//therefore can assume that dimensions and blocked spaces are the same
	public boolean equals(ReversiBoard other) {

		for (int i = 0; i < length; i++)
			for (int j = 0; j < height; j++)
				if (board[i][j] != other.board[i][j])
					return false;
		return true;

	}

	public ArrayList<Integer[]> getAllLegal() {
		return allLegal;
	}

}
