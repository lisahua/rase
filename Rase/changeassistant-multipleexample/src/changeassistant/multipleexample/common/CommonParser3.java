package changeassistant.multipleexample.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.multipleexample.contextualize.CHelper4Unifier;
import changeassistant.multipleexample.partition.MappingException;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.util.IdMapper;

public class CommonParser3 {

	private int abstractStmt = 0;
	private int abstractMethod = 0;
	private int abstractVariable = 0;
	private int abstractType = 0;
	private int abstractUnknown = 0;

	private Map<String, String> s1Tou = null;
	private Map<String, String> uTos1 = null;
	private List<Map<String, String>> s2Tous = null;
	private List<Map<String, String>> uTos2s = null;

	private EqualCalculator<Integer> integerComparator = new EqualCalculator<Integer>();

	private String getAbstractIdentifier(int nodeType1,
			List<Integer> nodeType2s, List<String> abstractIdentifiers) {
		StringBuffer uniName = new StringBuffer(Term.createAbstractName(
				ASTExpressionTransformer.ABSTRACT_STATEMENT, abstractStmt++));
		uniName.append(Term.createStmtSuffix(nodeType1, nodeType2s,
				abstractIdentifiers));
		return uniName.toString();
	}

	/**
	 * Two purposes: 1. when two nodes have different types, a wildcard is
	 * created to represent them both; 2. when two nodes are of the same type
	 * but only differ in identifiers used, unified identifiers are created to
	 * represent the different identifiers The function can return null if the
	 * map is not consistent
	 * 
	 * @param simpleASTNodesList
	 *            -- the 1st simpleASTNodesList
	 * @param simpleASTNodesListList
	 *            --all simpleASTNodesList except for the 1st one
	 * @param nodeIndexArray
	 * @param nodeTypeArray
	 * @return
	 */
	public List<List<SimpleASTNode>> getCommonStatement(
			EditInCommonCluster high_cluster,
			Map<String, String> inter_unifiedToSpecific,
			List<Map<String, String>> inter_unifiedToSpecific_list,
			Map<String, String> inter_specificToUnified,
			List<Map<String, String>> inter_specificToUnified_list,
			List<List<SimpleASTNode>> simpleASTNodesList,
			List<List<List<SimpleASTNode>>> simpleASTNodes1ListList,
			List<List<List<SimpleASTNode>>> simpleASTNodes2ListList,
			List<List<Integer>> nodeIndexLists,
			List<List<Integer>> nodeTypeLists) {
		Integer[][] nodeIndexArray = new Integer[nodeIndexLists.get(0).size()][nodeIndexLists
				.size()];
		Integer[][] nodeTypeArray = new Integer[nodeIndexLists.get(0).size()][nodeIndexLists
				.size()];
		List<List<SimpleASTNode>> newSimpleASTNodesList = new ArrayList<List<SimpleASTNode>>();
		List<Map<String, String>> specificToUnifiedList = high_cluster
				.getSpecificToUnifiedList();
		List<Map<String, String>> unifiedToSpecificList = high_cluster
				.getUnifiedToSpecificList();
		List<Integer> nodeIndexList = null, nodeTypeList = null;
		System.out.print("");
		setMap(specificToUnifiedList.get(0), unifiedToSpecificList.get(0));
		for (int i = 1; i < specificToUnifiedList.size(); i++) {
			s2Tous.add(new HashMap<String, String>(specificToUnifiedList.get(i)));
			uTos2s.add(new HashMap<String, String>(unifiedToSpecificList.get(i)));
		}

		for (int i = 0; i < nodeIndexLists.size(); i++) {
			nodeIndexList = nodeIndexLists.get(i);
			for (int j = 0; j < nodeIndexList.size(); j++) {// transpose
				nodeIndexArray[j][i] = nodeIndexList.get(j);
			}
		}
		for (int i = 0; i < nodeTypeLists.size(); i++) {
			nodeTypeList = nodeTypeLists.get(i);
			for (int j = 0; j < nodeTypeList.size(); j++) {
				nodeTypeArray[j][i] = nodeTypeList.get(j);
			}
		}
		List<SimpleASTNode> simpleASTNodes1 = null, simpleASTNodes2 = null, simpleASTNodes = null;
		List<List<SimpleASTNode>> simpleASTNodes2List = new ArrayList<List<SimpleASTNode>>(), simpleASTNodes1List = new ArrayList<List<SimpleASTNode>>();
		List<List<SimpleASTNode>> list = null;
		int nodeType1 = -1;
		boolean isUnknownType1 = false, isUnknownType2 = false;
		List<Integer> tmpNodeTypes = null;

		Map<String, String> updatedBeforeAndAfterMap = new HashMap<String, String>();
		for (int i = 0; i < nodeIndexArray.length; i++) {
			simpleASTNodes1 = simpleASTNodesList
					.get((int) nodeIndexArray[i][0] - 1);
			nodeType1 = nodeTypeArray[i][0];
			if (simpleASTNodes1.size() == 1
					&& simpleASTNodes1.get(0).getNodeType() == ASTExpressionTransformer.UNKNOWN_STATEMENT) {
				isUnknownType1 = true;
			} else {
				isUnknownType1 = false;
			}

			simpleASTNodes1List.clear();
			simpleASTNodes2List.clear();
			tmpNodeTypes = Arrays.asList(nodeTypeArray[i]);
			isUnknownType2 = false;
			for (int j = 0; j < simpleASTNodes2ListList.size(); j++) {
				list = simpleASTNodes2ListList.get(j);
				simpleASTNodes1 = simpleASTNodes1ListList.get(j).get(
						(int) nodeIndexArray[i][0] - 1);
				simpleASTNodes2 = list.get((int) nodeIndexArray[i][j + 1] - 1);

				simpleASTNodes1List.add(simpleASTNodes1);
				simpleASTNodes2List.add(simpleASTNodes2);
				if (simpleASTNodes2.size() == 1
						&& simpleASTNodes2.get(0).getNodeType() == ASTExpressionTransformer.UNKNOWN_STATEMENT) {
					isUnknownType2 = true;
				}
			}

			simpleASTNodes = new ArrayList<SimpleASTNode>();
			if (!integerComparator.hasAllEqualTo(nodeType1, tmpNodeTypes)
					|| isUnknownType1 || isUnknownType2) {
				List<String> abstractIdentifiers = new ArrayList<String>();
				if (simpleASTNodes1.size() == 1
						&& simpleASTNodes1.get(0).getNodeType() == ASTExpressionTransformer.UNKNOWN_STATEMENT) {
					abstractIdentifiers.add(simpleASTNodes1.get(0).getTerm()
							.getName());
				} else if (simpleASTNodes2.size() == 1
						&& simpleASTNodes2.get(0).getNodeType() == ASTExpressionTransformer.UNKNOWN_STATEMENT) {
					abstractIdentifiers.add(simpleASTNodes2.get(0).getTerm()
							.getName());
				} else {
					abstractIdentifiers = null;
				}
				simpleASTNodes.add(new SimpleASTNode(new Term(
						ASTExpressionTransformer.UNKNOWN_STATEMENT,
						getAbstractIdentifier(nodeType1, tmpNodeTypes,
								abstractIdentifiers))));
			} else {
				// all nodes have the same type, what we need to do is just
				// unify them
				if (simpleASTNodes1ListList.size() == 1) {
					simpleASTNodes = simpleASTNodesList
							.get((int) nodeIndexArray[i][0] - 1);
				} else {
					try {
						simpleASTNodes = getCommon(updatedBeforeAndAfterMap,
								simpleASTNodes1List, simpleASTNodes2List,
								inter_specificToUnified,
								inter_specificToUnified_list,
								inter_unifiedToSpecific,
								inter_unifiedToSpecific_list);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
			newSimpleASTNodesList.add(simpleASTNodes);
		}

		newSimpleASTNodesList = SimpleASTNode.customize(
				updatedBeforeAndAfterMap, newSimpleASTNodesList);

		simpleASTNodesList.clear();
		simpleASTNodesList.addAll(newSimpleASTNodesList);
		specificToUnifiedList.clear();
		unifiedToSpecificList.clear();
		if (simpleASTNodes1ListList.size() == 1) {
			specificToUnifiedList.add(inter_specificToUnified);
			specificToUnifiedList.add(inter_specificToUnified_list.get(0));
			unifiedToSpecificList.add(inter_unifiedToSpecific);
			unifiedToSpecificList.add(inter_unifiedToSpecific_list.get(0));
		} else {
			specificToUnifiedList.add(new HashMap<String, String>(s1Tou));
			for (Map<String, String> s2Tou : s2Tous) {
				specificToUnifiedList.add(new HashMap<String, String>(s2Tou));
			}
			unifiedToSpecificList.add(new HashMap<String, String>(uTos1));
			for (Map<String, String> uTos2 : uTos2s) {
				unifiedToSpecificList.add(new HashMap<String, String>(uTos2));
			}
		}
		return simpleASTNodesList;
	}

	private Map<String, String> mapSpecificToStandardUnified(
			SimpleASTNode sNode, SimpleASTNode uNode, Map<String, String> sTou)
			throws MappingException {
		Map<String, String> tmpMap = new HashMap<String, String>();
		CommonParser parser = new CommonParser();
		parser.initMap(sTou);
		parser.getCommon(uNode, sNode);
		Map<String, String> uTol = IdMapper.createReverseMap(parser.getLtoU());
		Map<String, String> uTor = IdMapper.createReverseMap(parser.getRtoU());
		String key = null;
		for (Entry<String, String> entry : uTol.entrySet()) {
			key = entry.getKey();
			tmpMap.put(uTor.get(key), entry.getValue());
		}
		return tmpMap;
	}

	/**
	 * tmpMap: new abstract values - old abstract values Get entries whose keys
	 * are not equal to values, meaning that the values are generalized
	 * 
	 * @param tmpMap
	 * @return
	 */
	private Map<String, String> getDiffEntries(Map<String, String> tmpMap) {
		Map<String, String> newUToOldU = new HashMap<String, String>();
		String key = null, value = null;
		for (Entry<String, String> entry : tmpMap.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if (!key.equals(value)) {
				newUToOldU.put(value, key);
			} else if (Term.ExactAbsPattern.matcher(key).matches()
					&& Term.ExactAbsPattern.matcher(value).matches()) {
				newUToOldU.put(value, key);
			}
		}
		return newUToOldU;
	}

	private Map<String, String> detectConflict(Map<String, String> U2ToU1s,
			Map<String, String> knownU1ToUs) {
		Map<String, String> conflictU2ToUs = new HashMap<String, String>();
		String u2 = null, u1 = null;
		for (Entry<String, String> entry : U2ToU1s.entrySet()) {
			u2 = entry.getKey();
			u1 = entry.getValue();
			if (knownU1ToUs.containsKey(u1)) {
				if (!knownU1ToUs.get(u1).equals(u2)) {
					conflictU2ToUs.put(u2, knownU1ToUs.get(u1));
				} else {
					// do nothing
				}
			}
		}
		return conflictU2ToUs;
	}

	/**
	 * Two steps should be included: (1) find the unified representation for
	 * different abstract strategy on simpleASTNodes1; (2) unify all
	 * simpleASTNodes2 in simpleASTNodes2List based on their alignment with
	 * their corresponding simpleASTNodes1.
	 * 
	 * TODO: This part can be further optimized by first checking whether there
	 * is more than one element in simpleASTNodesList.
	 * 
	 * TODO: Optimization can be carefully thought about later.
	 * 
	 * @param simpleASTNodes1List
	 * @param simpleASTNodes2List
	 * @param inter_specificToUnified
	 * @param inter_specificToUnified_list
	 * @param inter_unifiedToSpecific
	 * @param inter_unifiedToSpecific_list
	 * @return
	 * @throws MappingException
	 */
	private List<SimpleASTNode> getCommon(
			Map<String, String> updatedBeforeAndAfterMap,
			List<List<SimpleASTNode>> simpleASTNodes1List,
			List<List<SimpleASTNode>> simpleASTNodes2List,
			Map<String, String> inter_specificToUnified,
			List<Map<String, String>> inter_specificToUnified_list,
			Map<String, String> inter_unifiedToSpecific,
			List<Map<String, String>> inter_unifiedToSpecific_list)
			throws MappingException {
		List<SimpleASTNode> result = new ArrayList<SimpleASTNode>();
		CommonParser parser = new CommonParser();
		SimpleASTNode tmpNode = null;
		Map<String, String> tmpMap = null;
		Map<String, String> newAbstractEntries = null;
		Map<String, String> conflictU2ToUs = null;
		int methodCounter = 0, typeCounter = 0, varCounter = 0, unknownCounter = 0;

		for (int i = 0; i < simpleASTNodes1List.get(0).size(); i++) {
			tmpNode = new SimpleASTNode(simpleASTNodes1List.get(0).get(i));
			IdMapper.calcMaxAbsCounter(s1Tou.values());
			methodCounter = ++IdMapper.abstractMethod;
			typeCounter = ++IdMapper.abstractType;
			varCounter = ++IdMapper.abstractVariable;
			unknownCounter = ++IdMapper.abstractUnknown;
			for (int j = 1; j < simpleASTNodes1List.size(); j++) {
				parser.clear();
				parser.initCounter(methodCounter, typeCounter, varCounter,
						unknownCounter);
				tmpNode = parser.getCommon(tmpNode, simpleASTNodes1List.get(j)
						.get(i));
			}

			tmpMap = mapSpecificToStandardUnified(simpleASTNodes1List.get(0)
					.get(i), tmpNode, s1Tou);
			newAbstractEntries = getDiffEntries(tmpMap);

			conflictU2ToUs = detectConflict(newAbstractEntries,
					updatedBeforeAndAfterMap);
			if (!conflictU2ToUs.isEmpty()) {
				tmpNode = SimpleASTNode.customizeSingleSimpleASTNode(
						conflictU2ToUs, tmpNode);
				tmpMap = mapSpecificToStandardUnified(simpleASTNodes1List
						.get(0).get(i), tmpNode, s1Tou);
				newAbstractEntries = getDiffEntries(tmpMap);
				// String keyAndValue = null;
				// for (Entry<String, String> entry : conflictU2ToUs.entrySet())
				// {
				// keyAndValue = entry.getKey();
				// Entry<String, String> entryToFind = null;
				// for (Entry<String, String> entry2 : tmpMap.entrySet()) {
				// if (entry2.getValue().equals(keyAndValue)) {
				// entryToFind = entry2;
				// break;
				// }
				// }
				// tmpMap.remove(entryToFind.getKey());
				// }
			}
			updatedBeforeAndAfterMap = IdMapper
					.createReverseMap(newAbstractEntries);
			if (!tmpMap.isEmpty()) {
				CHelper4Unifier.integrateMap(tmpMap, s1Tou, uTos1,
						inter_specificToUnified, inter_unifiedToSpecific);
				// System.out.print("");
				for (int j = 0; j < simpleASTNodes2List.size(); j++) {
					tmpMap = mapSpecificToStandardUnified(simpleASTNodes2List
							.get(j).get(i), tmpNode, s2Tous.get(j));
					CHelper4Unifier.integrateMap(tmpMap, s2Tous.get(j),
							uTos2s.get(j), inter_specificToUnified_list.get(j),
							inter_unifiedToSpecific_list.get(j));
				}
				// align each simpleASTNodes2 with simpleASTNodes1 while
				// traversing
				// tmpNode and abstract that accordingly
			}
			result.add(tmpNode);
		}
		return result;
	}

	/**
	 * only set for s1Tou
	 * 
	 * @param specificToUnified
	 */
	public void setMap(Map<String, String> specificToUnified,
			Map<String, String> unifiedToSpecific) {
		s1Tou = new HashMap<String, String>(specificToUnified);
		uTos1 = new HashMap<String, String>(unifiedToSpecific);
		s2Tous = new ArrayList<Map<String, String>>();
		uTos2s = new ArrayList<Map<String, String>>();
		IdMapper.calcMaxAbsCounter(s1Tou.values());

		abstractMethod = IdMapper.abstractMethod + 1;
		abstractVariable = IdMapper.abstractVariable + 1;
		abstractType = IdMapper.abstractType + 1;
		abstractUnknown = IdMapper.abstractUnknown + 1;
	}
}
