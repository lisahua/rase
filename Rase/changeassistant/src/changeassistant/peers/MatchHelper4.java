package changeassistant.peers;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;

public class MatchHelper4 {

	private MatchCollector collector;
	public SubTreeModel left = null, right = null;
	private Set<SubTreeModelPair> oMatchedLeaves, oMatched;
	public Set<SubTreeModelPair> matchedLeaves, matched;
	public Map<TypeNameTerm, TypeNameTerm> primeTypeMap;
	private Map<TypeNameTerm, TypeNameTerm> oPrimeTypeMap;
	public Map<MethodNameTerm, MethodNameTerm> primeMethodMap;
	private Map<MethodNameTerm, MethodNameTerm> oPrimeMethodMap;
	public Map<VariableTypeBindingTerm, VariableTypeBindingTerm> primeVariableMap;
	private Map<VariableTypeBindingTerm, VariableTypeBindingTerm> oPrimeVariableMap;

	private BlackPairSet oBlackPairs;
	public BlackPairSet blackPairs;

	public boolean adjust(SubTreeModel left, SubTreeModel right) {
		// System.out.print("");
		boolean success = false;
		SubTreeModel x = null, y = null;
		SubTreeModel parent;
		List<SubTreeModel> candidates = new ArrayList<SubTreeModel>();

		System.out.print("");
		for (Enumeration<SubTreeModel> rightNodes = right
				.postorderEnumeration(); rightNodes.hasMoreElements();) {
			y = rightNodes.nextElement();
			if (y.isLeaf() && !y.isMatched()) {
				candidates.clear();
				candidates.addAll(MatchWorker.getMatchedLeftSubTreeModel(
						matchedLeaves, y));
				if (candidates.size() > 0) {
					for (SubTreeModel candidate : candidates) {
						if (MatchWorker.addCandiMatch(candidate, y, candidates,
								matchedLeaves, matched)) {
							success = true;
							break;
						}
					}
				}
			}
		}
		if (!success) {
			for (Enumeration<SubTreeModel> rightNodes = right
					.postorderEnumeration(); rightNodes.hasMoreElements();) {
				y = rightNodes.nextElement();
				if (y.isLeaf() && !y.isMatched()) {
					parent = (SubTreeModel) y.getParent();
					if (parent == null) {
						continue;
					} else {
						candidates = MatchWorker.searchEquivalent(y, matched,
								false);
						Set<SubTreeModel> candidatesToRemove = new HashSet<SubTreeModel>();
						for (SubTreeModel tmp : candidates) {
							if (blackPairs
									.contains(new SubTreeModelPair(tmp, y))) {
								candidatesToRemove.add(tmp);
							}
						}
						candidates.removeAll(candidatesToRemove);
					}
					if (candidates.size() == 0) {// there is no possible match
													// to use
						if (y.getEDITED_TYPE() == null)
							candidates = MatchWorker
									.searchEquivalentWithoutParent(y, left,
											false);
					}
					if (candidates.size() == 0) {
						continue;
					}
					for (SubTreeModel candi : candidates) {
						System.out.print("");
						SubTreeModelPair pair = new SubTreeModelPair(candi, y);
						if (!blackPairs.contains(pair)) {
							if (MatchWorker.getMatchedRightSubTreeModel(
									matchedLeaves, x).isEmpty()
									&& !MatchWorker.isEquivalentNode(candi, y,
											true) && pair.doMap()) {
								matchedLeaves.add(pair);
								success = true;
							} else {
								blackPairs.addBlackPair(pair);
							}
						}
					}
				}
			}
		}
		return success;
	}

	public List<SubTreeModel> findBestChildrenMatch(
			List<SubTreeModel> candidates, SubTreeModel y,
			Set<SubTreeModelPair> matched) {
		List<SubTreeModel> result = null;
		// System.out.print("");
		if (y.getParent() == null) {
			result = findPossibleBestForUnParented(candidates, y, matched);
		} else {
			result = findPossibleBestForParented(candidates, y, matched);
		}
		return result;
	}

