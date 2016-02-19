package changeassistant.change.group;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.crystal.analysis.DefUseAnalysisFactory;
import changeassistant.crystal.analysis.PostDominateAnalysisFactory;
import changeassistant.crystal.analysis.def.DefUseElementResult;
import changeassistant.crystal.analysis.postdominate.PostDominateElementResult;
import changeassistant.internal.MethodADT;
import changeassistant.internal.NodeASTMapping;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.DeleteOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;
import changeassistant.versions.treematching.edits.InsertOperation;
import changeassistant.versions.treematching.edits.MoveOperation;

/**
 * Each group manager corresponds to one changed method
 * 
 * @author ibm
 * 
 */
public class GroupManager {

	public static final boolean isParentConsidered = true;

	public static final int LatestUpdatedOldRevision = -2;

	public static final int NewRevision = -1;

	private ProjectResource prLeft, prRight;

	public MethodModification mm;

	public List<AbstractTreeEditOperation> edits;

	private List<ChangeGroup> changeGroups;

	private DefUseElementResult oldMethodAnalysisResult,
			newMethodAnalysisResult;

	private PostDominateElementResult oldPostDominateElementResult,
			newPostDominateElementResult;

	private MethodDeclaration oMd, nMd;

	private Node oldNode, updatedOldNode, newNode;

	private ICompilationUnit oIcu, nIcu;

	private Map<ASTNode, Node> astNodeMap;

	private Map<Node, WeakReference<List<Node>>> nodeAndControlDependingNodes,
			nodeAndDataDependingNodes, nodeAndDeletedNodes;
	// public Set<NodePair> cEdges, dEdges;
	public static int SOURCE = 1, SINK = 2;

	private Set<Node> deletedNodes;

	public GroupManager() {
	}

	public GroupManager(MethodModification mm, ProjectResource prLeft,
			ProjectResource prRight) {
		this.mm = mm;
		this.edits = mm.getEdits();
		this.prLeft = prLeft;
		this.prRight = prRight;
		init();
	}

	public void clearCache() {
		this.deletedNodes = null;
		this.nodeAndControlDependingNodes = null;
		this.nodeAndDataDependingNodes = null;
		this.nodeAndDeletedNodes = null;
	}

	// public void addCDependence(List<Node> list, Node nodeConcerned, int
	// direction){
	// List<Node> localList = list;
	// NodePair edge;
	// if(direction == SOURCE){
	// for(Node local : localList){
	// edge = new NodePair(nodeConcerned, local);
	// cEdges.add(edge);
	// }
	// }else{//direction == TARGET
	// for(Node local : localList){
	// edge = new NodePair(local, nodeConcerned);
	// cEdges.add(edge);
	// }
	// }
	// }

	// public void addDDependence(List<Node> list, Node nodeConcerned, int
	// direction){
	// List<Node> localList = new ArrayList<Node>(list);
	// if(localList.contains(nodeConcerned))
	// localList.remove(nodeConcerned);
	// NodePair edge;
	// if(direction == SOURCE){
	// for(Node local : localList){
	// edge = new NodePair(nodeConcerned, local);
	// dEdges.add(edge);
	// }
	// }else{
	// for(Node local : localList){
	// edge = new NodePair(local, nodeConcerned);
	// dEdges.add(edge);
	// }
	// }
	// }

