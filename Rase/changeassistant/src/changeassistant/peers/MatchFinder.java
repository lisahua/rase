package changeassistant.peers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import changeassistant.change.group.model.MatchingInfo;
import changeassistant.change.group.model.SubTreeModel;

public class MatchFinder {
	
	public static List<SubTreeModelPair> matched;
	public static Map<SubTreeModelPair, Set<SubTreeModelPair>> parentAndChildrenMap;

	public static MatchingInfo findPrimeMatch(
			SubTreeModel right, SubTreeModel left, 
			List<SubTreeModelPair> matched, 
			Map<SubTreeModelPair, Set<SubTreeModelPair>>parentAndChildrenMap){
		MatchingInfo mInfo = new MatchingInfo();
		MatchFinder.matched = matched;
		MatchFinder.parentAndChildrenMap = parentAndChildrenMap;
		Set<SubTreeModel> candidates = findMatches(right);
		
		
		return mInfo;
	}
	
	private static Set<SubTreeModel> findMatches(SubTreeModel right){
		Set<SubTreeModel> candidates = new HashSet<SubTreeModel>();
		for(SubTreeModelPair pair : matched){
			if(pair.getRight().equals(right)){
				candidates.add(pair.getLeft());
			}
		}
		return candidates;
	}
}
