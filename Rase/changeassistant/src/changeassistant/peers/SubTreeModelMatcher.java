package changeassistant.peers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import changeassistant.change.group.model.MatchingInfo;
import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;

public class SubTreeModelMatcher {

	private Set<SubTreeModelPair> matched;
	private Set<SubTreeModelPair> matchedLeaves;

	private Map<TypeNameTerm, TypeNameTerm> primeTypeMap;
	private Map<MethodNameTerm, MethodNameTerm> primeMethodMap;
	private Map<VariableTypeBindingTerm, VariableTypeBindingTerm> primeVariableMap;

	private BlackPairSet blackPairs;

	public static Stack<MatchSnapshot> snapshots = new Stack<MatchSnapshot>();

	// private Set<SubTreeModelPair> matchedParentAndChildrenPairs;

	private MatchHelper1 mHelper1;
	private MatchHelper2 mHelper2;
	private MatchHelper3 mHelper3;
	private MatchHelper4 mHelper4;
	private MatchCollector collector;

	public void clear() {
		mHelper1 = null;
		mHelper2 = null;
		mHelper3 = null;
		mHelper4 = null;
		matched = null;
		matchedLeaves = null;
		primeTypeMap = null;
		primeMethodMap = null;
		primeVariableMap = null;
		blackPairs = null;
	}

	public void initialize() {
		collector = new MatchCollector();
		mHelper1 = new MatchHelper1();
		mHelper2 = new MatchHelper2();
		mHelper3 = new MatchHelper3();
		mHelper4 = new MatchHelper4();
		matched = new HashSet<SubTreeModelPair>();
		primeTypeMap = new HashMap<TypeNameTerm, TypeNameTerm>();
		primeMethodMap = new HashMap<MethodNameTerm, MethodNameTerm>();
		primeVariableMap = new HashMap<VariableTypeBindingTerm, VariableTypeBindingTerm>();
		blackPairs = new BlackPairSet();
	}

	public int collectMatchingInfo(SubTreeModel left, SubTreeModel right) {
		Queue<SubTreeModelPair> queue = new LinkedList<SubTreeModelPair>();
		SubTreeModelPair pair;
		Set<SubTreeModelPair> pairs = new HashSet<SubTreeModelPair>();
		pairs = MatchWorker.getMatchedSubTreeModelPair(right, matched);
		// System.out.print("");
		if (pairs.size() == 0) {// fail to match
			// System.out.println("Wrong: The right root is not matched!");
		} else if (pairs.size() == 1) {
			pair = pairs.iterator().next();
			queue.add(pair);
			while (!queue.isEmpty()) {
				pair = queue.remove();
			}
		} else {
			// more process is needed
			System.out.println("Warning: more process is needed");
		}
		return pairs.size();
	}

	/**
	 * 
	 * @param left
	 *            = candidate
	 * @param right
	 *            = subTree
	 * @return
	 */
	public MatchingInfo match(SubTreeModel left, SubTreeModel right) {

		// System.out.print("");
		MatchingInfo info = null;
		int nodeCount = right.countNodes();

		// match leaves as many as possible
		matchedLeaves = mHelper1.matchLeavesAndEditedInners(left, right);

		if (matchedLeaves == null) {
			return null;// the edited node cannot find correspondence
		}
		if (matchedLeaves.size() != 0)
			// match leaves with consideration of ancestors
			mHelper2.matchLeavesAndInners(left, right, matchedLeaves, matched,
					false);

		Map<SubTreeModel, List<SubTreeModel>> candidateMap = null;
		if (matchedLeaves.size() != 0) {
			// match nodes according to LCS without introducing conflict
			candidateMap = mHelper3.matchNodes(right, matchedLeaves, matched,
					primeTypeMap, primeMethodMap, primeVariableMap, blackPairs,
					false);
		} else if (matched.size() == nodeCount) {
			blackPairs.addAll(collector.collectSpecificMatching(matchedLeaves,
					matched, primeTypeMap, primeMethodMap, primeVariableMap));
		}

		if (matched.size() < nodeCount) {
			if (candidateMap == null || candidateMap.isEmpty()) {
				mHelper4.initForAdjust(matchedLeaves, matched, primeTypeMap,
						primeMethodMap, primeVariableMap, blackPairs);
				while (mHelper4.adjust(left, right)) {
					mHelper4.matchNodes(left, right);
					if (matched.size() == nodeCount)
						break;
				}
			} else {// we can try different possibilities here
				do {
					Entry<SubTreeModel, List<SubTreeModel>> candiEntry = candidateMap
							.entrySet().iterator().next();
					SubTreeModel y = candiEntry.getKey();
					List<SubTreeModel> candidates = candiEntry.getValue();
					boolean isMatched = false;
					for (SubTreeModel x : candidates) {
						if (mHelper4.matchNodes(y, x, candidateMap,
								new ArrayList<SubTreeModel>(candidates),
								matchedLeaves, matched, primeTypeMap,
								primeMethodMap, primeVariableMap, blackPairs)) {
							isMatched = true;
							matchedLeaves = mHelper4.matchedLeaves;
							matched = mHelper4.matched;
							primeTypeMap = mHelper4.primeTypeMap;
							primeMethodMap = mHelper4.primeMethodMap;
							primeVariableMap = mHelper4.primeVariableMap;
							blackPairs = mHelper4.blackPairs;
							break;
						}
					}
					if (isMatched) {
						break;
					} else {
						candidateMap.remove(y);
					}
				} while (!candidateMap.isEmpty());
			}
		}
		if (matched.size() == nodeCount) {// all matched
			info = new MatchingInfo(right, left, matched, primeTypeMap,
					primeMethodMap, primeVariableMap);
			return info;
		} else {
			return null;
		}
	}

	// private boolean containConflictingMatches(){
	// boolean flag = false;
	// Set<SubTreeModel> leftNodes = new HashSet<SubTreeModel>();
	// Set<SubTreeModel> rightNodes = new HashSet<SubTreeModel>();
	// for(SubTreeModelPair pair : matched){
	// if(!leftNodes.add(pair.getLeft()) ||
	// !rightNodes.add(pair.getRight())){
	// flag = true;
	// break;
	// }
	// }
	// return flag;
	// }
}
