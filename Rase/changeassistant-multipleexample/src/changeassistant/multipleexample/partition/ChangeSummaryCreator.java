package changeassistant.multipleexample.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.changesuggestion.expression.representation.AbstractExpressionRepresentationGenerator;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.Term.TermType;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.NodeSummary;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;
import changeassistant.versions.treematching.edits.UpdateOperation;

public class ChangeSummaryCreator {

	private Node oldTree, updatedOldTree, newTree;

	public boolean isGeneral = false;

	private AbstractExpressionRepresentationGenerator oGenerator = null,
			nGenerator = null;

	/**
	 * may return null when this is an empty edit
	 * 
	 * @param oldSum
	 * @return
	 */
	public String createChgSumStr1(ChangeSummary oldSum) {
		switch (oldSum.editType) {
		case INSERT:
		case DELETE:
		case MOVE: {
			return oldSum.editType.toString() + " "
					+ oldSum.nodeSummaries.get(0).toString();
		}
		case UPDATE: {
			return (oldSum.editType.toString() + " "
					+ oldSum.nodeSummaries.get(0).toString() + " TO " + oldSum.nodeSummaries
					.get(1).toString());
		}
		}
		return null;
	}

	/**
	 * may return null when this is an empty edit
	 * 
	 * @param oldSum
	 * @return
	 */
	public String createChgSumStr2(ChangeSummary oldSum) {
		switch (oldSum.editType) {
		case INSERT:
		case DELETE:
		case MOVE: {
			return oldSum.editType.toString() + " " + oldSum.nodeTypes.get(0)
					+ " " + oldSum.nodeSummaries.get(0).toAbstractString();
		}
		case UPDATE: {
			return oldSum.editType.toString() + " " + oldSum.nodeTypes.get(0)
					+ " " + oldSum.nodeSummaries.get(0).toAbstractString()
					+ " TO " + oldSum.nodeTypes.get(1) + " "
					+ oldSum.nodeSummaries.get(1).toAbstractString();
		}
		}
		return null;
	}

	public ChangeSummary createEmptyChangeSummary(EDIT editType) {
		return new ChangeSummary(editType);
	}

	private NodeSummary createNodeSummary(Node node) {
		Node concernedNode = null;
		AbstractExpressionRepresentationGenerator generator;
		Node mappedNode = oldTree.lookforNodeBasedOnRange(node);
		if (mappedNode == null) {
			mappedNode = updatedOldTree.lookforNodeBasedOnRange(node);
			if (mappedNode == null) {// the updated new node is from the new
										// tree
				mappedNode = (Node) newTree.lookforNodeBasedOnRange(node);
			} else {
				mappedNode = (Node) newTree
						.lookforNodeBasedOnPosition(mappedNode);
			}
			concernedNode = mappedNode;
			generator = nGenerator;
		} else {
			concernedNode = mappedNode;
			generator = oGenerator;
		}

		List<List<Term>> termsList = null;
		List<Term> terms;
		int nodeType = concernedNode.getNodeType();

		if (nodeType == ASTNode.METHOD_DECLARATION) {
			terms = generator.getTokenizedRepresentation("method declaration",
					nodeType);
			termsList = new ArrayList<List<Term>>();
			termsList.add(terms);
		} else {
			try {
				termsList = generator.getTokenizedRepresentation(concernedNode
						.getASTExpressions2());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String nodeStr = concernedNode.getStrValue();
		if (nodeStr.contains(":"))
			nodeStr = nodeStr.substring(0, nodeStr.indexOf(':') + 1);
		return new NodeSummary(nodeStr, termsList, concernedNode.getNodeType());
	}

	public List<ChangeSummary> createSummary(MethodModification mm) {
		List<ChangeSummary> chgSums = new ArrayList<ChangeSummary>();
		List<AbstractTreeEditOperation> edits = mm.getEdits();
		AbstractTreeEditOperation edit = null;
		EDIT editType = null;
		ChangeSummary cs = null;
		Node node = null;
		oGenerator = new AbstractExpressionRepresentationGenerator();
		nGenerator = new AbstractExpressionRepresentationGenerator();
		oldTree = (Node) edits.get(0).getParentNode().getRoot();
		updatedOldTree = edits.get(edits.size() - 2).getNode();
		newTree = edits.get(edits.size() - 1).getNode();
		List<String> descriptions = new ArrayList<String>();
		List<String> descriptions2 = new ArrayList<String>();
		String description = null;
		for (int j = 0; j < edits.size(); j++) {
			edit = edits.get(j);
			editType = edit.getOperationType();
			if (editType.equals(EDIT.EMPTY))
				continue;
			cs = new ChangeSummary(editType, new ArrayList<NodeSummary>(),
					new ArrayList<String>());
			node = edit.getNode();
			cs.nodeSummaries.add(createNodeSummary(node));
			cs.nodeTypes.add(node.getNodeTypeString());
			if (editType.equals(EDIT.UPDATE)) {
				cs.nodeSummaries.add(createNodeSummary(((UpdateOperation) edit)
						.getNewNode()));
				cs.nodeTypes.add(((UpdateOperation) edit).getNewNode()
						.getNodeTypeString());
			}
			descriptions.add(description);
			descriptions2.add(cs.getAbstractSummaryString());
			chgSums.add(cs);
		}
		return chgSums;
	}

	public ChangeSummary updateSummary(
			List<List<SimpleASTNode>> simpleExprsList, ChangeSummary oldSum) {
		isGeneral = true;

		ChangeSummary newSum = (ChangeSummary) oldSum.clone();
		NodeSummary newNodeSum = null;
		List<NodeSummary> newNodeSums = newSum.nodeSummaries;
		SimpleASTNode simpleExpr = null;
		SimpleASTNode sNode = null;
		List<SimpleASTNode> simpleExprs = null;
		List<List<Term>> expressions = null;
		List<Term> terms = null;
		for (int i = 0; i < oldSum.nodeSummaries.size(); i++) {
			newNodeSum = newNodeSums.get(i);
			simpleExprs = simpleExprsList.get(i);
			expressions = new ArrayList<List<Term>>();
			for (int j = 0; j < simpleExprs.size(); j++) {
				simpleExpr = simpleExprs.get(j);
				Enumeration<SimpleASTNode> dEnum = simpleExpr
						.depthFirstEnumeration();
				terms = new ArrayList<Term>();
				while (dEnum.hasMoreElements()) {
					sNode = dEnum.nextElement();
					if (sNode.isLeaf()) {
						if (sNode.getTerm() == null) {
							terms.add(new Term(sNode.getNodeType(), sNode
									.getStrValue()));
						} else {
							TermType termType = sNode.getTerm().getTermType();
							if (isGeneral && !termType.equals(TermType.Term)) {
								// String name = sNode.getTerm().getName();
								/*
								 * if(name.startsWith(ASTExpressionTransformer.
								 * ABSTRACT_VARIABLE) ||
								 * name.startsWith(ASTExpressionTransformer
								 * .ABSTRACT_METHOD) ||
								 * name.startsWith(ASTExpressionTransformer
								 * .ABSTRACT_TYPE)){ }
								 */
								if (!sNode.getTerm().getName().equals("null")) {
									isGeneral = false;
								}
							} else if (sNode.getStrValue().equals(
									SimpleASTNode.LIST_LITERAL)) {
								continue;
							}
							terms.add(sNode.getTerm());
						}
					}
				}
				expressions.add(terms);
			}
			newNodeSum.expressions = Collections.unmodifiableList(expressions);
		}
		return newSum;
	}
}