	private List<ChangeGroup> constructChangeGroupsForDelete() {// each group
																// corresponds
																// to one DELETE
		List<ChangeGroup> groups = new ArrayList<ChangeGroup>();
		List<Node> tempList;
		Node nodeConcerned = null;
		Set<Node> nodesDependingOn = null;
		for (int i = 0; i < edits.size(); i++) {
			nodesDependingOn = new HashSet<Node>();
			if (edits.get(i).getOperationType().equals(EDIT.DELETE)) {
				nodeConcerned = edits.get(i).getNode();
				// System.out.println(edits.get(i).getNode().getSourceCodeRange().startPosition);
				nodeConcerned = oldNode.lookforNodeBasedOnRange(nodeConcerned);

				if (nodeConcerned == null) {
					System.out.println("The deleted node is not found!");
				}
				nodeConcerned.setEditIndex(i);

				tempList = findControlDependingNodesInOldRevision(nodeConcerned);
				nodesDependingOn.addAll(tempList
				/* findControlDependingNodesInOldRevision(nodeConcerned) */);
				tempList = null;

				tempList = findDataDependingNodes(nodeConcerned);
				nodesDependingOn.addAll(tempList
				/* findDataDependingNodes(nodeConcerned) */);
				tempList = null;
				nodesDependingOn.add((Node) nodeConcerned.getParent());
				nodesDependingOn.add(nodeConcerned);
				groups.add(new ChangeGroup(this, nodesDependingOn));
			}
			nodesDependingOn = null;
		}
		return groups;
	}

	/**
	 * To filter out change groups which do not have any relevant AST nodes in
	 * the original method
	 */
	private void filterGroups() {
		boolean isChanged = false;// this is a "changed" marker
		for (ChangeGroup changeGroup : changeGroups) {
			if (changeGroup.knownNodesInOriginalRevision.isEmpty()) {
				changeGroup.setToInvalid();
				isChanged = true;
			}
		}
		if (isChanged) {
			List<ChangeGroup> tempChangeGroups = new ArrayList<ChangeGroup>();
			for (ChangeGroup changeGroup : changeGroups) {
				if (changeGroup.isValid) {
					tempChangeGroups.add(changeGroup);
				}
			}
			changeGroups = tempChangeGroups;
		}
	}

	private void filterGroups2() {
		boolean isChanged = false;
		for (ChangeGroup changeGroup : changeGroups) {
			if (changeGroup.knownNodesInNewRevision.isEmpty()) {
				changeGroup.setToInvalid();
				isChanged = true;
			}
		}
		if (isChanged) {
			List<ChangeGroup> tempChangeGroups = new ArrayList<ChangeGroup>();
			for (ChangeGroup changeGroup : changeGroups) {
				if (changeGroup.isValid) {
					tempChangeGroups.add(changeGroup);
				}
			}
			changeGroups = tempChangeGroups;
		}
	}

	public List<ChangeGroup> getChangeGroups() {
		return this.changeGroups;
	}

	public Node getNewNode() {
		return this.newNode;
	}

	public Node getOldNode() {
		return this.oldNode;
	}

	public Node getUpdatedOldNode() {
		return this.updatedOldNode;
	}

	public void groupBasedOnNewRevision() {
		List<Node> tempList = new ArrayList<Node>();
		try {
			Enumeration<Node> enumeration = updatedOldNode
					.breadthFirstEnumeration();
			Set<Node> nodesDependingOn;
			Node node;
			astNodeMap = NodeASTMapping.createASTNodeMap(nMd, newNode);
			prepareCache();
			while (enumeration.hasMoreElements()) {
				node = enumeration.nextElement();
				nodesDependingOn = new HashSet<Node>();
				switch (node.getEDITED_TYPE()) {
				case MOVED:
				case INSERTED:
					if (node.getParent() != null)
						nodesDependingOn.add((Node) node.getParent());
					// nodesDependingOn.addAll(findDeletedPreviousSiblings(node));
				case UPDATED: {
					tempList = findControlDependingNodesInNewRevision(node);
					nodesDependingOn.addAll(tempList/*
													 * findControlDependingNodesInNewRevision
													 * (node)
													 */);

					tempList = findDataDependingNodesInNewRevision(node);
					nodesDependingOn.addAll(tempList/*
													 * findDataDependingNodesInNewRevision
													 * (node)
													 */);

					nodesDependingOn.add(node);
					changeGroups.add(new ChangeGroup(this, nodesDependingOn));
				}
					break;
				}
				nodesDependingOn = null;
			}
			astNodeMap.clear();
			clearCache();
			this.newPostDominateElementResult = null;
			this.newMethodAnalysisResult = null;
			updateGroups();
		} catch (Exception e) {
			e.printStackTrace();
		}
		tempList = null;
	}

