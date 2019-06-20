package application;

import java.util.Random;

public class Test {

	public static void main(String[] args) {
		
		ReversiBoard b = new ReversiBoard();
		Computer c = new Computer(b, 1);
		
		int x = 1;
		for (int i = 0; i < 20; i++) {
			b.updateLegal(x);
			b.printlegal();
			System.out.println();
			c.makeMove();
			b.print();
			System.out.println();
			x *= -1;
			//c.color = x;
		}
		
		Random rand = new Random();
		for (int i = 0; i < 100; i++)
			System.out.println(rand.nextInt(2) * 2 - 1);
		
	}
	
}
