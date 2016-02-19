package changeassistant.multipleexample.match;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.datastructure.Pair;
import changeassistant.multipleexample.partition.SimpleASTNodeConverter;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.LCSSequence;

public class CodePatternMatcher extends LCSSequence {

	private static SimpleTreeNode methodTree;
	private static List<SimpleTreeNode> pSNodes;

	public static int computeLCSSequence(Sequence s1, Sequence s2,
			Sequence part1, Sequence part2, List<Integer> nodeIndexes1,
			List<Integer> nodeIndexes2) {
		int max = 0;
		String digest = s1.toString() + "--" + s2.toString();
		Pair<Sequence> pair = null;

		if (dictionary.containsKey(digest)) {
			pair = dictionary.get(digest);
			part1.add(pair.getLeft());
			part2.add(pair.getRight());
			max = pair.getLeft().size();
		} else if (s1.isEmpty() || s2.isEmpty()) {
			// do nothing
		} else {
			if (s1.size() == 1
					&& s2.size() == 1
					&& matchMatrix[nodeIndexes1.indexOf(s1.get(0))][nodeIndexes2
							.indexOf(s2.get(0))] == 1) {
				max = 1;
				part1.add(s1);
				part2.add(s2);
				pair = new Pair<Sequence>(s1, s2);
			} else {
				List<Pair<Sequence>> pairResults = new ArrayList<Pair<Sequence>>();
				Sequence result1, result2;
				for (int i = 0; i < 3; i++) {
					result1 = new Sequence(new ArrayList<Integer>());
					result2 = new Sequence(new ArrayList<Integer>());
					pairResults.add(new Pair<Sequence>(result1, result2));
				}
				int[] lcs = new int[3];
				for (int i = 0; i < 3; i++)
					lcs[i] = 0;
				if (matchMatrix[nodeIndexes1.indexOf(s1.get(0))][nodeIndexes2
						.indexOf(s2.get(0))] == 1) {

					result1 = pairResults.get(0).getLeft();
					result2 = pairResults.get(0).getRight();
					lcs[0] = computeLCSSequence(s1.head(), s2.head(), result1,
							result2, nodeIndexes1, nodeIndexes2);
					result1.append(s1.get(0));
					result2.append(s2.get(0));
					lcs[0]++;
					lcs[0] += computeLCSSequence(s1.tail(), s2.tail(), result1,
							result2, nodeIndexes1, nodeIndexes2);
				}
				lcs[1] = computeLCSSequence(s1.head().concate(s1.tail()), s2,
						pairResults.get(1).getLeft(), pairResults.get(1)
								.getRight(), nodeIndexes1, nodeIndexes2);
				lcs[2] = computeLCSSequence(s1, s2.head().concate(s2.tail()),
						pairResults.get(2).getLeft(), pairResults.get(2)
								.getRight(), nodeIndexes1, nodeIndexes2);
				max = lcs[0];
				int maxIndex = 0;
				for (int i = 1; i < 3; i++) {
					if (lcs[i] > max) {
						max = lcs[i];
						maxIndex = i;
					}
				}
				pair = pairResults.get(maxIndex);
				part1.add(pair.getLeft());
				part2.add(pair.getRight());
			}
			dictionary.put(digest, pair);
		}
		return max;
	}

	// protected static boolean checkMatched(Map<Integer, Integer> matchSet,
	// Sequence s1, Sequence s2) {
	// return matchMatrix[s1.get(0) - 1][s2.get(0) - 1] == 1;
	// }

