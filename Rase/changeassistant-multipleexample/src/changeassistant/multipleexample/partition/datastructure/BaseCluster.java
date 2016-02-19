package changeassistant.multipleexample.partition.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.datastructure.Pair;
import changeassistant.multipleexample.main.CachedProjectMap;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;
import changeassistant.versions.treematching.edits.InsertOperation;
import changeassistant.versions.treematching.edits.UpdateOperation;

public class BaseCluster extends AbstractCluster {

	private int index;
	MethodModification mm;

	private Node updatedNode;
	private Node newNode;

	private List<Pair<Integer>> lineIndexPairs = null;
	private List<Integer> preorderIndexList = null;

	public BaseCluster(int instance, MethodModification mm,
			List<ChangeSummary> conChgSum, List<String> conChgSumStr,
			List<String> conAbsChgSumStr) {
		super(conChgSum, conChgSumStr, conAbsChgSumStr);
		SimpleASTCreator creator = new SimpleASTCreator();
		index = instance;
		this.mm = mm;

		this.simpleExprsLists = new ArrayList<List<List<SimpleASTNode>>>();
		List<AbstractTreeEditOperation> edits = mm.getEdits();
		updatedNode = edits.get(edits.size() - 2).getNode();
		newNode = edits.get(edits.size() - 1).getNode();

		List<ASTNode> exprs = null;
		List<SimpleASTNode> simpleExprs = null;
		List<List<SimpleASTNode>> simpleExprsList = null;
		List<List<List<SimpleASTNode>>> simpleExprsLists = new ArrayList<List<List<SimpleASTNode>>>();
		List<List<ASTNode>> exprsList = null;
		for (int i = 0; i < edits.size(); i++) {
			exprsList = getExprsList(edits.get(i), updatedNode, newNode);
			simpleExprsList = new ArrayList<List<SimpleASTNode>>();
			for (int j = 0; j < exprsList.size(); j++) {
				simpleExprs = new ArrayList<SimpleASTNode>();
				exprs = exprsList.get(j);
				for (int k = 0; k < exprs.size(); k++) {
					simpleExprs.add(creator.createSimpleASTNode(exprs.get(k)));
				}
				simpleExprsList.add(simpleExprs);
			}
			simpleExprsLists.add(simpleExprsList);
		}
		this.simpleExprsLists = Collections.unmodifiableList(simpleExprsLists);
		createSimpleASTNodesList();
	}

	public boolean contains(Integer index) {
		return this.index == index;
	}

	public void createLineIndexPairs() {
		CompilationUnit cu = CachedProjectMap
				.get((mm.originalMethod.getProjectName()))
				.findClassContext(mm.originalMethod.classname).getCU();
		lineIndexPairs = new ArrayList<Pair<Integer>>();
		Node tree = (Node) mm.getEdits().get(0).getParentNode().getRoot();
		Enumeration<Node> dEnum = tree.preorderEnumeration();
		Enumeration<SimpleTreeNode> sEnum = sTree.preorderEnumeration();
		Node tmpNode = null;
		SimpleTreeNode tmpSTree = null;
		while (dEnum.hasMoreElements()) {
			tmpNode = dEnum.nextElement();
			tmpSTree = sEnum.nextElement();
			lineIndexPairs.add(new Pair<Integer>(cu.getLineNumber(tmpNode
					.getSourceCodeRange().startPosition), tmpSTree
					.getNodeIndex()));
		}
	}

	public void createSimpleASTNodesList() {// contents of the method
		SimpleASTCreator creator = new SimpleASTCreator();
		simpleASTNodesList = creator.createSimpleASTNodesList((Node) mm
				.getEdits().get(0).getParentNode().getRoot());
	}

	public void encodeSequence() {
		sequence = new Sequence(sTree);
	}

	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof BaseCluster))
			return false;
		return true;
	}

	public int hashCode() {
		return super.hashCode();
	}

	private List<List<ASTNode>> getExprsList(AbstractTreeEditOperation edit,
			Node updatedNode, Node newNode) {
		List<List<ASTNode>> exprsList = new ArrayList<List<ASTNode>>();
		Node tmp = null;
		VariableDeclarationStatement vStmt = null;
		if (edit.getOperationType().equals(EDIT.INSERT)) {
			tmp = ((InsertOperation) edit).getNode();
			tmp = newNode.lookforNodeBasedOnRange(tmp);
			// dNodes = gManager.findDataDependingNodesInNewRevision(tmp);
			// for(Node dNode : dNodes){
			// if(dNode.getNodeType() ==
			// ASTNode.VARIABLE_DECLARATION_STATEMENT){
			// vStmt =
			// (VariableDeclarationStatement)dNode.getASTExpressions2().get(0);
			//
			// }
			// }
			exprsList.add(tmp.getASTExpressions2());
		} else {
			exprsList.add(edit.getNode().getASTExpressions2());
		}

		if (edit.getOperationType().equals(EDIT.UPDATE)) {
			tmp = ((UpdateOperation) edit).getNewNode();
			tmp = newNode.lookforNodeBasedOnRange(tmp);
			exprsList.add(tmp.getASTExpressions2());
		}
		return exprsList;
	}

	public int getIndex() {
		return index;
	}

	public List<Integer> getInstances() {
		List<Integer> instances = new ArrayList<Integer>();
		instances.add(index);
		return instances;
	}

	public MethodModification getMM() {
		return mm;
	}

	public List<Integer> getPreorderIndexList() {
		return preorderIndexList;
	}

	public void setSTree(SimpleTreeNode sTree) {
		this.sTree = sTree;
		this.preorderIndexList = new ArrayList<Integer>();
		Enumeration<SimpleTreeNode> sEnum = sTree.preorderEnumeration();
		while (sEnum.hasMoreElements()) {
			preorderIndexList.add(sEnum.nextElement().getNodeIndex());
		}
	}

	@Override
	public String toString() {
		return "base cluster for " + mm.originalMethod.toString();
	}
}
