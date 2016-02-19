package changeassistant.peers;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import changeassistant.change.group.model.SubTreeModel;

/**
 * To finish the third step
 * @author mn8247
 *
 */
public class MatchHelper2 {

	/**
	 * match nodes according to matching status of ancestors,
	 * to reduce 1-m matches as much as possible
	 * @param left
	 * @param right
	 */
	public void matchLeavesAndInners(SubTreeModel left, SubTreeModel right, 
			Set<SubTreeModelPair> matchedLeaves, Set<SubTreeModelPair> matched,
			boolean isExactInnerMatch){
		List<SubTreeModel> candidates = new ArrayList<SubTreeModel>();
		List<SubTreeModel> refinedCandidates = null;
//		System.out.print("");
		SubTreeModel y = null;
		boolean changed = true;
		while(changed){
			changed = false;
			for(Enumeration<SubTreeModel> rightNodes = right.postorderEnumeration(); 
				rightNodes.hasMoreElements();){
				y = rightNodes.nextElement();
				if(y.isLeaf() && !y.isMatched()){
					candidates.clear();
					candidates.addAll(MatchWorker.getMatchedLeftSubTreeModel(matchedLeaves, y));
					if(candidates.size() == 0){
						continue;
					}
					refinedCandidates = MatchWorker.refineWithBestAncestorPath(candidates, y, matched, isExactInnerMatch);
					if(refinedCandidates.size() == 1){
						if(MatchWorker.addCandiMatch(refinedCandidates.get(0), y, 
									candidates, matchedLeaves, matched)){
							changed = true;
							continue;
						}else{
							MatchWorker.removeUselessMatches(matchedLeaves, refinedCandidates, y);
						}
					}
					if(candidates.size() != refinedCandidates.size()){
						candidates.removeAll(refinedCandidates);
						MatchWorker.removeUselessMatchLeaves(matchedLeaves, candidates, y);
					}
				}
			}
		}
	}
	
	/**
	 * candidates.size() > 1
	 * @param candidates
	 * @param y
	 * @return
	 */
	public SubTreeModel removeAmbiguity(List<SubTreeModel> candidates, 
			SubTreeModel y, Set<SubTreeModelPair> matchedLeaves, 
			Set<SubTreeModelPair> matched, boolean isExactInnerMatch){ 
		SubTreeModel result = null;
		SubTreeModel parent, xPare;
		List<SubTreeModel> parentCandidates;
		List<SubTreeModel> refinedCandidates = new ArrayList<SubTreeModel>();
		List<SubTreeModel>parentCandidatesBasedOnChildren = new ArrayList<SubTreeModel>();
//		System.out.print("");
		//find all parents of the candidates
		MatchWorker.getCandidateParents(candidates, y,
				refinedCandidates, parentCandidatesBasedOnChildren,
				matchedLeaves, matched, isExactInnerMatch);
		//if both y and candidates have parents, we may remove ambiguity
		if(y.getParent() != null && parentCandidatesBasedOnChildren.size() != 0){
			parent = (SubTreeModel)y.getParent();
			//get known matches for the parent
			parentCandidates = new ArrayList<SubTreeModel>(
					MatchWorker.getMatchedLeftSubTreeModel(matched, parent));
			if(parentCandidates.size() == 0){
				xPare = removeAmbiguity(parentCandidatesBasedOnChildren, parent,
						matchedLeaves, matched, isExactInnerMatch);
				if(xPare != null){
					refinedCandidates = MatchWorker.refineBasedOnParent(refinedCandidates, 
							xPare);
					if(refinedCandidates.size() == 1){
						result = refinedCandidates.get(0);
					}
				}
			}else if(parentCandidates.size() == 1){//if the parent can be matched unambiguously
				parentCandidatesBasedOnChildren.retainAll(parentCandidates);
				if(parentCandidatesBasedOnChildren.isEmpty()){
					//do something
				}else if(parentCandidatesBasedOnChildren.size() == 1){
					//and if the matched one is just the one among which we are concerned
					refinedCandidates = MatchWorker.refineBasedOnParent(refinedCandidates,
							parentCandidatesBasedOnChildren.get(0));
					if(refinedCandidates.size() == 1){//only one candidate belongs to the matching parent
						//and if the matched parent only corresponds to one child
						result = refinedCandidates.get(0);
					}
				}else{
					//do nothing
				}
			}else{
				//do nothing, since the matched parents have ambiguity
			}
		}
		return result;
	}
}
