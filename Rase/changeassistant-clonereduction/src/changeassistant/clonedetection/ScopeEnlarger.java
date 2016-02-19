package changeassistant.clonedetection;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.clonereduction.main.CloneReductionException;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.MarkerUtility;
import changeassistant.model.AbstractNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;

public class ScopeEnlarger {

	List<Node> orderedForest = null;
	
	public List<Node> getOrderedForest(){
		return orderedForest;
	}
	/**
	 * To enlargeScope based on marked nodes
	 * @param node
	 * @param context
	 * @return
	 * @throws CloneReductionException 
	 */
	public Set<SourceCodeRange> enlargeScope(Node node, Set<Node> context) throws CloneReductionException {
		Set<SourceCodeRange> includedRanges = new HashSet<SourceCodeRange>();
		Queue<Node> queue = new LinkedList<Node>();
		Set<Node> forests = new HashSet<Node>();
		queue.add(node);
		Node tmp = null;
		Node parent = null;
		Enumeration<Node> nEnum = null;
		int markerIndex = node.getProperty(AbstractNode.MAX_MARKER_INDEX);
		boolean hasSuperConstructor = false;

		if(CloneReductionMain.refactoringAll){
			forests.add(node);
			markDescendants(node, markerIndex);
		}else{
			while (!queue.isEmpty()) {
				tmp = queue.remove();
				switch (tmp.getRole()) {
				case SimpleTreeNode.EDITED:
					parent = (Node) tmp.getParent();
					if (parent != null) {
						if (parent.getRole() == SimpleTreeNode.NONE
								|| parent.getRole() == SimpleTreeNode.CONTEXTUAL
								&& parent.getNodeType() == ASTNode.METHOD_DECLARATION){
							if(tmp.getNodeType() != ASTNode.SUPER_CONSTRUCTOR_INVOCATION){
								forests.add(tmp);
								if(tmp.getChildCount() > 0){
									markerIndex = markDescendants(tmp, markerIndex);
								}
							}else{
								hasSuperConstructor = true;
								break;
							}
						}
					}
					break;
				case SimpleTreeNode.CONTEXTUAL:
					break;
				case SimpleTreeNode.CONTAIN_DOWN_RELEVANT:
//					forests.add(tmp);
					continue;
				case SimpleTreeNode.NONE:
					if (needMarkDescendants(tmp)) {
						markerIndex = markDescendants(tmp, markerIndex);
						forests.add(tmp);
						continue;
					}
					break;
				}
				nEnum = tmp.children();
				while (nEnum.hasMoreElements()) {
					queue.add(nEnum.nextElement());
				}
			}
		}		
		node.setProperty(AbstractNode.MAX_MARKER_INDEX, markerIndex);
		if(forests.size() == 0){
			if(hasSuperConstructor){
				throw new CloneReductionException("Super constructor call cannot be extracted into a method");
			}else
				throw new CloneReductionException("No edited node is found in the new version");
		}
		if (forests.size() > 1) {
			merge(forests, node);
		}
		
		orderedForest = reorder(forests, node);
		nEnum = node.breadthFirstEnumeration();
		while (nEnum.hasMoreElements()) {
			tmp = nEnum.nextElement();
			if (tmp.getRole() != SimpleTreeNode.NONE) {
//				if(tmp.getChildCount() > 0){
//					includedRanges.addAll(tmp.getASTExpressions());
//				}else{
					includedRanges.add(tmp.getSourceCodeRange());
					context.add(tmp);
//				}
			}
		}
		context.remove(node);
		includedRanges.remove(node.getSourceCodeRange());
		return includedRanges;
	}
	
	private List<Node> reorder(Set<Node> forestSet, Node node){
		List<Node> result = new ArrayList<Node>();
		Enumeration<Node> bEnum = node.breadthFirstEnumeration();
		Node tmp = null;
		while(bEnum.hasMoreElements()){
			tmp = bEnum.nextElement();
			if(forestSet.contains(tmp)){
				result.add(tmp);
			}
		}
		return result;
	}

	private boolean needMarkDescendants(Node node) {
		Enumeration<Node> nEnum = null;
		Node tmp = null;
		HashMap<Integer, Set<Node>> editedBranches = null;
		switch (node.getNodeType()) {
		case ASTNode.SWITCH_STATEMENT:
			nEnum = node.children();
			editedBranches = new HashMap<Integer, Set<Node>>();
			int caseNo = -1;
			Set<Node> tmpEditedNodes = null;
			// counter number of edited branches
			while (nEnum.hasMoreElements()) {
				tmp = nEnum.nextElement();
				if (tmp.getNodeType() == ASTNode.SWITCH_CASE) {
					if (tmpEditedNodes != null && tmpEditedNodes.isEmpty()) {
						editedBranches.remove(caseNo);
					}
					caseNo++;
					tmpEditedNodes = new HashSet<Node>();
					editedBranches.put(caseNo, tmpEditedNodes);
				}
				if (tmp.getRole() != SimpleTreeNode.NONE) {
					tmpEditedNodes.add(tmp);
				}
			}
			return editedBranches.size() > 1;
		}
		return false;
	}

