package changeassistant.multipleexample.datastructure;

public class Pair<T> {

	T left;
	T right;
	public Pair(T a, T b){
		left = a;
		right = b;
	}
	
	public boolean equals(Pair<T> p){
		return left.equals(p.left) && right.equals(p.right);
	}
	
	public T getLeft(){
		return left;
	}
	
	public T getRight(){
		return right;
	}
	
	public void setLeft(T t){
		left = t;
	}
	
	public void setRight(T t){
		right = t;
	}
	
	public int hashCode(){
		return left.hashCode() * 1000 + right.hashCode();
	}
	
	public String toString(){
		return "left = " + left.toString() + "  right = " + right.toString();
	}
}
