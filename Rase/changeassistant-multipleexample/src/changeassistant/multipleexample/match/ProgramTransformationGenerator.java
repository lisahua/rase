package changeassistant.multipleexample.match;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.edit.STreeEditCreator;
import changeassistant.multipleexample.edit.STreeEditScript;
import changeassistant.multipleexample.internal.SimpleAnonymousClassDeclarationFinder;
import changeassistant.multipleexample.main.EnhancedChangeAssistantMain;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.IdGeneralizer;
import changeassistant.multipleexample.util.IdMapper;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;
import changeassistant.versions.treematching.edits.MoveOperation;
import changeassistant.versions.treematching.edits.UpdateOperation;

public class ProgramTransformationGenerator {

	private static Node updatedNode, oldNode, newNode;

	private static void normalizePattern(SimpleTreeNode sTree,
			List<List<SimpleASTNode>> sNodesList, EditInCommonCluster cluster,
			Set<Term> alphabetSet, Set<String> stmtSet) {
		Enumeration<SimpleTreeNode> sEnum = sTree.breadthFirstEnumeration();
		List<SimpleASTNode> sNodes = null;
		SimpleTreeNode sTreeNode = null;
		String stmt = null;
		System.out.print("");
		while (sEnum.hasMoreElements()) {
			sTreeNode = sEnum.nextElement();
			if (!sTreeNode.getTypes().contains(SimpleTreeNode.INSERTED)
					&& sTreeNode.getNodeIndex() > 0) {
				// process each unique statement only once
				sNodes = sNodesList.get(sTreeNode.getNodeIndex() - 1);
				stmt = PatternUtil.createStrValue(sTreeNode.getNodeType(),
						sTreeNode.getStrValue(), sNodes);
				sTreeNode.setStrValue(stmt);
				sNodes = sNodesList.get(sTreeNode.getNodeIndex() - 1);
				PatternUtil.collectTerms(alphabetSet, sNodes);
				if (!Term.Abs_And_Exact_Pattern.matcher(stmt).matches()) {
					stmtSet.add(stmt);// this statement is matched perfectly, so
					// put it into the stmtSet
				}
			}
		}
	}

	public static SimpleTreeNode normalizeTree(SimpleTreeNode sTree,
			List<List<SimpleASTNode>> sNodesList) {
		SimpleTreeNode root = null;
		SimpleTreeNode tmp1 = null;
		SimpleTreeNode tmp2 = null;
		SimpleTreeNode child1 = null;
		SimpleTreeNode child2 = null;
		Enumeration<SimpleTreeNode> cEnum = null;
		Enumeration<SimpleASTNode> astEnum = null;
		SimpleASTNode astTmp = null;
		Queue<SimpleTreeNode> queue1 = new LinkedList<SimpleTreeNode>();
		Queue<SimpleTreeNode> queue2 = new LinkedList<SimpleTreeNode>();
		queue1.add(sTree);
		root = (SimpleTreeNode) sTree.clone();
		root.setStrValue(PatternUtil.createStrValue(root.getNodeType(),
				root.getStrValue(), sNodesList.get(root.getNodeIndex() - 1)));
		queue2.add(root);

		while (!queue1.isEmpty()) {
			tmp1 = queue1.remove();
			tmp2 = queue2.remove();
			cEnum = tmp1.children();
			while (cEnum.hasMoreElements()) {
				child1 = cEnum.nextElement();
				child2 = (SimpleTreeNode) child1.clone();
				if (child1.getChildCount() == 1
						&& ((SimpleTreeNode) child1.getChildAt(0))
								.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION) {
					SimpleASTNode sNode = new SimpleASTNode(sNodesList.get(
							child2.getNodeIndex() - 1).get(0));
					SimpleAnonymousClassDeclarationFinder sacdFinder = new SimpleAnonymousClassDeclarationFinder();
					astTmp = sacdFinder.find(sNode);

					SimpleASTNode tmpParent = astTmp;
					while (tmpParent.getParent() != null) {
						tmpParent = (SimpleASTNode) tmpParent.getParent();
						tmpParent.setRecalc();
					}
					astTmp.removeFromParent();
					tmpParent.constructStrValue();

					List<SimpleASTNode> tmpList = new ArrayList<SimpleASTNode>();
					tmpList.add(sNode);
					child2.setStrValue(PatternUtil.createStrValue(
							child2.getNodeType(), child2.getStrValue(), tmpList));
					SimpleTreeNode subroot = normalizeTree(
							(SimpleTreeNode) child1.getChildAt(0), sNodesList);
					child2.add(subroot);
				} else {
					child2.setStrValue(PatternUtil.createStrValue(
							child2.getNodeType(), child2.getStrValue(),
							sNodesList.get(child2.getNodeIndex() - 1)));
					queue1.add(child1);
					queue2.add(child2);
				}
				tmp2.add(child2);
			}
		}
		return root;
	}

