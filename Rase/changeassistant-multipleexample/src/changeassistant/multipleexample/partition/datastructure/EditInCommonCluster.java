package changeassistant.multipleexample.partition.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import changeassistant.multipleexample.contextualize.CHelper4Unifier;
import changeassistant.multipleexample.contextualize.ContextualizationUtil;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.edit.STreeEditScript;
import changeassistant.multipleexample.match.CodePattern;
import changeassistant.multipleexample.match.ProgramTransformationGenerator;
import changeassistant.multipleexample.util.IdMapper;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;

public class EditInCommonCluster extends AbstractCluster {
	// indexesList and forestList are corresponding to instances in order
	private int index;

	private List<Integer> instances;

	private List<AbstractTreeEditOperation> edits;

	private List<SimpleTreeNode> sTreeList;
	private List<SimpleTreeNode> sTreeList2;// SimpleTreeNode List for new
											// version of the tree
	private List<Node> updatedNodeList;
	private List<Node> newNodeList;
	private String contextCode = null;

	private List<List<Integer>> indexesList;
	private List<List<SimpleTreeNode>> forests;
	private boolean hasOrderedForests = false;

	// This list corresponds to simpleASTNodesList for each instance
	private List<List<List<SimpleASTNode>>> simpleASTNodesList2;
	// customized nodes contents
	private List<Sequence> sequenceList;
	// This list corresponds to specificToUnified map for each instance
	private List<Map<String, String>> specificToUnifiedList;
	private List<Map<String, String>> unifiedToSpecificList;

	private List<AbstractCluster> incomings;

	private boolean isSame = false;

	private boolean applicable = true;

	private CodePattern codePattern = null;

	private STreeEditScript abstractEditScript = null;

	private List<List<SimpleTreeNode>> controlNodes = null;
	private List<List<List<SimpleASTNode>>> controlExprsLists = null;
	private List<List<SimpleTreeNode>> dataNodes = null;
	private List<List<List<SimpleASTNode>>> dataExprsLists = null;

	public EditInCommonCluster(List<ChangeSummary> conChgSum,
			List<String> conChgSumStr, List<String> conAbsChgSumStr) {
		super(conChgSum, conChgSumStr, conAbsChgSumStr);
		instances = new ArrayList<Integer>();
		indexesList = new ArrayList<List<Integer>>();
		incomings = new ArrayList<AbstractCluster>();
		specificToUnifiedList = new ArrayList<Map<String, String>>();
		unifiedToSpecificList = new ArrayList<Map<String, String>>();
	}

	public void addIncoming(AbstractCluster child, List<Integer> indexes,
			Map<String, String> specificToUnified) {
		List<Integer> newValue = null;
		List<Integer> tmpInsts = null;
		List<List<Integer>> tmpIndList = null;
		Integer tmpInst = null;
		List<Integer> value = null;
		Map<String, String> tmpSpecToUni = null;
		Map<String, String> tmpUniToSpec = null;
		Map<String, String> basicSpecToUni = null;
		Map<String, String> basicUniToSpec = null;
		List<Map<String, String>> tmpSpecToUniList = null;
		List<Map<String, String>> tmpUniToSpecList = null;
		Map<String, String> unifiedToSpecific = null;
		if (child instanceof BaseCluster) {
			newValue = new ArrayList<Integer>(indexes);
			instances.add(((BaseCluster) child).getIndex());
			indexesList.add(newValue);
			specificToUnifiedList.add(new HashMap<String, String>(
					specificToUnified));
			unifiedToSpecific = IdMapper.createReverseMap(specificToUnified);
			unifiedToSpecificList.add(unifiedToSpecific);
		} else {
			tmpInsts = ((EditInCommonCluster) child).instances;
			tmpIndList = ((EditInCommonCluster) child).indexesList;
			tmpSpecToUniList = ((EditInCommonCluster) child).specificToUnifiedList;
			tmpUniToSpecList = ((EditInCommonCluster) child).unifiedToSpecificList;
			for (int j = 0; j < tmpInsts.size(); j++) {
				tmpInst = tmpInsts.get(j);
				if (instances.contains(tmpInst))// the child has some overlap
												// with the parent
					continue;
				instances.add(tmpInst); // add the instance

				value = tmpIndList.get(j);
				newValue = new ArrayList<Integer>();
				for (Integer index : indexes) {
					newValue.add(value.get(index));
				}
				indexesList.add(newValue);// add the relevant indexes

				basicSpecToUni = tmpSpecToUniList.get(j);
				basicUniToSpec = tmpUniToSpecList.get(j);
				tmpSpecToUni = new HashMap<String, String>();
				tmpUniToSpec = new HashMap<String, String>();
				System.out.print("");
				CHelper4Unifier.integrateMap(specificToUnified, tmpSpecToUni,
						tmpUniToSpec, basicSpecToUni, basicUniToSpec);
				specificToUnifiedList.add(tmpSpecToUni);// add the relevant
														// identifier mappings
				unifiedToSpecificList.add(tmpUniToSpec);
			}
		}

		if (incomings == null) {
			incomings = new ArrayList<AbstractCluster>();
		}
		if (!incomings.contains(child)) {
			incomings.add(child);
		}
	}

