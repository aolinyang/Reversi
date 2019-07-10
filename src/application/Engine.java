package application;

import java.util.*;
import java.util.concurrent.*;


public class Engine {

	public static int color;
	public static int length;
	public static int height;
	public static int MAX_DEPTH;

	public static int[] minimax(BoardState state, boolean isMax, int alpha, int beta, boolean isParentMax, int depth, int change) {

		if (depth >= MAX_DEPTH) {
			state.value = change;
			if (color == -1)
				state.value = -state.value;
			return null;
		}
		
		int nextcode = state.hasNext();

		if (nextcode == 0) {
			int winner = state.findWinner();
			if (winner == color) {
				state.value = 10000;
			}
			else {
				state.value = -10000;
			}
			return null;
		}
		else if (nextcode == 1 || nextcode == 2) {
			state.findNextStates();
			if (isMax) {
				int bestValue = Integer.MIN_VALUE;
				int[] returndata = new int[2]; //placedx and placedy
				for (int i = 0; i < state.nextStates.length; i++) {
					if (nextcode == 1) {
						minimax(state.nextStates[i], isMax, alpha, beta, isMax, depth+1, change + state.nextStates[i].change);
					}
					else
						minimax(state.nextStates[i], !isMax, alpha, beta, isMax, depth+1, change + state.nextStates[i].change);
					if (state.nextStates[i].value > bestValue) {
						bestValue = state.nextStates[i].value;
						returndata[0] = state.nextStates[i].placedX;
						returndata[1] = state.nextStates[i].placedY;
					}
					state.nextStates[i] = null;
					alpha = Math.max(alpha, bestValue);
					if (!isParentMax && alpha >= beta) {
						break;
					}
				}
				state.nextStates = null;
				state.value = bestValue;
				if (depth == 0) {
					return returndata;
				}
				return null;
			}
			else {
				int bestValue = Integer.MAX_VALUE;
				for (int i = 0; i < state.nextStates.length; i++) {
					if (nextcode == 1) {
						minimax(state.nextStates[i], isMax, alpha, beta, isMax, depth+1, change + state.nextStates[i].change);
					}
					else
						minimax(state.nextStates[i], !isMax, alpha, beta, isMax, depth+1, change + state.nextStates[i].change);
					if (state.nextStates[i].value < bestValue) {
						bestValue = state.nextStates[i].value;
					}
					state.nextStates[i] = null;
					beta = Math.min(beta, bestValue);
					if (isParentMax && alpha >= beta) {
						break;
					}
				}
				state.nextStates = null;
				state.value = bestValue;
				return null;
			}
			
		}
		return null;

	}
}