	/**
	 * ASSUMPTION: no left string can be empty since this is generated based on
	 * sequence. Sequence match requires that the matched node must have
	 * content.
	 * 
	 * @param simpleASTNodesListLeft
	 * @param simpleASTNodesListRight
	 * @param leftStrings
	 * @param rightStrings
	 */
	private static void initMatrix(
			List<List<SimpleASTNode>> simpleASTNodesListLeft,
			List<List<SimpleASTNode>> simpleASTNodesListRight,
			List<String> leftStrings, List<String> rightStrings,
			List<Integer> nodeTypesLeft, List<Integer> nodeTypesRight) {
		System.out.print("");
		matchMatrix = new double[leftStrings.size()][rightStrings.size()];
		List<SimpleASTNode> nodesLeft = null;
		List<SimpleASTNode> nodesRight = null;
		SimpleASTNode nodeLeft = null;
		SimpleASTNode nodeRight = null;
		int nodeTypeLeft = 0;
		int nodeTypeRight = 0;
		String left = null;
		Matcher matcher = null;
		boolean matched = true;

		for (int i = 0; i < leftStrings.size(); i++) {
			left = leftStrings.get(i);
			System.out.print("");
			matcher = Term.Abs_And_Exact_Pattern.matcher(left);

			if (matcher.find()) {
				nodesLeft = simpleASTNodesListLeft.get(i);
				nodeTypeLeft = nodeTypesLeft.get(i);
				for (int j = 0; j < rightStrings.size(); j++) {
					nodesRight = simpleASTNodesListRight.get(j);
					nodeTypeRight = nodeTypesRight.get(j);
					if (nodesLeft.size() != nodesRight.size()) {
						matchMatrix[i][j] = 0;
					} else {
						matched = true;
						for (int k = 0; k < nodesLeft.size(); k++) {
							nodeLeft = nodesLeft.get(k);
							nodeRight = nodesRight.get(k);
							if (!matchPattern(nodeLeft, nodeRight)) {
								matchMatrix[i][j] = 0;
								matched = false;
								break;
							}
						}
						if (matched) {
							if (nodeTypeLeft == nodeTypeRight)
								matchMatrix[i][j] = 1;
						}
					}
				}
			} else {// this is an exact match for the contents
				for (int j = 0; j < rightStrings.size(); j++) {
					// if the two strings are equal, the cell is set to 1
					if (left.equals(rightStrings.get(j))) {
						matchMatrix[i][j] = 1;
					} else {
						// if the two strings are not equal, the cell is set to
						// 0
						matchMatrix[i][j] = 0;
					}
				}
			}
		}

	}