	private void init() {
		changeGroups = new ArrayList<ChangeGroup>();
		MethodADT methodADT = mm.originalMethod;
		ClassContext CC = prLeft.findClassContext(methodADT.classname);
		oIcu = JavaCore.createCompilationUnitFrom(prLeft
				.getFile(CC.relativeFilePath));
		oldNode = (Node) edits.get(0).getParentNode().getRoot();
		oMd = oldNode.getMethodDeclaration();

		methodADT = mm.newMethod;
		CC = prRight.findClassContext(methodADT.classname);
		nIcu = JavaCore.createCompilationUnitFrom(prRight
				.getFile(CC.relativeFilePath));
		newNode = edits.get(edits.size() + NewRevision).getNode();
		nMd = newNode.getMethodDeclaration();

		updatedOldNode = edits.get(edits.size() + LatestUpdatedOldRevision)
				.getNode();

		// cEdges = new HashSet<NodePair>();
		// dEdges = new HashSet<NodePair>();
	}

	private void merge(ChangeGroup group1, ChangeGroup group2) {
		List<Integer> edits2 = group2.getEditsInvolved();
		List<Integer> edits1 = group1.getEditsInvolved();
		List<Integer> combined = new ArrayList<Integer>();
		int j = 0;
		int i = 0;
		int size1 = edits1.size();
		int size2 = edits2.size();
		while (i < size1 && j < size2) {
			if (edits1.get(i).intValue() < edits2.get(j).intValue()) {
				combined.add(edits1.get(i));
				i++;
			} else if (edits1.get(i).intValue() > edits2.get(j).intValue()) {
				combined.add(edits2.get(j));
				j++;
			} else {
				combined.add(edits1.get(i));
				i++;
				j++;
			}
		}
		while (i < size1) {
			combined.add(edits1.get(i));
			i++;
		}
		while (j < size2) {
			combined.add(edits2.get(j));
			j++;
		}
		group1.getEditsInvolved().clear();
		group1.getEditsInvolved().addAll(combined);
		group1.knownGroup.addAll(group2.knownGroup);
		group2.setToInvalid();
	}

	/**
	 * Combine all groups together
	 */
	public void modelSubTree() {
		if (changeGroups.size() > 1) {// To guarantee there is only one group
										// left
			ChangeGroup group1 = changeGroups.get(0);
			for (int i = 1; i < changeGroups.size(); i++) {
				ChangeGroup group2 = changeGroups.get(i);
				merge(group1, group2);
				group1.knownNodesInOriginalRevision
						.addAll(group2.knownNodesInOriginalRevision);
				group2.knownNodesInOriginalRevision.clear();
			}
			filterGroups();
		}
		ChangeGroup changeGroup = changeGroups.get(0);// there is only one
														// element in the list
		changeGroup.constructSubTree();

		changeGroup.refineTransformationRules();
		// for(ChangeGroup changeGroup : changeGroups){
		// changeGroup.constructSubTree();//create sub tree with known node
		// group
		// changeGroup.refineTransformationRules();
		// }
	}

	public void modelSubTree2() {
		if (changeGroups.size() > 1) {
			ChangeGroup group1 = changeGroups.get(0);
			for (int i = 1; i < changeGroups.size(); i++) {
				ChangeGroup group2 = changeGroups.get(i);
				merge(group1, group2);
				group1.knownNodesInNewRevision
						.addAll(group2.knownNodesInNewRevision);
				group2.knownNodesInNewRevision.clear();
			}
			filterGroups2();
		}
		for (ChangeGroup changeGroup : changeGroups) {
			changeGroup.constructSubTree2();// create sub tree with known node
											// group
			changeGroup.refineTransformationRules2();
		}
	}

