package changeassistant.multipleexample.staticanalysis;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.crystal.analysis.DefUseAnalysisFactory;
import changeassistant.crystal.analysis.PostDominateAnalysisFactory;
import changeassistant.crystal.analysis.def.DefUseElementResult;
import changeassistant.crystal.analysis.postdominate.PostDominateElementResult;
import changeassistant.internal.MethodADT;
import changeassistant.multipleexample.main.EnhancedChangeAssistantMain;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.InsertOperation;
import changeassistant.versions.treematching.edits.MoveOperation;
import changeassistant.versions.treematching.edits.UpdateOperation;
import edu.cmu.cs.crystal.tac.model.Variable;

public class AnalysisManager {

	private ProjectResource prLeft, prRight;

	public MethodModification mm;

	public List<AbstractTreeEditOperation> edits;

	private DefUseElementResult oldMethodAnalysisResult,
			newMethodAnalysisResult;

	private PostDominateElementResult oldPostDominateElementResult,
			newPostDominateElementResult;

	private MethodDeclaration oMd, nMd;

	private Node oldNode, updatedOldNode, newNode;

	private ICompilationUnit oIcu, nIcu;

	private Map<Node, WeakReference<Set<Node>>> oldRelevants, newRelevants;

	private Map<Integer, Set<Node>> controlNodes = null;
	private Map<Integer, Set<Node>> dataNodes = null;

	public AnalysisManager() {

	}

	public AnalysisManager(ProjectResource prLeft, ProjectResource prRight) {
		this.prLeft = prLeft;
		this.prRight = prRight;
	}

	public DefUseElementResult getNewDefUseElementResult() {
		return newMethodAnalysisResult;
	}
	
	public DefUseElementResult getOldDefUseElementResult(){
		return oldMethodAnalysisResult;
	}

	private Set<Node> findNewRelevant(Node node, Integer index) {
		Set<Node> result = new HashSet<Node>();
		Set<Node> mappedResult = new HashSet<Node>();
		if (newRelevants.containsKey(node)
				&& newRelevants.get(node).get() != null) {
			return newRelevants.get(node).get();
		}

		controlNodes.put(index,
				newPostDominateElementResult.getNodeControlDependence(node));
		result.addAll(findControlDependingNodes(node,
				newPostDominateElementResult));
		dataNodes.put(index,
				newMethodAnalysisResult.getNodeDataDependence(node));
		result.addAll(findDataDependingNodes(node, newMethodAnalysisResult));
		// result.remove(node);
		result.add(node);
		if (node.getParent() != null)
			result.add((Node) node.getParent());
		for (Node tmpNode : result) {
			tmpNode = (Node) updatedOldNode.lookforNodeBasedOnPosition(tmpNode);
			tmpNode = oldNode.lookforNodeBasedOnRange(tmpNode);
			if (tmpNode != null)
				mappedResult.add(tmpNode);
		}
		newRelevants.put(node, new WeakReference<Set<Node>>(mappedResult));
		return mappedResult;
	}

	private Set<Node> findOldRelevant(Node node) {
		Set<Node> result = new HashSet<Node>();
		if (oldRelevants.containsKey(node)
				&& oldRelevants.get(node).get() != null) {
			return oldRelevants.get(node).get();
		}
		result.addAll(findControlDependingNodes(node,
				oldPostDominateElementResult));
		result.addAll(findDataDependingNodes(node, oldMethodAnalysisResult));
		if (node.getParent() != null
				&& !((Node) node.getParent()).getStrValue().equals("then:"))
			result.add((Node) node.getParent());
		result.add(node);
		// result.remove(node);
		oldRelevants.put(node, new WeakReference<Set<Node>>(result));
		return result;
	}

