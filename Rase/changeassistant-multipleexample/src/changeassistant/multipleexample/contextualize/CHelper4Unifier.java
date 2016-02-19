package changeassistant.multipleexample.contextualize;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.multipleexample.common.CommonParser;
import changeassistant.multipleexample.common.CommonParser3;
import changeassistant.multipleexample.partition.MappingException;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.IdMapper;

public class CHelper4Unifier {

	/**
	 * Side effect: nodeIndexLists' size is cut by half because all minus
	 * numbers are removed to find the node type for each node involved in each
	 * method, composing a matrix for multiple methods
	 * 
	 * @param nodeIndexArray
	 * @param clusters
	 * @return
	 */
	private static List<List<Integer>> createNodeTypeLists(
			List<List<Integer>> nodeIndexLists, List<SimpleTreeNode> sTrees) {
		List<List<Integer>> newNodeIndexLists = new ArrayList<List<Integer>>();
		List<Integer> nodeIndexes = null, newNodeIndexes = null;
		int nodeIndex = -1;
		// To update nodeIndexLists so that they only contain positive indexes
		for (int i = 0; i < nodeIndexLists.size(); i++) {
			nodeIndexes = nodeIndexLists.get(i);
			newNodeIndexes = new ArrayList<Integer>();
			for (int j = 0; j < nodeIndexes.size(); j++) {
				nodeIndex = nodeIndexes.get(j);
				if (nodeIndex > 0) {
					newNodeIndexes.add(nodeIndex);
				}
			}
			newNodeIndexLists.add(newNodeIndexes);
		}
		nodeIndexLists.clear();
		nodeIndexLists.addAll(newNodeIndexLists);

		List<List<Integer>> nodeTypeLists = new ArrayList<List<Integer>>();

		List<Integer> nodeTypes = null;
		SimpleTreeNode sTree = null, sNode = null;
		Enumeration<SimpleTreeNode> sEnum = null;

		System.out.print("");
		for (int i = 0; i < sTrees.size(); i++) {
			nodeTypes = new ArrayList<Integer>();
			nodeIndexes = nodeIndexLists.get(i);
			sTree = sTrees.get(i);
			sEnum = sTree.preorderEnumeration();
			int counter = 0;
			while (sEnum.hasMoreElements()) {
				sNode = sEnum.nextElement();
				if (nodeIndexes.contains(sNode.getNodeIndex())) {
					nodeTypes.add(sNode.getNodeType());
					counter++;
					if (counter == nodeIndexes.size())
						break;
				}
			}
			nodeTypeLists.add(nodeTypes);
		}
		return nodeTypeLists;
	}