	public void refineGroupsBasedOnOldRevision() {
		astNodeMap = NodeASTMapping.createASTNodeMap(oMd, oldNode);
		prepareCache();
		for (ChangeGroup changeGroup : changeGroups) {
			changeGroup.groupBasedOnOldRevision();
		}
		changeGroups.addAll(constructChangeGroupsForDelete());

		astNodeMap.clear();
		clearCache();
		this.oldPostDominateElementResult = null;
		this.oldMethodAnalysisResult = null;

		updateGroups();
		for (ChangeGroup changeGroup : changeGroups) {
			changeGroup.collectKnownNodesInOldMethod();// set all
														// knownNodesInOldeMethod
														// for each changeGroup
		}
		filterGroups();
	}

	public void refineGroupsBasedOnOldRevision2() {
		astNodeMap = NodeASTMapping.createASTNodeMap(oMd, oldNode);
		prepareCache();
		for (ChangeGroup changeGroup : changeGroups) {
			changeGroup.groupBasedOnOldRevision();
		}
		changeGroups.addAll(constructChangeGroupsForDelete());

		astNodeMap.clear();
		clearCache();
		this.oldPostDominateElementResult = null;
		this.oldMethodAnalysisResult = null;

		updateGroups();
		for (ChangeGroup changeGroup : changeGroups) {
			changeGroup.collectKnownNodesInNewMethod();// set all
														// knownNodesInOldeMethod
														// for each changeGroup
		}
		filterGroups2();
	}

	/**
	 * To merge the groups when necessary, and then compress the redundant
	 * groups
	 */
	private void updateGroups() {
		ChangeGroup group1, group2;
		boolean isChanged = true;
		while (isChanged) {// iteratively merge groups until there are no two
							// groups which can be merged together
			isChanged = false;
			if (changeGroups.size() != 1) {// to mark redundant groups
				for (int i = 0; i < changeGroups.size() - 1; i++) {
					// if group1 and group2 has intersection, join them together
					group1 = changeGroups.get(i);
					if (group1.isValid) {
						for (int j = i + 1; j < changeGroups.size(); j++) {
							group2 = changeGroups.get(j);
							if (group2.isValid
									&& intersect(group1, group2) != null) {
								merge(group1, group2);
								isChanged = true;
							}
						}
					}
				}
			}
			if (isChanged) {
				List<ChangeGroup> tempChangeGroups = new ArrayList<ChangeGroup>();
				for (int i = 0; i < changeGroups.size(); i++) {
					group1 = changeGroups.get(i);
					if (group1.isValid) {// even if no original ASTNode is
											// involved, we still consider it as
											// a possible valid change group
											// since the parameters passed in
											// may be involved
						tempChangeGroups.add(group1);
					}
				}
				changeGroups = null;
				changeGroups = tempChangeGroups;
			} else {
				break;
			}
		}
		if (changeGroups.size() > 1) {
			System.out
					.println("There are multiple changes which cannot be put into the same group");
		}
	}

	public List<Node> findControlDependingNodesInOldRevision(Node node) {
		if (nodeAndControlDependingNodes.containsKey(node)
				&& nodeAndControlDependingNodes.get(node).get() != null) {
			return nodeAndControlDependingNodes.get(node).get();
		}
		if (oldPostDominateElementResult == null) {
			oldPostDominateElementResult = PostDominateAnalysisFactory
					.getInstance().getAnalysisResultForMethod(oIcu, oMd);
			oldPostDominateElementResult.init(oldNode);
		}

		List<Node> cdNodes = new ArrayList<Node>(
				oldPostDominateElementResult.getNodeControlDependence(node));
		// graphHelper.addCDependence(oldPostDominateElementResult.fUpstreamSeeds,
		// node, SINK);
		// graphHelper.addCDependence(oldPostDominateElementResult.fDownstreamSeeds,
		// node, SOURCE);
		// addCDependence(new
		// ArrayList<Node>(oldPostDominateElementResult.fUpstreamSeeds),
		// node, SINK);
		// addCDependence(new
		// ArrayList<Node>(oldPostDominateElementResult.fDownstreamSeeds),
		// node, SOURCE);

		nodeAndControlDependingNodes.put(node, new WeakReference<List<Node>>(
				cdNodes));
		return cdNodes;
	}

