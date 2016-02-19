package changeassistant.multipleexample.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TermsList;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.multipleexample.common.CommonParser;
import changeassistant.multipleexample.editfilter.ContentFilter;
import changeassistant.multipleexample.editfilter.RenameUpdateFilter;
import changeassistant.multipleexample.main.CachedProjectMap;
import changeassistant.multipleexample.partition.datastructure.AbstractCluster;
import changeassistant.multipleexample.partition.datastructure.BaseCluster;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.NodeSummary;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.util.LongestCommonSubsequence;
import changeassistant.versions.treematching.edits.ITreeEditOperation;
import changeassistant.versions.treematching.measure.IStringSimilarityCalculator;
import changeassistant.versions.treematching.measure.NGramsCalculator;

public class CommonEditParser extends CommonParser {

	public static int THRESHOLD_FOR_NUMBER_OF_NON_EMPTY_EDITS = 2;
	public static int THRESHOLD_FOR_NUMBER_OF_EDITS = 1;
	public static int N = 2;
	public static double nEdTh = 0.2;
	public static double nStTh = 0.59;
	public static double weightLess = 0.2;

	private IStringSimilarityCalculator nodeStringCalc = null;

	public CommonEditParser() {
		super();
		nodeStringCalc = new NGramsCalculator();
		((NGramsCalculator) nodeStringCalc).setN(N);
	}

	private boolean checkValid(List<ChangeSummary> left,
			List<ChangeSummary> right) {
		int leftIndex, rightIndex;
		ChangeSummary leftCS, rightCS;
		Map<TypeNameTerm, TypeNameTerm> typeMap = new HashMap<TypeNameTerm, TypeNameTerm>();
		Map<VariableTypeBindingTerm, VariableTypeBindingTerm> varMap = new HashMap<VariableTypeBindingTerm, VariableTypeBindingTerm>();
		Map<MethodNameTerm, MethodNameTerm> methodMap = new HashMap<MethodNameTerm, MethodNameTerm>();
		List<List<Term>> leftTermsList, rightTermsList;
		List<Term> leftTerms, rightTerms;
		Term term1, term2;
		boolean success = true;
		for (int i = 0; i < rightCSIndexes.size(); i++) {
			leftIndex = leftCSIndexes.get(i);
			rightIndex = rightCSIndexes.get(i);
			leftCS = left.get(leftIndex);
			rightCS = right.get(rightIndex);
			success = true;
			for (int j = 0; j < leftCS.nodeSummaries.size(); j++) {
				if (!leftCS.nodeSummaries.get(j).nodeStr
						.equals(rightCS.nodeSummaries.get(j).nodeStr)) {
					success = false;
					break;
				}

				leftTermsList = leftCS.nodeSummaries.get(j).expressions;
				rightTermsList = rightCS.nodeSummaries.get(j).expressions;

				for (int k = 0; k < leftTermsList.size(); k++) {
					leftTerms = leftTermsList.get(k);
					rightTerms = rightTermsList.get(k);
					for (int m = 0; m < leftTerms.size(); m++) {
						term1 = leftTerms.get(m);
						term2 = rightTerms.get(m);
						switch (term1.getTermType()) {
						case TypeNameTerm: {
							TypeNameTerm tTerm1 = (TypeNameTerm) term1;
							TypeNameTerm tTerm2 = (TypeNameTerm) term2;
							if (typeMap.containsKey(tTerm1)) {
								if (typeMap.get(tTerm1).equals(tTerm2)) {
									// do nothing
								} else {// find conflict
									return false;
								}
							} else {
								typeMap.put(tTerm1, tTerm2);
							}
						}
							break;
						case MethodNameTerm: {
							MethodNameTerm mTerm1 = (MethodNameTerm) term1;
							MethodNameTerm mTerm2 = (MethodNameTerm) term2;
							if (methodMap.containsKey(mTerm1)) {
								if (methodMap.get(mTerm1).equals(mTerm2)) {
									// do nothing
								} else {// find conflict
									return false;
								}
							} else {
								methodMap.put(mTerm1, mTerm2);
							}
						}
							break;
						case VariableTypeBindingTerm: {
							VariableTypeBindingTerm vTerm1 = (VariableTypeBindingTerm) term1;
							VariableTypeBindingTerm vTerm2 = (VariableTypeBindingTerm) term2;
							TypeNameTerm tTerm1 = vTerm1.getTypeNameTerm();
							TypeNameTerm tTerm2 = vTerm2.getTypeNameTerm();
							if (varMap.containsKey(vTerm1)
									&& !varMap.get(vTerm1).equals(vTerm2)
									|| typeMap.containsKey(tTerm1)
									&& !typeMap.get(tTerm1).equals(tTerm2)) {
								return false;
							}
							if (!varMap.containsKey(vTerm1)) {
								varMap.put(vTerm1, vTerm2);
							}
							if (!typeMap.containsKey(tTerm1)) {
								typeMap.put(tTerm1, tTerm2);
							}
						}
							break;
						case Term: {
							// System.out.print("need more process");
						}
							break;
						}
					}
				}
			}
		}
		return success;
	}