	/**
	 * To create a CodePattern for each cluster
	 * 
	 * @param nodeIndexes
	 * @param sTree
	 * @param sNodesList
	 * @param simpleExprsLists
	 * @param cluster
	 */
	private static void createCodePattern(SimpleTreeNode sTree,
			List<List<SimpleASTNode>> sNodesList,
			List<List<List<SimpleASTNode>>> simpleExprsLists,
			EditInCommonCluster cluster, EditInCommonGroup group) {
		// System.out.print("");
		CodePattern pat = new CodePattern(cluster);
		cluster.setCodePattern(pat);
		Set<Term> terms = pat.getAlphabetSet();
		Set<String> stmts = pat.getStmtSet();
		// 1. create body pattern
		pat.sTree = sTree;
		pat.simpleASTNodesList = sNodesList;
		// 2. create alphabet set and stmt set
		normalizePattern(sTree, sNodesList, cluster, terms, stmts);
		// 3. limit scope
		limitScope(group, pat);
	}

	private static STreeEditScript createEditScript(SimpleTreeNode sTree,
			SimpleTreeNode sTree2, List<Integer> nodeIndexes,
			List<List<SimpleASTNode>> sNodesList, List<ChangeSummary> chgSums,
			List<List<List<SimpleASTNode>>> simpleExprsLists,
			List<AbstractTreeEditOperation> edits, List<Integer> indexes,
			Map<Node, Integer> nodeIndexMap) {
		SimpleTreeNode newSTree = new SimpleTreeNode(sTree);
		STreeEditCreator creator = new STreeEditCreator();

		newSTree = creator.updateInsertMove(newSTree, sTree2, chgSums,
				simpleExprsLists, indexes, nodeIndexMap, edits, nodeIndexes,
				sNodesList);

		if (newSTree != null) {// there are some delete operations
			newSTree = creator.deletes(newSTree, chgSums, nodeIndexes);
		}
		if (newSTree != null) { // there are some left move operations
			creator.moves2(newSTree, sTree2, chgSums, simpleExprsLists,
					indexes, nodeIndexMap, edits, nodeIndexes, sNodesList);
		}

		STreeEditScript editScript = new STreeEditScript(
				creator.updateInsertMoveMap, creator.deleteMap, nodeIndexes,
				sNodesList);
		return editScript;
	}

