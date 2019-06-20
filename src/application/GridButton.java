package application;

import javafx.scene.control.Button;

public class GridButton extends Button {

	private int x;
	private int y;
	
	public GridButton(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public int[] getcoord() {
		int[] coords = {x, y};
		return coords;
	}
	
}