	/**
	 * 
	 * @param tmpMap
	 *            map from u2/u2's expression in interSpecificToUnified to the
	 *            global U
	 * @param specificToUnified
	 *            map from specific to u1
	 * @param unifiedToSpecific
	 * @param interSpecificToUnified
	 *            map from specific to u2 or from u1 to u2
	 * @param interUnifiedToSpecific
	 */
	public static void integrateMap(Map<String, String> tmpMap,
			Map<String, String> specificToUnified,
			Map<String, String> unifiedToSpecific,
			Map<String, String> interSpecificToUnified,
			Map<String, String> interUnifiedToSpecific) {
		String key = null, value = null;
		// to record the extra entries in interSpecificToUnified and
		// interUnifiedToSpecific
		Map<String, String> tmpInterSTou = new HashMap<String, String>();
		Map<String, String> tmpInterUTos = new HashMap<String, String>();
		// the newly found relations between s and global U
		Map<String, String> newSTou = new HashMap<String, String>();
		Map<String, String> newUTos = new HashMap<String, String>();
		for (Entry<String, String> entry : interSpecificToUnified.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if (specificToUnified.containsKey(key)
					&& specificToUnified.get(key).equals(entry.getValue())) {
				// do nothing
			} else {
				tmpInterSTou.put(key, value);
				tmpInterUTos.put(value, key);
			}
		}
		for (Entry<String, String> entry : tmpMap.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			// this is a remap from u1 to u2
			if (tmpInterSTou.containsValue(key)) {
				key = tmpInterUTos.get(key);
				// this is a map from s to u1
				if (specificToUnified.containsValue(key)) {
					if (!key.equals(specificToUnified.get(key))) {
						key = unifiedToSpecific.get(key);
						newSTou.put(key, value);
						newUTos.put(value, key);
					}
				} else {
					if (!specificToUnified.containsKey(key)) {
						newSTou.put(key, value);
						newUTos.put(value, key);
					}
				}
			} else {// tmpInterSTou does not contain key
				if (Term.S_Pattern.matcher(key).matches()) {
					// statement match is not included
					// do nothing
				} else {
					Matcher matcher = Term.ExactAbsPattern.matcher(key);
					String tmp = null;
					String tmp2 = null;
					String newKey = key;
					boolean isFound = false;
					while (matcher.find()) {
						isFound = true;
						tmp = matcher.group();
						// System.out.print("");
						if (tmp != null) {
							tmp2 = tmpInterUTos.get(tmp);
							// remap happens when the interUToS does not find
							// proper tmp2 or tmp2 can be mapped again in
							// specficToUnified
							if (tmp2 == null
									&& specificToUnified.containsValue(tmp)) {
								tmp2 = unifiedToSpecific.get(tmp);
							} else if (tmp2 != null
									&& specificToUnified.containsValue(tmp2)) {
								tmp2 = unifiedToSpecific.get(tmp2);
							}
							if (tmp2.startsWith(ASTExpressionTransformer.ARGS_PRE)) {
								tmp2 = tmp2
										.substring(ASTExpressionTransformer.ARGS_PRE
												.length());
							}
							while (newKey.contains(tmp)) {
								newKey = newKey.replace(tmp, tmp2);
							}
						}
					}
					if (isFound) {
						newSTou.put(newKey, value);
						newUTos.put(value, newKey);
					} else {
						newSTou.put(key, value);
						newUTos.put(value, key);
					}
				}
			}
		}
		for (Entry<String, String> entry : newSTou.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if (unifiedToSpecific.containsValue(key)) {
				unifiedToSpecific.remove(specificToUnified.get(key));
			}
			specificToUnified.put(key, value);
			unifiedToSpecific.put(value, key);
		}

	}

	/**
	 * To integrate all entries newly found in interSTou to sTou
	 * 
	 * @param sTou
	 * @param interSTou
	 */
	public static void integrateMap(Map<String, String> sTou,
			Map<String, String> interSTou) {
		Map<String, String> uTos = IdMapper.createReverseMap(sTou);
		Map<String, String> newEntries = new HashMap<String, String>();
		String key = null, value = null, originalKey = null;
		for (Entry<String, String> entry : interSTou.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if (sTou.containsKey(key) && sTou.get(key).equals(value)) {
				// do nothing
			} else {
				newEntries.put(key, value);
			}
		}
		for (Entry<String, String> entry : newEntries.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			originalKey = uTos.get(key);
			if (sTou.containsValue(key)) {
				if (originalKey.equals(key)) {
					// do nothing
				} else {
					sTou.put(originalKey, value);
					uTos.put(value, originalKey);
					uTos.remove(key);
				}
			} else if (sTou.containsKey(key) && sTou.get(key).equals(value)) {
				// do nothing
			} else {
				sTou.put(key, value);
				uTos.put(value, key);
			}
		}
	}

