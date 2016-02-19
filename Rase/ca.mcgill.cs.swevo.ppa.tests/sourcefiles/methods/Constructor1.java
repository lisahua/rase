package p1;

public class Constructor1 extends SuperConstructor1 {
	
	public Constructor1D1 main() {
		int i = 2;
		String s = "";
		Constructor1A1 a = new Constructor1A1();
		
		Constructor1B1 b = new Constructor1B1();
		Constructor1B1 b2 = new Constructor1B1(i,s,a);
		Constructor1C1 c = new Constructor1C1(b2,"",2);
		f1 = f2;
		f2 = i;
		Constructor1C2 c2 = new Constructor1C2(f2,f1);
		return new Constructor1D1(b,s);
		
		
	}
	
}