	public void addIncomingSimple(AbstractCluster child, List<Integer> indexes,
			Map<String, String> specificToUnified) {
		instances.add(child.getIndex());
		indexesList.add(indexes);
		specificToUnifiedList
				.add(new HashMap<String, String>(specificToUnified));
		Map<String, String> unifiedToSpecific = IdMapper
				.createReverseMap(specificToUnified);
		unifiedToSpecificList.add(unifiedToSpecific);
		if (incomings == null)
			incomings = new ArrayList<AbstractCluster>();
		incomings.add(child);
	}

	public void addSequence(Sequence s) {
		if (sequenceList == null)
			sequenceList = new ArrayList<Sequence>();
		sequenceList.add(s);
	}

	public void addSimpleASTNodesList(List<List<SimpleASTNode>> nodesList) {
		if (simpleASTNodesList2 == null)
			simpleASTNodesList2 = new ArrayList<List<List<SimpleASTNode>>>();
		simpleASTNodesList2.add(nodesList);
	}

	public void addSTree(SimpleTreeNode sTree) {
		if (sTreeList == null) {
			sTreeList = new ArrayList<SimpleTreeNode>();
		}
		sTreeList.add(sTree);
	}

	public void addSTree2(SimpleTreeNode sTree2) {
		if (sTreeList2 == null) {
			sTreeList2 = new ArrayList<SimpleTreeNode>();
		}
		sTreeList2.add(sTree2);
	}

	public boolean checkForestOrder() {
		return hasOrderedForests;
	}

	public boolean contains(Integer index) {
		return instances.contains(index);
	}

	@Override
	public Object clone() {
		EditInCommonCluster clus = null;
		List<List<SimpleASTNode>> newSimpleASTNodes = null;
		clus = (EditInCommonCluster) super.clone();
		clus.instances = new ArrayList<Integer>(instances);
		clus.edits = edits;
		clus.sTree = sTree;
		if (sTreeList != null) {
			clus.sTreeList = new ArrayList<SimpleTreeNode>(sTreeList);
		}
		if (sTreeList2 != null) {
			clus.sTreeList2 = new ArrayList<SimpleTreeNode>(sTreeList2);
		}
		if (updatedNodeList != null) {
			clus.updatedNodeList = new ArrayList<Node>(updatedNodeList);
		}
		if (newNodeList != null) {
			clus.newNodeList = new ArrayList<Node>(newNodeList);
		}
		if (indexesList != null) {
			clus.indexesList = new ArrayList<List<Integer>>();
			for (List<Integer> indexes : indexesList) {
				clus.indexesList.add(indexes);
			}
		}
		clus.forests = null;
		if (simpleASTNodesList != null) {
			clus.simpleASTNodesList = new ArrayList<List<SimpleASTNode>>();
			for (List<SimpleASTNode> sNodes : simpleASTNodesList) {
				clus.simpleASTNodesList
						.add(new ArrayList<SimpleASTNode>(sNodes));
			}
		}
		if (simpleASTNodesList2 != null) {
			clus.simpleASTNodesList2 = new ArrayList<List<List<SimpleASTNode>>>(
					simpleASTNodesList2);
			for (List<List<SimpleASTNode>> simpleASTNodes : simpleASTNodesList2) {
				newSimpleASTNodes = new ArrayList<List<SimpleASTNode>>();
				for (List<SimpleASTNode> simpleASTNodesSingle : simpleASTNodes) {
					newSimpleASTNodes.add(new ArrayList<SimpleASTNode>(
							simpleASTNodesSingle));
				}
				clus.simpleASTNodesList2.add(newSimpleASTNodes);
			}
		}

		clus.sequenceList = new ArrayList<Sequence>();
		if (sequenceList != null)
			for (Sequence sequence : sequenceList) {
				clus.sequenceList.add(new Sequence(sequence.getNodeIndexes()));
			}
		clus.specificToUnifiedList = new ArrayList<Map<String, String>>();
		for (Map<String, String> sTou : specificToUnifiedList) {
			clus.specificToUnifiedList.add(new HashMap<String, String>(sTou));
		}
		clus.unifiedToSpecificList = new ArrayList<Map<String, String>>();
		for (Map<String, String> uTos : unifiedToSpecificList) {
			clus.unifiedToSpecificList.add(new HashMap<String, String>(uTos));
		}
		clus.incomings = new ArrayList<AbstractCluster>(incomings);
		if (codePattern != null)
			clus.codePattern = (CodePattern) codePattern.clone();
		return clus;
	}