	private static boolean compareTwoLeaves(SimpleASTNode pNode,
			SimpleASTNode rNode) {
		List<Integer> nodeTypes = null;
		Term tmpTerm = null;
		String pString = pNode.getStrValue();
		String rString = rNode.getStrValue();
		if (Term.ExactAbsPattern.matcher(pString).matches()) {
			if (Term.U_Pattern.matcher(pString).matches()) {
				System.out.print("");
				nodeTypes = Term.parseTypes(pString);
				if (!nodeTypes.isEmpty()
						&& !nodeTypes.contains(rNode.getNodeType())) {
					if (nodeTypes.size() == 1
							&& nodeTypes.get(0).equals(
									ASTExpressionTransformer.LIST_LITERAL)
							&& (((SimpleASTNode) rNode.getParent())
									.getStrValue().equals(
											SimpleASTNode.LIST_LITERAL) || rNode
									.getNodeType() == ASTExpressionTransformer.LIST)) {
						// do nothing
					} else
						return false;
				} else {
					// addToMap(idMap, pString, rString);
				}
			} else {// pString is a v$/m$/t$
				if (rNode.getTerm() != null) {
					if (!matchType(pString, rNode.getTerm()))
						return false;
					// else
					// addToMap(idMap, pString, rString);
				} else if (rNode.getChildCount() == 1) {
					tmpTerm = ((SimpleASTNode) rNode.getChildAt(0)).getTerm();
					if (tmpTerm != null && !matchType(pString, tmpTerm)
							|| tmpTerm == null) {
						return false;
					}
				} else {
					return false;
				}
			}
		} else {
			if (!pString.equals(rString)) {
				// do not meet the requirement
				if (rString.equals(SimpleASTNode.LIST_LITERAL)) {
					if (rNode.getChildCount() == 1
							&& rNode.getChildAt(0).toString().equals(pString)) {
						// do nothing
					} else if (rNode.getChildCount() == 0 && pString.isEmpty()) {
						// do nothing
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				// if (pNode.getTerm() != null
				// && !pNode.getTerm().getTermType()
				// .equals(TermType.Term))
				// addToMap(idMap, pString, rString);
			}
		}
		return true;
	}

	private static boolean matchPattern(SimpleASTNode pat, SimpleASTNode right) {
		SimpleASTNode pNode = null;
		SimpleASTNode rNode = null;
		Enumeration<SimpleASTNode> pChildEnum = null;
		Enumeration<SimpleASTNode> rChildEnum = null;
		SimpleASTNode pChild = null, rChild = null;
		String pString = null;
		String rString = null;
		List<Integer> nodeTypes = null;
		Queue<SimpleASTNode> pQueue = new LinkedList<SimpleASTNode>();
		Queue<SimpleASTNode> rQueue = new LinkedList<SimpleASTNode>();
		Term tmpTerm = null;
		int pChildCount = 0, rChildCount = 0;
		System.out.print("");
		boolean matched = false;
		try {
			pQueue.add(pat);
			rQueue.add(right);

			while (!pQueue.isEmpty()) {
				pNode = pQueue.remove();
				rNode = rQueue.remove();
				pChildCount = pNode.getChildCount();
				rChildCount = rNode.getChildCount();
				if (pChildCount == rChildCount) {
					if (pChildCount != 0) {
						pChildEnum = pNode.children();
						rChildEnum = rNode.children();
						while (pChildEnum.hasMoreElements()) {
							pChild = pChildEnum.nextElement();
							rChild = rChildEnum.nextElement();
							if (pChild.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION
									&& rChild.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION) {
								continue;
							}
							pQueue.add(pChild);
							rQueue.add(rChild);
						}
					} else {// comparing two leaves
						matched = compareTwoLeaves(pNode, rNode);
						if (!matched) {
							return false;
						}
					}
				} else {// pChildCount != rChildCount
					pString = pNode.getStrValue();
					rString = rNode.getStrValue();
					if (pString.equals(SimpleASTNode.LIST_LITERAL)
							&& rString.equals(SimpleASTNode.LIST_LITERAL)) {
						if (pNode.getChildCount() == 1
								&& Term.U_Pattern.matcher(
										((SimpleASTNode) pNode.getChildAt(0))
												.getStrValue()).matches()) {
							// do nothing, since this argument list can
							// be matched
						} else {
							return false;
						}
					} else if (Term.U_Pattern.matcher(pString).matches()) {
						Set<String> suffixes = Term.getSuffixes(pString);
						String typeExpr = Term.IndexToExpr.get(rNode
								.getNodeType());
						if (!suffixes.contains(typeExpr + "_")) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static void prepareForMatches(Enumeration<SimpleTreeNode> sEnum,
			List<List<SimpleASTNode>> simpleASTNodesList,
			List<List<SimpleASTNode>> knownASTNodesList, List<String> strings,
			List<Integer> nodeIndexes, List<Integer> nodeTypes) {
		SimpleTreeNode sTmp = null;
		List<SimpleASTNode> tmpNodes = null;
		while (sEnum.hasMoreElements()) {
			sTmp = sEnum.nextElement();
			if (sTmp.getNodeIndex() < 0)
				continue;
			tmpNodes = knownASTNodesList.get(sTmp.getNodeIndex() - 1);
			simpleASTNodesList.add(tmpNodes);
			// termsListListLeft.add(SimpleASTNodeConverter
			// .convertToTermsList(tmpNodes));
			strings.add(SimpleASTNodeConverter.convertToString(tmpNodes,
					sTmp.getStrValue()));
			nodeIndexes.add(sTmp.getNodeIndex());
			nodeTypes.add(sTmp.getNodeType());
		}
	}

	private static MatchResult doMatch(
			List<List<SimpleASTNode>> simpleASTNodesListLeft,
			List<List<SimpleASTNode>> simpleASTNodesListRight,
			List<String> left, List<String> right,
			List<Integer> nodeIndexesLeft, List<Integer> nodeIndexesRight,
			List<List<SimpleASTNode>> patSimpleASTNodesList,
			List<List<SimpleASTNode>> simpleASTNodesList, Sequence patSequence,
			Sequence sequence, List<Integer> nodeTypesLeft,
			List<Integer> nodeTypesRight) {
		// System.out.print("");
		initMatrix(simpleASTNodesListLeft, simpleASTNodesListRight, left,
				right, nodeTypesLeft, nodeTypesRight);

		Sequence result1 = new Sequence(new ArrayList<Integer>());
		Sequence result2 = new Sequence(new ArrayList<Integer>());
		dictionary = new HashMap<String, Pair<Sequence>>();
		computeLCSSequence(patSequence, sequence, result1, result2,
				nodeIndexesLeft, nodeIndexesRight);
		if (result1.size() == patSequence.size()) {
			return new MatchResult(result1, result2, patSimpleASTNodesList,
					simpleASTNodesList);
		}
		return null;
	}

	/**
	 * left---pattern right---content to match
	 * 
	 * @param simpleASTNodesList
	 * @param sequence
	 * @param patSimpleASTNodesList
	 * @param patSequence
	 * @return
	 */
	public static MatchResult matches(SimpleTreeNode methodTree,
			List<List<SimpleASTNode>> simpleASTNodesList, Sequence sequence,
			SimpleTreeNode patTree,
			List<List<SimpleASTNode>> patSimpleASTNodesList,
			Sequence patSequence) {
		CodePatternMatcher.methodTree = methodTree;
		CodePatternMatcher.pSNodes = new ArrayList<SimpleTreeNode>();
		pSNodes.add(patTree);
		Enumeration<SimpleTreeNode> sEnum = null;
		sEnum = patTree.preorderEnumeration();
		List<List<SimpleASTNode>> simpleASTNodesListLeft = new ArrayList<List<SimpleASTNode>>();
		List<String> left = new ArrayList<String>();
		List<Integer> nodeIndexesLeft = new ArrayList<Integer>();
		List<Integer> nodeTypesLeft = new ArrayList<Integer>();
		prepareForMatches(sEnum, simpleASTNodesListLeft, patSimpleASTNodesList,
				left, nodeIndexesLeft, nodeTypesLeft);

		sEnum = methodTree.preorderEnumeration();
		List<List<SimpleASTNode>> simpleASTNodesListRight = new ArrayList<List<SimpleASTNode>>();
		List<String> right = new ArrayList<String>();
		List<Integer> nodeIndexesRight = new ArrayList<Integer>();
		List<Integer> nodeTypesRight = new ArrayList<Integer>();
		prepareForMatches(sEnum, simpleASTNodesListRight, simpleASTNodesList,
				right, nodeIndexesRight, nodeTypesRight);

		return doMatch(simpleASTNodesListLeft, simpleASTNodesListRight, left,
				right, nodeIndexesLeft, nodeIndexesRight,
				patSimpleASTNodesList, simpleASTNodesList, patSequence,
				sequence, nodeTypesLeft, nodeTypesRight);
	}

	public static MatchResult matches(SimpleTreeNode methodTree,
			List<List<SimpleASTNode>> simpleASTNodesList, Sequence sequence,
			List<SimpleTreeNode> pSNodes,
			List<List<SimpleASTNode>> patSimpleASTNodesList,
			Sequence patSequence) {
		CodePatternMatcher.methodTree = methodTree;
		CodePatternMatcher.pSNodes = pSNodes;
		Enumeration<SimpleTreeNode> sEnum = null;
		List<List<SimpleASTNode>> simpleASTNodesListLeft = new ArrayList<List<SimpleASTNode>>();
		List<String> left = new ArrayList<String>();
		List<Integer> nodeIndexesLeft = new ArrayList<Integer>();
		List<Integer> nodeTypesLeft = new ArrayList<Integer>();
		for (SimpleTreeNode patTree : pSNodes) {
			sEnum = patTree.preorderEnumeration();
			while (sEnum.hasMoreElements()) {
				prepareForMatches(sEnum, simpleASTNodesListLeft,
						patSimpleASTNodesList, left, nodeIndexesLeft,
						nodeTypesLeft);
			}
		}
		sEnum = methodTree.preorderEnumeration();
		List<List<SimpleASTNode>> simpleASTNodesListRight = new ArrayList<List<SimpleASTNode>>();
		List<String> right = new ArrayList<String>();
		List<Integer> nodeIndexesRight = new ArrayList<Integer>();
		List<Integer> nodeTypesRight = new ArrayList<Integer>();
		prepareForMatches(sEnum, simpleASTNodesListRight, simpleASTNodesList,
				right, nodeIndexesRight, nodeTypesRight);
		return doMatch(simpleASTNodesListLeft, simpleASTNodesListRight, left,
				right, nodeIndexesLeft, nodeIndexesRight,
				patSimpleASTNodesList, simpleASTNodesList, patSequence,
				sequence, nodeTypesLeft, nodeTypesRight);
	}

	private static boolean matchType(String pString, Term rTerm) {
		boolean flag = true;
		switch (rTerm.getTermType()) {
		case VariableTypeBindingTerm:
			if (!Term.V_Pattern.matcher(pString).matches()) {
				if (Term.T_Pattern.matcher(pString).matches()) {
					String varName = rTerm.getName();
					String typeName = ((VariableTypeBindingTerm) rTerm)
							.getTypeNameTerm().getName();
					if (varName.equals(typeName)) {
						flag = true;
						break;
					}
				}
				flag = false;
			}
			break;
		case MethodNameTerm:
			if (!Term.M_Pattern.matcher(pString).matches())
				flag = false;
			break;
		case TypeNameTerm:
			if (!Term.T_Pattern.matcher(pString).matches()) {
				flag = false;
			}
			break;
		}
		return flag;
	}
}
