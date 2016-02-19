package changeassistant.peers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;

/**
 * To finish the fourth step of matching with LCS
 * @author mn8247
 *
 */
public class MatchHelper3 {
	
    private MatchCollector collector;
    
    private Map<TypeNameTerm, TypeNameTerm> primeTypeMap;
	private Map<MethodNameTerm, MethodNameTerm> primeMethodMap;
	private Map<VariableTypeBindingTerm, VariableTypeBindingTerm> primeVariableMap;
    
	private BlackPairSet blackPairs;
	
	public void disableBlackPairSet(){
		blackPairs.disable();
	}
	
	public void init(Map<TypeNameTerm, TypeNameTerm> pTMap,
			Map<MethodNameTerm, MethodNameTerm> pMMap,
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> pVMap){
		primeTypeMap = pTMap;
		primeMethodMap = pMMap;
		primeVariableMap = pVMap;
		collector = new MatchCollector();
	}
	
	public List<SubTreeModel> findBestChildrenMatch(List<SubTreeModel>candidates, SubTreeModel y,
			Set<SubTreeModelPair> matched){
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		if(y.getParent() == null){
			result = findBestMatchForUnParented(candidates, y, matched);
		}else{
			result = findBestMatchForParented(candidates, y, matched);
		}
		return result;
	}
	
	/**
	 * Try to match candidates with y according to LCS among siblings
	 * @param candidates
	 * @param y
	 * @param matched
	 * @return Maybe null
	 */
	private List<SubTreeModel> findBestMatchForParented(List<SubTreeModel> candidates, SubTreeModel y,
			Set<SubTreeModelPair> matched){
//		System.out.print("");
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		List<SubTreeModel> refinedCandidates = new ArrayList<SubTreeModel>();
		List<Integer> mIndexes = MatchWorker.findBestParentWithLCS(candidates, y, matched);
		
		if(mIndexes.size() == 1){
			result.add(candidates.get(mIndexes.get(0)));
		}else{
			for(int i = 0; i < mIndexes.size(); i++){
				refinedCandidates.add(candidates.get(mIndexes.get(i)));
			}
			MatchWorker.addBlackPairs(candidates, refinedCandidates, y, blackPairs);
			result = processParent(refinedCandidates, y, matched);
		}
		return result;	
	}
	
	private List<SubTreeModel> findBestMatchForUnParented(List<SubTreeModel>candidates, SubTreeModel y,
			Set<SubTreeModelPair> matched){
		SubTreeModel leftParent = null;
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		List<SubTreeModel> refinedCandidates = new ArrayList<SubTreeModel>();
		for(int i = 0; i < candidates.size(); i++){
			leftParent = (SubTreeModel)candidates.get(i).getParent();
			if(leftParent == null){
				refinedCandidates.add(candidates.get(i));
			}
		}
		if(refinedCandidates.size() == 1){
			result.add(refinedCandidates.get(0));
		}else {
			if(refinedCandidates.size() == 0){
				refinedCandidates.addAll(candidates);
			}else{
				MatchWorker.addBlackPairs(candidates, refinedCandidates, y, blackPairs);
			}
			result = findRelativeBestMatch(refinedCandidates, y);
		}
		return result;
	}
	
	/**
	 * Match according to content
	 * @param candidates
	 * @param y
	 * @return
	 */
	private List<SubTreeModel> findRelativeBestMatch(List<SubTreeModel> candidates, SubTreeModel y){
//		System.out.print("");
		SubTreeModel can;
		if(candidates.size() == 1)
			return candidates;
		List<SubTreeModel> refinedCandidates = new ArrayList<SubTreeModel>();
		for(SubTreeModel xCan : candidates){
			if(MatchWorker.isConsistentMap(new SubTreeModelPair(xCan, y), 
					primeTypeMap, primeMethodMap, primeVariableMap)){
				refinedCandidates.add(xCan);
			}
		}
		if(refinedCandidates.size() == 1){//to remove possible conflict introducing mapping
			return refinedCandidates;
		}else{//to try to map with help of scoring system
			CellInfo computer = new CellInfo();
			double[] scores = new double[candidates.size()];
			for(int i = 0; i < refinedCandidates.size(); i++){
				can = refinedCandidates.get(i);
				scores[i] = computer.computeSimilarity(new SubTreeModelPair(can, y));
			}
			List<Integer> mIndexes = MatchWorker.findMaxScoreIndexes(scores);
			List<SubTreeModel> result = new ArrayList<SubTreeModel>();
			if(mIndexes.size() == 0 || scores[mIndexes.get(0)] == 0)
				return result;
			for(Integer mIndex : mIndexes){
				result.add(refinedCandidates.get(mIndex));
			}
			return result;
		}	
	}
	
	public BlackPairSet getBlackPairs(){
		return this.blackPairs;
	}

