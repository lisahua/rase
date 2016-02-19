package changeassistant.peers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TermsList;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;

public class MatchWorker {

	private static final int UP = 1;
	private static final int LEFT = 2;
	private static final int DIAG = 3;
	public static boolean IsExactInner = false;

	public static List<List<Integer>> EquivalentLists;

	static {
		EquivalentLists = new ArrayList<List<Integer>>();
		EquivalentLists.add(Arrays.asList(ASTNode.DO_STATEMENT,
				ASTNode.ENHANCED_FOR_STATEMENT, ASTNode.FOR_STATEMENT,
				ASTNode.WHILE_STATEMENT));
		EquivalentLists.add(Arrays.asList(ASTNode.BREAK_STATEMENT,
				ASTNode.CONTINUE_STATEMENT, ASTNode.RETURN_STATEMENT));
	}

	public enum Location {
		LEFT, RIGHT
	};

	public static void addBlackPairs(List<SubTreeModel> allCandis,
			List<SubTreeModel> keptCandis, SubTreeModel y,
			BlackPairSet blackPairs) {
		if (allCandis.size() != keptCandis.size()) {
			allCandis.removeAll(keptCandis);
			for (SubTreeModel candi : allCandis) {
				blackPairs.addBlackPair(new SubTreeModelPair(candi, y));
			}
		}
	}

	public static boolean addCandiMatch(SubTreeModel candi, SubTreeModel y,
			List<SubTreeModel> candidates, Set<SubTreeModelPair> matchedLeaves,
			Set<SubTreeModelPair> matched) {
		if (addToMatchedIteratively(candi, y, matchedLeaves, matched)) {
			List<SubTreeModel> candidates2 = new ArrayList<SubTreeModel>(
					candidates);
			candidates2.remove(candi);
			if (y.isLeaf()) {
				MatchWorker.removeUselessMatchLeaves(matchedLeaves,
						candidates2, y);
				MatchWorker.removeUselessMatchLeavesAccordingLeft(
						matchedLeaves, candi, y);
			}
			MatchWorker.removeUselessMatches(matched, candidates2, y);
			MatchWorker.removeUselessMatchesAccordingLeft(matched, candi, y);
			return true;
		} else {// the pair is not valid
			matchedLeaves.remove(new SubTreeModelPair(candi, y));
		}
		return false;
	}

	/**
	 * When adding a matching leaf to a matched set, remove the matching leafs
	 * from the matchLeaves set
	 * 
	 * @param x
	 * @param y
	 * @param matchedLeaves
	 * @param matched
	 * @return
	 */
	private static boolean addToMatchedIteratively(SubTreeModel x,
			SubTreeModel y, Set<SubTreeModelPair> matchedLeaves,
			Set<SubTreeModelPair> matched) {
		boolean success = true;
		SubTreeModel tempX = x;
		SubTreeModel tempY = y;
		SubTreeModelPair pair;
		if (y.isLeaf()) {
			matchedLeaves.remove(new SubTreeModelPair(x, y));
		}
		while (tempX != null && tempY != null) {
			if (tempX.isMatched() && !tempY.isMatched() || !tempX.isMatched()
					&& tempY.isMatched()) {
				success = false;
				break;
			}
			if (tempX.isMatched() && tempY.isMatched()) {
				if (!matched.contains(new SubTreeModelPair(tempX, tempY))) {
					success = false;
					break;
				} else {
					break;
				}
			}
			tempX = (SubTreeModel) tempX.getParent();
			tempY = (SubTreeModel) tempY.getParent();
		}
		if (success) {
			tempX = x;
			tempY = y;
			while (tempX != null && tempY != null && !tempX.isMatched()
					&& !tempY.isMatched()) {
				pair = new SubTreeModelPair(tempX, tempY);
				matched.add(pair);
				pair.getLeft().enableMatched();
				pair.getRight().enableMatched();
				tempX = (SubTreeModel) tempX.getParent();
				tempY = (SubTreeModel) tempY.getParent();
			}
		}
		return success;
	}