	private List<SubTreeModel> findPossibleBestForParented(
			List<SubTreeModel> candidates, SubTreeModel y,
			Set<SubTreeModelPair> matched) {
		System.out.print("");
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		List<SubTreeModel> refinedCandidates = MatchWorker
				.refineWithBestAncestorPath(candidates, y, matched, false);
		List<Integer> mIndexes = MatchWorker.findBestParentWithLCS(
				refinedCandidates, y, matched);
		for (Integer mIndex : mIndexes) {
			result.add(refinedCandidates.get(mIndex));
		}
		if (result.size() > 1) {
			result = processParent(result, y, matched);
		}
		return result;
	}

	private List<SubTreeModel> findPossibleBestForUnParented(
			List<SubTreeModel> candidates, SubTreeModel y,
			Set<SubTreeModelPair> matched) {
		SubTreeModel leftParent = null;
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		List<SubTreeModel> refinedCandidates = new ArrayList<SubTreeModel>();
		for (int i = 0; i < candidates.size(); i++) {
			leftParent = (SubTreeModel) candidates.get(i).getParent();
			if (leftParent == null) {
				refinedCandidates.add(candidates.get(i));
			}
		}
		if (refinedCandidates.size() == 1) {
			result.add(refinedCandidates.get(0));
		} else {
			if (refinedCandidates.size() == 0) {
				refinedCandidates.addAll(candidates);
			}
			result = findRelativeBestMatch(refinedCandidates, y);
		}
		return result;
	}

	/**
	 * Match according to content
	 * 
	 * @param candidates
	 * @param y
	 * @return
	 */
	private List<SubTreeModel> findRelativeBestMatch(
			List<SubTreeModel> candidates, SubTreeModel y) {
		// System.out.print("");
		SubTreeModel can;
		if (candidates.size() == 1)
			return candidates;
		List<SubTreeModel> refinedCandidates = new ArrayList<SubTreeModel>();
		for (SubTreeModel xCan : candidates) {
			if (MatchWorker.isConsistentMap(new SubTreeModelPair(xCan, y),
					primeTypeMap, primeMethodMap, primeVariableMap)) {
				refinedCandidates.add(xCan);
			}
		}
		if (refinedCandidates.size() <= 1) {// to remove possible conflict
											// introducing mapping
			return refinedCandidates;
		} else {// to try to map with help of scoring system
			CellInfo computer = new CellInfo();
			double[] scores = new double[candidates.size()];
			for (int i = 0; i < refinedCandidates.size(); i++) {
				can = refinedCandidates.get(i);
				scores[i] = computer.computeSimilarity(new SubTreeModelPair(
						can, y));
			}
			List<Integer> mIndexes = MatchWorker.findMaxScoreIndexes(scores);
			List<SubTreeModel> result = new ArrayList<SubTreeModel>();
			if (mIndexes.size() == 0 && (scores[0] - 0 < Math.pow(10, -6))) {
				result.addAll(refinedCandidates);
			} else {
				for (Integer mIndex : mIndexes) {
					result.add(refinedCandidates.get(mIndex));
				}
			}
			// if(scores[mIndexes.get(0)] == 0)
			// return result;
			return result;
		}
	}

	// public BlackPairSet getBlackPairs(){
	// return this.blackPairs;
	// }

	public void initForAdjust(Set<SubTreeModelPair> matchedLeaves,
			Set<SubTreeModelPair> matched,
			Map<TypeNameTerm, TypeNameTerm> pTMap,
			Map<MethodNameTerm, MethodNameTerm> pMMap,
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> pVMap,
			BlackPairSet blackPairs) {
		collector = new MatchCollector();
		primeTypeMap = pTMap;
		primeMethodMap = pMMap;
		primeVariableMap = pVMap;

		oPrimeTypeMap = null;
		oPrimeMethodMap = null;
		oPrimeVariableMap = null;
		this.matchedLeaves = matchedLeaves;
		this.matched = matched;
		this.blackPairs = blackPairs;
	}

