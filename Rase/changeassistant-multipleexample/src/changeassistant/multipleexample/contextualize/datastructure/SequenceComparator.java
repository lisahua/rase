package changeassistant.multipleexample.contextualize.datastructure;

import java.util.Comparator;

public class SequenceComparator implements Comparator<Sequence>{

	@Override
	public int compare(Sequence s1, Sequence s2) {
		if(s1.size() > s2.size())
			return 1;
		if(s1.size() < s2.size())
			return -1;
		Integer i1 = null, i2 = null;
		for(int i = 0; i < s1.getNodeIndexes().size(); i++){
			i1 = s1.get(i);
			i2 = s2.get(i);
			if(Math.abs(i1) > Math.abs(i2))
				return 1;
			if(Math.abs(i1) < Math.abs(i2))
				return -1;	
		}
		return 0;
	}

}