	/**
	 * invoke when we need exact match in matchNodes()
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static boolean checkEquivalentIteratively(SubTreeModel x,
			SubTreeModel y) {
		boolean success = true;
		SubTreeModel tempX = x;
		SubTreeModel tempY = y;
		while (tempX != null && tempY != null) {
			if (!MatchWorker.isEquivalentNode(tempX, tempY, IsExactInner)) {
				success = false;
				break;
			}
			tempX = (SubTreeModel) tempX.getParent();
			tempY = (SubTreeModel) tempY.getParent();
		}
		return success;
	}

	private static boolean checkPossibleMatch(List<SubTreeModel> lChildren,
			List<SubTreeModel> rChildren, Set<SubTreeModelPair> matched) {
		boolean isPossible = false;
		System.out.print("");
		Set<SubTreeModel> rightMatches;
		for (SubTreeModel lChild : lChildren) {
			rightMatches = MatchWorker.getMatchedRightSubTreeModel(matched,
					lChild);
			rightMatches.retainAll(rChildren);
			if (!rightMatches.isEmpty()) {// if there is at least match between
											// lChildren and rChildren
				isPossible = true;
				break;
			}
		}
		return isPossible;
	}

	/**
	 * node is supposed to conflict with the pair
	 * 
	 * @param node
	 * @param pair
	 */
	public static void decideMatch(SubTreeModel node, SubTreeModelPair pair,
			Location loc, Set<SubTreeModelPair> matchedSet) {
		Set<SubTreeModelPair> pairs = null;
		switch (loc) {
		case LEFT: {
			pairs = getMatchedSubTreeModelPair2(node, matchedSet);
		}
			break;
		case RIGHT: {
			pairs = getMatchedSubTreeModelPair(node, matchedSet);
		}
			break;
		}
		pairs.remove(pair);
		if (pairs.isEmpty()) {
			node.disableMatched();
		}
		pairs = null;
	}

	public static boolean detectConflict(Term lTerm, Term rTerm,
			Map<Term, Term> knownMap) {
		boolean success = false;
		if (knownMap.containsKey(lTerm)) {
			if (knownMap.get(lTerm).equals(rTerm)) {
				// do nothing
			} else {
				success = true;
			}
		} else if (knownMap.containsValue(rTerm)) {// does not contain lTerm,
													// but contain rTerm
			// conflict is detected here
			success = true;
		}
		return success;
	}

	public static boolean detectConflict(Map<Term, Term> map,
			final Map<Term, Term> knownMap) {
		boolean success = false;
		Term key, value;
		for (Entry<Term, Term> entry : map.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if (detectConflict(key, value, knownMap)) {
				success = true;
				break;
			}
		}
		return success;
	}

