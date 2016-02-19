package changeassistant.clonereduction.manipulate;

import java.util.Enumeration;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonereduction.helper.NodeHelper;
import changeassistant.crystal.analysis.PostDominateAnalysisFactory;
import changeassistant.crystal.analysis.postdominate.PostDominateElementResult;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;

public class SemanticsEqualConversion {

	private PostDominateElementResult postDominateElementResult;
	private ProjectResource pr;

	public SemanticsEqualConversion(ProjectResource pr) {
		this.pr = pr;
	}

	/**
	 * Modified from AbstractNode.deepCopy
	 * 
	 * @param node
	 * @param adt
	 * @return
	 */
	public Node convert(Node node, ChangedMethodADT adt) {
		ClassContext cc = pr.findClassContext(adt.classname);
		ICompilationUnit icu = JavaCore.createCompilationUnitFrom(pr
				.getFile(cc.relativeFilePath));
		postDominateElementResult = PostDominateAnalysisFactory.getInstance()
				.getAnalysisResultForMethod(icu, node.getMethodDeclaration());
		postDominateElementResult.init(node);
		Node copy = (Node) node.clone();
		Stack<Node> stackOriginal = new Stack<Node>();
		Stack<Node> stackNew = new Stack<Node>();
		stackOriginal.push(node);
		stackNew.push(copy);
		NodeHelper nHelper = new NodeHelper();
		while (!stackOriginal.isEmpty()) {
			Node originalNode = stackOriginal.pop();
			Node newNode = stackNew.pop();
			Enumeration<Node> children = originalNode.children();
			while (children.hasMoreElements()) {
				Node child = children.nextElement();
				Node childCopy = (Node) child.clone();
				newNode.add(childCopy);

				Node child2 = null;
				Node child2Copy = null;
				while (nHelper.isIf(child) && children.hasMoreElements()
						&& child.getChildCount() == 1) {// lookup
					child2 = children.nextElement();
					child2Copy = (Node) child2.clone();
					// introduce else-block when there is an implicit else
					Set<Node> tmpSet = postDominateElementResult
							.getTransitiveControlDependence(child2);
					if (tmpSet.contains(child)) {
						// then-copy
						Node grandson = (Node) child.getChildAt(0);
						Node grandsonCopy = (Node) grandson.clone();
						childCopy.add(grandsonCopy);
						stackOriginal.add(grandson);
						stackNew.add(grandsonCopy);
						// else-create
						Node elseNode = new Node(ASTNode.BLOCK, "else:",
								SourceCodeRange.DefaultRange, new Object[] {});
						childCopy.add(elseNode);
						newNode = elseNode;
					} else {
						stackOriginal.push(child);
						stackNew.push(childCopy);
					}
					newNode.add(child2Copy);
					// stackOriginal.push(child2);
					// stackNew.push(child2Copy);
					child = child2;
					childCopy = child2Copy;
				}
				stackOriginal.push(child);
				stackNew.push(childCopy);

			}
		}
		return copy;
	}
}