	public static void removeShadowedMap(Map<String, String> sTou,
			Map<String, String> interSTou) {
		Map<String, String> uTos = IdMapper.createReverseMap(sTou);
		Map<String, String> newEntries = new HashMap<String, String>();
		String key = null, value = null, originalKey = null;
		for (Entry<String, String> entry : interSTou.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if (sTou.containsKey(key) && sTou.get(key).equals(value)) {
				// do nothing
			} else {
				newEntries.put(key, value);
			}
		}
		for (Entry<String, String> entry : newEntries.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			originalKey = uTos.get(key);
			if (originalKey != null) {
				sTou.remove(originalKey);
				uTos.remove(key);
				sTou.put(key, value);
			} else {
				sTou.put(key, value);
			}
		}
	}

	/**
	 * Rule: do not change indexLists
	 * 
	 * @param clusters
	 * @param high_cluster
	 * @param indexLists
	 */
	public static void unifyCommon(List<EditInCommonCluster> clusters,
			EditInCommonCluster high_cluster, List<List<Integer>> indexLists) {
		List<SimpleTreeNode> sTrees = new ArrayList<SimpleTreeNode>();
		List<List<List<SimpleASTNode>>> simpleASTNodes2ListList = new ArrayList<List<List<SimpleASTNode>>>();
		List<List<List<SimpleASTNode>>> simpleASTNodes1ListList = new ArrayList<List<List<SimpleASTNode>>>();
		List<Map<String, String>> unifiedToSpecificList = new ArrayList<Map<String, String>>();
		List<Map<String, String>> specificToUnifiedList2 = new ArrayList<Map<String, String>>();
		EditInCommonCluster tmpCluster = null;
		for (int i = 0; i < clusters.size(); i++) {
			tmpCluster = clusters.get(i);
			// there is no need to make a copy for any sTree since it is not
			// modified
			sTrees.add(tmpCluster.getSTrees().get(1));
			simpleASTNodes1ListList.add(ContextualizationUtil
					.getCopy(tmpCluster.getSimpleASTNodesList(0)));
			// get different unification strategy for the same method
			simpleASTNodes2ListList.add(ContextualizationUtil
					.getCopy(tmpCluster.getSimpleASTNodesList(1)));

			unifiedToSpecificList.add(new HashMap<String, String>(tmpCluster
					.getUnifiedToSpecificList().get(1)));
			specificToUnifiedList2.add(new HashMap<String, String>(tmpCluster
					.getSpecificToUnifiedList().get(1)));
		}
		sTrees.add(0, clusters.get(0).getSTree());
		List<List<Integer>> nodeTypeLists = createNodeTypeLists(indexLists,
				sTrees);
		List<List<SimpleASTNode>> simpleASTNodesList = ContextualizationUtil
				.getCopy(clusters.get(0).getSimpleASTNodesList(0));
		Map<String, String> unifiedToSpecific = new HashMap<String, String>(
				clusters.get(0).getUnifiedToSpecificList().get(0));
		Map<String, String> specificToUnified2 = new HashMap<String, String>(
				clusters.get(0).getSpecificToUnifiedList().get(0));
		// 4. find the unified representation for each statement shared
		CommonParser3 parser3 = new CommonParser3();
		System.out.print("");
		simpleASTNodesList = parser3.getCommonStatement(high_cluster,
				unifiedToSpecific, unifiedToSpecificList, specificToUnified2,
				specificToUnifiedList2, simpleASTNodesList,
				simpleASTNodes1ListList, simpleASTNodes2ListList, indexLists,
				nodeTypeLists);
		if (simpleASTNodesList == null)
			high_cluster.setApplicable(false);
		high_cluster.setSimpleASTNodesList(simpleASTNodesList);
	}

