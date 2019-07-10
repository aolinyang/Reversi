package application;

public class Test {
	
	public static void main(String[] args) {
		
		String t = "abc";
		String b = t;
		b = "aaa";
		System.out.println(t);
		
	}
	
	public static void move(String t) {
		t = "aaa";
	}
	
}