	/**
	 * to get a transitive closure of the control dependence relations
	 * 
	 * @param node
	 * @return
	 */
	private Set<Node> findControlDependingNodes(Node node,
			PostDominateElementResult postDominateElementResult) {
		Set<Node> result = null;
		if (EnhancedChangeAssistantMain.LIMIT_HOP_DISTANCE) {
			result = postDominateElementResult.getNodeControlDependence(node);
		} else {
			result = postDominateElementResult
					.getTransitiveControlDependence(node);
		}
		return result;
	}

	private Set<Node> findDataDependingNodes(Node node,
			DefUseElementResult analysisResult) {
		Set<Node> result = null;
		if (EnhancedChangeAssistantMain.LIMIT_HOP_DISTANCE) {
			result = analysisResult.getNodeDataDependence(node);
		} else {
			result = analysisResult.getTransitiveDataDependence(node);
		}
		return result;
	}
	
	private Set<Node> findControlDependingNodes2(Node node, PostDominateElementResult analysisResult){
		List<SourceCodeRange> ranges = analysisResult.getUpstreamControlDependence2(node);
		Set<Node> result = analysisResult.searchForRelevantNodes(ranges);
		result.remove(node);
		return result;
	}

	public Set<Node> findNewControlDependingNodes(Node node) {
		return findControlDependingNodes2(node, newPostDominateElementResult);
	}
	
	public List<Node> findNewControlDependingNodeList(Node node){
		List<SourceCodeRange> ranges = newPostDominateElementResult
		.getUpstreamControlDependence2(node);
		List<Node> result = newPostDominateElementResult.searchForRelevantNodeList(ranges);
		int index = result.indexOf(node);
		if(index == -1)
			return result;
		List<Node> tmpResult = result.subList(0, index);
		tmpResult.addAll(result.subList(index+1, result.size()));
		result = tmpResult;
		return result;
	}
	
	public Set<Node> findOldControlDependingNodes(Node node) {
		return findControlDependingNodes2(node, oldPostDominateElementResult);
	}
	
	public Set<Node> findNewControlDependingNodesTransitively(Node node){
		return newPostDominateElementResult.getTransitiveControlDependence(node);
	}
	
	private Map<String, Set<Node>> findDataDependingNodes2(Node node, DefUseElementResult analysisResult){
		Map<Variable, Set<SourceCodeRange>> map = analysisResult.getUseFact(node);
		Map<String, Set<Node>> varNodesMap = new HashMap<String, Set<Node>>();
		Variable var = null;
		Set<SourceCodeRange> ranges = null;
		Set<Node> nodes = null;
		for (Entry<Variable, Set<SourceCodeRange>> entry : map.entrySet()) {
			var = entry.getKey();
			ranges = entry.getValue();
			nodes = analysisResult.searchForRelevantNodes(ranges);
			//remove current node
			nodes.remove(node);
			varNodesMap.put(var.getSourceString(), nodes);
		}
		return varNodesMap;
	}

	public Map<String, Set<Node>> findNewDataDependingNodes(Node node) {
		return findDataDependingNodes2(node, newMethodAnalysisResult);
	}
	
	public Map<String, Set<Node>> findOldDataDependingNodes(Node node){
		return findDataDependingNodes2(node, oldMethodAnalysisResult);
	}
	
	public Map<String, Node> findNewFlowSinks(Node node, Set<SourceCodeRange> includedRanges){
		return newPostDominateElementResult.getFlowSinks(node, includedRanges);
	}
	
	public Map<String, Node> findOldFlowSinks(Node node, Set<SourceCodeRange> includedRanges){
		return oldPostDominateElementResult.getFlowSinks(node, includedRanges);
	}