	/**
	 * return nodes found in the new revision which are mapped back to the old
	 * version
	 * 
	 * @param node
	 * @return
	 */
	public List<Node> findControlDependingNodesInNewRevision(Node node) {
		if (nodeAndControlDependingNodes.containsKey(node)
				&& nodeAndControlDependingNodes.get(node).get() != null) {
			return nodeAndControlDependingNodes.get(node).get();
		}
		if (newPostDominateElementResult == null) {
			newPostDominateElementResult = PostDominateAnalysisFactory
					.getInstance().getAnalysisResultForMethod(nIcu, nMd);
			newPostDominateElementResult.init(newNode);
		}
		System.out.print("");
		Node mappedNewNode = (Node) newNode.lookforNodeBasedOnPosition(node);
		// find all nodes control dependent on by current node
		Set<Node> nodesControlDepending = newPostDominateElementResult
				.getNodeControlDependence(mappedNewNode);
		// addCDependence(new
		// ArrayList<Node>(newPostDominateElementResult.fUpstreamSeeds),
		// mappedNewNode, SINK);
		// addCDependence(new
		// ArrayList<Node>(newPostDominateElementResult.fDownstreamSeeds),
		// mappedNewNode, SOURCE);

		List<Node> cdNodes = new ArrayList<Node>();
		for (Node nodeControlDepending : nodesControlDepending) {
			cdNodes.add((Node) updatedOldNode
					.lookforNodeBasedOnPosition(nodeControlDepending));
		}
		nodeAndControlDependingNodes.put(node, new WeakReference<List<Node>>(
				cdNodes));
		return cdNodes;
	}

	public List<Node> findDataDependingNodes(Node node) {
		if (nodeAndDataDependingNodes.containsKey(node)
				&& nodeAndDataDependingNodes.get(node).get() != null) {
			return nodeAndDataDependingNodes.get(node).get();
		}
		if (oldMethodAnalysisResult == null) {
			oldMethodAnalysisResult = DefUseAnalysisFactory.getInstance()
					.getAnalysisResultForMethod(oIcu, oMd);
			oldMethodAnalysisResult.init(oldNode);
		}
		List<Node> ddNodes = new ArrayList<Node>(
				oldMethodAnalysisResult.getNodeDataDependence(node));
		// graphHelper.addDDependence(
		// oldMethodAnalysisResult.fUpstreamSeeds, node, SINK, true);
		// graphHelper.addDDependence(
		// oldMethodAnalysisResult.fDownstreamSeeds, node, SOURCE, true);
		// graphHelper.addDDependence(
		// oldMethodAnalysisResult.fUpstreamSeeds2, node, SINK, false);
		// graphHelper.addDDependence(
		// oldMethodAnalysisResult.fDownstreamSeeds2, node, SOURCE, false);
		// addDDependence(new
		// ArrayList<Node>(oldMethodAnalysisResult.fUpstreamSeeds), node, SINK);
		// addDDependence(new
		// ArrayList<Node>(oldMethodAnalysisResult.fUpstreamSeeds2), node,
		// SINK);
		// addDDependence(new
		// ArrayList<Node>(oldMethodAnalysisResult.fDownstreamSeeds), node,
		// SOURCE);
		// addDDependence(new
		// ArrayList<Node>(oldMethodAnalysisResult.fDownstreamSeeds2), node,
		// SOURCE);

		nodeAndDataDependingNodes.put(node, new WeakReference<List<Node>>(
				ddNodes));
		return ddNodes;
	}

