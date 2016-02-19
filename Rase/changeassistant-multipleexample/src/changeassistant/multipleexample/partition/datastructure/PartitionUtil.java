package changeassistant.multipleexample.partition.datastructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PartitionUtil {
	
	public static int compareSet(Set<?> indexes, Set<?> otherIndexes){
		if(indexes.containsAll(otherIndexes)){
			if(indexes.size() == otherIndexes.size())//the two sets are equal
				return 0;
			return 1; //the first set is larger
		}
		return -1;
	}
	
	public static boolean equalList(List<?> indexes, List<?> otherIndexes){
		List<?> thisIndexes = new ArrayList(indexes);
		if(thisIndexes.size() != otherIndexes.size())
			return false;
		thisIndexes.removeAll(otherIndexes);
		if(!thisIndexes.isEmpty())
			return false;
		return true;
	}
	
//	public static boolean equalSet(Set<?> indexes, Set<?> otherIndexes){
//		Set<?> thisIndexes = new HashSet(indexes);
//		if(thisIndexes.size() != otherIndexes.size())
//			return false;
//		thisIndexes.removeAll(otherIndexes);
//		if(!thisIndexes.isEmpty())
//			return false;
//		return true;
//	}
//	
//	public static Set<?> intersectSet(Set<?> indexes, Set<?> otherIndexes){
//		Set<?> thisIndexes = new HashSet(indexes);
//		thisIndexes.retainAll(otherIndexes);
//		return thisIndexes;
//	}
}