	private Map<String, Set<Node>> findDataDependedNodes(Node node, DefUseElementResult analysisResult){
		SourceCodeRange range = node.getSourceCodeRange();
		Set<Node> nodes = analysisResult.getDefSinkNodes(node);
		Map<String, Set<SourceCodeRange>> varRangesMap = new HashMap<String, Set<SourceCodeRange>>();
		Map<Variable, Set<SourceCodeRange>> map = null;
		Variable var = null;
		Set<SourceCodeRange> ranges = null;
		Set<SourceCodeRange> tmpRanges = null;
		String tmpKey = null;
		for (Node n : nodes) {
			map = analysisResult.getUseFact(n);
			for (Entry<Variable, Set<SourceCodeRange>> entry : map.entrySet()) {
				var = entry.getKey();
				ranges = entry.getValue();
				for (SourceCodeRange r : ranges) {
					if (r.isInside(range)) {
						tmpKey = var.getSourceString();
						tmpRanges = varRangesMap.get(tmpKey);
						if (tmpRanges == null) {
							tmpRanges = new HashSet<SourceCodeRange>();
							varRangesMap.put(tmpKey, tmpRanges);
						}
						if (n.getChildCount() == 0) {
							tmpRanges.add(n.getSourceCodeRange());
						} else {
							tmpRanges.addAll(n.getASTExpressions());
						}
					}
				}
			}
		}
		Map<String, Set<Node>> varNodesMap = new HashMap<String, Set<Node>>();
		Set<Node> tmpNodes = null;
		for (Entry<String, Set<SourceCodeRange>> entry : varRangesMap
				.entrySet()) {
			tmpNodes = analysisResult.searchForRelevantDataNodes(entry
					.getValue());
			//remove current node
			tmpNodes.remove(node);
			varNodesMap.put(entry.getKey(), tmpNodes);
		}
		Map<Variable, Set<SourceCodeRange>> defMap = analysisResult.getDefFact(node);
		for(Entry<Variable, Set<SourceCodeRange>> entry : defMap.entrySet()){
			tmpKey = entry.getKey().getSourceString();
			if(!varNodesMap.containsKey(tmpKey)){
				varNodesMap.put(tmpKey, Collections.EMPTY_SET);
			}
		}
		return varNodesMap;
	}
	
	/**
	 * Get downstream use for current def
	 * 
	 * @param node
	 * @return
	 */
	public Map<String, Set<Node>> findNewDataDependedNodes(Node node) {		
		return findDataDependedNodes(node, newMethodAnalysisResult);		
	}
	
	public Map<String, Set<Node>> findOldDataDependedNodes(Node node) {
		return findDataDependedNodes(node, oldMethodAnalysisResult);
	}

	public Set<Node> findRelevantNodes(List<Integer> indexes) {
		controlNodes = new HashMap<Integer, Set<Node>>();
		dataNodes = new HashMap<Integer, Set<Node>>();
		Set<Node> result = new HashSet<Node>();
		Node tmpNode = null;
		AbstractTreeEditOperation op = null;
		for (Integer index : indexes) {
			op = edits.get(index);
			switch (op.getOperationType()) {
			case DELETE:
				result.addAll(findOldRelevant(op.getNode()));
				break;
			case INSERT:
				result.addAll(findNewRelevant(
						newNode.lookforNodeBasedOnRange(op.getNode()), index));
				tmpNode = ((InsertOperation) op).getParentNode();
				if (oldNode.lookforNodeBasedOnRange(tmpNode) != null) {
					result.addAll(findOldRelevant(tmpNode));
				}
				break;
			case UPDATE:
				result.addAll(findOldRelevant(op.getNode()));
				tmpNode = ((UpdateOperation) op).getNewNode();
				tmpNode = newNode.lookforNodeBasedOnRange(tmpNode);
				result.addAll(findNewRelevant(tmpNode, index));
				break;
			case MOVE:
				// the move opearation can be applied to an already edited node
				// or unchanged node
				tmpNode = op.getNode();
				if (oldNode.lookforNodeBasedOnRange(tmpNode) != null) {
					// to move an unchanged node
					result.addAll(findOldRelevant(tmpNode));
					tmpNode = ((MoveOperation) op).getNewNode();
					result.addAll(findNewRelevant(tmpNode, index));
				}
				tmpNode = ((MoveOperation) op).getNewParentNode();
				if (oldNode.lookforNodeBasedOnRange(tmpNode) != null) {
					result.addAll(findOldRelevant(tmpNode));
				}
				// if this is an already changed node, it has been processed
				// either because of
				// insert or update
				break;
			}
		}
		return result;
	}