	public static String createProgramTransformations(
			EditInCommonCluster cluster, EditInCommonGroup group) {
		// System.out.print("");
		String contextCode = null;
		List<Sequence> sequenceList = cluster.getSequenceList();
		if (sequenceList == null || sequenceList.get(0).isEmpty())
			return contextCode;
		List<AbstractTreeEditOperation> edits = null;
		List<ChangeSummary> chgSums = cluster.getConChgSum();
		List<List<List<SimpleASTNode>>> simpleExprsLists = cluster
				.getSimpleExprsLists();
		SimpleTreeNode sTree = null, sTree2 = null;
		SimpleTreeNode embeddedTree = null;
		List<Integer> nodeIndexes = null;
		List<Integer> indexes = null;
		Map<String, String> specificToUnified = cluster
				.getSpecificToUnifiedList().get(0);
		edits = group.getMMList().get(cluster.getInstances().get(0)).getEdits();

		oldNode = (Node) ((Node) edits.get(0).getParentNode().getRoot())
				.deepCopy();
		updatedNode =  (Node) edits.get(edits.size() - 2).getNode()
				.deepCopy();
		newNode = (Node)edits.get(edits.size() - 1).getNode()
				.deepCopy();
		Map<String, String> uMap = SimpleASTNode.createUMap(specificToUnified);

		System.out.print("");
		SimpleASTCreator creator = new SimpleASTCreator();
		List<List<SimpleASTNode>> newSNodesList = creator
				.createSimpleASTNodesList(newNode);

		if (EnhancedChangeAssistantMain.ABSTRACT_ALL) {
			IdGeneralizer generalizer = new IdGeneralizer();
			generalizer.generalize(specificToUnified,
					cluster.getSimpleASTNodesList(0));
			generalizer.generalize(specificToUnified, newSNodesList);
			Map<String, String> unifiedToSpecific = cluster
					.getUnifiedToSpecificList().get(0);
			Map<String, String> newUToS = IdMapper
					.createReverseMap(specificToUnified);
			unifiedToSpecific.clear();
			unifiedToSpecific.putAll(newUToS);
			List<List<List<SimpleASTNode>>> tmpSimpleExprsLists = new ArrayList<List<List<SimpleASTNode>>>();
			for (int i = 0; i < simpleExprsLists.size(); i++) {
				tmpSimpleExprsLists.add(SimpleASTNode.customize(
						specificToUnified, uMap, simpleExprsLists.get(i)));
			}
			cluster.setExprsLists(tmpSimpleExprsLists);
			simpleExprsLists = tmpSimpleExprsLists;
		}

		List<List<SimpleASTNode>> sNodesList = SimpleASTNode.customize(
				specificToUnified, uMap, cluster.getSimpleASTNodesList(0));
		sTree = new SimpleTreeNode(cluster.getSTrees().get(0));
		sTree2 = new SimpleTreeNode(cluster.getSTrees2().get(0));

		nodeIndexes = new ArrayList<Integer>(sequenceList.get(0)
				.getNodeIndexes());
		indexes = cluster.getIndexesList().get(0);

		// 1. label edited node in the tree
		// REQUIREMENT: neither sTree nor sTree2 is normalized in order to
		// match with the original node
		labelTree(sTree, sTree2, nodeIndexes, edits, indexes);

		sTree = normalizeTree(sTree, sNodesList);
		sTree2 = normalizeTree(sTree2,
				SimpleASTNode.customize(specificToUnified, uMap, newSNodesList));

		// 3. generate code for each node of sTree in cluster
		embeddedTree = EmbeddedTreeCreator.createEmbeddedTree(nodeIndexes,
				sTree, sNodesList);
		cluster.setSTree(embeddedTree);
		// maybe need further process
		cluster.setSTree2(sTree2);

		// 5. side effect: also set alphabetSet and stmtSet for the cluster
		createCodePattern(new SimpleTreeNode(embeddedTree),
				new ArrayList<List<SimpleASTNode>>(sNodesList),
				simpleExprsLists, cluster, group);

		// 2. create nodeIndexMap, extend sNodesList when necessary
		Map<Node, Integer> nodeIndexMap = labelNewTree(sTree, sTree2, edits,
				indexes, sNodesList, specificToUnified, uMap, simpleExprsLists,
				nodeIndexes);

		// 2. figure out where to insert/move to, and create edit while
		// extending sequence when necessary
		STreeEditScript abstractEditScript = createEditScript(sTree, sTree2,
				nodeIndexes, sNodesList, chgSums, simpleExprsLists, edits,
				indexes, nodeIndexMap);
		cluster.setAbstractEditScript(abstractEditScript);
		// 4. generate code for each node of sEditedTree in cluster
		// cluster.setSEditedTree(createCode(chgSums, nodeIndexes, sTree,
		// sNodesList, assistStrValues, simpleExprsLists));

		// SimpleTreeNode root = createCode(chgSums, nodeIndexes, sTree,
		// sNodesList, assistStrValues, simpleExprsLists);
		// cluster.setSTree(root);

		// 6. create edit script by integrating the edit information
		// contextCode = buffer.toString();
		return contextCode;
	}

