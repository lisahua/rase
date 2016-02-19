package changeassistant.peers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;

public class MatchCollector {

	private Map<Term, Set<Term>> conflictMap;
	private Map<Term, Set<Term>> leftToRightConflictMap;

	public Map<TypeNameTerm, TypeNameTerm> primeTypeMap;
	public Map<MethodNameTerm, MethodNameTerm> primeMethodMap;
	public Map<VariableTypeBindingTerm, VariableTypeBindingTerm> primeVariableMap;

	private Set<SubTreeModelPair> localMatched;

	public MatchCollector() {
		localMatched = new HashSet<SubTreeModelPair>();
		primeTypeMap = new HashMap<TypeNameTerm, TypeNameTerm>();
		primeMethodMap = new HashMap<MethodNameTerm, MethodNameTerm>();
		primeVariableMap = new HashMap<VariableTypeBindingTerm, VariableTypeBindingTerm>();
	}

	public boolean appendMapInfo(SubTreeModelPair pair) {
		// left-right: *-1
		boolean success = true;
		Map<TypeNameTerm, TypeNameTerm> tmpTMap = new HashMap<TypeNameTerm, TypeNameTerm>();
		Map<MethodNameTerm, MethodNameTerm> tmpMMap = new HashMap<MethodNameTerm, MethodNameTerm>();
		Map<VariableTypeBindingTerm, VariableTypeBindingTerm> tmpVMap = new HashMap<VariableTypeBindingTerm, VariableTypeBindingTerm>();
		TypeNameTerm tKey, tValue;
		MethodNameTerm mKey, mValue;
		VariableTypeBindingTerm vKey, vValue;
		Set<Term> termSet;

		leftToRightConflictMap = new HashMap<Term, Set<Term>>();
		for (Entry<TypeNameTerm, TypeNameTerm> entry : pair.getTypeMap()
				.entrySet()) {
			tKey = entry.getKey();
			tValue = entry.getValue();
			if (primeTypeMap.containsKey(tKey)) {
				if (primeTypeMap.get(tKey).equals(tValue)) {
					// do nothing, since the mapping is consistent
				} else {
					success = false;
					if (leftToRightConflictMap.containsKey(tKey)) {
						leftToRightConflictMap.get(tKey).add(tValue);
						leftToRightConflictMap.get(tKey).add(
								primeTypeMap.get(tKey));
					} else {
						termSet = new HashSet<Term>();
						termSet.add(tValue);
						termSet.add(primeTypeMap.get(tKey));
						leftToRightConflictMap.put(tKey, termSet);
					}
				}
			} else {
				if (!tmpTMap.containsKey(tKey)) {
					tmpTMap.put(tKey, tValue);
				}
			}
		}

		for (Entry<MethodNameTerm, MethodNameTerm> entry : pair.getMethodMap()
				.entrySet()) {
			mKey = entry.getKey();
			mValue = entry.getValue();
			if (primeMethodMap.containsKey(mKey)) {
				if (primeMethodMap.get(mKey).equals(mValue)) {
					// do nothing
				} else {
					success = false;
					if (leftToRightConflictMap.containsKey(mKey)) {
						leftToRightConflictMap.get(mKey).add(mValue);
						leftToRightConflictMap.get(mKey).add(
								primeMethodMap.get(mKey));
					} else {
						termSet = new HashSet<Term>();
						termSet.add(mValue);
						termSet.add(primeMethodMap.get(mKey));
						leftToRightConflictMap.put(mKey, termSet);
					}
				}
			} else {
				if (!tmpMMap.containsKey(mKey)) {
					tmpMMap.put(mKey, mValue);
				}
			}
		}

		for (Entry<VariableTypeBindingTerm, VariableTypeBindingTerm> entry : pair
				.getVariableMap().entrySet()) {
			vKey = entry.getKey();
			vValue = entry.getValue();
			if (primeVariableMap.containsKey(vKey)) {
				if (primeVariableMap.get(vKey).equals(vValue)) {
					// do nothing
				} else {
					success = false;
					if (leftToRightConflictMap.containsKey(vKey)) {
						leftToRightConflictMap.get(vKey).add(vValue);
						leftToRightConflictMap.get(vKey).add(
								primeVariableMap.get(vKey));
					} else {
						termSet = new HashSet<Term>();
						termSet.add(vValue);
						termSet.add(primeVariableMap.get(vKey));
						leftToRightConflictMap.put(vKey, termSet);
					}
				}
			} else {
				if (!tmpVMap.containsKey(vKey)) {
					tmpVMap.put(vKey, vValue);
				}
			}
		}
		if (success) {
			primeTypeMap.putAll(tmpTMap);
			primeMethodMap.putAll(tmpMMap);
			primeVariableMap.putAll(tmpVMap);
		} else {
			// leftToRightConflictMap = null;
		}
		return success;
	}