	private void initForMatchNodes(Set<SubTreeModelPair> matchedLeaves,
			Set<SubTreeModelPair> matched,
			Map<TypeNameTerm, TypeNameTerm> pTMap,
			Map<MethodNameTerm, MethodNameTerm> pMMap,
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> pVMap,
			BlackPairSet blackPairs) {
		collector = new MatchCollector();
		oPrimeTypeMap = pTMap;
		oPrimeMethodMap = pMMap;
		oPrimeVariableMap = pVMap;
		primeTypeMap = new HashMap<TypeNameTerm, TypeNameTerm>(pTMap);
		primeMethodMap = new HashMap<MethodNameTerm, MethodNameTerm>(pMMap);
		primeVariableMap = new HashMap<VariableTypeBindingTerm, VariableTypeBindingTerm>(
				pVMap);

		oMatchedLeaves = matchedLeaves;
		oMatched = matched;
		this.matchedLeaves = new HashSet<SubTreeModelPair>(matchedLeaves);
		this.matched = new HashSet<SubTreeModelPair>(matched);

		this.blackPairs = blackPairs;
		this.oBlackPairs = new BlackPairSet(this.blackPairs);
	}

	public void matchNodes(SubTreeModel left, SubTreeModel right) {
		boolean changed = true;
		SubTreeModel y = null;
		do {
			while (changed) {// to iterate without guesses
				changed = false;
				for (Enumeration<SubTreeModel> rightNodes = right
						.postorderEnumeration(); rightNodes.hasMoreElements();) {
					y = rightNodes.nextElement();
					int ii = 0;
					if (y.isLeaf() && !y.isMatched()) {
						ii++;
						List<SubTreeModel> candidates = new ArrayList<SubTreeModel>();
						List<SubTreeModel> refinedCandidates = new ArrayList<SubTreeModel>();
						candidates.addAll(MatchWorker
								.getMatchedLeftSubTreeModel(matchedLeaves, y));
						if (candidates.size() >= 1) {
							refinedCandidates = findBestChildrenMatch(
									candidates, y, matched);
						} else {
							continue;
						}
						if (refinedCandidates.size() == 1) {
							SubTreeModel x = refinedCandidates.get(0);
							SubTreeModelPair pair = new SubTreeModelPair(x, y);
							if (blackPairs.contains(pair)) {
								continue;
							} else if (!MatchWorker.checkEquivalentIteratively(
									x, y)
									|| !MatchWorker.addCandiMatch(x, y,
											refinedCandidates, matchedLeaves,
											matched)) {
								MatchWorker.removeUselessMatches(matchedLeaves,
										refinedCandidates, y);
								blackPairs.addBlackPair(pair);
							} else {
								changed = true;
							}
						}
						if (candidates.size() != refinedCandidates.size()) {
							candidates.removeAll(refinedCandidates);
							MatchWorker.removeUselessMatches(matchedLeaves,
									candidates, y);
							blackPairs.addAll(candidates, y);
						}
					}
				}
			}
			if (matched.size() == right.countNodes())
				break;
			for (Enumeration<SubTreeModel> rightNodes = right
					.postorderEnumeration(); rightNodes.hasMoreElements();) {
				y = rightNodes.nextElement();
				int ii = 0;
				if (y.isLeaf() && !y.isMatched()) {
					ii++;
					List<SubTreeModel> candidates = new ArrayList<SubTreeModel>();
					List<SubTreeModel> refinedCandidates = new ArrayList<SubTreeModel>();
					candidates.addAll(MatchWorker.getMatchedLeftSubTreeModel(
							matchedLeaves, y));
					if (candidates.size() > 1) {
						refinedCandidates = findBestChildrenMatch(candidates,
								y, matched);
					} else {
						continue;
					}
					if (refinedCandidates.size() > 1) {
						for (SubTreeModel x : refinedCandidates) {
							SubTreeModelPair pair = new SubTreeModelPair(x, y);
							if (blackPairs.contains(pair)) {
								continue;
							} else if (!MatchWorker.checkEquivalentIteratively(
									x, y)
									|| !MatchWorker.addCandiMatch(x, y,
											refinedCandidates, matchedLeaves,
											matched)) {
								MatchWorker.removeUselessMatches(matchedLeaves,
										refinedCandidates, y);
								blackPairs.addBlackPair(pair);
							} else {// the first candidate which meets all
									// requirements
								changed = true;
								break;
							}
						}
					}
					if (candidates.size() != refinedCandidates.size()) {
						candidates.removeAll(refinedCandidates);
						MatchWorker.removeUselessMatches(matchedLeaves,
								candidates, y);
						blackPairs.addAll(candidates, y);
					}
				}
			}
		} while (changed);
	}