	private Set<Node> findNewRelevantNodes(Node node) {
		Set<Node> result = new HashSet<Node>();
		result.addAll(newPostDominateElementResult
				.getTransitiveControlDependence(node));
		result.addAll(newMethodAnalysisResult.getTransitiveDataDependence(node));
		return result;
	}

	/**
	 * Different from findNewRelevant, because it does not map back to the
	 * original version
	 * 
	 * @param indexes
	 * @return
	 */
	public Set<Node> findNewRelevantNodes(List<Integer> indexes) {
		Set<Node> result = new HashSet<Node>();
		Node tmpNode = null;
		AbstractTreeEditOperation op = null;
		Node node = null;
		for (Integer index : indexes) {
			op = edits.get(index);
			switch (op.getOperationType()) {
			case INSERT:
				node = op.getNode();
				tmpNode = newNode.lookforNodeBasedOnRange(node);
				result.addAll(findNewRelevantNodes(tmpNode));
				break;
			case UPDATE:
				tmpNode = ((UpdateOperation) op).getNewNode();
				tmpNode = newNode.lookforNodeBasedOnRange(tmpNode);
				result.addAll(findNewRelevantNodes(tmpNode));
				break;
			case MOVE:
				// the move opearation can be applied to an already edited node
				// or unchanged node
				MoveOperation move = (MoveOperation) op;
				tmpNode = move.getNewNode();
				result.addAll(findNewRelevantNodes(tmpNode));
				tmpNode = (Node) tmpNode.getParent();
				result.addAll(findNewRelevantNodes(tmpNode));
				break;
			default:
				break;
			}
		}
		return result;
	}

	public Map<Integer, Set<Node>> getNewControlNodes() {
		return controlNodes;
	}

	public Map<Integer, Set<Node>> getNewDataNodes() {
		return dataNodes;
	}

	private void init() {
		MethodADT methodADT = mm.originalMethod;
		ClassContext cc = prLeft.findClassContext(methodADT.classname);
		oIcu = JavaCore.createCompilationUnitFrom(prLeft
				.getFile(cc.relativeFilePath));
		oldNode = (Node) edits.get(0).getParentNode().getRoot();
		oMd = oldNode.getMethodDeclaration();

		methodADT = mm.newMethod;
		cc = prRight.findClassContext(methodADT.classname);
		nIcu = JavaCore.createCompilationUnitFrom(prRight
				.getFile(cc.relativeFilePath));
		newNode = edits.get(edits.size() - 1).getNode();
		nMd = newNode.getMethodDeclaration();

		updatedOldNode = edits.get(edits.size() - 2).getNode();
	}

	public void setMethodModification(MethodModification mm) {
		this.mm = mm;
		this.edits = mm.getEdits();
		init();
		oldRelevants = new HashMap<Node, WeakReference<Set<Node>>>();
		newRelevants = new HashMap<Node, WeakReference<Set<Node>>>();
		oldPostDominateElementResult = PostDominateAnalysisFactory
				.getInstance().getAnalysisResultForMethod(oIcu, oMd);
		oldPostDominateElementResult.init(oldNode);
		oldMethodAnalysisResult = DefUseAnalysisFactory.getInstance()
				.getAnalysisResultForMethod(oIcu, oMd);
		oldMethodAnalysisResult.init(oldNode);
		newPostDominateElementResult = PostDominateAnalysisFactory
				.getInstance().getAnalysisResultForMethod(nIcu, nMd);
		newPostDominateElementResult.init(newNode);
		newMethodAnalysisResult = DefUseAnalysisFactory.getInstance()
				.getAnalysisResultForMethod(nIcu, nMd);
		newMethodAnalysisResult.init(newNode);
	}

}