	/**
	 * Two targets: 1. collect specific mapping information, 2. remove conflict
	 * when it is detected
	 */
	public Set<SubTreeModelPair> collectSpecificMatching(
			Set<SubTreeModelPair> matchedLeaves, Set<SubTreeModelPair> matched,
			Map<TypeNameTerm, TypeNameTerm> pTMap,
			Map<MethodNameTerm, MethodNameTerm> pMMap,
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> pVMap) {
		conflictMap = new HashMap<Term, Set<Term>>();
		primeTypeMap = pTMap;
		primeMethodMap = pMMap;
		primeVariableMap = pVMap;
		Term cKey;
		Set<Term> cValue;
		Set<SubTreeModelPair> removedPairs = new HashSet<SubTreeModelPair>();
		Set<SubTreeModelPair> pairsToRemove = new HashSet<SubTreeModelPair>();
		for (SubTreeModelPair pair : matched) {
			if (localMatched.contains(pair))
				continue;
			// System.out.print("");
			if (pair.doMap()) {
				if (appendMapInfo(pair)) {
					// do nothing
				} else {
					for (Entry<Term, Set<Term>> entry : leftToRightConflictMap
							.entrySet()) {
						cKey = entry.getKey();
						cValue = entry.getValue();
						if (conflictMap.containsKey(cKey)) {
							conflictMap.get(cKey).addAll(cValue);
						} else {
							conflictMap.put(cKey, cValue);
						}
					}
				}
			} else {
				pairsToRemove.add(pair);
			}
		}
		for (SubTreeModelPair pair : pairsToRemove) {
			MatchWorker.removeMatch(pair, matchedLeaves, matched);
		}

		localMatched = new HashSet<SubTreeModelPair>(matched);
		// System.out.print("");
		if (!conflictMap.isEmpty()) {
			removedPairs = removeConflict();
			if (!removedPairs.isEmpty()) {
				for (SubTreeModelPair pair : removedPairs) {
					MatchWorker.removeMatch(pair, matchedLeaves, matched);
				}
				removedPairs.addAll(collectSpecificMatching(matchedLeaves,
						matched, this.primeTypeMap, this.primeMethodMap,
						this.primeVariableMap));
			}
		}
		if (!pairsToRemove.isEmpty()) {
			removedPairs.addAll(pairsToRemove);
		}
		return removedPairs;
	}

	private Set<SubTreeModelPair> removeConflict() {
		Set<SubTreeModelPair> redundantPairs = new HashSet<SubTreeModelPair>();
		Set<Term> keys = conflictMap.keySet();
		List<Term> values;
		Term value, tempValue;
		for (Term key : keys) {
			value = null;
			values = new ArrayList<Term>(conflictMap.get(key));
			double[] supporters = new double[values.size()];
			try {
				for (SubTreeModelPair pair : localMatched) {
					if (pair.containsKey(key)) {
						tempValue = pair.get(key);
						if (tempValue != null
								&& values.indexOf(tempValue) != -1) {
							supporters[values.indexOf(tempValue)]++;
						} else {
							System.out.println("The value is not found!");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<Integer> mIndexes = MatchWorker
					.findMaxScoreIndexes(supporters);
			if (mIndexes.size() == 1) {
				value = values.get(mIndexes.get(0));
			} else {
				List<Term> refinedValues = new ArrayList<Term>(mIndexes.size());
				for (int i = 0; i < mIndexes.size(); i++) {
					refinedValues.add(values.get(mIndexes.get(i)));
				}
				value = null;
				// remove conflict according to string literals
				for (int i = 0; i < refinedValues.size(); i++) {
					if (refinedValues.get(i).getName().equals(key.getName())) {
						value = refinedValues.get(i);
					}
				}
			}
			if (value != null) {
				values.remove(value);
				// all the other conflicting pairs are kicked out
				redundantPairs.addAll(kickOutConflict(key, values));
			}
		}
		return redundantPairs;
	}

	private Set<SubTreeModelPair> kickOutConflict(Term key, List<Term> values) {
		Set<SubTreeModelPair> redundantPairs = new HashSet<SubTreeModelPair>();
		Set<SubTreeModelPair> tempLocalMatched = new HashSet<SubTreeModelPair>(
				localMatched);
		Term tempValue;
		for (SubTreeModelPair pair : localMatched) {
			tempValue = pair.get(key);
			if (tempValue != null && values.contains(tempValue)) {
				redundantPairs.add(pair);
				tempLocalMatched.remove(pair);
			}
		}
		localMatched = tempLocalMatched;
		switch (key.getTermType()) {
		case TypeNameTerm: {
			if (values.contains(primeTypeMap.get(key))) {
				primeTypeMap.remove(key);
			}
		}
			break;
		case MethodNameTerm: {
			if (values.contains(primeMethodMap.get(key))) {
				primeMethodMap.remove(key);
			}
		}
			break;
		case VariableTypeBindingTerm: {
			if (values.contains(primeVariableMap.get(key))) {
				primeVariableMap.remove(key);
			}
		}
			break;
		}
		return redundantPairs;
	}
}