	public boolean matchNodes(SubTreeModel rightCandidate,
			SubTreeModel leftCandidate,
			Map<SubTreeModel, List<SubTreeModel>> candidateMap,
			List<SubTreeModel> leftCandidates,
			Set<SubTreeModelPair> pmatchedLeaves,
			Set<SubTreeModelPair> pmatched,
			Map<TypeNameTerm, TypeNameTerm> pTMap,
			Map<MethodNameTerm, MethodNameTerm> pMMap,
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> pVMap,
			BlackPairSet blackPairSet) {
		this.left = (SubTreeModel) leftCandidate.getRoot();
		this.right = (SubTreeModel) rightCandidate.getRoot();
		initForMatchNodes(pmatchedLeaves, pmatched, pTMap, pMMap, pVMap,
				blackPairSet);
		if (MatchWorker.addCandiMatch(leftCandidate, rightCandidate,
				leftCandidates, this.matchedLeaves, this.matched)) {
			do {
				matchNodes(left, right);
				blackPairs.addAll(collector.collectSpecificMatching(
						this.matchedLeaves, this.matched, primeTypeMap,
						primeMethodMap, primeVariableMap));
				if (matched.size() == right.countNodes()) {
					break;
				}
			} while (adjust(left, right));
		}
		if (matched.size() != right.countNodes()) {
			rollback();
			return false;
		} else {
			return true;
		}
	}

	private List<SubTreeModel> processParent(
			List<SubTreeModel> refinedCandidates, SubTreeModel y,
			Set<SubTreeModelPair> matched) {
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		SubTreeModel tempParent = null;
		SubTreeModel parent = (SubTreeModel) y.getParent();
		List<SubTreeModel> parentCandidates = new ArrayList<SubTreeModel>();
		for (int i = 0; i < refinedCandidates.size(); i++) {
			tempParent = (SubTreeModel) refinedCandidates.get(i).getParent();
			if (tempParent == null)
				continue;
			if (parentCandidates.contains(tempParent))
				continue;
			parentCandidates.add(tempParent);
		}
		List<SubTreeModel> primeParents = null;
		if (parentCandidates.size() == 1) {
			primeParents = parentCandidates;
		} else if (parent.getParent() == null) {
			primeParents = findPossibleBestForUnParented(parentCandidates,
					parent, matched);
		} else {
			primeParents = findPossibleBestForParented(parentCandidates,
					parent, matched);
		}
		List<SubTreeModel> refinedCandidates2 = new ArrayList<SubTreeModel>();
		for (int i = 0; i < refinedCandidates.size(); i++) {
			tempParent = (SubTreeModel) refinedCandidates.get(i).getParent();
			if (tempParent != null && primeParents.contains(tempParent)) {
				refinedCandidates2.add(refinedCandidates.get(i));
			}
		}
		if (refinedCandidates2.size() == 1) {
			return refinedCandidates2;
		} else {
			result = findRelativeBestMatch(refinedCandidates2, y);
		}
		return result;
	}

	private void rollback() {
		if (!primeTypeMap.equals(oPrimeTypeMap)) {
			primeTypeMap.clear();
			primeTypeMap.putAll(oPrimeTypeMap);
		}
		if (!primeMethodMap.equals(oPrimeMethodMap)) {
			primeMethodMap.clear();
			primeMethodMap.putAll(oPrimeMethodMap);
		}
		if (!primeVariableMap.equals(oPrimeVariableMap)) {
			primeVariableMap.clear();
			primeVariableMap.putAll(oPrimeVariableMap);
		}
		if (!matchedLeaves.equals(oMatchedLeaves)) {
			matchedLeaves.clear();
			matchedLeaves.addAll(oMatchedLeaves);
		}
		if (!blackPairs.equals(oBlackPairs)) {
			blackPairs.clear();
			blackPairs.addAll(oBlackPairs);
		}
		if (!matched.equals(oMatched)) {
			matched.removeAll(oMatched);
			for (SubTreeModelPair pair : matched) {
				pair.getLeft().disableMatched();
				pair.getRight().disableMatched();
			}
			matched.clear();
			matched.addAll(oMatched);
		}
	}
}