	public static List<Integer> findBestParentWithLCS(
			List<SubTreeModel> candidates, SubTreeModel y,
			Set<SubTreeModelPair> matched) {
		if (candidates.size() == 0)
			return Collections.EMPTY_LIST;
		if (candidates.size() == 1) {
			return Arrays.asList(new Integer[] { 0 });
		}
		// System.out.print("");
		double[] scores = new double[candidates.size()];
		SubTreeModel candi, leftParent, rightParent;
		int location;
		List<Integer> mIndexes;
		List<SubTreeModel> leftChildren1, leftChildren2;
		rightParent = (SubTreeModel) y.getParent();
		List<SubTreeModel> rightChildren1 = new ArrayList<SubTreeModel>();
		for (int j = 0; j < y.locationInParent(); j++) {
			rightChildren1.add((SubTreeModel) rightParent.getChildAt(j));
		}

		List<SubTreeModel> rightChildren2 = new ArrayList<SubTreeModel>();
		for (int j = y.locationInParent() + 1; j < rightParent.getChildCount(); j++) {
			rightChildren2.add((SubTreeModel) rightParent.getChildAt(j));
		}

		for (int i = 0; i < candidates.size(); i++) {
			candi = candidates.get(i);
			leftParent = (SubTreeModel) candi.getParent();
			if (leftParent == null) {
				scores[i] = 0;
				continue;
			}
			location = candi.locationInParent();
			leftChildren1 = new ArrayList<SubTreeModel>();
			for (int j = 0; j < location; j++) {
				leftChildren1.add((SubTreeModel) leftParent.getChildAt(j));
			}
			leftChildren2 = new ArrayList<SubTreeModel>();
			for (int j = location + 1; j < leftParent.getChildCount(); j++) {
				leftChildren2.add((SubTreeModel) leftParent.getChildAt(j));
			}
			int count = 0;
			int m = leftChildren1.size();
			int n = rightChildren1.size();
			int[][] c = new int[m + 1][n + 1];
			int[][] b = new int[m + 1][n + 1];
			if (checkPossibleMatch(leftChildren1, rightChildren1, matched) == true) {
				LCSmatrix(c, b, m, n, leftChildren1, rightChildren1, matched);
			}
			count = c[m][n];
			m = leftChildren2.size();
			n = rightChildren2.size();
			c = new int[m + 1][n + 1];
			b = new int[m + 1][n + 1];
			System.out.print("");
			if (checkPossibleMatch(leftChildren2, rightChildren2, matched) == true) {
				LCSmatrix(c, b, m, n, leftChildren2, rightChildren2, matched);
			}
			count += c[m][n];
			count += 1;// the candidate element itself
			scores[i] = count * 1.0 / rightParent.getChildCount();
		}

		mIndexes = MatchWorker.findMaxScoreIndexes(scores);
		return mIndexes;
	}

	public static List<Integer> findMaxScoreIndexes(double[] scores) {
		if (scores.length == 0)
			return Collections.emptyList();
		List<Integer> mIndexes = new ArrayList<Integer>();
		mIndexes.add(0);
		double maxScore = scores[0];
		if (scores.length > 1) {
			for (int i = 1; i < scores.length; i++) {
				if (scores[i] > maxScore) {
					mIndexes.clear();
					mIndexes.add(i);
					maxScore = scores[i];
				} else if (scores[i] == maxScore) {
					mIndexes.add(i);
				}
			}
		}
		if (maxScore - 0 < Math.pow(10, -6))
			return Collections.emptyList();
		return mIndexes;
	}

	public static void getCandidateParents(List<SubTreeModel> candidates,
			SubTreeModel y, List<SubTreeModel> refinedCandidates,
			List<SubTreeModel> parentCandidatesBasedOnChildren,
			Set<SubTreeModelPair> matchedLeaves, Set<SubTreeModelPair> matched,
			boolean isExactInnerMatch) {
		SubTreeModel tempParent;
		SubTreeModel parent = (SubTreeModel) y.getParent();
		if (parent == null)
			return;
		for (int i = 0; i < candidates.size(); i++) {
			tempParent = (SubTreeModel) candidates.get(i).getParent();
			if (tempParent == null)
				continue;
			if (!MatchWorker.isEquivalentNode(tempParent, parent,
					isExactInnerMatch))
				continue;
			parentCandidatesBasedOnChildren.add(tempParent);
			refinedCandidates.add(candidates.get(i));
		}
		List<SubTreeModel> localCandidates = new ArrayList<SubTreeModel>(
				candidates);
		localCandidates.removeAll(refinedCandidates);
		if (!localCandidates.isEmpty()) {
			if (y.isLeaf()) {
				MatchWorker.removeUselessMatchLeaves(matchedLeaves,
						localCandidates, y);
			} else {
				MatchWorker.removeUselessMatches(matched, localCandidates, y);
			}
		}
	}

	public static List<SubTreeModel> getChildrenList(
			Enumeration<SubTreeModel> enu) {
		List<SubTreeModel> list = new ArrayList<SubTreeModel>();
		while (enu.hasMoreElements()) {
			list.add(enu.nextElement());
		}
		return list;
	}

