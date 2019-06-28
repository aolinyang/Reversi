package application;

import java.util.*;

public class Test {
	
	public static ArrayList<Obj> list = null;

	public static void main(String[] args) {
		
		list = new ArrayList<Obj>();
		
		Obj one = new Obj(1);
		Obj two = new Obj(2);
		Obj three = new Obj(3);
		list.add(one);
		list.add(two);
		list.add(three);
		
		one.exit();
		System.out.println(list.size());
		System.out.println(list.get(0).num);
		
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
