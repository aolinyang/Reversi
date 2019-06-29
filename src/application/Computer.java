package application;

import java.util.ArrayList;
import java.util.Random;

public class Computer {

	/*
	 * This class defines an AI that will make moves based on a set of defined rules for maximizing points.
	 */

	private ReversiBoard rboard;
	private int color;
	private int size;

	public Computer(ReversiBoard playBoard, int color) {

		rboard = playBoard;
		this.color = color;
		size = rboard.getSize();

	}

	/*
	 * This method will first choose a move based off of the max points algorithm, then send it to rboard's setPiece() method
	 * 
	 * MAX POINTS ALGORITHM
	 * 
	 * Every Move will have a maximum number of points.
	 * Uses the same directional coordinates as updatelegal.
	 * 
	 * If piece is at corner: +100 points
	 * If piece is next to corner: -50 points
	 * If piece is at side: +8 points
	 * Piece will always gain +n points, where n is the number of pieces flipped
	 * In that case, if both ends is a border, +5 points.
	 * If opponent can reverse everything next turn: points * -1.
	 */
	public void makeMove() {

		int[][][] legalboard = rboard.getLegal();

		ArrayList<Integer[]> bestMoves = null; //will use this as parameter to call setPiece() in the end
		int maxpoints = Integer.MIN_VALUE;

		//each pair corresponds to a move coordinate in updatelegal
		int[] xDirList = {0, 1, 1, 1, 0, -1, -1, -1};
		int[] yDirList = {1, 1, 0, -1, -1, -1, 0, 1};

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {

				int[] curSquare = legalboard[x][y];
				if (curSquare[0] == 0) //make sure there is a legal move
					continue;

				int tpoints = 0; //total points for this piece
				//at corner
				if (x == 0 && y == 0 || x == size-1 && y == 0 || x == 0 && y == size-1 || x == size-1 && y == size-1)
					tpoints += 100;
				else if (x == 0 || x == size-1 || y == 0 || y == size-1) //at edge
					tpoints += 8;
				
				if (isNextToCorner(x, y))
					tpoints -= 50;

				//goes through all 8 directions
				for (int i = 0; i < 8; i++) {
					if (curSquare[i+1] == 0) //this direction is illegal, don't even consider it
						continue;
					int score = 0; //the score of this direction
					int xdir = xDirList[i];
					int ydir = yDirList[i];

					int numFlipped = curSquare[i+1];
					score += numFlipped;
					int xend1 = x + xdir * (numFlipped + 2);
					int yend1 = y + ydir * (numFlipped + 2);
					int[] end1 = {xend1, yend1}; //the piece beyond the end piece of the same color on the other end
					int xend2 = x - xdir;
					int yend2 = y - ydir;
					int[] end2 = {xend2, yend2}; //the piece on this end right before the placed piece

					if (isTravelOnEdge(x, y, xdir, ydir))
						score += 10;
					
					if (rboard.outOfBounds(end1) || rboard.outOfBounds(end2)) {
						score += 5;
					}
					else if ((rboard.getSpace(end1) == 0 && rboard.getSpace(end2) == color * -1) || (rboard.getSpace(end2) == 0 && rboard.getSpace(end1) == color * -1)) {
						score *= -1;
					}
					
					tpoints += score;

				}
				
				if (tpoints > maxpoints) {
					bestMoves = new ArrayList<Integer[]>();
					Integer[] toAdd = {x, y};
					bestMoves.add(toAdd);
					maxpoints = tpoints;
				} else if (tpoints == maxpoints) {
					Integer[] toAdd = {x, y};
					bestMoves.add(toAdd);
				}

			}
		}

		Integer[] bMove = null;
		if (bestMoves.size() == 1)
			bMove = bestMoves.get(0);
		else {
			Random rand = new Random();
			int num = rand.nextInt(bestMoves.size());
			bMove = bestMoves.get(num);
		}

		rboard.setPiece(color, bMove[0], bMove[1]);

	}

	private boolean isNextToCorner(int x, int y) {

		int distance1 = x*x + y*y;
		int distance2 = (x-(size-1))*(x-(size-1)) + y*y;
		int distance3 = x*x + (y-(size-1))*(y-(size-1));
		int distance4 = (x-(size-1))*(x-(size-1)) + (y-(size-1))*(y-(size-1));

		return (distance1 <= 2 && rboard.getSpace(0, 0) != 2) || 
			   (distance2 <= 2 && rboard.getSpace(size-1, 0) != 2) || 
			   (distance3 <= 2 && rboard.getSpace(0, size-1) != 2) || 
			   (distance4 <= 2 && rboard.getSpace(size-1, size-1) != 2);

	}
	
	private boolean isTravelOnEdge(int x, int y, int xdir, int ydir) {
		
		if (x == 0 || x == (size-1)) {
			if (ydir != 0 && xdir == 0)
				return true;
		}
		if (y == 0 || y == (size-1)) {
			if (xdir != 0 && ydir == 0)
				return true;
		}
		
		return false;
		
	}

}
