package changeassistant.multipleexample.util;

import java.util.List;

public class SameChecker<T> {

	public boolean areSame(List<T> elems){
		boolean result = true;
		T elem0 = elems.get(0);
		if(elem0 == null){
			for(int i = 1; i < elems.size(); i++){
				if(elems.get(i) != null){
					result = false;
					break;
				}
			}
		}else{
			for(int i = 1; i < elems.size(); i++){
				if(!elem0.equals(elems.get(i))){
					result = false;
					break;
				}
			}
		}
		return result;
	}
}