	// private static void insertIndex(SimpleTreeNode enclosingNode,
	// SimpleTreeNode child, List<Integer> nodeIndexes,
	// List<List<SimpleASTNode>> sNodesList, List<ChangeSummary> chgSums,
	// List<List<List<SimpleASTNode>>> simpleExprsLists) {
	// int newNodeIndex = nodeIndexes.size() / 2 + 1;
	// child.setNodeIndex(newNodeIndex);
	// Sequence s = new Sequence((SimpleTreeNode) child.getRoot());
	// List<Integer> newNodeIndexes = s.getNodeIndexes();
	// List<Integer> preIndexes = newNodeIndexes.subList(0,
	// newNodeIndexes.indexOf(newNodeIndex));
	// LongestCommonSubsequence<Integer> lcs = new
	// LongestCommonSubsequence<Integer>();
	// lcs.getLCS(nodeIndexes, preIndexes);
	// List<Integer> leftIndexes = lcs.getLeftCSIndexes();
	// int insertAfterAnchor = nodeIndexes.get(leftIndexes.get(leftIndexes
	// .size() - 1));
	// nodeIndexes.add(insertAfterAnchor + 1, newNodeIndex);
	// nodeIndexes.add(insertAfterAnchor + 2, -newNodeIndex);
	// Enumeration<SimpleTreeNode> childEnum = enclosingNode.children();
	// int insertAt = 0, tmpIndex = -1;
	// while (childEnum.hasMoreElements()) {
	// tmpIndex = childEnum.nextElement().getNodeIndex();
	// if (tmpIndex > 0 && leftIndexes.contains(tmpIndex)) {
	// insertAt++;
	// } else {
	// break;
	// }
	// }
	// enclosingNode.insert((SimpleTreeNode) child.clone(), insertAt);
	// int firstEditIndex = child.getEditIndexes().nextSetBit(0);
	// List<List<SimpleASTNode>> insertedStmts = null;
	// if (chgSums.get(firstEditIndex).editType.equals(EDIT.INSERT)) {
	// insertedStmts = simpleExprsLists.get(firstEditIndex);
	// child.setType(SimpleTreeNode.INSERTED);
	// }
	// if (newNodeIndex > sNodesList.size()) {
	// for (int i = sNodesList.size(); i < newNodeIndex; i++) {
	// sNodesList.add(null);
	// }
	// sNodesList.add(insertedStmts.get(0));
	// }
	//
	// }

	private static boolean labelDescendant(SimpleTreeNode sTree,
			List<Integer> indexes, List<Integer> parentEditIndexes, int j) {
		Enumeration<SimpleTreeNode> enumeration = sTree
				.breadthFirstEnumeration();
		Set<Integer> parentBitset = new HashSet<Integer>();
		Set<Integer> interSet = null;
		for (Integer index : parentEditIndexes) {
			parentBitset.add(indexes.indexOf(index));
		}
		SimpleTreeNode sNode = null;

		while (enumeration.hasMoreElements()) {
			sNode = enumeration.nextElement();
			interSet = new HashSet<Integer>(sNode.getEditIndexes());
			interSet.retainAll(parentBitset);
			if (!interSet.isEmpty()) {
				sNode.addEntryToEditAndRoletype(j, SimpleTreeNode.CONTEXTUAL);
				return true;
			}
		}
		return false;
	}