	/**
	 * improve matching according to mapped LCS
	 * @param left
	 * @param right
	 */
	public Map<SubTreeModel, List<SubTreeModel>> matchNodes(SubTreeModel right,
			Set<SubTreeModelPair> matchedLeaves,
			Set<SubTreeModelPair> matched,
			Map<TypeNameTerm, TypeNameTerm> pTMap,
	        Map<MethodNameTerm, MethodNameTerm> pMMap,
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> pVMap,
			BlackPairSet blackPairSet,
			boolean isExactMatch){
		init(pTMap, pMMap, pVMap);
		SubTreeModel x = null, y = null;
		Map<SubTreeModel, List<SubTreeModel>> candiMap = new HashMap<SubTreeModel, List<SubTreeModel>>();
		this.blackPairs = blackPairSet;
		if(matched.size() == right.countNodes()){
			blackPairs.addAll(collector.collectSpecificMatching(matchedLeaves, matched, primeTypeMap,
					primeMethodMap, primeVariableMap));
		}else{
			boolean changed = true;
			while(changed){
				changed = false;
				//collect specific matching info
				blackPairs.addAll(collector.collectSpecificMatching(matchedLeaves, matched, primeTypeMap,
						primeMethodMap, primeVariableMap));
				for(Enumeration<SubTreeModel> rightNodes = right.postorderEnumeration(); rightNodes.hasMoreElements();){
					y = rightNodes.nextElement();
					int ii = 0;
					if(!y.isMatched() && y.isLeaf()){
//						System.out.print("");
						ii++;
						x = null;
						List<SubTreeModel> candidates = new ArrayList<SubTreeModel>();
						candidates.addAll(MatchWorker.getMatchedLeftSubTreeModel(matchedLeaves, y));
						if(candidates.size() == 0){
							//do nothing
						}else{//candidates.size() > 0
//							System.out.print("");
							candidates = findBestChildrenMatch(candidates, y, matched);
							if(candidates.size() == 1){
								x = candidates.get(0);
							}else if(candidates.size() > 1){
								candiMap.put(y, candidates);
							}
						}
//						if(x == null){
//							x = candidates.get(0);
//						}
						if(x != null && !blackPairs.contains(new SubTreeModelPair(x, y))){
							boolean isMatched = true;
							if(!MatchWorker.checkEquivalentIteratively(x, y)){
								blackPairs.addBlackPair(new SubTreeModelPair(x, y));
								isMatched = false;
							}
							if(isMatched){
								if(MatchWorker.addCandiMatch(x, y, candidates, matchedLeaves, matched)){
									changed = true;
								}else{
									blackPairs.addBlackPair(new SubTreeModelPair(x, y));
								}
							}
						}
					}
				}
			}
		}
		if(candiMap.size() > 1){//to remove redundant candidate mappings in candiMap
			candiMap = refineCandiMap(candiMap);
		}
		return candiMap;
	}
	
	/**
	 * y is guaranteed to have a parent
	 * @param refinedCandidates
	 * @param y
	 * @return
	 */
	private List<SubTreeModel> processParent(List<SubTreeModel> refinedCandidates, SubTreeModel y,
			Set<SubTreeModelPair> matched){
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		SubTreeModel tempParent = null;
		SubTreeModel parent = (SubTreeModel)y.getParent();
		List<SubTreeModel> parentCandidates = new ArrayList<SubTreeModel>();
		for(int i = 0; i < refinedCandidates.size(); i++){
			tempParent = (SubTreeModel)refinedCandidates.get(i).getParent();
			if(tempParent == null){
				continue;
			}else{
				if(parentCandidates.contains(tempParent)){
					continue;
				}else{
					parentCandidates.add(tempParent);
				}
			}
		}
		List<SubTreeModel> primeParents = new ArrayList<SubTreeModel>();
		if(parentCandidates.size() == 1){
			primeParents.addAll(parentCandidates);
		}else{
			if(parent.getParent() == null){
				primeParents = findBestMatchForUnParented(parentCandidates, parent, matched);
			}else{
				primeParents = findBestMatchForParented(parentCandidates, parent, matched);
			}
		}
		List<SubTreeModel> refinedCandidates2 = new ArrayList<SubTreeModel>();
		for(SubTreeModel primeParent : primeParents){
			refinedCandidates2.clear();
			for(int i = 0; i < refinedCandidates.size(); i++){
				tempParent = (SubTreeModel)refinedCandidates.get(i).getParent();
				if(tempParent != null && tempParent.equals(primeParent)){
						refinedCandidates2.add(refinedCandidates.get(i));
				}
			}
			if(refinedCandidates2.size() == 1){
				result.addAll(refinedCandidates2);
			}else{
				result.addAll(findRelativeBestMatch(refinedCandidates2, y));
			}
		}
		return result;
	}

	private Map<SubTreeModel, List<SubTreeModel>> refineCandiMap(Map<SubTreeModel, List<SubTreeModel>> candiMap){
		Map<SubTreeModel, List<SubTreeModel>> newCandiMap = new HashMap<SubTreeModel, List<SubTreeModel>>();
		for(SubTreeModel key : new HashSet<SubTreeModel>(candiMap.keySet())){
			if(key.isMatched() || key.getStrValue().equals("break:")||
					key.getStrValue().equals("continue:") || key.getStrValue().equals("return:")){
				candiMap.remove(key);
			}
		}
		if(candiMap.size() >= 2){
			List<SubTreeModel> value = null;
			SubTreeModel key = null;
			Iterator<SubTreeModel> keyIter = (new HashSet<SubTreeModel>(candiMap.keySet())).iterator();
			key = keyIter.next();
			newCandiMap.put(key, candiMap.get(key));
			boolean isRedundant = false;
			while(keyIter.hasNext()){
				key = keyIter.next();
				value = candiMap.get(key);
				isRedundant = false;
				for(Entry<SubTreeModel, List<SubTreeModel>> newEntry : newCandiMap.entrySet()){
					if(((List<SubTreeModel>)newEntry.getValue()).containsAll(value)){
						isRedundant = true;
						break;
					}
				}
				if(!isRedundant){
					newCandiMap.put(key, value);
				}
			}
			candiMap = newCandiMap;
		}
		return newCandiMap;
	}
}