	// private String createRandomTermName(String abstractName){
	// String prefix = abstractName.substring(0, abstractName.indexOf('_') + 1);
	// String tmp = null;
	// Random r = new Random();
	// do{
	// tmp = prefix + Integer.toString(r.nextInt());
	// }while(lTou.containsValue(tmp) || rTou.containsValue(tmp));
	// return tmp;
	// }

	public String editDigest(List<ChangeSummary> chgSum, List<Integer> indexes) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < indexes.size(); i++) {
			buffer.append(chgSum.get(indexes.get(i)).editType.toString());
		}
		return buffer.toString();
	}

	private void extractLCS(int[][] b, List<String> l, List<String> r, int i,
			int j) {
		if ((i != 0) && (j != 0)) {
			if (b[i][j] == DIAG) {
				leftCSIndexes.add(0, i - 1);
				rightCSIndexes.add(0, j - 1);
				extractLCS(b, l, r, i - 1, j - 1);
			} else if (b[i][j] == UP) {
				extractLCS(b, l, r, i - 1, j);
			} else {
				extractLCS(b, l, r, i, j - 1);
			}
			/*
			 * else{ if(b[i-1][j] > b[i][j-1]){//UP is better extractLCS(b, l,
			 * r, i-1, j); }else{ extractLCS(b, l, r, i, j-1); } }
			 */
		}
	}

	private List<String> getCommonCS(List<String> left, List<String> right) {
		List<String> commonList = new ArrayList<String>();
		int leftIndex = 0;
		String leftCS = null;
		for (int i = 0; i < leftCSIndexes.size(); i++) {// enumerate each change
			leftIndex = leftCSIndexes.get(i);
			leftCS = left.get(leftIndex);
			commonList.add(leftCS);
		}
		return commonList;
	}

	public List<Integer> getLeftCSIndexes() {
		return leftCSIndexes;
	}

	public Map<String, String> getLtoU() {
		return lTou;
	}

	public List<Integer> getRightCSIndexes() {
		return rightCSIndexes;
	}

	public Map<String, String> getRtoU() {
		return rTou;
	}

	public boolean inSameCluster(List<ChangeSummary> chgSum1,
			List<ChangeSummary> chgSum2, List<String> chgSumStr1,
			List<String> chgSumStr2, List<String> absChgSumStr1,
			List<String> absChgSumStr2) {
//		System.out.print("");
		boolean flag = false;
		int commonLength = longestCommonSubsequence(chgSumStr1, chgSumStr2);
		String digest1 = editDigest(chgSum1, leftCSIndexes);
		String digest2 = editDigest(chgSum2, rightCSIndexes);
		if (!digest1.equals(digest2)) {
			return flag;
		}
		List<String> commonDescription = getCommonCS(chgSumStr1, chgSumStr2);
		double weightedCommon;

		if (commonLength >= THRESHOLD_FOR_NUMBER_OF_EDITS) {
			// if the number of edits is above a certain threshold
			// System.out.print("");
			weightedCommon = weightedLength(commonDescription);
			if (weightedCommon >= THRESHOLD_FOR_NUMBER_OF_EDITS * 1.0 / 2 /*
																		 * && (
																		 * weightedCommon
																		 * /
																		 * weightedLength
																		 * (
																		 * chgSumStr1
																		 * ) >=
																		 * nEdTh
																		 * &&
																		 * weightedCommon
																		 * /
																		 * weightedLength
																		 * (
																		 * chgSumStr2
																		 * ) >=
																		 * nEdTh
																		 * )
																		 */) {
				flag = true;
			}
		} else {
			commonLength = longestCommonSubsequence2(absChgSumStr1,
					absChgSumStr2);
			if (commonLength >= THRESHOLD_FOR_NUMBER_OF_EDITS) {
				if (checkValid(chgSum1, chgSum2)) {
					commonDescription = getCommonCS(absChgSumStr1,
							absChgSumStr2);
					weightedCommon = weightedLength2(leftCSIndexes, chgSum1);
					if (weightedCommon >= THRESHOLD_FOR_NUMBER_OF_EDITS * 1.0 / 2 /*
																				 * &&
																				 * weightedCommon
																				 * /
																				 * weightedLength2
																				 * (
																				 * chgSum1
																				 * )
																				 * >=
																				 * nEdTh
																				 * &&
																				 * weightedCommon
																				 * /
																				 * weightedLength2
																				 * (
																				 * chgSum2
																				 * )
																				 * >=
																				 * nEdTh
																				 */) {
						flag = true;
					}
				}

			}
		}
		return flag;
	}

	protected void clearRedundantIdentifierMap() {
		Set<String> values = new HashSet<String>();
		Set<String> values2 = new HashSet<String>();
		Set<String> keys = new HashSet<String>();
		Set<String> keys2 = new HashSet<String>();
		String key = null;
		String value = null;
		for (Entry<String, String> entry : lTou.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if (Term.U_Simple_Pattern.matcher(value).matches()) {
				keys.add(key);
				values.add(value);
			}
		}
		for (Entry<String, String> entry : rTou.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if (Term.U_Simple_Pattern.matcher(value).matches()) {
				keys2.add(key);
				values2.add(value);
			}
		}
		if (values2.equals(values)) {
			for (String tmpKey : keys) {
				lTou.remove(tmpKey);
			}
			for (String tmpKey : keys2) {
				rTou.remove(tmpKey);
			}
		} else {
			System.out.println("There is something wrong");
		}
	}

	public EditInCommonCluster intersect(AbstractCluster cluster1,
			AbstractCluster cluster2, List<ChangeSummary> chgSum1,
			List<ChangeSummary> chgSum2, List<String> chgSumStr1,
			List<String> chgSumStr2, List<String> absChgSumStr1,
			List<String> absChgSumStr2,
			List<List<List<SimpleASTNode>>> simpleExprsLists1,
			List<List<List<SimpleASTNode>>> simpleExprsLists2,
			EditInCommonGroup group) {
		boolean flag = false;
		EditInCommonCluster cluster = null;
		// List<Integer> tmpIndexes1 = null;
		// List<Integer> tmpIndexes2 = null;
		List<Integer> indexes1 = null;
		List<Integer> indexes2 = null;

		// System.out.print("");
		if (cluster1 instanceof EditInCommonCluster
				&& cluster2 instanceof EditInCommonCluster) {
			EditInCommonCluster eClus1 = (EditInCommonCluster) cluster1;
			EditInCommonCluster eClus2 = (EditInCommonCluster) cluster2;
			List<Integer> instances1 = eClus1.getInstances();
			List<Integer> instances2 = eClus2.getInstances();
			List<Integer> instances = new ArrayList<Integer>(
					eClus1.getInstances());
			instances.retainAll(instances2);
			if (!instances.isEmpty()) {// they have some overlap with each other
				int index1 = instances1.indexOf(instances.get(0));
				indexes1 = eClus1.getIndexesList().get(index1);
				int index2 = instances2.indexOf(instances.get(0));
				indexes2 = eClus2.getIndexesList().get(index2);
				LongestCommonSubsequence<Integer> lcs = new LongestCommonSubsequence<Integer>();
				int commonLength = lcs.getLCS(indexes1, indexes2);
				if (commonLength >= THRESHOLD_FOR_NUMBER_OF_EDITS) {// you can
																	// get the
																	// meaningful
																	// intersection
					flag = true;
					leftCSIndexes = lcs.getLeftCSIndexes();
					rightCSIndexes = lcs.getRightCSIndexes();
				}
			}
		} else {// baseCluster
			TermsList.prLeft = CachedProjectMap.get(((BaseCluster) cluster1)
					.getMM().originalMethod.getProjectName());
			TermsList.prRight = CachedProjectMap.get(((BaseCluster) cluster2)
					.getMM().originalMethod.getProjectName());
			if (inSameCluster(chgSum1, chgSum2, chgSumStr1, chgSumStr2,
					absChgSumStr1, absChgSumStr2)) {
				flag = true;
			}
		}

		if (flag) {
			List<List<List<SimpleASTNode>>> commonExprsLists = new ArrayList<List<List<SimpleASTNode>>>();
			List<List<SimpleASTNode>> commonExprsList = null;
			List<ChangeSummary> commonChgSum = new ArrayList<ChangeSummary>();
			List<String> chgSumStr = new ArrayList<String>();
			List<String> absChgSumStr = new ArrayList<String>();
			ChangeSummaryCreator csCreator = new ChangeSummaryCreator();
			ChangeSummary chgSum = null;
			try {
				boolean isGeneral = true;
				for (int i = 0; i < leftCSIndexes.size(); i++) {
					// System.out.print("");
					List<List<SimpleASTNode>> simpleExprsList1 = simpleExprsLists1
							.get(leftCSIndexes.get(i));
					List<List<SimpleASTNode>> simpleExprsList2 = simpleExprsLists2
							.get(rightCSIndexes.get(i));
					ChangeSummary cs1 = chgSum1.get(leftCSIndexes.get(i));
					ChangeSummary cs2 = chgSum2.get(rightCSIndexes.get(i));
					if (cs1.editType.equals(ITreeEditOperation.EDIT.UPDATE)
							&& cs1.nodeSummaries.get(1).nodeType != cs2.nodeSummaries
									.get(1).nodeType) {
						commonExprsList = null;
					} else {
						commonExprsList = getCommon(simpleExprsList1,
								simpleExprsList2);
					}

					if (commonExprsList == null
							|| commonExprsList.isEmpty()
							|| commonExprsList.get(0).size() == 1
							&& Term.U_Pattern
									.matcher(
											commonExprsList.get(0).get(0)
													.getStrValue()).matches()) {
						return null;
//						chgSum = csCreator.createEmptyChangeSummary(chgSum1
//								.get(leftCSIndexes.get(i)).editType);
//						commonChgSum.add(chgSum);
//						chgSumStr.add(chgSum.editType.toString());
//						absChgSumStr.add(chgSum.editType.toString());
					} else {
						flag = false;
						// here we hope the change summary which is constructed
						// by
						// ASTExpressionTransformer and SimpleASTNode should
						// correspond to each other
						System.out.print("");
						chgSum = csCreator.updateSummary(commonExprsList,
								chgSum1.get(leftCSIndexes.get(i)));
						if (isGeneral && !csCreator.isGeneral)
							isGeneral = false;
						commonChgSum.add(chgSum);
						chgSumStr.add(csCreator.createChgSumStr1(chgSum));
						absChgSumStr.add(csCreator.createChgSumStr2(chgSum));
					}
					commonExprsLists.add(commonExprsList);
				}
				if (RenameUpdateFilter.filterOut(commonChgSum))
					return null;
//				if (ContentFilter.filterOut(commonChgSum, chgSumStr,
//						absChgSumStr, leftCSIndexes, rightCSIndexes,
//						commonExprsLists))
//					return null;
				if (flag || isGeneral) {
					cluster = null;
				} else {
					cluster = new EditInCommonCluster(commonChgSum, chgSumStr,
							absChgSumStr);
					cluster.setExprsLists(Collections
							.unmodifiableList(commonExprsLists));
					clearRedundantIdentifierMap();
					cluster.addIncoming(cluster1, leftCSIndexes, lTou);
					cluster.addIncoming(cluster2, rightCSIndexes, rTou);
				}
				return cluster;
			} catch (MappingException e) {
				return null;
			}
		}
		return null;
	}

	private int longestCommonSubsequence(List<String> left, List<String> right) {
		int m = left.size();
		int n = right.size();
		double similarity = 0;
		boolean sameType = false;
		String lStr = null, rStr = null;
		String lType = null, rType = null;

		float[][] c = new float[m + 1][n + 1];
		int[][] b = new int[m + 1][n + 1];

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
				sameType = false;
				lStr = left.get(i - 1);
				rStr = right.get(j - 1);
				lType = lStr.substring(0, lStr.indexOf(':'));
				rType = rStr.substring(0, rStr.indexOf(':'));
				sameType = lType.equals(rType);
				if (sameType)
					similarity = nodeStringCalc.calculateSimilarity(
							left.get(i - 1), right.get(j - 1));
				if (sameType && similarity > nStTh) {
					c[i][j] = c[i - 1][j - 1] + 1;
					b[i][j] = DIAG;
				} else if (c[i - 1][j] >= c[i][j - 1]) {// here UP may be no
														// better than LEFT
					c[i][j] = c[i - 1][j];
					b[i][j] = UP;
				} else {
					c[i][j] = c[i][j - 1];
					b[i][j] = LEFT;
				}
			}
		}
		leftCSIndexes = new ArrayList<Integer>();
		rightCSIndexes = new ArrayList<Integer>();

		int primeM = m, primeN = n;
		// float primeSim = c[0][n];
		// for(int i = 1; i <= m; i++){
		// if(c[i][n] > primeSim){
		// primeM = i;
		// primeN = n;
		// primeSim = c[i][n];
		// }
		// }
		// for(int i = 1; i <= n; i++){
		// if(c[m][i] > primeSim){
		// primeM = m;
		// primeN = i;
		// primeSim = c[m][i];
		// }
		// }
		extractLCS(b, left, right, primeM, primeN);
		return leftCSIndexes.size();
	}

	private int longestCommonSubsequence2(List<String> left, List<String> right) {
		int m = left.size();
		int n = right.size();

		int[][] c = new int[m + 1][n + 1];
		int[][] b = new int[m + 1][n + 1];

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
				if (left.get(i - 1).equals(right.get(j - 1))) {
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
		leftCSIndexes = new ArrayList<Integer>();
		rightCSIndexes = new ArrayList<Integer>();
		extractLCS(b, left, right, m, n);
		return c[m][n];
	}

	private double weightedLength(List<String> description) {
		double result = 0;
		String tmp = null;
		for (String str : description) {
			tmp = str.substring(str.indexOf(' ') + 1);
			if (tmp.trim().equals("then:")) {
				// do nothing
			} else if (tmp.substring(tmp.indexOf(':') + 1).trim().isEmpty()) {
				result += weightLess;
			} else {
				result += 1;
			}
		}
		return result;
	}

	private double weightedLength2(List<Integer> csIndexes,
			List<ChangeSummary> changeSummaries) {
		double wLength = 0;
		List<NodeSummary> nodeSummaries = null;
		boolean flag = false;
		for (int i = 0; i < csIndexes.size(); i++) {
			nodeSummaries = changeSummaries.get(csIndexes.get(i)).nodeSummaries;
			flag = false;
			for (int j = 0; j < nodeSummaries.size(); j++) {
				if (!nodeSummaries.get(j).expressions.isEmpty()) {
					flag = true;
					break;
				}
			}
			if (flag) {
				wLength += 1;
			} else {
				wLength += weightLess;
			}
		}
		return wLength;
	}

	private double weightedLength2(List<ChangeSummary> changeSummaries) {
		double wLength = 0;
		List<NodeSummary> nodeSummaries = null;
		boolean flag = false;
		for (int i = 0; i < changeSummaries.size(); i++) {
			nodeSummaries = changeSummaries.get(i).nodeSummaries;
			flag = false;
			for (int j = 0; j < nodeSummaries.size(); j++) {
				if (!nodeSummaries.get(j).expressions.isEmpty()) {
					flag = true;
					break;
				}
			}
			if (flag) {
				wLength += 1;
			} else {
				// wLength += weightLess;
			}
		}
		return wLength;
	}
}