	/**
	 * all nodes in sTree2 are indexed as 0 sNodesList is extended when
	 * nodeIndexMap is extended
	 * 
	 * @param sTree
	 * @param sTree2
	 * @param edits
	 * @param indexes
	 * @return
	 */
	protected static Map<Node, Integer> labelNewTree(SimpleTreeNode sTree,
			SimpleTreeNode sTree2, List<AbstractTreeEditOperation> edits,
			List<Integer> indexes, List<List<SimpleASTNode>> sNodesList,
			Map<String, String> specificToUnified, Map<String, String> uMap,
			List<List<List<SimpleASTNode>>> simpleExprsLists,
			List<Integer> nodeIndexes) {
		Enumeration<SimpleTreeNode> sEnum = null;
		Enumeration<Node> nEnum = null;
		Map<Node, Integer> nodeIndexMap = new HashMap<Node, Integer>();
		int nodeCounter = 0;
		int nodeIndex = 0;
		Map<Node, Node> updateNewToOld = new HashMap<Node, Node>();
		Map<Node, Integer> insertIndexMap = new HashMap<Node, Integer>();
		AbstractTreeEditOperation edit = null;
		Node node = null;
		Node intermediateNode = null;
		Node originalNode = null;
		SimpleTreeNode sNode = null;
		nEnum = oldNode.breadthFirstEnumeration();
		sEnum = sTree.breadthFirstEnumeration();
		while (nEnum.hasMoreElements()) {
			node = nEnum.nextElement();
			sNode = sEnum.nextElement();
			nodeIndexMap.put(node, sNode.getNodeIndex());
			nodeCounter++;
		}
		for (int i = 0; i < indexes.size(); i++) {
			edit = edits.get(indexes.get(i));
			if (edit.getOperationType().equals(EDIT.UPDATE)) {
				updateNewToOld.put(((UpdateOperation) edit).getNewNode(),
						((UpdateOperation) edit).getNode());
			} else if (edit.getOperationType().equals(EDIT.INSERT)) {
				insertIndexMap.put(edit.getNode(), i);
			}
		}

		nEnum = newNode.breadthFirstEnumeration();
		sEnum = sTree2.breadthFirstEnumeration();
		while (nEnum.hasMoreElements()) {
			node = nEnum.nextElement();
			sNode = sEnum.nextElement();
			intermediateNode = (Node) updatedNode
					.lookforNodeBasedOnPosition(node);
			originalNode = (Node) oldNode
					.lookforNodeBasedOnRange(intermediateNode);
			if (originalNode != null) {
				if (nodeIndexMap.get(originalNode) != null) {
					nodeIndex = nodeIndexMap.get(originalNode);
					// if (nodeIndexes.contains(nodeIndex))
					sNode.setNodeIndex(nodeIndex);
				}
			} else if (updateNewToOld.get(node) != null) {
				originalNode = updateNewToOld.get(node);
				if (nodeIndexMap.get(originalNode) != null)
					sNode.setNodeIndex(nodeIndexMap.get(updateNewToOld
							.get(node)));
			} else if (insertIndexMap.containsKey(node)) {// the node is newly
															// inserted
				int newIndex = ++nodeCounter;
				nodeIndexMap.put(node, newIndex);
				sNode.setNodeIndex(newIndex);
				sNodesList.add(SimpleASTNode.customizeSingleNode(
						specificToUnified, uMap,
						simpleExprsLists.get(insertIndexMap.get(node)).get(0)));
			}
		}
		return nodeIndexMap;
	}

	/**
	 * To label the tree with edits except for inserted nodes
	 * 
	 * @param cluster
	 * @param sTree
	 * @param sTree2
	 * @param nodeIndexes
	 * @param edits
	 * @return
	 */
	public static void labelTree(SimpleTreeNode sTree, SimpleTreeNode sTree2,
			List<Integer> nodeIndexes, List<AbstractTreeEditOperation> edits,
			List<Integer> indexes) {
		for (Integer index : indexes) {
			labelTree(edits.get(index), sTree, sTree2, indexes.indexOf(index),
					indexes, edits);
		}
	}

