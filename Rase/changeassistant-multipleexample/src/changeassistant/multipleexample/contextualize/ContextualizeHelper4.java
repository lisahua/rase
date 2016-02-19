package changeassistant.multipleexample.contextualize;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TermsList;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.multipleexample.ccfinder.RunCCFinder;
import changeassistant.multipleexample.common.CommonParser;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.datastructure.Pair;
import changeassistant.multipleexample.main.CachedProjectMap;
import changeassistant.multipleexample.partition.SimpleASTNodeConverter;
import changeassistant.multipleexample.partition.datastructure.AbstractCluster;
import changeassistant.multipleexample.partition.datastructure.BaseCluster;
import changeassistant.multipleexample.partition.datastructure.ClusterHelper;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.LCSSequence;
import changeassistant.multipleexample.util.PathUtil;
import changeassistant.peers.SourceCodeRange;
import changeassistant.versions.comparison.MethodModification;

public class ContextualizeHelper4 extends LCSSequence {

	private EditInCommonGroup group = null;
	private List<EditInCommonCluster> clusters = null;
	private List<MethodModification> mmList;

	private Map<Integer, Integer> matchSet = null;
	private Map<Integer, Integer> blackMatchSet = null;// node index pairs are
														// saved
	private Map<String, Set<String>> blackIdentifierMap = null;
	private Map<String, Set<String>> blackIdentifierMap2 = null;

	private Map<String, Set<TypeNameTerm>> typeTermMap1 = null;
	private Map<String, Set<TypeNameTerm>> typeTermMap2 = null;

	public ContextualizeHelper4(EditInCommonGroup group,
			List<EditInCommonCluster> clusters, List<MethodModification> mmList) {
		this.group = group;
		this.clusters = clusters;
		this.mmList = mmList;
	}