	public List<Node> findDataDependingNodesInNewRevision(Node node) {
		if (nodeAndDataDependingNodes.containsKey(node)
				&& nodeAndDataDependingNodes.get(node).get() != null) {
			return nodeAndDataDependingNodes.get(node).get();
		}
		if (newMethodAnalysisResult == null) {
			newMethodAnalysisResult = DefUseAnalysisFactory.getInstance()
					.getAnalysisResultForMethod(nIcu, nMd);
			newMethodAnalysisResult.init(newNode);
		}
		Node mappedNewNode = (Node) newNode.lookforNodeBasedOnPosition(node);
		Set<Node> nodesDependingOn = newMethodAnalysisResult
				.getNodeDataDependence(mappedNewNode);
		// addDDependence(new
		// ArrayList<Node>(newMethodAnalysisResult.fUpstreamSeeds), node, SINK);
		// addDDependence(new
		// ArrayList<Node>(newMethodAnalysisResult.fUpstreamSeeds2), node,
		// SINK);
		// addDDependence(new
		// ArrayList<Node>(newMethodAnalysisResult.fDownstreamSeeds), node,
		// SOURCE);
		// addDDependence(new
		// ArrayList<Node>(newMethodAnalysisResult.fDownstreamSeeds2), node,
		// SOURCE);

		List<Node> mappedNodes = new ArrayList<Node>();
		for (Node nodeDependingOn : nodesDependingOn) {
			mappedNodes.add((Node) updatedOldNode
					.lookforNodeBasedOnPosition(nodeDependingOn));
		}
		nodeAndDataDependingNodes.put(node, new WeakReference<List<Node>>(
				mappedNodes));
		return mappedNodes;
	}

	public List<Node> findDeletedPreviousSiblings(Node node) {// node is
																// inserted or
																// moved to
		if (this.deletedNodes.isEmpty())
			return Collections.emptyList();// no deleted node
		if (node.getEDITset() == null)
			return Collections.emptyList();
		if (!node.getEDITset().contains(EDIT.INSERT)
				&& !node.getEDITset().contains(EDIT.MOVE))
			return Collections.emptyList();
		if (nodeAndDeletedNodes.containsKey(node)
				&& nodeAndDeletedNodes.get(node).get() != null) {
			return nodeAndDeletedNodes.get(node).get();
		}

		List<Node> dependedNodes = new ArrayList<Node>();
		Node parent = null;
		int position = -1;
		ITreeEditOperation tempEdit;
		List<Integer> indexes = node.getEditIndexes();
		if (node.getEDITset().contains(EDIT.MOVE)) {
			for (Integer index : indexes) {
				tempEdit = edits.get(index);
				if (tempEdit.getOperationType().equals(
						ITreeEditOperation.EDIT.MOVE)) {
					parent = ((MoveOperation) tempEdit).getNewParentNode();
					position = ((MoveOperation) tempEdit).getNewPosition();
				}
			}
		} else {
			parent = (Node) node.getParent();
			for (Integer index : indexes) {
				tempEdit = edits.get(index);
				if (tempEdit.getOperationType().equals(
						ITreeEditOperation.EDIT.INSERT)) {
					position = ((InsertOperation) tempEdit).getPosition();
				}
			}
		}
		Node tempParent;
		for (Node deletedNode : deletedNodes) {
			tempParent = (Node) deletedNode.getParent();
			if (tempParent.getSourceCodeRange().equals(
					parent.getSourceCodeRange())
					&& tempParent.getStrValue().equals(parent.getStrValue())) {
				if (deletedNode.locationInParent() < position) {
					dependedNodes.add(deletedNode);
				}
			}
		}
		return dependedNodes;
	}

	private void prepareCache() {
		this.nodeAndControlDependingNodes = new HashMap<Node, WeakReference<List<Node>>>();
		this.nodeAndDataDependingNodes = new HashMap<Node, WeakReference<List<Node>>>();
		this.nodeAndDeletedNodes = new HashMap<Node, WeakReference<List<Node>>>();
		this.deletedNodes = new HashSet<Node>();
		ITreeEditOperation tempEdit = null;
		for (int i = 0; i < edits.size(); i++) {
			tempEdit = edits.get(i);
			if (tempEdit instanceof DeleteOperation) {
				deletedNodes
						.add(((DeleteOperation) tempEdit).getNodeToDelete());
			}
		}
	}

	public Set<Node> intersect(ChangeGroup group1, ChangeGroup group2) {
		Set<Node> temp1 = new HashSet<Node>(group1.knownGroup);
		Set<Node> temp2 = new HashSet<Node>(group2.knownGroup);
		temp1.retainAll(temp2);
		if (temp1.isEmpty())
			return null;
		return temp1;
	}
}
