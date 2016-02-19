package p1;

public class Binary2 extends Binary2Super {

	public void m1() {
		A a1 = new A();
		A a2 = new A();
		boolean b = a1 == a1s || a2 == a2s;
		System.out.println(b);
	}
	
}