	public static boolean unifyCommon(EditInCommonCluster high_cluster,
			EditInCommonCluster interCluster, EditInCommonCluster eClus,
			List<List<Integer>> indexesList, List<Integer> insts,
			Integer tmpInst, List<List<List<SimpleASTNode>>> simpleASTNodesLists) {
		List<List<SimpleASTNode>> simpleASTNodesList1 = simpleASTNodesLists
				.get(0);
		List<List<SimpleASTNode>> simpleASTNodesList2 = simpleASTNodesLists
				.get(1);
		List<Map<String, String>> high_cluster_specificToUnified = high_cluster
				.getSpecificToUnifiedList();
		List<Map<String, String>> high_cluster_unifiedToSpecific = high_cluster
				.getUnifiedToSpecificList();
		List<Map<String, String>> new_inter_specificToUnified = new ArrayList<Map<String, String>>();
		List<Map<String, String>> new_inter_unifiedToSpecific = new ArrayList<Map<String, String>>();
		List<List<List<SimpleASTNode>>> interSimpleExprsLists = null;
		List<List<List<SimpleASTNode>>> simpleExprsLists = new ArrayList<List<List<SimpleASTNode>>>();
		List<List<SimpleASTNode>> simpleExprsList = null;
		List<Integer> instances = null;
		Map<String, String> specificToUnified = null, unifiedToSpecific = null;
		CommonParser parser = new CommonParser();
		parser.initMap(high_cluster.getSpecificToUnifiedList().get(0));
		try {
			List<List<SimpleASTNode>> simpleASTNodesList = parser.getCommon(
					simpleASTNodesList1, simpleASTNodesList2);

			Map<String, String> lTou = parser.getLtoU();
			Map<String, String> rTou = parser.getRtoU();

			instances = interCluster.getInstances();

			Map<String, String> newUToOldU = detectRedundantMap(lTou,
					high_cluster_specificToUnified.get(high_cluster
							.getInstances().indexOf(instances.get(0))),
					high_cluster_unifiedToSpecific.get(high_cluster
							.getInstances().indexOf(instances.get(0))),
					interCluster.getSpecificToUnifiedList().get(0),
					interCluster.getUnifiedToSpecificList().get(0));
			if (!newUToOldU.isEmpty()) {
				updateMap(lTou, newUToOldU);
				updateMap(rTou, newUToOldU);
			}
			Map<String, String> uMap = SimpleASTNode.createUMap(lTou);
			if (!newUToOldU.isEmpty()) {
				simpleASTNodesList = SimpleASTNode.customize(newUToOldU,
						simpleASTNodesList);
			}
			interSimpleExprsLists = interCluster.getSimpleExprsLists();
			List<Integer> interIndexes = interCluster.getIndexesList().get(
					interCluster.getInstances().indexOf(tmpInst));
			List<Integer> indexes = high_cluster.getIndexesList().get(
					high_cluster.getInstances().indexOf(tmpInst));

			for (int i = 0; i < interIndexes.size(); i++) {
				if (indexes.contains(interIndexes.get(i))) {
					simpleExprsList = SimpleASTNode.customize(lTou, uMap,
							interSimpleExprsLists.get(i));
					simpleExprsLists.add(simpleExprsList);
				}
			}

			Set<Integer> processed = new HashSet<Integer>();
			for (int i = 0; i < instances.size(); i++) {
				if (processed.add(instances.get(i))) {
					// the instance has not been processed
					specificToUnified = new HashMap<String, String>(
							high_cluster_specificToUnified.get(high_cluster
									.getInstances().indexOf(instances.get(i))));
					unifiedToSpecific = new HashMap<String, String>(
							high_cluster_unifiedToSpecific.get(high_cluster
									.getInstances().indexOf(instances.get(i))));
					integrateMap(lTou, specificToUnified, unifiedToSpecific,
							interCluster.getSpecificToUnifiedList().get(i),
							interCluster.getUnifiedToSpecificList().get(i));
					new_inter_specificToUnified.add(specificToUnified);
					new_inter_unifiedToSpecific.add(unifiedToSpecific);
				}
			}

			instances = eClus.getInstances();
			for (int i = 0; i < instances.size(); i++) {
				if (processed.add(instances.get(i))) {
					specificToUnified = new HashMap<String, String>(
							high_cluster_specificToUnified.get(high_cluster
									.getInstances().indexOf(instances.get(i))));
					unifiedToSpecific = new HashMap<String, String>(
							high_cluster_unifiedToSpecific.get(high_cluster
									.getInstances().indexOf(instances.get(i))));
					integrateMap(rTou, specificToUnified, unifiedToSpecific,
							eClus.getSpecificToUnifiedList().get(i), eClus
									.getUnifiedToSpecificList().get(i));
					new_inter_specificToUnified.add(specificToUnified);
					new_inter_unifiedToSpecific.add(unifiedToSpecific);
				}
			}
			boolean isConsistentMap = true;
			// check whether all instances are mapped consistently based on the
			// invariant:
			// the number of entries in each map should always be the same
			int mapCount = new_inter_specificToUnified.get(0).size();
			for (int i = 1; i < new_inter_specificToUnified.size(); i++) {
				if (mapCount != new_inter_specificToUnified.get(i).size()) {
					isConsistentMap = false;
					return isConsistentMap;
				}
			}
			interCluster.setSpecificToUnifiedList(new_inter_specificToUnified);
			interCluster.setUnifiedToSpecificList(new_inter_unifiedToSpecific);
			interCluster.setInstances(insts);
			interCluster.setSimpleASTNodesList(simpleASTNodesList);
			interCluster.setExprsLists(simpleExprsLists);
			interCluster.setSequenceList2(indexesList);
			interCluster.setSequence(interCluster.getSequenceList().get(0));

			List<Integer> hInsts = high_cluster.getInstances();
			Integer hInst = null;
			List<List<Integer>> hEditIndexesList = high_cluster
					.getIndexesList();
			List<List<Integer>> editIndexesList = new ArrayList<List<Integer>>();
			for (int i = 0; i < hInsts.size(); i++) {
				hInst = hInsts.get(i);
				if (insts.contains(hInst)) {
					editIndexesList.add(hEditIndexesList.get(i));
				}
			}
			interCluster.setIndexesList(editIndexesList);
			return isConsistentMap;
		} catch (MappingException e) {
			return false;
		}
	}

