package application;

import java.util.*;

public class Test {
	
	public static ArrayList<Obj> list = null;
	public static int length = 5;
	public static int[] li = new int[10];

	public static void main(String[] args) {
		
		System.out.println(length);
		System.out.println(li.length);
		
	}
	
}

class Obj {
	
	int num;
	
	public Obj(int n) {
		num = n;
	}
	
	public void exit() {
		Test.list.remove(this);
	}
	
}