	public static Set<SubTreeModel> getMatchedLeftSubTreeModel(
			Set<SubTreeModelPair> matchedPairs, SubTreeModel right) {
		Set<SubTreeModel> matchedNodes = new HashSet<SubTreeModel>();
		if (right == null) {
			return Collections.emptySet();
		} else {
			for (SubTreeModelPair pair : matchedPairs) {
				if (pair.getRight().equals(right))
					matchedNodes.add(pair.getLeft());
			}
		}
		return matchedNodes;
	}

	public static Set<SubTreeModel> getMatchedRightSubTreeModel(
			Set<SubTreeModelPair> matchedPairs, SubTreeModel left) {
		Set<SubTreeModel> matchedNodes = new HashSet<SubTreeModel>();
		if (left == null) {
			return Collections.emptySet();
		} else {
			for (SubTreeModelPair pair : matchedPairs) {
				if (pair.getLeft().equals(left))
					matchedNodes.add(pair.getRight());
			}
		}
		return matchedNodes;
	}

	public static Set<SubTreeModelPair> getMatchedSubTreeModelPair(
			SubTreeModel right, Set<SubTreeModelPair> matched) {
		Set<SubTreeModelPair> pairs = new HashSet<SubTreeModelPair>();
		if (pairs == null || !right.isMatched()) {
			return Collections.emptySet();
		}
		for (SubTreeModelPair pair : matched) {
			if (pair.getRight().equals(right))
				pairs.add(pair);
		}
		return pairs;
	}

	public static Set<SubTreeModelPair> getMatchedSubTreeModelPair2(
			SubTreeModel left, Set<SubTreeModelPair> matched) {
		Set<SubTreeModelPair> pairs = new HashSet<SubTreeModelPair>();
		if (pairs == null || !left.isMatched()) {
			return Collections.emptySet();
		}
		for (SubTreeModelPair pair : matched) {
			if (pair.getLeft().equals(left))
				pairs.add(pair);
		}
		return pairs;
	}

