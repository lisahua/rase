package changeassistant.multipleexample.common;

import java.util.List;

public class EqualCalculator<T> {
	
	public boolean hasAllEqualTo(T l, List<T> rs){
		boolean equal = true;
		for(T r : rs){
			if(!l.equals(r)){
				equal = false;
				break;
			}
		}
		return equal;
	}
	
	public boolean hasAllNotEqualTo(T l, List<T> rs){
		boolean notEqual = true;
		for(T r : rs){
			if(l.equals(r)){
				notEqual = false;
				break;
			}
		}
		return notEqual;
	}
	
	public boolean hasAllEqualTo2(T l, List<T> rs){
		boolean equal = true;
		for(T r : rs){
			if(l != r){
				equal = false;
				break;
			}
		}
		return equal;
	}

	public boolean hasAllNotEqualTo2(T l, List<T> rs){ 
		boolean notEqual = true;
		for(T r : rs){
			if(l == r){
				notEqual = false;
				break;
			}
		}
		return notEqual;
	}
	
	public boolean hasSomeEqualTo2(T l, List<T> rs){
		boolean equal = false;
		for(T r : rs){
			if(r == l){
				equal = true;
				break;
			}
		}
		return equal;
	}
	
	public boolean hasSomeNotEqualTo2(T l, List<T> rs){
		boolean notEqual = false;
		for(T r : rs){
			if(r != l){
				notEqual = true;
				break;
			}
		}
		return notEqual;
	}
}