	public static int markDescendants(Node node, int markerIndex) {
		Node tmp = null;
		Enumeration<Node> nEnum = node.breadthFirstEnumeration();
		while (nEnum.hasMoreElements()) {
			tmp = nEnum.nextElement();
			if (tmp.getRole() == SimpleTreeNode.NONE) {
				tmp.setRole(SimpleTreeNode.CONTAIN_DOWN_RELEVANT);
				markerIndex = MarkerUtility.setMarkerProperty(tmp, markerIndex);
			}
		}
		return markerIndex;
	}

//	private void markPath(Node tmpRoot, List<Node> path) {
//		Node tmp = null;
//		for (int i = path.size() - 1; i >= 0; i--) {
//			tmp = path.get(i);
//			if (tmp.getRole() == SimpleTreeNode.NONE) {
//				tmp.setRole(SimpleTreeNode.CONTAIN_DOWN_RELEVANT);
//				if (i != 0 && path.get(i - 1).equals(tmpRoot)) {
//					markDescendants(tmp);
//				}
//			}
//			if (tmp.equals(tmpRoot))
//				break;
//		}
//	}

	private boolean markUnderSameParent(Set<Node> forests, Node root){
		boolean hasSameParent = true;
		if(forests.size()== 1)
			return hasSameParent;
		Node parent = null, tmpParent = null;
		for (Node f : forests) {
			assert f.getParent() != null;
			tmpParent = (Node) f.getParent();
			if (parent == null) {
				parent = (Node) f.getParent();
			} else if (!tmpParent.equals(parent)) {
				hasSameParent = false;
				break;
			}
		}
		Node cTmp = null;
		int markerIndex = root.getProperty(AbstractNode.MAX_MARKER_INDEX);
		if (hasSameParent) {			
			if(CloneReductionMain.onlyHasOneSubtree){
				forests.clear();
				if(parent.getNodeType() == ASTNode.METHOD_DECLARATION){					
					Enumeration<Node> cEnum = parent.children();
					while(cEnum.hasMoreElements()){
						cTmp = cEnum.nextElement();
						forests.add(cTmp);
						markerIndex = markDescendants(cTmp, markerIndex);
					}
				}else{
					forests.add(parent);
				}
			}else{
				int tmpIndex = -1;
				int min = parent.getChildCount(), max = -1;
				for(Node f : forests){
					tmpIndex = parent.getIndex(f);
					if(tmpIndex < min){
						min = tmpIndex;
					}else if(tmpIndex > max){
						max = tmpIndex;
					}
				}
				for(int i = min; i <= max; i++){
					cTmp = (Node)parent.getChildAt(i); 
					forests.add(cTmp);
					markerIndex = markDescendants(cTmp, markerIndex);
				}
			}								
		}
		root.setProperty(AbstractNode.MAX_MARKER_INDEX, markerIndex);
		return hasSameParent;
	}
	/**
	 * merge() can cause two possible effects: 1. find a subtree to include all
	 * edited nodes 2. find a list of edited nodes
	 * 
	 * @param forests
	 * @param root
	 */
	private void merge(Set<Node> forests, Node root) {
		if(markUnderSameParent(forests, root)){
			return;
		}
		List<Node> treeList = new ArrayList<Node>(forests);
		List<List<Node>> pathList = new ArrayList<List<Node>>();
		List<Node> path = null;
		Enumeration<Node> pEnum = null;
		for (int i = 0; i < treeList.size(); i++) {
			pEnum = treeList.get(i).pathFromAncestorEnumeration(root);
			path = new ArrayList<Node>();
			pathList.add(path);
			// each path starts with root, end with the given node
			while (pEnum.hasMoreElements()) {
				path.add(pEnum.nextElement());
			}
		}
		while (pathList.size() > 1) {
			// find the longest two paths
			int p1 = 0;
			int l1 = pathList.get(0).size();
			int p2 = -1;
			int l2 = -1;
			int len = 0;
			for (int i = 1; i < pathList.size(); i++) {
				len = pathList.get(i).size();
				if (len > l1) {
					l2 = l1;
					p2 = p1;
					l1 = len;
					p1 = i;
				} else if (len > l2) {
					l2 = len;
					p2 = i;
				}
			}
			List<Node> path1 = new ArrayList<Node>(pathList.get(p1));
			List<Node> path2 = pathList.get(p2);
			path1.retainAll(path2);
			Node mergeNode = path1.get(path1.size() - 1);
			markDescendants(mergeNode, root.getProperty(AbstractNode.MAX_MARKER_INDEX));
			forests.clear();
			forests.addAll(reorganizePaths(pathList, mergeNode, root));			
			if(markUnderSameParent(forests, root))
				break;
		}
	}

	private Set<Node> reorganizePaths(List<List<Node>> pathList, Node mergeNode,
			Node root) {
		Set<Integer> remains = new HashSet<Integer>();
		Set<Node> forests = new HashSet<Node>();
		List<Node> path = null;
		Node parent = null;
		for (int i = 0; i < pathList.size(); i++) {
			path = pathList.get(i);
			if (path.size() > 1) {
				parent = path.get(path.size() - 2);
				if (parent.getRole() == SimpleTreeNode.NONE) {
					remains.add(i);
				}
			}
		}
		List<List<Node>> newPaths = new ArrayList<List<Node>>();
		for (Integer remain : remains) {
			path = pathList.get(remain);
			newPaths.add(pathList.get(remain));
			forests.add(path.get(path.size() - 1));
		}
		if (mergeNode.getParent() == null
				|| ((Node) mergeNode.getParent()).getRole() == SimpleTreeNode.NONE) {
			path = new ArrayList<Node>();
			Enumeration<Node> pEnum = mergeNode
					.pathFromAncestorEnumeration(root);
			while (pEnum.hasMoreElements()) {
				path.add(pEnum.nextElement());
			}
			if(!newPaths.contains(path)){
				newPaths.add(path);
				forests.add(mergeNode);
			}
		}
		pathList.clear();
		pathList.addAll(newPaths);
		return forests;
	}
}