	/**
	 * To customize the instances' simpleASTNodesList based on
	 * specificToUnifiedList
	 */
	public List<List<List<SimpleASTNode>>> customize(
			Map<Integer, BaseCluster> baseClusters) {
		Map<String, String> specificToUnified = null;
		Map<String, String> uMap = null;
		List<List<List<SimpleASTNode>>> customizedSimpleASTNodesList2 = new ArrayList<List<List<SimpleASTNode>>>();
		List<List<SimpleASTNode>> newNodesList = null;

		for (int k = 0; k < instances.size(); k++) {
			specificToUnified = specificToUnifiedList.get(k);
			uMap = SimpleASTNode.createUMap(specificToUnified);
			newNodesList = SimpleASTNode.customize(
					specificToUnified,
					uMap,
					ContextualizationUtil.getCopy(baseClusters.get(
							instances.get(k)).getSimpleASTNodesList()));
			customizedSimpleASTNodesList2.add(newNodesList);
		}
		return customizedSimpleASTNodesList2;
	}

	public void disableSame() {
		this.isSame = false;
	}

	public void enableForestOrder() {
		hasOrderedForests = true;
	}

	public void enableSame() {
		this.isSame = true;
	}

	/**
	 * If two groups contain the same instances, they should be regarded the
	 * same
	 */
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof EditInCommonCluster))
			return false;
		EditInCommonCluster other = (EditInCommonCluster) obj;
		if (this.instances.size() != other.instances.size())
			return false;
		if (!instances.containsAll(other.instances))
			return false;
		if (this.conChgSum.size() != other.conChgSum.size())
			return false;
		return true;
	}

	/**
	 * The two groups are not necessarily the same, but only contain some
	 * instances in common and the parsed "common" edits are the same
	 * 
	 * @param other
	 * @return
	 */
	public boolean equivalentTo(EditInCommonCluster other) {
		List<Integer> tmpInsts = new ArrayList<Integer>(instances);
		tmpInsts.retainAll(other.instances);
		if (tmpInsts.isEmpty())
			return false;
		Map<String, String> map1 = null, map2 = null;
		int index1 = -1, index2 = -1;
		String key = null, value1 = null, value2 = null;
		boolean isMapped = false;
		for (Integer tmpInst : tmpInsts) {
			index1 = instances.indexOf(tmpInst);
			index2 = other.instances.indexOf(tmpInst);
			// for the common changed method shared, if both map identifiers in
			// the same manner, we can combine them
			if (PartitionUtil.equalList(indexesList.get(index1),
					other.indexesList.get(index2))) {
				map1 = specificToUnifiedList.get(index1);
				map2 = other.specificToUnifiedList.get(index2);
				if (map1.size() != map2.size())
					continue;
				isMapped = true;
				for (Entry<String, String> entry : map1.entrySet()) {
					key = entry.getKey();
					if (!map2.containsKey(key)) {
						isMapped = false;
						break;
					} else {
						value1 = map1.get(key);
						value2 = map2.get(key);
						if (!(value1.equals(key) && value2.equals(key) || !value1
								.equals(key) && !value2.equals(key))) {
							isMapped = false;
							break;
						}
					}
				}
				if (isMapped)
					return true;
			}
		}
		return false;
	}

	public STreeEditScript getAbstractEditScript() {
		return abstractEditScript;
	}

	public boolean getApplicable() {
		return applicable;
	}

	public CodePattern getCodePattern() {
		return codePattern;
	}

	public String createEdits(EditInCommonGroup group) {
		ProgramTransformationGenerator
				.createProgramTransformations(this, group);
		return contextCode;
	}

	public List<AbstractCluster> getIncomings() {
		return this.incomings;
	}

	public int getIndex() {
		return index;
	}

	public List<List<Integer>> getIndexesList() {
		return indexesList;
	}

	public List<List<List<SimpleASTNode>>> getSimpleASTNodesLists() {
		return simpleASTNodesList2;
	}

	/**
	 * interCluster uses this field to put customized simpleASTNodesList while
	 * highCluster uses this field to put the original simpleASTNodesList
	 * 
	 * @param index
	 * @return
	 */
	public List<List<SimpleASTNode>> getSimpleASTNodesList(int index) {
		if (simpleASTNodesList2 != null)
			return simpleASTNodesList2.get(index);
		// if simpleASTNodesList2 == null, initialize the lists with the
		// original simpleASTNodesList
		simpleASTNodesList2 = new ArrayList<List<List<SimpleASTNode>>>();
		List<EditInCommonCluster> level_1_clusters = ClusterHelper
				.getLevel_1_clusters(this);
		Map<Integer, BaseCluster> map = ClusterHelper
				.getBaseClusters(level_1_clusters);
		for (Integer inst : instances) {
			simpleASTNodesList2.add(map.get(inst).getSimpleASTNodesList());
		}
		return simpleASTNodesList2.get(index);
	}

	public int hashCode() {
		int value = super.hashCode() * 1000000;
		for (int i = 0; i < instances.size(); i++) {
			value += instances.get(i) * 1000;
			for (Integer index : indexesList.get(i)) {
				value += index;
			}
		}
		return value;
	}

	public boolean isComposite(String key, Map<String, String> knownMap) {
		return false;
	}

	public boolean allSameInstances() {
		return isSame;
	}

	/**
	 * merge other into this cluster
	 * 
	 * @param other
	 */
	public boolean merge(EditInCommonCluster other) {
		Integer inst = null;
		boolean flag = false;
		for (int i = 0; i < other.instances.size(); i++) {
			inst = other.instances.get(i);
			if (!instances.contains(inst)) {
				instances.add(inst);
				indexesList.add(other.indexesList.get(i));
				specificToUnifiedList.add(other.specificToUnifiedList.get(i));
				unifiedToSpecificList.add(other.unifiedToSpecificList.get(i));
				if(forests != null){
					forests.add(other.forests.get(i));
					sTreeList.add(other.getSTrees().get(i));
					sTreeList2.add(other.getSTrees2().get(i));	
				}							
				updatedNodeList.add(other.getUpdatedNodeList().get(i));
				newNodeList.add(other.getNewNodeList().get(i));
			}
		}

		flag = true;

		for (AbstractCluster clus : other.incomings) {
			if (!this.equals(clus)) {
				if (!this.incomings.contains(clus)) {
					this.incomings.add(clus);
					clus.outgoings.add(this);
				}
				clus.outgoings.remove(other);
			}
		}
		return flag;
	}

	public List<List<SimpleTreeNode>> getControlNodes() {
		return controlNodes;
	}

	public List<List<List<SimpleASTNode>>> getControlExprsLists() {
		return controlExprsLists;
	}

	public List<List<SimpleTreeNode>> getDataNodes() {
		return dataNodes;
	}

	public List<List<List<SimpleASTNode>>> getDataExprsLists() {
		return dataExprsLists;
	}

	public List<List<SimpleTreeNode>> getForests() {
		return forests;
	}

	public List<Integer> getInstances() {
		return instances;
	}

	public List<Node> getNewNodeList() {
		return newNodeList;
	}

	public List<Sequence> getSequenceList() {
		if (sequenceList == null)
			sequenceList = new ArrayList<Sequence>();
		return sequenceList;
	}

	public List<Map<String, String>> getSpecificToUnifiedList() {
		return specificToUnifiedList;
	}

	public List<Map<String, String>> getUnifiedToSpecificList() {
		if (unifiedToSpecificList == null) {
			unifiedToSpecificList = new ArrayList<Map<String, String>>();
			Map<String, String> newMap = null;
			for (Map<String, String> unifiedToSpecific : specificToUnifiedList) {
				newMap = IdMapper.createReverseMap(unifiedToSpecific);
				unifiedToSpecificList.add(newMap);
			}
		}
		return unifiedToSpecificList;
	}

	public List<Node> getUpdatedNodeList() {
		return updatedNodeList;
	}

	public List<SimpleTreeNode> getSTrees() {
		return sTreeList;
	}

	public List<SimpleTreeNode> getSTrees2() {
		return sTreeList2;
	}

	public void removeIncoming(EditInCommonCluster clus) {
		incomings.remove(clus);
	}

	public void setAbstractEditScript(STreeEditScript script) {
		abstractEditScript = script;
	}

	public void setApplicable(boolean flag) {
		this.applicable = flag;
	}

	public void setCodePattern(CodePattern pattern) {
		codePattern = pattern;
	}

	public void setControlConstraints(List<List<SimpleTreeNode>> controlNodes,
			List<List<List<SimpleASTNode>>> exprsLists) {
		this.controlNodes = controlNodes;
		this.controlExprsLists = exprsLists;
	}

	public void setDataConstraints(List<List<SimpleTreeNode>> dataNodes,
			List<List<List<SimpleASTNode>>> exprsLists) {
		this.dataNodes = dataNodes;
		this.dataExprsLists = exprsLists;
	}

	public void setExprsLists(List<List<List<SimpleASTNode>>> exprsLists) {
		this.simpleExprsLists = exprsLists;
	}

	public void setForests(List<List<SimpleTreeNode>> list) {
		forests = list;
	}

	public void setIncomings(List<AbstractCluster> clusters) {
		incomings = clusters;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setIndexesList(List<List<Integer>> indexesList) {
		this.indexesList = indexesList;
	}

	public void setInstances(List<Integer> insts) {
		instances = insts;
	}

	public void setNewNodeList(List<Node> list) {
		newNodeList = list;
	}

	public void setSpecificToUnifiedList(List<Map<String, String>> mapList) {
		this.specificToUnifiedList = mapList;
	}

	public void setSimpleASTNodesLists(
			List<List<List<SimpleASTNode>>> nodesLists) {
		simpleASTNodesList2 = nodesLists;
	}

	public void setSTreeList(List<SimpleTreeNode> list) {
		sTreeList = list;
	}

	public void setSTreeList2(List<SimpleTreeNode> list) {
		sTreeList2 = list;
	}

	public void setSequenceList(List<Sequence> list) {
		this.sequenceList = list;
	}

	public void setSequenceList2(List<List<Integer>> indexesLists) {
		List<Sequence> sequenceList = new ArrayList<Sequence>();
		for (int i = 0; i < indexesLists.size(); i++) {
			sequenceList.add(new Sequence(indexesLists.get(i)));
		}
		this.sequenceList = sequenceList;
	}

	public void setUnifiedToSpecificList(List<Map<String, String>> mapList) {
		this.unifiedToSpecificList = mapList;
	}

	public void setUpdatedNodeList(List<Node> list) {
		updatedNodeList = list;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("income:");
		for (AbstractCluster clus : incomings) {
			buffer.append(clus.getIndex() + "  ");
		}
		return buffer.toString();
	}

	/**
	 * return the base clusters which are involved in at least one cluster which
	 * has "applicable == true"
	 * 
	 * @return
	 */
	public Set<BaseCluster> update() {
		// the reason to define list instead of stack: the list can be reused
		// again and again
		// This list is the prototype for an iterator
		// System.out.print("");
		List<EditInCommonCluster> list = new ArrayList<EditInCommonCluster>();
		Queue<AbstractCluster> queue = new LinkedList<AbstractCluster>();
		List<EditInCommonCluster> level1_list = new ArrayList<EditInCommonCluster>();
		queue.add(this);
		AbstractCluster clus = null;
		boolean isLevel1 = true;
		List<AbstractCluster> children = null;
		EditInCommonCluster eClus = null;
		// enumerate all clusters, save level1-clusters in level1_list,
		while (!queue.isEmpty()) {
			clus = queue.remove();
			if (clus instanceof BaseCluster) // level-0
				continue;
			if (list.contains(clus))// the cluster has already been processed
				continue;
			eClus = (EditInCommonCluster) clus;
			list.add(0, eClus);
			children = eClus.incomings;// from top to bottom
			isLevel1 = true;
			for (AbstractCluster child : children) {
				if (child instanceof EditInCommonCluster) {
					isLevel1 = false;
					break;
				}
			}
			if (isLevel1)
				level1_list.add(eClus);
			queue.addAll(children);
		}
		// remove level1 cluster if its applicable = false and leveln cluster if
		// all its incomings have been removed
		for (int i = 0; i < list.size(); i++) {
			eClus = list.get(i);
			if (!eClus.applicable
					&& (level1_list.contains(eClus) || eClus.incomings.size() == 0)
					&& eClus.outgoings != null) {
				for (EditInCommonCluster eClus2 : eClus.outgoings) {// from
																	// bottom to
																	// top
					eClus2.incomings.remove(eClus);
				}
			}
		}

		// return level_0 set
		Set<BaseCluster> level0_set = new HashSet<BaseCluster>();
		for (EditInCommonCluster eClus2 : level1_list) {
			if (!eClus2.applicable)
				continue;
			for (AbstractCluster aClus : eClus2.incomings)
				level0_set.add((BaseCluster) aClus);
		}
		return level0_set;
	}
}