	private static void updateMap(Map<String, String> lTou,
			Map<String, String> newUToOldU) {
		String value = null;
		for (Entry<String, String> entry : lTou.entrySet()) {
			value = entry.getValue();
			if (newUToOldU.containsKey(value)) {
				entry.setValue(newUToOldU.get(value));
			}
		}
	}

	/**
	 * To integrate maps lTou, interSpecificToUnified, interUnifiedToSpecific
	 * and compare the resulting sTou uTos with the known specificToUnified
	 * unifiedToSpecific If there is a key mapped to two values, use the
	 * original value
	 * 
	 * @param lTou
	 * @param map
	 * @param map2
	 * @param map3
	 * @param map4
	 * @return
	 */
	private static Map<String, String> detectRedundantMap(
			Map<String, String> lTou, Map<String, String> specificToUnified,
			Map<String, String> unifiedToSpecific,
			Map<String, String> interSpecificToUnified,
			Map<String, String> interUnifiedToSpecific) {
		Map<String, String> sTou = new HashMap<String, String>();
		Map<String, String> uTos = new HashMap<String, String>();
		Map<String, String> newUToOldU = new HashMap<String, String>();
		integrateMap(lTou, sTou, uTos, interSpecificToUnified,
				interUnifiedToSpecific);
		sTou.entrySet().removeAll(specificToUnified.entrySet());
		uTos.entrySet().removeAll(unifiedToSpecific.entrySet());
		String oldValue = null;
		String key = null, value = null;
		for (Entry<String, String> entry : sTou.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			if (specificToUnified.containsKey(key)) {
				oldValue = specificToUnified.get(key);
				if (!value.equals(oldValue)
						&& Term.ExactAbsPattern.matcher(oldValue).matches()) {
					newUToOldU.put(value, oldValue);
				}
			}
		}
		return newUToOldU;
	}
}
