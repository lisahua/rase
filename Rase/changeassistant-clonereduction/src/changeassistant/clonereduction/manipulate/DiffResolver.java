package changeassistant.clonereduction.manipulate;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.clonereduction.main.CloneReductionException;
import changeassistant.multipleexample.common.CommonParser;
import changeassistant.multipleexample.datastructure.MapList;
import changeassistant.multipleexample.partition.MappingException;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.comparison.Node;

public class DiffResolver {
	
	private List<List<SimpleTreeNode>> partialSNodes;
	private MapList specificToUnifiedList;
	private List<List<List<SimpleASTNode>>> customizedNodesLists;

	private boolean checkResolvable() {
		List<SimpleTreeNode> pNodes = partialSNodes.get(0);
		int size = pNodes.size();
		List<Integer> treeSizes = new ArrayList<Integer>();
		for (int i = 0; i < pNodes.size(); i++) {
			treeSizes.add(pNodes.get(i).countNodes());
		}
		List<SimpleTreeNode> tempSNodes = null;
		for (int i = 1; i < partialSNodes.size(); i++) {
			tempSNodes = partialSNodes.get(i);
			if (tempSNodes.size() != size) {
				return false;
			}
			for (int j = 0; j < size; j++) {
				if (tempSNodes.get(j).countNodes() != treeSizes.get(j)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Assumption: partialSNodes.size() == 2
	 * 
	 * @throws MappingException
	 */
	public void resolveDiff(List<List<SimpleTreeNode>> pSNodesList, MapList sTouList, 
			List<List<List<SimpleASTNode>>> cNodesLists) throws CloneReductionException {
		this.partialSNodes = pSNodesList;
		this.specificToUnifiedList = sTouList;
		this.customizedNodesLists = cNodesLists;
		boolean flag = checkResolvable();
		if (!flag)
			throw new CloneReductionException("The difference between two examples is not resolvable");
		assert partialSNodes.size() == 2;
		Map<String, String> sTou1 = specificToUnifiedList.get(0);
		Map<String, String> sTou2 = specificToUnifiedList.get(1);
		CommonParser parser = new CommonParser();
		parser.setMap(sTou1, sTou2);
		List<SimpleTreeNode> psNodes1 = partialSNodes.get(0);
		List<SimpleTreeNode> psNodes2 = partialSNodes.get(1);
		List<List<SimpleASTNode>> customizedNodesList1 = customizedNodesLists
				.get(0);
		List<List<SimpleASTNode>> customizedNodesList2 = customizedNodesLists
				.get(1);
		List<SimpleASTNode> cList1 = null, cList2 = null, cResult = null;
		SimpleASTNode node = null;
		SimpleTreeNode tree1 = null, tree2 = null, tmp1 = null, tmp2 = null;
		Enumeration<SimpleTreeNode> enum1 = null, enum2 = null;
		Set<Integer> nodesWithConflict = new HashSet<Integer>();
		Set<String> errorMessages = new HashSet<String>();
		for (int i = 0; i < psNodes1.size(); i++) {
			tree1 = psNodes1.get(i);
			tree2 = psNodes2.get(i);
			enum1 = tree1.breadthFirstEnumeration();
			enum2 = tree2.breadthFirstEnumeration();
			while (enum1.hasMoreElements()) {
				tmp1 = enum1.nextElement();
				tmp2 = enum2.nextElement();
				cList1 = customizedNodesList1.get(tmp1.getNodeIndex() - 1);
				cList2 = customizedNodesList2.get(tmp2.getNodeIndex() - 1);
				for (int j = 0; j < cList1.size(); j++) {
					try {
						node = parser.getCommon(cList1.get(j), cList2.get(j));
						if (node == null) {
							throw new CloneReductionException("The difference between two examples " +
									"is not resolvable because some statements cannot find a general representation");
						}
					} catch (MappingException e) {
						nodesWithConflict.add(i);
						errorMessages.add(e.getMessage());
						// throw new CloneReductionException(
						// "There is conflicting mapping "
						// + e.getMessage());
					}
				}
			}
		}
		removeRedundantAbsMaps(sTou1);
		removeRedundantAbsMaps(sTou2);
		if (nodesWithConflict.size() == psNodes1.size()) {
			StringBuffer buffer = new StringBuffer(
					"There is/are conflicting mapping(s):\n");
			for (String e : errorMessages) {
				buffer.append(e).append("\n");
			}
			throw new CloneReductionException(buffer.toString());
		}
	}
	
	/**
	 * If an abstract identifier is remapped to another abstract identifier, it
	 * means two things: (1) we need to remove the remapped entry, (2) for any
	 * abstract identifier which is not remapped, we need to remove it as well
	 * because it is not used in the new version at all.
	 * 
	 * @param map
	 */
	private void removeRedundantAbsMaps(Map<String, String> map) {
		Set<String> toRemoveForKeys = new HashSet<String>();
		for (Entry<String, String> entry : map.entrySet()) {
			if (Term.ExactAbsPattern.matcher(entry.getKey()).matches()) {
				toRemoveForKeys.add(entry.getKey());
			}
		}
		for (String key : toRemoveForKeys) {
			map.remove(key);
		}
	}
}