	public static boolean isConsistentMap(
			SubTreeModelPair pair,
			Map<TypeNameTerm, TypeNameTerm> primeTypeMap,
			Map<MethodNameTerm, MethodNameTerm> primeMethodMap,
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> primeVariableMap) {
		boolean success = false;
		if (pair.doMap()) {
			try {
				if (isValidPair(pair, primeTypeMap, primeMethodMap,
						primeVariableMap)) {
					success = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return success;
	}

	private static boolean isLooseEquivalent(SubTreeModel x, SubTreeModel y) {
		boolean result = false;
		boolean flag1, flag2;
		if (x.getNodeType() == y.getNodeType()) {
			flag1 = isSpecial(x.getStrValue());
			flag2 = isSpecial(y.getStrValue());
			if (flag1 && flag2) {
				if (x.getStrValue().equals(y.getStrValue())) {
					result = true;
				} else {
					// do nothing, since these have different string values
				}
			} else if ((flag1 && !flag2) || (!flag1 && flag2)) {
				// do nothing, since these should not be matched to each other
			} else {// !flag1 && !flag2
				result = true;
			}
		} else if (x.getNodeType() == ASTNode.BLOCK
				&& y.getNodeType() == ASTNode.METHOD_DECLARATION
				|| x.getNodeType() == ASTNode.METHOD_DECLARATION
				&& y.getNodeType() == ASTNode.BLOCK) {
			result = true;
		} else if (x.getStrValue().equals(y.getStrValue())) {
			// of different type but have the same string value--then vs. then
			result = true;
		} else {
			for (List<Integer> eList : EquivalentLists) {
				if (eList.contains(x.getNodeType())
						&& eList.contains(y.getNodeType())) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	private static boolean isEquivalent(SubTreeModel x, SubTreeModel y) {
		if (x.getNodeType() != y.getNodeType())
			return false;
		String xStr = x.getStrValue(), yStr = y.getStrValue();
		try {
			if (!xStr.substring(0, xStr.indexOf(':')).equals(
					yStr.substring(0, yStr.indexOf(':'))))
				return false;
		} catch (Exception e) {
			if (xStr.equals(SubTreeModel.METHOD_DECLARATION)
					&& yStr.equals(SubTreeModel.METHOD_DECLARATION))
				return true;
			if (!xStr.equals(yStr))
				return false;
		}

		if (!TermsList.isEquivalent(x.getAbstractExpressions(),
				y.getAbstractExpressions())) {
			return false;
		}
		return true;
	}

	/**
	 * This map is established according to node type and simple node values
	 * instead of concrete information contained in each node
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static boolean isEquivalentNode(SubTreeModel x, SubTreeModel y,
			boolean isExactInnerMatch) {
		if (y.getEDITset() != null) {
			return isEquivalent(x, y);
		} else if (isExactInnerMatch) {
			return isEquivalent(x, y);
		} else {
			return isLooseEquivalent(x, y);
		}
	}

	public static boolean isSpecial(String strValue) {
		return strValue.equals("then:") || strValue.equals("else:")
				|| strValue.equals("try-body:") || strValue.equals("finally:");
	}

	/**
	 * To judge whether there is already a mapping for the known identifier
	 * which is different from the current pair's counterpart
	 * 
	 * @param pair
	 * @param primeTypeMap
	 * @param primeMethodMap
	 * @param primeVariableMap
	 * @return
	 */
	private static boolean isValidPair(
			SubTreeModelPair pair,
			Map<TypeNameTerm, TypeNameTerm> primeTypeMap,
			Map<MethodNameTerm, MethodNameTerm> primeMethodMap,
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> primeVariableMap) {
		boolean result = true;
		try {
			if (detectConflict(new HashMap<Term, Term>(pair.getTypeMap()),
					new HashMap<Term, Term>(primeTypeMap))
					|| detectConflict(
							new HashMap<Term, Term>(pair.getMethodMap()),
							new HashMap<Term, Term>(primeMethodMap))
					|| detectConflict(
							new HashMap<Term, Term>(pair.getVariableMap()),
							new HashMap<Term, Term>(primeVariableMap))) {
				result = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void LCSmatrix(int[][] c, int[][] b, int m, int n,
			List<SubTreeModel> left, List<SubTreeModel> right,
			Set<SubTreeModelPair> matched) {
		for (int i = 0; i <= m; i++) {
			c[i][0] = 0;
			b[i][0] = 0;
		}
		for (int i = 0; i <= n; i++) {
			c[0][i] = 0;
			b[0][i] = 0;
		}

		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				if (matchContains(left.get(i - 1), right.get(j - 1), matched)) {
					c[i][j] = c[i - 1][j - 1] + 1;
					b[i][j] = DIAG;
				} else if (c[i - 1][j] >= c[i][j - 1]) {
					c[i][j] = c[i - 1][j];
					b[i][j] = UP;
				} else {
					c[i][j] = c[i][j - 1];
					b[i][j] = LEFT;
				}
			}
		}
		// int max = c[0][0];
		// for(int i = 0; i <= m; i++){
		// for(int j = 0; j <= n; j++){
		// if(max <c[i][j]){
		// max = c[i][j];
		// }
		// }
		// }
		// if(max > c[m][n])
		// System.out.print("c[m][n] is not the maximum value");
	}

	public static boolean matchContains(SubTreeModel x, SubTreeModel y,
			Set<SubTreeModelPair> matched) {
		if (matched.contains(new SubTreeModelPair(x, y)))
			return true;
		return false;
	}

	/**
	 * can's parent == parent
	 * 
	 * @param candidates
	 * @param parent
	 * @return
	 */
	public static List<SubTreeModel> refineBasedOnParent(
			List<SubTreeModel> candidates, SubTreeModel parent) {
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		for (int i = 0; i < candidates.size(); i++) {
			if (candidates.get(i).getParent().equals(parent)) {
				result.add(candidates.get(i));
			}
		}
		return result;
	}

	public static List<SubTreeModel> refineWithBestAncestorPath(
			List<SubTreeModel> candidates, SubTreeModel y,
			Set<SubTreeModelPair> matched, boolean isExactInnerMatch) {
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		List<SubTreeModel> refinedCandidates = new ArrayList<SubTreeModel>();
		List<SubTreeModel> candidateParents = new ArrayList<SubTreeModel>();
		List<SubTreeModel> refinedParents;
		SubTreeModel parent, tempParent, temp;
		if (y.getParent() == null) {
			result = new ArrayList<SubTreeModel>(candidates);
		} else {
			parent = (SubTreeModel) y.getParent();
			for (int i = 0; i < candidates.size(); i++) {
				temp = (SubTreeModel) candidates.get(i);
				tempParent = (SubTreeModel) temp.getParent();
				if (tempParent == null) {
					continue;// this candidate cannot be matched to the node
								// since the parents cannot match
				}
				if (MatchWorker.getMatchedSubTreeModelPair(parent, matched)// stop
																			// calling
																			// back
																			// when
																			// the
																			// parent
																			// pair
																			// is
																			// already
																			// contained
						.contains(new SubTreeModelPair(tempParent, parent))) {
					result.add(candidates.get(i));
				} else if (MatchWorker.isEquivalentNode(tempParent, parent,
						isExactInnerMatch)) {
					refinedCandidates.add(candidates.get(i));
					candidateParents.add(tempParent);
				}
			}
			if (!result.isEmpty()) {
				// return the best match found
			} else {
				if (refinedCandidates.size() == 0) {
					// do nothing, because we cannot find any child which
					// ancestors matched
				} else {
					refinedParents = refineWithBestAncestorPath(
							candidateParents, parent, matched,
							isExactInnerMatch);
					if (refinedParents.size() == 0) {
						// do nothing, since ancestors do not match
					} else {
						for (int i = 0; i < refinedCandidates.size(); i++) {
							tempParent = (SubTreeModel) refinedCandidates
									.get(i).getParent();
							if (refinedParents.contains(tempParent)) {
								result.add(refinedCandidates.get(i));
							}
						}
					}
				}
			}
		}
		refinedCandidates = candidateParents = refinedParents = null;
		parent = tempParent = null;
		return result;
	}

	/**
	 * pair is expected to remove
	 * 
	 * @param pair
	 * @param matchedLeaves
	 * @param matched
	 * @return
	 */
	public static boolean removeMatch(SubTreeModelPair pair,
			Set<SubTreeModelPair> matchedLeaves, Set<SubTreeModelPair> matched) {
		boolean success = false;
		Set<SubTreeModelPair> pairs;
		SubTreeModel left = pair.getLeft();
		SubTreeModel right = pair.getRight();
		if (matchedLeaves.contains(pair))
			matchedLeaves.remove(pair);
		if (matched.contains(pair)) {
			matched.remove(pair);
			pairs = getMatchedSubTreeModelPair(right, matched);
			if (pairs.isEmpty()) {
				right.disableMatched();
			}
			pairs = getMatchedSubTreeModelPair(left, matched);
			if (pairs.isEmpty()) {
				left.disableMatched();
			}
		}
		return success;
	}

	/**
	 * The relation between each candidate and y is m-m, instead of 1-m
	 * 
	 * @param matchedSet
	 * @param candidates
	 * @param y
	 * @return
	 */
	public static boolean removeUselessMatchLeaves(
			Set<SubTreeModelPair> matchedLeaves, List<SubTreeModel> candidates,
			SubTreeModel y) {
		boolean success = false;
		SubTreeModelPair pair;
		for (SubTreeModel can : candidates) {
			pair = new SubTreeModelPair(can, y);
			if (matchedLeaves.contains(pair)) {
				matchedLeaves.remove(pair);
				success = true;
			}
		}
		return success;
	}

	public static boolean removeUselessMatches(Set<SubTreeModelPair> matched,
			List<SubTreeModel> candidates, SubTreeModel y) {
		boolean success = false;
		SubTreeModelPair pair;
		boolean isRemoved = false;
		for (SubTreeModel can : candidates) {
			isRemoved = false;
			pair = new SubTreeModelPair(can, y);
			if (matched.contains(pair)) {
				matched.remove(pair);
				success = true;
				isRemoved = true;
			}
			if (isRemoved)
				decideMatch(can, pair, Location.LEFT, matched);
		}
		return success;
	}

	public static boolean removeUselessMatchLeavesAccordingLeft(
			Set<SubTreeModelPair> matchedLeaves, SubTreeModel x, SubTreeModel y) {
		boolean success = false;
		SubTreeModelPair pair;
		Set<SubTreeModel> ySet;
		ySet = getMatchedRightSubTreeModel(matchedLeaves, x);
		ySet.remove(y);

		for (SubTreeModel tempY : ySet) {
			pair = new SubTreeModelPair(x, tempY);
			if (matchedLeaves.contains(pair)) {
				matchedLeaves.remove(pair);
				success = true;
			}
		}
		return success;
	}

	public static boolean removeUselessMatchesAccordingLeft(
			Set<SubTreeModelPair> matchedSet, SubTreeModel x, SubTreeModel y) {
		boolean success = false;
		boolean isRemoved = false;
		SubTreeModelPair pair;
		Set<SubTreeModel> ySet;
		ySet = getMatchedRightSubTreeModel(matchedSet, x);
		ySet.remove(y);

		for (SubTreeModel tempY : ySet) {
			isRemoved = false;
			pair = new SubTreeModelPair(x, tempY);
			if (matchedSet.contains(pair)) {
				matchedSet.remove(pair);
				success = true;
				isRemoved = true;
			}
			if (isRemoved)
				decideMatch(tempY, pair, Location.RIGHT, matchedSet);
		}
		return success;
	}

	public static List<SubTreeModel> searchEquivalent(SubTreeModel y,
			Set<SubTreeModelPair> matched, boolean isExactMatch) {
		List<SubTreeModel> candidates = new ArrayList<SubTreeModel>();
		SubTreeModel tempY = y, tempX = null, x;
		Set<SubTreeModelPair> pairs;
		// search until we find matching parents
		while (tempY != null && !tempY.isMatched()) {
			tempY = (SubTreeModel) tempY.getParent();
		}
		if (tempY != null) {
			pairs = MatchWorker.getMatchedSubTreeModelPair(tempY, matched);
			if (pairs.size() == 1) {
				tempX = pairs.iterator().next().getLeft();
				for (Enumeration<SubTreeModel> leftNodes = tempX
						.postorderEnumeration(); leftNodes.hasMoreElements();) {
					x = leftNodes.nextElement();
					if (!x.isMatched()
							&& MatchWorker.isEquivalentNode(x, y, isExactMatch)) {
						candidates.add(x);
					}
				}
			} else if (pairs.size() == 0) {
				// do nothing
			} else {
				System.out.println("Observe");
			}
		}
		return candidates;
	}

	public static List<SubTreeModel> searchEquivalentWithoutParent(
			SubTreeModel y, SubTreeModel left, boolean isExactMatch) {
		List<SubTreeModel> candidates = new ArrayList<SubTreeModel>();
		Enumeration<SubTreeModel> enumeration = left.breadthFirstEnumeration();
		SubTreeModel x;
		while (enumeration.hasMoreElements()) {
			x = enumeration.nextElement();
			if (!x.isMatched()
					&& MatchWorker.isEquivalentNode(x, y, isExactMatch)) {
				candidates.add(x);
			}
		}
		return candidates;
	}
}