	/**
	 * @param simpleASTNodesList1
	 * @param simpleASTNodesList2
	 * @param indexes1
	 * @param indexes2
	 * @param specificToUnified1
	 *            --concrete to abstract map for simpleASTNodesList1
	 * @param unifiedToSpecific
	 *            --abstract to concrete map for simpleASTNodesList2
	 * @param specificToUnified2
	 *            --concrete to abstract map for simpleASTNodesList2
	 */
	private void addMatchPairs(List<List<SimpleASTNode>> simpleASTNodesList1,
			List<List<SimpleASTNode>> simpleASTNodesList2,
			List<Integer> indexes1, List<Integer> indexes2,
			Map<String, String> specificToUnified1,
			Map<String, String> unifiedToSpecific,
			Map<String, String> specificToUnified2) {
		Map<String, String> basicMap = new HashMap<String, String>();
		Map<String, String> basicMap2 = new HashMap<String, String>();

		Set<Integer> insts = null;
		for (Entry<String, String> entry : specificToUnified1.entrySet()) {
			basicMap.put(entry.getKey(),
					unifiedToSpecific.get(entry.getValue()));
			basicMap2.put(unifiedToSpecific.get(entry.getValue()),
					entry.getKey());
		}
		List<Term> leftTerms = new ArrayList<Term>(), rightTerms = new ArrayList<Term>();
		List<Set<Integer>> supportingInsts = new ArrayList<Set<Integer>>();
		List<List<Term>> termsList1 = null, termsList2 = null;
		// assumption: the size of indexes1 is equal to that of indexes2
		System.out.print("");
		for (int i = 0; i < indexes1.size(); i++) {
			if (indexes1.get(i) < 0)
				continue;
			try {
				// System.out
				// .println(simpleASTNodesList1.get(indexes1.get(i) - 1));
				// System.out
				// .println(simpleASTNodesList2.get(indexes2.get(i) - 1));
				// System.out.println("*********" + (indexes1.get(i) - 1)
				// + "******" + (indexes2.get(i) - 1) + "*****");
				List<SimpleASTNode> sNodes1 = simpleASTNodesList1.get(indexes1
						.get(i) - 1);
				List<SimpleASTNode> sNodes2 = simpleASTNodesList2.get(indexes2
						.get(i) - 1);
				termsList1 = SimpleASTNodeConverter.convertToTermsList(sNodes1);
				termsList2 = SimpleASTNodeConverter.convertToTermsList(sNodes2);

				if (BlackMapChecker.isBlackMap(termsList1, termsList2, sNodes1,
						sNodes2, specificToUnified1, unifiedToSpecific,
						specificToUnified2, basicMap, basicMap2, typeTermMap1,
						typeTermMap2, leftTerms, rightTerms, i,
						supportingInsts, blackIdentifierMap,
						blackIdentifierMap2)) {
					blackMatchSet.put(indexes1.get(i), indexes2.get(i));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		BlackMapChecker.filterConflict(leftTerms, rightTerms, supportingInsts);
		Set<Integer> tmpProcessed = new HashSet<Integer>();
		for (int i = 0; i < supportingInsts.size(); i++) {
			insts = supportingInsts.get(i);
			for (Integer inst : insts) {
				if (tmpProcessed.add(inst)
						&& !conflict(matchSet, indexes1.get(inst),
								indexes2.get(inst))) {
					matchSet.put(indexes1.get(inst), indexes2.get(inst));
				}
			}
		}
		extendMap(specificToUnified1, unifiedToSpecific, specificToUnified2,
				leftTerms, rightTerms);
	}

	private void addMatchPairs(Hashtable<String, StringBuilder> pairList,
			List<List<SimpleASTNode>> simpleASTNodesList1,
			List<List<SimpleASTNode>> simpleASTNodesList2,
			List<Integer> subIndexList1, List<Integer> subIndexList2,
			Map<String, String> specificToUnified1,
			Map<String, String> unifiedToSpecific,
			Map<String, String> specificToUnified2) {
		String key = null;
		Map<String, String> basicMap = new HashMap<String, String>();
		Map<String, String> basicMap2 = new HashMap<String, String>();// reverse
																		// map
																		// of
																		// basicMap
		for (Entry<String, String> entry : specificToUnified1.entrySet()) {
			basicMap.put(entry.getKey(),
					unifiedToSpecific.get(entry.getValue()));
			basicMap2.put(unifiedToSpecific.get(entry.getValue()),
					entry.getKey());
		}
		StringBuilder value = null;
		// collect all line range pairs in sequence
		List<Pair<SourceCodeRange>> lineRangePairs = new ArrayList<Pair<SourceCodeRange>>();
		String tmpRangeStr = null, tmpRangeStr1 = null, tmpRangeStr2 = null;
		String[] rangeTokens = null, rangeTokens2 = null;
		Set<String> processed = new HashSet<String>();
		Set<String> processed2 = new HashSet<String>();
		List<SourceCodeRange> candis = null;
		for (Entry<String, StringBuilder> entry : pairList.entrySet()) {
			key = entry.getKey();
			if (key.startsWith("2") || processed.contains(key.toString()))
				// only record the pairs mapping from 1 to 2
				continue;
			value = entry.getValue();
			rangeTokens = value.toString().split(" ");// possible candidates
														// matching the key
														// component
			tmpRangeStr2 = null;
			for (String rangeToken : rangeTokens) {
				if (!processed2.contains(rangeToken)) {
					tmpRangeStr2 = rangeToken;// find the first unmatched range2
					break;
				}
			}
			if (tmpRangeStr2 == null)
				continue;
			// System.out.print("");
			candis = new ArrayList<SourceCodeRange>();
			for (Entry<String, StringBuilder> entry2 : pairList.entrySet()) {
				if (entry2.getValue().toString().contains(tmpRangeStr2)) {
					tmpRangeStr = entry2.getKey();
					if (!processed.contains(tmpRangeStr)) {
						tmpRangeStr = tmpRangeStr.substring(tmpRangeStr
								.indexOf('.') + 1);
						rangeTokens = tmpRangeStr.split("-");
						candis.add(new SourceCodeRange(Integer
								.valueOf(rangeTokens[0]) - 1, Integer
								.valueOf(rangeTokens[1])
								- Integer.valueOf(rangeTokens[0]) + 1));
					}
				}
			}
			if (candis.size() == 0) {
				continue;// there is no range matching range2, continue
			} else if (candis.size() > 1) {
				Collections.sort(candis);
			}
			// there should be two tokens included: starting line and ending
			// line
			tmpRangeStr = tmpRangeStr2.substring(tmpRangeStr2.indexOf('.') + 1);
			rangeTokens2 = tmpRangeStr.split("-");
			lineRangePairs.add(new Pair<SourceCodeRange>(candis.get(0),
					new SourceCodeRange(Integer.valueOf(rangeTokens2[0]) - 1,
							Integer.valueOf(rangeTokens2[1])
									- Integer.valueOf(rangeTokens2[0]) + 1)));
			processed.add("1." + (candis.get(0).startPosition + 1) + "-"
					+ (candis.get(0).startPosition + candis.get(0).length + 1));
			processed2.add(tmpRangeStr2);
		}
		// filter out overlapping clone pairs
		filterOverlap(lineRangePairs);

		List<Term> leftTerms = new ArrayList<Term>(), rightTerms = new ArrayList<Term>();
		List<List<Integer>> leftSupportingInstances = new ArrayList<List<Integer>>();
		List<List<Integer>> rightSupportingInstances = new ArrayList<List<Integer>>();
		collectSupportingInstances(lineRangePairs, simpleASTNodesList1,
				simpleASTNodesList2, subIndexList1, subIndexList2, basicMap,
				basicMap2, leftTerms, rightTerms, leftSupportingInstances,
				rightSupportingInstances);

		filterConflict(leftTerms, rightTerms, leftSupportingInstances,
				rightSupportingInstances);
		Set<Integer> tmpProcessed = new HashSet<Integer>();
		List<Integer> insts = null;
		List<Integer> insts2 = null;
		Integer inst = null;
		Integer inst2 = null;
		for (int i = 0; i < leftSupportingInstances.size(); i++) {
			insts = leftSupportingInstances.get(i);
			insts2 = rightSupportingInstances.get(i);
			for (int j = 0; j < insts.size(); j++) {
				inst = insts.get(j);
				inst2 = insts2.get(j);
				if (tmpProcessed.add(inst)
						&& !conflict(matchSet, subIndexList1.get(inst) + 1,
								subIndexList2.get(inst2) + 1)) {
					matchSet.put(subIndexList1.get(inst) + 1,
							subIndexList2.get(inst2) + 1);
				}
			}
		}
		extendMap(specificToUnified1, unifiedToSpecific, specificToUnified2,
				leftTerms, rightTerms);
	}

	private void collectSupportingInstances(
			List<Pair<SourceCodeRange>> lineRangePairs,
			List<List<SimpleASTNode>> simpleASTNodesList1,
			List<List<SimpleASTNode>> simpleASTNodesList2,
			List<Integer> subIndexList1, List<Integer> subIndexList2,
			Map<String, String> basicMap, Map<String, String> basicMap2,
			List<Term> leftTerms, List<Term> rightTerms,
			List<List<Integer>> leftSupportingInstances,
			List<List<Integer>> rightSupportingInstances) {
		SourceCodeRange range1 = null, range2 = null;
		List<Integer> leftSupInsts = null;
		List<Integer> rightSupInsts = null;
		List<List<List<Term>>> termsListList1 = new ArrayList<List<List<Term>>>();
		List<List<List<Term>>> termsListList2 = new ArrayList<List<List<Term>>>();
		LCSTerms lcsApplier = new LCSTerms();
		List<Integer> tmpIndexes1 = null, tmpIndexes2 = null;
		int tmpIndex1 = -1, tmpIndex2 = -1;

		List<List<Integer>> tmpSupportingInsts1 = null;
		List<List<Integer>> tmpSupportingInsts2 = null;
		List<Integer> tmpSupInsts1 = null;
		List<Integer> tmpSupInsts2 = null;
		for (Pair<SourceCodeRange> pair : lineRangePairs) {
			// collect supporting instances within a specific line range
			tmpSupportingInsts1 = new ArrayList<List<Integer>>();
			tmpSupportingInsts2 = new ArrayList<List<Integer>>();
			for (int i = 0; i < leftTerms.size(); i++) {
				tmpSupportingInsts1.add(new ArrayList<Integer>());
				tmpSupportingInsts2.add(new ArrayList<Integer>());
			}
			termsListList1.clear();
			termsListList2.clear();
			range1 = pair.getLeft();
			for (int i = range1.startPosition; i < range1.startPosition
					+ range1.length; i++) {
				termsListList1.add(SimpleASTNodeConverter
						.convertToTermsList(simpleASTNodesList1
								.get(subIndexList1.get(i))));
			}
			range2 = pair.getRight();
			for (int i = range2.startPosition; i < range2.startPosition
					+ range2.length; i++) {
				termsListList2.add(SimpleASTNodeConverter
						.convertToTermsList(simpleASTNodesList2
								.get(subIndexList2.get(i))));
			}
			System.out.print("");
			// supportingInsts.clear();
			lcsApplier.getLCS(termsListList1, termsListList2);
			tmpIndexes1 = lcsApplier.getLeftCSIndexes();
			tmpIndexes2 = lcsApplier.getRightCSIndexes();
			// infer identifier mapping based on node mapping, and check the
			// node mapping at the same time
			for (int i = 0; i < tmpIndexes1.size(); i++) {
				tmpIndex1 = tmpIndexes1.get(i);
				tmpIndex2 = tmpIndexes2.get(i);
				if (!TermsList.doMap(termsListList1.get(tmpIndex1),
						termsListList2.get(tmpIndex2), basicMap, basicMap2,
						typeTermMap1, typeTermMap2, leftTerms, rightTerms,
						range1.startPosition + tmpIndex1, range2.startPosition
								+ tmpIndex2, tmpSupportingInsts1,
						tmpSupportingInsts2, blackIdentifierMap,
						blackIdentifierMap2)) {
					// fail to map, then the two nodes should be put into
					// blackPairs
					blackMatchSet.put(
							subIndexList1.get(range1.startPosition
									+ tmpIndexes1.get(i)) + 1,
							subIndexList2.get(range2.startPosition
									+ tmpIndexes2.get(i)) + 1);
				}
			}
			// process the conflicting identifier mappings
			for (int i = 0; i < leftSupportingInstances.size(); i++) {
				leftSupInsts = leftSupportingInstances.get(i);
				rightSupInsts = rightSupportingInstances.get(i);
				tmpSupInsts1 = tmpSupportingInsts1.get(i);
				tmpSupInsts2 = tmpSupportingInsts2.get(i);
				for (int j = 0; j < tmpSupInsts1.size(); j++) {
					if (!leftSupInsts.contains(tmpSupInsts1.get(j))) {
						leftSupInsts.add(tmpSupInsts1.get(j));
						rightSupInsts.add(tmpSupInsts2.get(j));
					}
				}
			}
			for (int i = leftSupportingInstances.size(); i < tmpSupportingInsts1
					.size(); i++) {
				leftSupportingInstances.add(tmpSupportingInsts1.get(i));
				rightSupportingInstances.add(tmpSupportingInsts2.get(i));
			}
		}
	}

	private boolean conflict(Map<Integer, Integer> matchSet, Integer left,
			Integer right) {
		Integer pLeft = null, pRight = null;
		for (Entry<Integer, Integer> entry : matchSet.entrySet()) {
			pLeft = entry.getKey();
			pRight = entry.getValue();
			if (pLeft.equals(left) && pRight.equals(right))
				return false;
			if (!pLeft.equals(left) && !pRight.equals(right)) {
				// do nothing
				continue;
			}
			return true; // pLeft.equals(left) && !pRight.equals(right) ||
							// !pLeft.equals(left) && pRight.equals(right)
		}
		return false;
	}

	/**
	 * To extend the basic map shown with specificToUnified and
	 * unifiedToSpecific, while resetting the terms' names with their abstract
	 * names
	 * 
	 * @param specificToUnified
	 * @param unifiedToSpecific
	 * @param leftTerms
	 * @param rightTerms
	 */
	private void extendMap(Map<String, String> specificToUnified1,
			Map<String, String> unifiedToSpecific,
			Map<String, String> specificToUnified2, List<Term> leftTerms,
			List<Term> rightTerms) {
		CommonParser parser = new CommonParser();
		parser.setMap(specificToUnified1, specificToUnified2);

		// System.out.print("");
		String name1 = null, name2 = null;
		Term term1 = null, term2 = null;
		String abstractName = null;
		TypeNameTerm tmpTerm1 = null, tmpTerm2 = null;
		Set<String> processed1 = new HashSet<String>();
		try {
			for (int i = 0; i < leftTerms.size(); i++) {
				term1 = leftTerms.get(i);
				term2 = rightTerms.get(i);
				name1 = term1.getName();
				name2 = term2.getName();
				if (!processed1.add(term1.getAbstractName())) {
					// if the term is processed, do not process it again to
					// avoid generating repetitive terms
					if (specificToUnified1.get(name1) != null) {
						term1.setName(specificToUnified1.get(name1));
						term2.setName(specificToUnified2.get(name2));
						if (term1 instanceof VariableTypeBindingTerm) {
							tmpTerm1 = ((VariableTypeBindingTerm) term1)
									.getTypeNameTerm();
							tmpTerm2 = ((VariableTypeBindingTerm) term2)
									.getTypeNameTerm();
							tmpTerm1.setName(specificToUnified1.get(tmpTerm1
									.getName()));
							tmpTerm2.setName(specificToUnified2.get(tmpTerm2
									.getName()));
						}
					} else {
						// no need to process it since it has already
						// been changed as a nested term
					}

					continue;
				}

				if (specificToUnified1.containsValue(name1)) {
					continue;
				}
				if (Term.ExactAbsPattern.matcher(name1).matches()
						|| !name1.equals(name2)) {
					switch (term1.getTermType()) {
					case VariableTypeBindingTerm: {
						tmpTerm1 = ((VariableTypeBindingTerm) term1)
								.getTypeNameTerm();
						tmpTerm2 = ((VariableTypeBindingTerm) term2)
								.getTypeNameTerm();
						if (processed1.add(tmpTerm1.getAbstractName())) {
							// if the typeNameTerm is already processed, do not
							// process it again
							if (!specificToUnified1.containsValue(tmpTerm1
									.getName())) {
								abstractName = parser.getAbstractIdentifier(
										tmpTerm1.getName(), tmpTerm2.getName(),
										ASTExpressionTransformer.ABSTRACT_TYPE);
								specificToUnified1.put(tmpTerm1.getName(),
										abstractName);
								unifiedToSpecific.put(abstractName,
										tmpTerm2.getName());
								tmpTerm1.setName(abstractName);
								tmpTerm2.setName(abstractName);
							}
						} else {
							if (specificToUnified1.get(tmpTerm1.getName()) != null) {
								tmpTerm1.setName(specificToUnified1
										.get(tmpTerm1.getName()));
								tmpTerm2.setName(specificToUnified2
										.get(tmpTerm2.getName()));
							} else {
								// no need to process it since it has already
								// been changed as a nested term
							}
						}
						abstractName = parser.getAbstractIdentifier(name1,
								name2,
								ASTExpressionTransformer.ABSTRACT_VARIABLE);
					}
						break;
					case MethodNameTerm: {
						abstractName = parser
								.getAbstractIdentifier(
										name1,
										name2,
										ASTExpressionTransformer.ABSTRACT_METHOD);
					}
						break;
					case TypeNameTerm: {
						abstractName = parser.getAbstractIdentifier(name1,
								name2, ASTExpressionTransformer.ABSTRACT_TYPE);
					}
						break;
					case Term: {
						abstractName = parser.getAbstractSpecialIdentifier(
								term1.getNodeType(), term2.getNodeType(),
								name1, name2,
								ASTExpressionTransformer.ABSTRACT_UNKNOWN);
					}
					}
				} else {
					abstractName = name1;
				}
				specificToUnified1.put(name1, abstractName);
				unifiedToSpecific.put(abstractName, name2);
				specificToUnified2.put(name2, abstractName);
				term1.setName(abstractName);
				term2.setName(abstractName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void collectConflictMap(Map<Integer, Integer> candisInsts, int i,
			List<List<Integer>> supportingInsts, List<Term> terms, Term tmpTerm) {
		candisInsts.put(i, supportingInsts.get(i).size());
		Term term1 = null;
		for (int j = i + 1; j < terms.size(); j++) {
			term1 = terms.get(j);
			if (term1.equals(tmpTerm)) {
				candisInsts.put(j, supportingInsts.get(j).size());
			}
		}
	}

	// private int collectConflictCounter(Map<Integer, Integer> candisInsts,
	// int repetitiveKey, List<List<Integer>> supportingInsts,
	// List<Term> terms, Term tmpTerm) {
	// candisInsts.put(repetitiveKey, supportingInsts.get(repetitiveKey)
	// .size());
	// Term term1 = null;
	// int counter = -1;
	// for (int j = 0; j < terms.size(); j++) {
	// if (j == repetitiveKey)
	// continue;
	// term1 = terms.get(j);
	// if (term1.equals(tmpTerm)) {
	// counter += supportingInsts.get(j).size();
	// }
	// }
	// return counter;
	// }

	/**
	 * remove some conflicts via election
	 * 
	 * @param leftTerms
	 * @param rightTerms
	 * @param supportingInsts
	 */
	public void filterConflict(List<Term> leftTerms, List<Term> rightTerms,
			List<List<Integer>> leftSupportingInsts,
			List<List<Integer>> rightSupportingInsts) {
		int i = 0;
		Term tmpTerm = null, tmpTerm2 = null;
		List<Term> terms1 = null, terms2 = null, tmpTerms = null;
		List<List<Integer>> supportingInsts2 = null;
		List<List<Integer>> supportingInsts3 = null;
		List<Integer> insts = null;
		Set<Integer> uselessInsts2 = new HashSet<Integer>();
		Set<Integer> uselessInsts3 = new HashSet<Integer>();
		boolean leftRepeat = false, rightRepeat = false;
		while (i < leftTerms.size()) {
			tmpTerm = leftTerms.get(i);
			tmpTerm2 = rightTerms.get(i);
			leftRepeat = leftTerms.subList(i + 1, leftTerms.size()).contains(
					tmpTerm);
			rightRepeat = rightTerms.subList(i + 1, rightTerms.size())
					.contains(tmpTerm2);
			if (leftRepeat || rightRepeat) {
				// tmpTerm is a repetitive term in leftTerms
				Map<Integer, Integer> candisInsts = new HashMap<Integer, Integer>();
				if (leftRepeat) {
					collectConflictMap(candisInsts, i, leftSupportingInsts,
							leftTerms, tmpTerm);
				} else {// rightRepeat
					collectConflictMap(candisInsts, i, rightSupportingInsts,
							rightTerms, tmpTerm2);
				}

				int maxValue = 0, maxKey = -1, key = -1, value = -1;
				boolean hasRepetitiveMaxKey = false;
				int repetitiveKey = -1;
				for (Entry<Integer, Integer> entry : candisInsts.entrySet()) {
					key = entry.getKey();
					value = entry.getValue();
					if (value > maxValue) {
						maxKey = key;
						maxValue = value;
						hasRepetitiveMaxKey = false;
					} else if (value == maxValue) {
						hasRepetitiveMaxKey = true;
						repetitiveKey = key;
					}
				}
				if (maxValue != 0) {
					if (!hasRepetitiveMaxKey) {
						candisInsts.remove(maxKey);
					} else {
						tmpTerm = leftTerms.get(repetitiveKey);
						tmpTerm2 = rightTerms.get(repetitiveKey);
						if (leftRepeat && !rightRepeat) {
							if (rightTerms.subList(0, repetitiveKey).contains(
									tmpTerm2)
									|| rightTerms.subList(repetitiveKey + 1,
											rightTerms.size()).contains(
											tmpTerm2)) {
								candisInsts.remove(maxKey);
							} else if (rightTerms.subList(0, maxKey).contains(
									tmpTerm2)
									|| rightTerms.subList(maxKey + 1,
											rightTerms.size()).contains(
											tmpTerm2)) {
								candisInsts.remove(repetitiveKey);
							} else {
								System.out.print("More process is needed");
							}
						} else if (!leftRepeat && rightRepeat) {// rightRepeat
							if (leftTerms.subList(0, repetitiveKey).contains(
									tmpTerm)
									|| leftTerms.subList(repetitiveKey + 1,
											leftTerms.size()).contains(tmpTerm)) {
								candisInsts.remove(maxKey);
							} else if (leftTerms.subList(0, maxKey).contains(
									tmpTerm)
									|| leftTerms.subList(maxKey + 1,
											leftTerms.size()).contains(tmpTerm)) {
								candisInsts.remove(repetitiveKey);
							} else {
								System.out.print("More process is needed");
							}
						} else {
							System.out.print("More process is needed");
						}
					}
				}
				terms1 = new ArrayList<Term>();
				terms2 = new ArrayList<Term>();
				supportingInsts2 = new ArrayList<List<Integer>>();
				supportingInsts3 = new ArrayList<List<Integer>>();
				if (leftRepeat) {
					tmpTerms = leftTerms;
				} else {// rightRepeat
					tmpTerms = rightTerms;
				}

				for (int k = 0; k < tmpTerms.size(); k++) {
					if (!candisInsts.containsKey(k)) {
						terms1.add(leftTerms.get(k));
						terms2.add(rightTerms.get(k));
						supportingInsts2.add(leftSupportingInsts.get(k));
						supportingInsts3.add(rightSupportingInsts.get(k));
					} else {
						uselessInsts2.addAll(leftSupportingInsts.get(k));
						uselessInsts3.addAll(rightSupportingInsts.get(k));
					}
				}

				for (int k = 0; k < supportingInsts2.size(); k++) {
					insts = supportingInsts2.get(k);
					insts.removeAll(uselessInsts2);
				}
				for (int k = 0; k < supportingInsts3.size(); k++) {
					insts = supportingInsts3.get(k);
					insts.removeAll(uselessInsts3);
				}

				leftTerms.clear();
				rightTerms.clear();
				leftSupportingInsts.clear();
				rightSupportingInsts.clear();
				if (!checkEmpty(supportingInsts2)
						&& !checkEmpty(supportingInsts3)) {
					leftTerms.addAll(terms1);
					rightTerms.addAll(terms2);
					leftSupportingInsts.addAll(supportingInsts2);
					rightSupportingInsts.addAll(supportingInsts3);
				} else {
					break;
				}
			} else {
				i++;
			}
		}
	}

	private boolean checkEmpty(List<List<Integer>> insts) {
		boolean isEmpty = true;
		for (int k = 0; k < insts.size(); k++) {
			if (!insts.get(k).isEmpty()) {
				isEmpty = false;
				break;
			}
		}
		return isEmpty;
	}

	/**
	 * To filter out the overlapping line ranges, the complexity should be
	 * O(n^2)
	 * 
	 * @param lineRangePairs
	 */
	private void filterOverlap(List<Pair<SourceCodeRange>> lineRangePairs) {
		if (lineRangePairs.size() < 2)
			return;
		int i = 0, j = i + 1;
		boolean isChanged = false;
		SourceCodeRange range1 = null, range2 = null;
		while (i < lineRangePairs.size() - 1) {
			isChanged = false;
			range1 = lineRangePairs.get(i).getLeft();
			for (j = i + 1; j < lineRangePairs.size(); j++) {
				range2 = lineRangePairs.get(j).getLeft();
				if (range1.isInside(range2)) {
					lineRangePairs.remove(i);
					isChanged = true;
					break;
				}
				if (range2.isInside(range1)) {
					lineRangePairs.remove(j);
					isChanged = true;
					break;
				}
			}
			if (isChanged) {
				continue;
			} else {
				i++;
			}
		}
	}

	/**
	 * Algorithm: 1. In the cluster set, every cluster after the 1st one is
	 * compared with it separately to extend the common context as much as
	 * possible. 2. Record the common context with respect to the 1st one, while
	 * keeping track of correspondence between the 1st one and its corresponding
	 * cluster. All the information is kept in an intermediate
	 * EditInCommonCluster instance, which is cached for later use. 3. Intersect
	 * all clusters based on 1st one representations to find the common context.
	 */
	public void parseCommonContext() {
		List<EditInCommonCluster> level_1_clusters = ClusterHelper
				.getLevel_1_clusters(clusters);
		Map<Integer, BaseCluster> baseClusters = ClusterHelper
				.getBaseClusters(level_1_clusters);
		initBase(level_1_clusters);

		List<Sequence> sequenceList = null;
		List<EditInCommonCluster> high_clusters = level_1_clusters;
		Set<EditInCommonCluster> new_high_clusters = new HashSet<EditInCommonCluster>();
		AbstractCluster tmpClus = null;
		List<Integer> instances = new ArrayList<Integer>();
		List<List<Integer>> nodeIndexLists = null;
		List<Integer> insts = null;
		// System.out.print("");
		while (!high_clusters.isEmpty()) {
			for (EditInCommonCluster high_cluster : high_clusters) {
				nodeIndexLists = new ArrayList<List<Integer>>();
				insts = new ArrayList<Integer>();
				if (high_cluster.allSameInstances()) {
					tmpClus = high_cluster.getIncomings().get(0);
					high_cluster.setSequence(tmpClus.getSequence());
					sequenceList = new ArrayList<Sequence>();
					for (AbstractCluster aClus : high_cluster.getIncomings()) {
						sequenceList.add(aClus.getSequence());
					}
					high_cluster.setSequenceList(sequenceList);
					high_cluster.setSimpleASTNodesList(tmpClus
							.getSimpleASTNodesList());
					if (high_cluster.getOutgoings() != null)
						new_high_clusters.addAll(high_cluster.getOutgoings());
					continue;
				}
				if (level_1_clusters.contains(high_cluster)) {
					List<AbstractCluster> tmpSubClusters = null;
					// The reason why we want to comment out customize is that
					// in high_cluster, we still want to keep the original
					// version of the method
					List<List<List<SimpleASTNode>>> customizedSimpleASTNodesList2 = high_cluster
							.customize(baseClusters);

					tmpSubClusters = high_cluster.getIncomings();
					AbstractCluster bClus1 = tmpSubClusters.get(0);
					AbstractCluster bClus2 = null;
					List<EditInCommonCluster> interClusters = new ArrayList<EditInCommonCluster>();
					EditInCommonCluster interCluster = null;
					for (int i = 1; i < tmpSubClusters.size(); i++) {
						bClus2 = tmpSubClusters.get(i);
						instances.clear();
						instances.add(bClus1.getIndex());
						instances.add(bClus2.getIndex());
						interCluster = match(bClus1, bClus2,
								customizedSimpleASTNodesList2, high_cluster, 0,
								i);
						if (interCluster == null) {
							interClusters = null;
							break;
						} else {
							interClusters.add(interCluster);
						}
					}
					// align common parts by computing set intersection on the
					// same method
					if (interCluster != null) {
						CHelper4Aligner.alignCommonBasedOnSameInstance(
								interClusters, high_cluster, nodeIndexLists,
								insts);
						CHelper4Unifier.unifyCommon(interClusters,
								high_cluster, nodeIndexLists);
					} else {
						high_cluster.setApplicable(false);
					}
				} else {// assumption: bClus1 and bClus2 are EditInCommonCluster
					CHelper4Merger.merge(high_cluster);
				}
				if (high_cluster.getOutgoings() != null) {

					// high_cluster.setSequenceList(sequenceList);
					new_high_clusters.addAll(high_cluster.getOutgoings());
				}
			}
			high_clusters = new ArrayList<EditInCommonCluster>(
					new_high_clusters);
			Collections.sort(high_clusters);
			new_high_clusters.clear();
		}

	}

	private void computeIndexes(List<Integer> indexes,
			List<Integer> newIndexes, List<Integer> indexesToRemove,
			List<SimpleTreeNode> forest, List<Integer> nodeIndexes) {
		SimpleTreeNode tree = null;
		for (int i = 0; i < forest.size(); i++) {
			tree = forest.get(i);
			if (tree.getTypes().contains(SimpleTreeNode.INSERTED_CONTEXTUAL)) {
				indexesToRemove.add(i); // mark the inserted node and filter the
										// tree later
			} else {
				indexes.add(nodeIndexes.indexOf(tree.getNodeIndex()));
			}
		}
		newIndexes.addAll(indexes);
		Integer min = -1;
		int minJ = -1;
		for (int i = 0; i < newIndexes.size() - 1; i++) {
			min = newIndexes.get(i);
			minJ = i;
			for (int j = i + 1; j < newIndexes.size(); j++) {
				if (newIndexes.get(j) < min) {
					minJ = j;
					min = newIndexes.get(j);
				}
			}
			if (minJ != i) {// to swap the two values
				newIndexes.set(minJ, newIndexes.get(i));
				newIndexes.set(i, min);
			}
		}
	}

	private void initBase(List<EditInCommonCluster> level_1_clusters) {
		BaseCluster bClus = null;
		Set<Integer> processed = new HashSet<Integer>();
		List<SimpleTreeNode> sTrees = null;
		int tmpIndex = -1;
		for (EditInCommonCluster level_1_cluster : level_1_clusters) {
			sTrees = level_1_cluster.getSTrees();
			for (AbstractCluster aClus : level_1_cluster.getIncomings()) {
				tmpIndex = aClus.getIndex();
				if (aClus instanceof BaseCluster && processed.add(tmpIndex)) {
					bClus = (BaseCluster) aClus;
					bClus.setSTree(sTrees.get(level_1_cluster.getInstances()
							.indexOf(tmpIndex)));
					bClus.encodeSequence();
					// bClus.createSimpleASTNodesList();
				}
			}
		}
	}

	/**
	 * 
	 * @param s1
	 *            UNCHANGED
	 * @param s2
	 *            UNCHANGED
	 * @param sTree1
	 *            CHANGED to enable some "selected" marks
	 * @param sTree2
	 *            CHANGED to enable some "selected" marks
	 * @param simpleASTNodesList1
	 *            CHANGED to only include the mapped nodes
	 * @param simpleASTNodesList2
	 *            CHANGED to only include the mapped nodes
	 * @param specificToUnified1
	 *            CHANGED to include more maps
	 * @param unifiedToSpecific
	 *            CHANGED to include more maps
	 * @param specificToUnified2
	 *            CHANGED to include more maps
	 * @param forest1
	 *            UNCHANGED
	 * @param forest2
	 *            UNCHANGED
	 */
	private void initMatchMatrix(boolean hasForestOrder, Sequence s1,
			Sequence s2, SimpleTreeNode sTree1, SimpleTreeNode sTree2,
			List<List<SimpleASTNode>> simpleASTNodesList1,
			List<List<SimpleASTNode>> simpleASTNodesList2,
			Map<String, String> specificToUnified1, Map<String, String> uMap1,
			Map<String, String> unifiedToSpecific,
			Map<String, String> specificToUnified2, Map<String, String> uMap2,
			List<SimpleTreeNode> forest1, List<SimpleTreeNode> forest2,
			Map<Integer, Integer> forestMatchSet, List<SimpleTreeNode> sNodes1,
			List<SimpleTreeNode> sNodes2) {
		List<Integer> subIndexList1 = new ArrayList<Integer>(), subIndexList2 = new ArrayList<Integer>();
		String oldVersion = null, newVersion = null;
		RunCCFinder runner = new RunCCFinder();
		boolean isGeneral1 = false, isGeneral2 = false;
		matchSet = new HashMap<Integer, Integer>();
		blackMatchSet = new HashMap<Integer, Integer>();
		Enumeration<SimpleTreeNode> sEnum1 = null, sEnum2 = null;
		SimpleTreeNode sNode1 = null, sNode2 = null, leftMostNode1 = null, rightMostNode1 = null, leftMostNode2 = null, rightMostNode2 = null;
		SimpleTreeNode tree1 = null, tree2 = null, tree3 = null, tree4 = null;
		int i = 0, j = 0;
		if (forest1 != null && forest2 != null) {// match the forest no matter
													// whether the trees are in
													// order
			for (int k = 0; k < forest1.size(); k++) {
				tree1 = forest1.get(k);
				tree2 = forest2.get(k);
				sEnum1 = tree1.depthFirstEnumeration();
				sEnum2 = tree2.depthFirstEnumeration();
				while (sEnum1.hasMoreElements()) {
					sNode1 = sEnum1.nextElement();
					sNode2 = sEnum2.nextElement();
					if (!sNode1.getTypes().contains(
							SimpleTreeNode.INSERTED_CONTEXTUAL)) {
						matchSet.put(sNode1.getNodeIndex(),
								sNode2.getNodeIndex());
						forestMatchSet.put(sNode1.getNodeIndex(),
								sNode2.getNodeIndex());
					}
				}
			}
		}
		tree1 = sTree1;
		tree3 = sTree2;
		sEnum1 = sTree1.preorderEnumeration();
		sEnum2 = sTree2.preorderEnumeration();
		rightMostNode1 = tree1;
		rightMostNode2 = tree3;
		if (hasForestOrder) {
			// align the two code fragments based on edited forests
			// System.out.print("");
			// find the rightMostNode1 in the enumeration
			while (sEnum1.hasMoreElements()) {
				sNode1 = sEnum1.nextElement();
				if (sNode1.equals(rightMostNode1)) {
					break;
				}
			}

			isGeneral1 = sTree1.getGeneralMark();
			isGeneral2 = sTree2.getGeneralMark();
			// find the rightMostNode2 in the enumeration
			while (sEnum2.hasMoreElements()) {
				sNode2 = sEnum2.nextElement();
				if (sNode2.equals(rightMostNode2))
					break;
			}
			while (i < forest1.size()) {
				tree2 = forest1.get(i);
				tree4 = forest2.get(i);
				if (tree2.getTypes().contains(
						SimpleTreeNode.INSERTED_CONTEXTUAL)
						|| tree4.getTypes().contains(
								SimpleTreeNode.INSERTED_CONTEXTUAL)) {
					i++;
					continue;
				}
				if (!tree1.equals(sTree1))
					rightMostNode1 = (SimpleTreeNode) tree1.getLastLeaf();
				leftMostNode1 = tree2;
				if (!tree3.equals(sTree2))
					rightMostNode2 = (SimpleTreeNode) tree3.getLastLeaf();
				leftMostNode2 = tree4;
				// System.out.print("");
				if (!rightMostNode1.equals(leftMostNode1)
						&& !rightMostNode2.equals(leftMostNode2)) {
					subIndexList1.clear();
					subIndexList2.clear();
					oldVersion = createTmpFile(rightMostNode1, leftMostNode1,
							sEnum1, subIndexList1, simpleASTNodesList1,
							"tmp1.java", isGeneral1);
					newVersion = createTmpFile(rightMostNode2, leftMostNode2,
							sEnum2, subIndexList2, simpleASTNodesList2,
							"tmp2.java", isGeneral2);
					runner.clear();
					runner.runCcfinder(oldVersion, newVersion);
					addMatchPairs(runner.getNewPairList(), simpleASTNodesList1,
							simpleASTNodesList2, subIndexList1, subIndexList2,
							specificToUnified1, unifiedToSpecific,
							specificToUnified2);
				}
				tree1 = tree2;
				tree3 = tree4;
				i++;
			}
		} else {
			tree1 = sTree1;
			tree3 = sTree2;
		}

		if (!tree1.getTypes().contains(SimpleTreeNode.INSERTED_CONTEXTUAL)
				&& !tree3.getTypes().contains(
						SimpleTreeNode.INSERTED_CONTEXTUAL)) {
			tree2 = (SimpleTreeNode) sTree1.getLastLeaf();
			tree4 = (SimpleTreeNode) sTree2.getLastLeaf();
			if (tree1.equals(sTree1))
				rightMostNode1 = tree1;
			else
				rightMostNode1 = (SimpleTreeNode) tree1.getLastLeaf();
			leftMostNode1 = tree2;
			if (tree3.equals(sTree2))
				rightMostNode2 = tree3;
			else
				rightMostNode2 = (SimpleTreeNode) tree3.getLastLeaf();
			leftMostNode2 = tree4;
			if (!rightMostNode1.equals(leftMostNode1)
					&& !rightMostNode2.equals(leftMostNode2)) {
				subIndexList1.clear();
				subIndexList2.clear();
				oldVersion = createTmpFile(rightMostNode1, leftMostNode1,
						sEnum1, subIndexList1, simpleASTNodesList1,
						"tmp1.java", isGeneral1);
				newVersion = createTmpFile(rightMostNode2, leftMostNode2,
						sEnum2, subIndexList2, simpleASTNodesList2,
						"tmp2.java", isGeneral2);
				runner.clear();
				runner.runCcfinder(oldVersion, newVersion);
				addMatchPairs(runner.getNewPairList(), simpleASTNodesList1,
						simpleASTNodesList2, subIndexList1, subIndexList2,
						specificToUnified1, unifiedToSpecific,
						specificToUnified2);
			}
		}
		List<List<SimpleASTNode>> tmpNodesList = null;
		tmpNodesList = SimpleASTNode.customize(specificToUnified1, uMap1,
				simpleASTNodesList1);
		simpleASTNodesList1.clear();
		simpleASTNodesList1.addAll(tmpNodesList);

		String key = null;
		for (Entry<String, String> entry : unifiedToSpecific.entrySet()) {
			key = entry.getValue();
			if (!specificToUnified2.containsKey(key))
				specificToUnified2.put(key, entry.getKey());
		}
		tmpNodesList = SimpleASTNode.customize(specificToUnified2, uMap2,
				simpleASTNodesList2);
		simpleASTNodesList2.clear();
		simpleASTNodesList2.addAll(tmpNodesList);

		matchMatrix = new double[simpleASTNodesList1.size()][simpleASTNodesList2
				.size()];
		for (i = 0; i < simpleASTNodesList1.size(); i++) {
			for (j = 0; j < simpleASTNodesList2.size(); j++) {
				matchMatrix[i][j] = -1;
			}
		}
		int index1 = -1, index2 = -1;
		int seqInd1 = -1, seqInd2 = -1;
		int nodeInd1 = -1, nodeInd2 = -1;
		for (Entry<Integer, Integer> match : matchSet.entrySet()) {
			index1 = match.getKey();
			index2 = match.getValue();
			seqInd1 = s1.indexOf(index1);
			seqInd2 = s2.indexOf(index2);
			for (i = seqInd1; i < s1.getNodeIndexes().size(); i++) {
				nodeInd1 = s1.get(i);
				if (nodeInd1 < 0)
					continue;
				for (j = 0; j <= seqInd2; j++) {
					nodeInd2 = s2.get(j);
					if (nodeInd2 < 0)
						continue;
					matchMatrix[nodeInd1 - 1][nodeInd2 - 1] = 0;
				}
			}
			for (i = 0; i <= seqInd1; i++) {
				nodeInd1 = s1.get(i);
				if (nodeInd1 < 0)
					continue;
				for (j = seqInd2; j < s2.getNodeIndexes().size(); j++) {
					nodeInd2 = s2.get(j);
					if (nodeInd2 < 0)
						continue;
					matchMatrix[nodeInd1 - 1][nodeInd2 - 1] = 0;
				}
			}
			matchMatrix[index1 - 1][index2 - 1] = 1;
		}
		List<SimpleASTNode> simpleNodesList1 = null, simpleNodesList2 = null;
		boolean isMatched = true;
		for (i = 0; i < simpleASTNodesList1.size(); i++) {
			for (j = 0; j < simpleASTNodesList2.size(); j++) {
				if (matchMatrix[i][j] == -1) {
					// double tmp = calcSimilarity(simpleASTNodesList1.get(i),
					// simpleASTNodesList2.get(j), specificToUnified1,
					// unifiedToSpecific);
					// if(tmp > 0.9)
					// matchMatrix[i][j] = 1;
					// else
					// matchMatrix[i][j] = 0;
					simpleNodesList1 = simpleASTNodesList1.get(i);
					simpleNodesList2 = simpleASTNodesList2.get(j);
					if (!simpleNodesList1.isEmpty()
							&& simpleNodesList1.size() == simpleNodesList2
									.size()) {
						isMatched = true;
						for (int m = 0; m < simpleNodesList1.size(); m++) {
							if (!simpleNodesList1
									.get(m)
									.getStrValue()
									.equals(simpleNodesList2.get(m)
											.getStrValue())) {
								isMatched = false;
								break;
							}
						}
						if (isMatched) {
							matchMatrix[i][j] = 1;
						} else {
							matchMatrix[i][j] = 0;
						}
					} else {
						String tmpStr1 = sNodes1.get(i).getStrValue();
						String tmpStr2 = sNodes2.get(j).getStrValue();
						if (tmpStr1.equals(tmpStr2)
								&& (tmpStr1.equals("return:")
										|| tmpStr1.equals("continue:")
										|| tmpStr1.equals("break:") || tmpStr1
										.equals("case:"))) {
							matchMatrix[i][j] = 1;
						} else
							matchMatrix[i][j] = 0;
					}
				}
			}
		}
	}

	private void initMatchMatrix(Sequence s1, Sequence s2,
			List<List<SimpleASTNode>> simpleASTNodesList1,
			List<List<SimpleASTNode>> simpleASTNodesList2,
			Map<String, String> specificToUnified1, Map<String, String> uMap1,
			Map<String, String> unifiedToSpecific,
			Map<String, String> specificToUnified2, Map<String, String> uMap2,
			List<SimpleTreeNode> sNodes1, List<SimpleTreeNode> sNodes2) {
		matchSet = new HashMap<Integer, Integer>();

		List<List<SimpleASTNode>> tmpNodesList = null;
		tmpNodesList = SimpleASTNode.customize(specificToUnified1, uMap1,
				simpleASTNodesList1);
		simpleASTNodesList1.clear();
		simpleASTNodesList1.addAll(tmpNodesList);
		String key = null;
		for (Entry<String, String> entry : unifiedToSpecific.entrySet()) {
			key = entry.getValue();
			if (!specificToUnified2.containsKey(key))
				specificToUnified2.put(key, entry.getKey());
		}
		tmpNodesList = SimpleASTNode.customize(specificToUnified2, uMap2,
				simpleASTNodesList2);
		simpleASTNodesList2.clear();
		simpleASTNodesList2.addAll(tmpNodesList);

		matchMatrix = new double[simpleASTNodesList1.size()][simpleASTNodesList2
				.size()];
		for (int i = 0; i < simpleASTNodesList1.size(); i++) {
			for (int j = 0; j < simpleASTNodesList2.size(); j++) {
				matchMatrix[i][j] = -1;
			}
		}
		int index1 = -1, index2 = -1;
		int seqInd1 = -1, seqInd2 = -1;
		int nodeInd1 = -1, nodeInd2 = -1;
		for (Entry<Integer, Integer> match : matchSet.entrySet()) {
			index1 = match.getKey();
			index2 = match.getValue();
			seqInd1 = s1.indexOf(index1);
			seqInd2 = s2.indexOf(index2);
			for (int i = seqInd1; i < s1.getNodeIndexes().size(); i++) {
				nodeInd1 = s1.get(i);
				if (nodeInd1 < 0)
					continue;
				for (int j = 0; j <= seqInd2; j++) {
					nodeInd2 = s2.get(j);
					if (nodeInd2 < 0)
						continue;
					matchMatrix[nodeInd1 - 1][nodeInd2 - 1] = 0;
				}
			}
			for (int i = 0; i <= seqInd1; i++) {
				nodeInd1 = s1.get(i);
				if (nodeInd1 < 0)
					continue;
				for (int j = seqInd2; j < s2.getNodeIndexes().size(); j++) {
					nodeInd2 = s2.get(j);
					if (nodeInd2 < 0)
						continue;
					matchMatrix[nodeInd1 - 1][nodeInd2 - 1] = 0;
				}
			}
			matchMatrix[index1 - 1][index2 - 1] = 1;
		}
		List<SimpleASTNode> simpleNodesList1 = null, simpleNodesList2 = null;
		boolean isMatched = true;
		for (int i = 0; i < simpleASTNodesList1.size(); i++) {
			for (int j = 0; j < simpleASTNodesList2.size(); j++) {
				if (matchMatrix[i][j] == -1) {
					// double tmp = calcSimilarity(simpleASTNodesList1.get(i),
					// simpleASTNodesList2.get(j), specificToUnified1,
					// unifiedToSpecific);
					// if(tmp > 0.9)
					// matchMatrix[i][j] = 1;
					// else
					// matchMatrix[i][j] = 0;
					simpleNodesList1 = simpleASTNodesList1.get(i);
					simpleNodesList2 = simpleASTNodesList2.get(j);
					if (!simpleNodesList1.isEmpty()
							&& simpleNodesList1.size() == simpleNodesList2
									.size()) {
						isMatched = true;
						for (int m = 0; m < simpleNodesList1.size(); m++) {
							if (!simpleNodesList1
									.get(m)
									.getStrValue()
									.equals(simpleNodesList2.get(m)
											.getStrValue())) {
								isMatched = false;
								break;
							}
						}
						if (isMatched) {
							matchMatrix[i][j] = 1;
						} else {
							matchMatrix[i][j] = 0;
						}
					} else {
						String tmpStr1 = sNodes1.get(i).getStrValue();
						String tmpStr2 = sNodes2.get(j).getStrValue();
						if (tmpStr1.equals(tmpStr2)
								&& (tmpStr1.equals("return:")
										|| tmpStr1.equals("continue:") || tmpStr1
										.equals("break:"))) {
							matchMatrix[i][j] = 1;
						} else
							matchMatrix[i][j] = 0;
					}
				}
			}
		}
	}

	private EditInCommonCluster match(AbstractCluster bClus1,
			AbstractCluster bClus2,
			List<List<List<SimpleASTNode>>> customizedSimpleASTNodesList2,
			EditInCommonCluster high_cluster, int index1, int index2) {
		EditInCommonCluster cluster = new EditInCommonCluster(
				high_cluster.getConChgSum(), high_cluster.getChgSumStr(),
				high_cluster.getAbsChgSumStr());
		// System.out.print("");
		Map<String, String> specificToUnified1 = new HashMap<String, String>(
				high_cluster.getSpecificToUnifiedList().get(index1)), specificToUnified2 = new HashMap<String, String>(
				high_cluster.getSpecificToUnifiedList().get(index2)), unifiedToSpecific = new HashMap<String, String>(
				high_cluster.getUnifiedToSpecificList().get(index2));
		List<List<SimpleASTNode>> simpleASTNodesList1 = ContextualizationUtil
				.getCopy(customizedSimpleASTNodesList2.get(index1));
		List<List<SimpleASTNode>> simpleASTNodesList2 = ContextualizationUtil
				.getCopy(customizedSimpleASTNodesList2.get(index2));
		SimpleTreeNode sTree1 = new SimpleTreeNode(bClus1.getSTree()), sTree2 = new SimpleTreeNode(
				bClus2.getSTree());
		List<SimpleTreeNode> sNodes1 = new ArrayList<SimpleTreeNode>();
		List<SimpleTreeNode> sNodes2 = new ArrayList<SimpleTreeNode>();
		Enumeration<SimpleTreeNode> sEnum = sTree1.breadthFirstEnumeration();
		while (sEnum.hasMoreElements()) {
			sNodes1.add(sEnum.nextElement());
		}
		sEnum = sTree2.breadthFirstEnumeration();
		while (sEnum.hasMoreElements()) {
			sNodes2.add(sEnum.nextElement());
		}
		Sequence result1 = null, result2 = null;
		// if(high_cluster.checkForestOrder()){
		Sequence s1 = bClus1.getSequence(), s2 = bClus2.getSequence();
		typeTermMap1 = bClus1.getTypeTermMap();
		typeTermMap2 = bClus2.getTypeTermMap();
		blackIdentifierMap = new HashMap<String, Set<String>>();
		blackIdentifierMap2 = new HashMap<String, Set<String>>();

		List<List<SimpleTreeNode>> forests = high_cluster.getForests();
		List<SimpleTreeNode> forest1 = null, forest2 = null;
		if (forests != null && forests.size() >= 2) {
			forest1 = high_cluster.getForests().get(index1);
			forest2 = high_cluster.getForests().get(index2);
		}

		Map<Integer, Integer> forestMatchSet = new HashMap<Integer, Integer>();
		Map<String, String> uMap1 = SimpleASTNode
				.createUMap(specificToUnified1);
		Map<String, String> uMap2 = SimpleASTNode
				.createUMap(specificToUnified2);
		if (bClus1 instanceof BaseCluster && bClus2 instanceof BaseCluster) {
			// the match is initialized with assistance of CCFinder, and the
			// result is filtered to improve accuracy
			TermsList.prLeft = CachedProjectMap.get(((BaseCluster) bClus1)
					.getMM().originalMethod.getProjectName());
			TermsList.prRight = CachedProjectMap.get(((BaseCluster) bClus2)
					.getMM().originalMethod.getProjectName());
			initMatchMatrix(high_cluster.checkForestOrder(), s1, s2, sTree1,
					sTree2, simpleASTNodesList1, simpleASTNodesList2,
					specificToUnified1, uMap1, unifiedToSpecific,
					specificToUnified2, uMap2, forest1, forest2,
					forestMatchSet, sNodes1, sNodes2);
		} else {// assumption: all clusters should be EditInCommonCluster or
				// BaseCluster
				// match and add matching pairs while filtering conflict
			initMatchMatrix(s1, s2, simpleASTNodesList1, simpleASTNodesList2,
					specificToUnified1, uMap1, unifiedToSpecific,
					specificToUnified2, uMap2, sNodes1, sNodes2);
		}
		result1 = new Sequence(new ArrayList<Integer>());
		result2 = new Sequence(new ArrayList<Integer>());

		// MATCH
		dictionary = new HashMap<String, Pair<Sequence>>();
		computeLCSSequence(s1, s2, result1, result2, matchSet);
		List<Integer> indexes1 = new ArrayList<Integer>(
				result1.getNodeIndexes());
		List<Integer> indexes2 = new ArrayList<Integer>(
				result2.getNodeIndexes());
		// System.out.print("");
		// check inclusion of all forest matching pairs
		boolean includeAll = true;
		int i1 = -1, i2 = -1;
		for (Entry<Integer, Integer> match : forestMatchSet.entrySet()) {
			int key = match.getKey();
			int value = match.getValue();
			i1 = indexes1.indexOf(key);
			i2 = indexes2.indexOf(value);
			if (i1 != -1 && i2 == i1) {
				continue;
			} else if (i1 == -1 && i2 == i1) {
				if (simpleASTNodesList1.get(key - 1).isEmpty()
						&& simpleASTNodesList2.get(value - 1).isEmpty()) {
					SimpleTreeNode sNode1 = sNodes1.get(key - 1);
					SimpleTreeNode sNode2 = sNodes2.get(value - 1);
					if (sNode1.getNodeType() == ASTNode.BLOCK
							&& sNode2.getNodeType() == ASTNode.BLOCK) {
						if (updateIndexes(indexes1, indexes2,
								s1.getNodeIndexes(), s2.getNodeIndexes(), key,
								value))
							continue;
						else {
							includeAll = false;
							break;
						}
					}
				} else {
					includeAll = false;
					break;
				}
			} else {
				includeAll = false;
				break;
			}
		}

		if (!includeAll) {
			if (high_cluster.checkForestOrder())
				return null;
			else { // high_cluster has a random forest order
				indexes1.clear();// the found indexes do not cover all matched
									// indexes
				indexes2.clear();
				for (Entry<Integer, Integer> match : forestMatchSet.entrySet()) {
					indexes1.add(match.getKey());
					indexes2.add(match.getValue());
				}
			}
		} else {
			// ADD MATCHING PAIRS WHILE FILTERING CONFLICT
			addMatchPairs(simpleASTNodesList1, simpleASTNodesList2, indexes1,
					indexes2, specificToUnified1, unifiedToSpecific,
					specificToUnified2);
			if (!forestMatchSet.isEmpty())
				cluster.enableForestOrder();
			List<List<SimpleASTNode>> originalList1 = customizedSimpleASTNodesList2
					.get(index1);
			simpleASTNodesList1 = SimpleASTNode
					.customize(specificToUnified1,
							SimpleASTNode.createUMap(specificToUnified1),
							originalList1);
			List<List<SimpleASTNode>> originalList2 = customizedSimpleASTNodesList2
					.get(index2);
			simpleASTNodesList2 = SimpleASTNode
					.customize(specificToUnified2,
							SimpleASTNode.createUMap(specificToUnified2),
							originalList2);
		}

		removeRedundantIndexes(indexes1, indexes2);
		result1 = new Sequence(new ArrayList<Integer>(indexes1));
		result2 = new Sequence(new ArrayList<Integer>(indexes2));

		Collections.sort(indexes1);
		Collections.sort(indexes2);

		// this is the updated version of the common sequence
		updateSimpleASTNodesList(simpleASTNodesList1, indexes1);
		updateSTree(sTree1, indexes1);

		updateSimpleASTNodesList(simpleASTNodesList2, indexes2);
		updateSTree(sTree2, indexes2);

		// add sub-clusters + term maps

		cluster.addIncomingSimple(bClus1,
				high_cluster.getIndexesList().get(index1), specificToUnified1);
		cluster.addIncomingSimple(bClus2,
				high_cluster.getIndexesList().get(index2), specificToUnified2);
		// add sequences
		cluster.addSequence(result1);
		cluster.addSequence(result2);
		// add simpleASTNodesList
		cluster.addSimpleASTNodesList(simpleASTNodesList1);
		cluster.addSimpleASTNodesList(simpleASTNodesList2);
		// add sTrees
		cluster.addSTree(sTree1);
		cluster.addSTree(sTree2);
		// set sTree
		cluster.setSTree(new SimpleTreeNode(sTree1));
		cluster.setSequence(result1);

		// set simpleASTNodesList
		cluster.setSimpleASTNodesList(simpleASTNodesList1);
		return cluster;
	}

	// private Sequence match(Sequence s1, Sequence s2,
	// SimpleTreeNode sTree1, SimpleTreeNode sTree2,
	// List<List<SimpleASTNode>> simpleASTNodesList1,
	// List<List<SimpleASTNode>> simpleASTNodesList2,
	// Map<String, String> specificToUnified1,
	// Map<String, String> specificToUnified2,
	// Map<String, String> unifiedToSpecific,
	// List<Sequence> sequenceList, List<List<SimpleASTNode>>
	// simpleASTNodesList,
	// List<SimpleTreeNode> forest1,
	// List<SimpleTreeNode> forest2){
	// System.out.print("");
	// initMatchMatrix(s1, s2,
	// sTree1, sTree2,
	// simpleASTNodesList1, simpleASTNodesList2,
	// specificToUnified1, unifiedToSpecific,
	// specificToUnified2, forest1, forest2);
	// dictionary = new HashMap<String, Pair<Sequence>>();
	// Sequence result = new Sequence(new ArrayList<Integer>());
	// Sequence result1 = new Sequence(new ArrayList<Integer>());
	// Sequence result2 = new Sequence(new ArrayList<Integer>());
	// computeLCS(s1, s2, result1, result2);
	// List<Integer> indexes1 = new
	// ArrayList<Integer>(result1.getNodeIndexes());
	// List<Integer> indexes2 = new
	// ArrayList<Integer>(result2.getNodeIndexes());
	//
	// sequenceList.add(result1);
	// sequenceList.add(result2);
	//
	// //check inclusion of all forest matching pairs
	// boolean includeAll = true;
	// int i1 = -1, i2 = -1;
	// for(Entry<Integer, Integer> match : matchSet.entrySet()){
	// i1 = indexes1.indexOf(match.getKey());
	// i2 = indexes2.indexOf(match.getValue());
	// if(i1 != -1 && i2 == i1){
	// continue;
	// }else{
	// includeAll = false;
	// break;
	// }
	// }
	//
	// if(!includeAll){
	// return null;
	// }
	//
	// Collections.sort(indexes1);
	// Collections.sort(indexes2);
	//
	// updateSimpleASTNodesList(simpleASTNodesList, indexes1);
	// updateSTree(sTree1, indexes1);
	//
	// List<List<SimpleASTNode>> simpleASTNodesList3 = new
	// ArrayList<List<SimpleASTNode>>();
	// for(List<SimpleASTNode> nodes : simpleASTNodesList2){
	// simpleASTNodesList3.add(nodes);
	// }
	// updateSimpleASTNodesList(simpleASTNodesList3, indexes2);
	//
	// result.add(result1);
	// return result;
	// }

	private String createTmpFile(SimpleTreeNode rightMostNode,
			SimpleTreeNode leftMostNode, Enumeration<SimpleTreeNode> sEnum,
			List<Integer> subIndexList,
			List<List<SimpleASTNode>> simpleASTNodesList, String fileName,
			boolean isGeneral) {
		String path = PathUtil.createPath(fileName);
		StringBuffer buffer = new StringBuffer();
		SimpleTreeNode sNode = null;
		int counter = 0;
		List<SimpleASTNode> simpleASTNodes = null;
		String typeStr = null;
		boolean needNewLine = true;
		if (isGeneral) {
			while (sEnum.hasMoreElements()) {
				sNode = sEnum.nextElement();
				if (sNode.equals(leftMostNode))// until we find the leftMostNode
					break;
				if (sNode.getSelectedMark())
					subIndexList.add(sNode.getNodeIndex() - 1);
				if (counter++ != 0 && needNewLine)
					buffer.append("\n");
				else if (!needNewLine)
					needNewLine = true;
				simpleASTNodes = simpleASTNodesList
						.get(sNode.getNodeIndex() - 1);
				typeStr = sNode.getStrValue();
				typeStr = typeStr.substring(0, typeStr.indexOf(':') + 1);
				buffer.append(typeStr);
				for (int k = 0; k < simpleASTNodes.size(); k++) {
					if (k != 0)
						buffer.append(";");
					buffer.append(simpleASTNodes.get(k));
				}
			}
		} else {
			// System.out.print("");
			while (sEnum.hasMoreElements()) {
				sNode = sEnum.nextElement();
				if (sNode.equals(leftMostNode))// until we find the leftMostNode
					break;
				subIndexList.add(sNode.getNodeIndex() - 1);
				if (counter++ != 0 && needNewLine)
					buffer.append("\n");
				else if (!needNewLine)
					needNewLine = true;
				simpleASTNodes = simpleASTNodesList
						.get(sNode.getNodeIndex() - 1);
				typeStr = sNode.getStrValue();
				typeStr = typeStr.substring(0, typeStr.indexOf(':') + 1);
				buffer.append(typeStr);
				for (int k = 0; k < simpleASTNodes.size(); k++) {
					if (k != 0)
						buffer.append(";");
					buffer.append(simpleASTNodes.get(k).getStrValue().trim());
				}
			}
		}
		FileWriter fstream;
		try {
			fstream = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(buffer.toString());
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}

	private void removeRedundantIndexes(List<Integer> indexes1,
			List<Integer> indexes2) {
		if (blackMatchSet.isEmpty())
			return;
		Integer left = null, right = null;
		for (Entry<Integer, Integer> match : blackMatchSet.entrySet()) {
			left = match.getKey();
			right = match.getValue();
			if (indexes1.contains(left) && indexes2.contains(right)) {
				indexes1.remove(left);
				indexes1.remove(Integer.valueOf(-left));
				indexes2.remove(right);
				indexes2.remove(Integer.valueOf(-right));
			}
		}
	}

	private boolean updateIndexes(List<Integer> indexes1,
			List<Integer> indexes2, List<Integer> originalIndexes1,
			List<Integer> originalIndexes2, int index1, int index2) {
		int locationInOriginal1 = originalIndexes1.indexOf(index1);
		int locationInOriginal2 = originalIndexes2.indexOf(index2);

		List<Integer> sub1 = originalIndexes1.subList(0, locationInOriginal1);
		List<Integer> sub2 = originalIndexes2.subList(0, locationInOriginal2);
		// search in indexes1 to see how many are covered by sub1 to location
		// the position to insert new index
		int counterBefore1 = 0;
		for (int i = 0; i < sub1.size(); i++) {
			if (indexes1.contains(sub1.get(i))) {
				counterBefore1++;
			}
		}
		int counterBefore2 = 0;
		for (int i = 0; i < sub2.size(); i++) {
			if (indexes2.contains(sub2.get(i))) {
				counterBefore2++;
			}
		}
		if (counterBefore1 != counterBefore2)
			return false;
		locationInOriginal1 = originalIndexes1.indexOf(-index1);
		locationInOriginal2 = originalIndexes2.indexOf(-index2);
		if (locationInOriginal1 != -1 && locationInOriginal2 != -1) {
			int counterAfter1 = 0;
			int counterAfter2 = 0;
			sub1 = originalIndexes1.subList(locationInOriginal1,
					originalIndexes1.size());
			sub2 = originalIndexes2.subList(locationInOriginal2,
					originalIndexes2.size());
			for (int i = sub1.size() - 1; i >= 0; i--) {
				if (indexes1.contains(sub1.get(i))) {
					counterAfter1++;
				}
			}
			for (int i = sub2.size() - 1; i >= 0; i--) {
				if (indexes2.contains(sub2.get(i))) {
					counterAfter2++;
				}
			}
			if (counterAfter1 != counterAfter2) {
				return false;
			}
			indexes1.add(counterBefore1, index1);
			indexes2.add(counterBefore2, index2);
			indexes1.add(indexes1.size() - counterAfter1, -index1);
			indexes2.add(indexes2.size() - counterAfter2, -index2);
		} else if (locationInOriginal1 == -1 && locationInOriginal2 == -1) {
			indexes1.add(counterBefore1, index1);
			indexes2.add(counterBefore2, index2);
		} else {
			return false;
		}
		return true;
	}

	private void updateSimpleASTNodesList(
			List<List<SimpleASTNode>> simpleASTNodesList, List<Integer> indexes) {
		int counter = 0;
		for (Integer index : indexes) {
			if (index < 0)
				continue;
			if (counter < index) {
				for (int j = counter; j < index - 1; j++) {
					simpleASTNodesList.get(j).clear();
					counter++;
				}
				counter++;// the one indexed as (index - 1) is kept
			}
		}
		while (counter < simpleASTNodesList.size()) {
			simpleASTNodesList.get(counter).clear();
			counter++;
		}
	}

	/**
	 * To reorder the forests in a level_1_cluster based on pre-order traversal
	 * manner Purpose: when trancating the sequence, we traverse the forest in
	 * the left-to-right manner
	 * 
	 * @param cluster
	 */
	private void reorderForests(EditInCommonCluster cluster,
			Map<Integer, BaseCluster> baseClusters) {
		List<List<SimpleTreeNode>> forests = cluster.getForests();
		if (forests == null) {
			System.out.println("The forests are null");
			return;
		} else if (forests.isEmpty()) {
			cluster.enableForestOrder();
			return;
		}
		// System.out.print("");
		List<Sequence> sequenceList = new ArrayList<Sequence>();
		for (Integer inst : cluster.getInstances()) {
			sequenceList.add(baseClusters.get(inst).getSequence());
		}

		Sequence sequence = cluster.getIncomings().get(0).getSequence();
		if (sequence == null) { // if there is no indexes available to order the
								// forests, return
			cluster.enableForestOrder();
			return;
		}

		List<SimpleTreeNode> forest1 = forests.get(0);
		List<List<SimpleTreeNode>> newForests = new ArrayList<List<SimpleTreeNode>>();
		List<Integer> indexes = null, newIndexes = null;
		List<Integer> indexes2 = null, newIndexes2 = null;
		List<Integer> indexesToRemove = null, indexesToRemove2 = null;
		boolean canBeOrdered = true;

		indexes = new ArrayList<Integer>();
		newIndexes = new ArrayList<Integer>();
		indexesToRemove = new ArrayList<Integer>();
		computeIndexes(indexes, newIndexes, indexesToRemove, forest1,
				sequenceList.get(0).getNodeIndexes());

		List<SimpleTreeNode> forest2 = null, forest3 = null, newForest2 = null, newForest3 = null;
		for (int i = 0; i < forests.size(); i++) {// 1. to remove the inserted
													// trees
			forest1 = forests.get(i);
			if (!indexesToRemove.isEmpty()) {
				forest2 = new ArrayList<SimpleTreeNode>();
				for (int j = 0; j < forest1.size(); j++) {
					if (!indexesToRemove.contains(j)) {
						forest2.add(forest1.get(j));
					}
				}
			} else {
				forest2 = forest1;
			}
			newForest2 = new ArrayList<SimpleTreeNode>();
			for (int j = 0; j < indexes.size(); j++) {
				newForest2.add(forest2.get(indexes.indexOf(newIndexes.get(j))));
			}

			if (canBeOrdered && i != 0) {
				// newForest3 is computed to double check whether the ordered
				// sequence is consistent between 1st forest and other forests
				indexesToRemove2 = new ArrayList<Integer>();
				indexes2 = new ArrayList<Integer>();
				newIndexes2 = new ArrayList<Integer>();
				computeIndexes(indexes2, newIndexes2, indexesToRemove2,
						forest1, sequenceList.get(i).getNodeIndexes());
				if (!indexesToRemove2.isEmpty()) {
					forest3 = new ArrayList<SimpleTreeNode>();
					for (int j = 0; j < forest1.size(); j++) {
						if (!indexesToRemove2.contains(j)) {
							forest3.add(forest1.get(j));
						}
					}
				} else {
					forest3 = forest1;
				}
				newForest3 = new ArrayList<SimpleTreeNode>();
				for (int j = 0; j < indexes2.size(); j++) {
					newForest3.add(forest3.get(indexes2.indexOf(newIndexes2
							.get(j))));
				}

				if (!newForest2.equals(newForest3)) {
					canBeOrdered = false;
				}
			}
			newForests.add(newForest2);
		}

		forests.clear();
		forests.addAll(newForests);
		if (canBeOrdered) {
			cluster.enableForestOrder();
		}
	}

	private void updateSTree(SimpleTreeNode sTree, List<Integer> indexes) {
		sTree.enableGeneral();
		Enumeration<SimpleTreeNode> sEnum = sTree.breadthFirstEnumeration();
		SimpleTreeNode sNode = null;
		while (sEnum.hasMoreElements()) {
			sNode = sEnum.nextElement();
			if (indexes.contains(sNode.getNodeIndex())) {
				sNode.enableSelected();
			} else {
				sNode.disableSelected();
			}
		}
	}
}