	/**
	 * label edit indexes for each edited node
	 * 
	 * @param op
	 * @param sTree
	 *            the tree labeled with edit indexes
	 * @param sTree2
	 *            the tree without any labeling
	 * @param j
	 * @param indexes
	 * @return
	 */
	private static boolean labelTree(AbstractTreeEditOperation op,
			SimpleTreeNode sTree, SimpleTreeNode sTree2, int j,
			List<Integer> indexes, List<AbstractTreeEditOperation> edits) {
		SimpleTreeNode sNode = null, iNode = null;
		Node pnode = null;
		System.out.print("");
		switch (op.getOperationType()) {
		case INSERT:
			pnode = op.getParentNode();
			sNode = sTree.lookforNodeBasedOnRange(pnode);
			if (sNode != null) {
				sNode.addEntryToEditAndRoletype(j, SimpleTreeNode.CONTEXTUAL);
			} else {
				// this is a newly inserted node which does not exist in the old
				// version
				List<Integer> knownParentEditIndexes = new ArrayList<Integer>(
						pnode.getEditIndexes());
				knownParentEditIndexes.retainAll(indexes);
				if (!knownParentEditIndexes.isEmpty()) {// the edit is within
														// the
														// knownParentEditIndexes
					// first try to label the old tree; if fail, then try to
					// label the new tree
					if (!labelDescendant(sTree, indexes,
							knownParentEditIndexes, j))
						labelDescendant(sTree2, indexes,
								knownParentEditIndexes, j);
				} else {
					sNode = sTree2.lookforNodeBasedOnRange(pnode);
					if (sNode != null) {// the parent is inserted, although it
										// is not included as common edit
						sNode.addEntryToEditAndRoletype(j,
								SimpleTreeNode.INSERTED_CONTEXTUAL);
					} else { // the parent is updated or moved from some node
						AbstractTreeEditOperation tmpOp = null;
						for (Integer index : pnode.getEditIndexes()) {
							tmpOp = edits.get(index);
							if (tmpOp.getOperationType().equals(
									ITreeEditOperation.EDIT.UPDATE)) {
								sNode = sTree.lookforNodeBasedOnRange(tmpOp
										.getNode());
								if (sNode != null) {
									sNode.addEntryToEditAndRoletype(j,
											SimpleTreeNode.CONTEXTUAL);
								} else {
									System.out
											.println("More process is needed");
								}
							} else if (tmpOp.getOperationType().equals(
									ITreeEditOperation.EDIT.MOVE)) {
								System.out.println("More process is needed");
							}
						}
					}
				}
			}
			break;
		case DELETE:
		case UPDATE:
		case MOVE:
			sNode = sTree.lookforNodeBasedOnRange(op.getNode());
			if (sNode != null) {
				sNode.addEntryToEditAndRoletype(j, SimpleTreeNode.EDITED);
			}
			if (op.getOperationType().equals(EDIT.MOVE)) {
				Node node = updatedNode
						.lookforNodeBasedOnRange(((MoveOperation) op)
								.getNewParentNode());
				sNode = sTree.lookforNodeBasedOnRange(node);
				if (sNode != null) {
					sNode.addEntryToEditAndRoletype(j,
							SimpleTreeNode.CONTEXTUAL);
				} else {
					List<Integer> knownParentEditIndexes = new ArrayList<Integer>(
							node.getEditIndexes());
					knownParentEditIndexes.retainAll(indexes);
					if (!knownParentEditIndexes.isEmpty()) {
						labelDescendant(sTree2, indexes,
								knownParentEditIndexes, j);
					} else {
						sNode = sTree2.lookforNodeBasedOnRange(node);
						sNode.addEntryToEditAndRoletype(j,
								SimpleTreeNode.INSERTED_CONTEXTUAL);
					}
				}
			}
			break;
		}
		return true;
	}

	private static void limitScope(EditInCommonGroup group, CodePattern pat) {
		EditInCommonCluster cluster = pat.getCluster();
		List<Integer> insts = cluster.getInstances();
		List<MethodModification> mmList = group.getMMList();
		List<ChangedMethodADT> adts = new ArrayList<ChangedMethodADT>();
		for (Integer inst : insts) {
			adts.add(mmList.get(inst).originalMethod);
		}
		NamingPatternCreator.process(adts, pat);

	}

	// protected static void createScript(List<Integer> nodeIndexes,
	// SimpleTreeNode root, List<ChangeSummary> chgSums,
	// List<String> assistStrValues,
	// List<List<List<SimpleASTNode>>> simpleExprsLists,
	// List<List<SimpleASTNode>> sNodesList) {
	// STreeEditCreator creator = new STreeEditCreator();
	// List<AbstractTreeEditOperation2<SimpleTreeNode>> edits = creator
	// .createEdits(nodeIndexes, root, chgSums, assistStrValues,
	// simpleExprsLists, sNodesList);

	// if (root.getNodeIndex() != -1) {
	// if (!root.getEditIndexes().isEmpty()) {
	// bs = root.getEditIndexes();
	// for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
	// cs = chgSums.get(i);
	// buffer.append(cs.editType.toString()).append(": ")
	// .append(root.getStrValue());
	// switch (cs.editType) {
	// case UPDATE:
	// buffer.append(" TO: ").append(assistStrValues.get(i));
	// break;
	// case MOVE:
	// buffer.append(" TO SOMEWHERE UNDER: ").append(
	// assistStrValues.get(i));
	// break;
	// }
	// }
	// } else {
	// buffer.append(root.getStrValue());
	// }
	// }
	// if (root.getChildCount() > 0) {
	// Enumeration<SimpleTreeNode> sEnum = root.children();
	// buffer.append("{").append("\n");
	// while (sEnum.hasMoreElements()) {
	// createScript(sEnum.nextElement(), buffer, chgSums,
	// assistStrValues);
	// }
	// buffer.append("}").append("\n");
	// } else {
	// switch (root.getNodeType()) {
	// case ASTNode.IF_STATEMENT:
	// buffer.append("{\n").append("...\n").append("}\n");
	// break;
	// default:
	// buffer.append("\n");
	// break;
	// }
	// }
	// }
}
