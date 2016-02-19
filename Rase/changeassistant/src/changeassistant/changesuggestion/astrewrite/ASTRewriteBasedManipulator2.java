package changeassistant.changesuggestion.astrewrite;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import changeassistant.change.group.edits.AbstractTreeEditOperation2;
import changeassistant.change.group.edits.SubTreeMoveOperation;
import changeassistant.change.group.edits.SubTreeUpdateOperation;
import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.AbstractExpressionRepresentationGenerator;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.internal.ASTMethodFinder;
import changeassistant.internal.ASTNodeFinder;
import changeassistant.internal.WorkspaceUtilities;
import changeassistant.main.ChangeAssistantMain;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.ASTMethodBodyTransformer;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class ASTRewriteBasedManipulator2 {

	private ASTRewrite rewrite;

	private ASTMethodBodyTransformer transformer;

	private ASTMethodFinder methodFinder;

	private ManipulatorHelper helper;

	private ChangedMethodADT2 result;

	// private int counter = 0;

	protected void beforeManipulate(CompilationUnit unit) {
		rewrite = ASTRewrite.create(unit.getAST());
	}

	protected void afterManipulate(CompilationUnit unit) {
		try {
			helper.saveASTRewriteContents(unit, rewrite);
			rewrite = null;
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ASTRewriteBasedManipulator2() {
		transformer = new ASTMethodBodyTransformer();
		methodFinder = new ASTMethodFinder();
		helper = new ManipulatorHelper();
	}

	public ChangedMethodADT2 getResult() {
		return result;
	}

	public void manipulate(
			ProjectResource prLeft,
			ProjectResource prRight,
			List<AbstractTreeEditOperation2<SubTreeModel>> editScriptOnCandidate,
			ChangedMethodADT peer) throws RewriteException {
		ChangedMethodADT oADT = peer, nADT = new ChangedMethodADT(
				peer.classname, peer.methodSignature);
		String oStr = "", nStr = "";
		CompilationUnit cu = null;
		helper.initializeCopy(prLeft, peer.classname);
		System.out.print("");
		result = null;
		AbstractTreeEditOperation2<SubTreeModel> edit2 = null;
		Node methodNode;
		boolean changed;
		ASTNode currentASTNode = null, currentASTParent;
		Node currentParent, currentNode = null;
		SubTreeModel editedNode;
		int position = -1;
		MethodDeclaration md = null;
		try {
			for (int i = 0; i < editScriptOnCandidate.size(); i++) {
				// System.out.println(i);
				edit2 = editScriptOnCandidate.get(i);
				cu = helper.createCopy();
				// md = methodFinder.lookforMethod(cu, simpleName,
				// peer.methodSignature);
				md = methodFinder.lookforMethod(cu, peer);
				assert (md != null);
				if (i == 0) {
					oStr = md.toString();
				}
				methodNode = transformer.createMethodBodyTree(md);
				changed = false;

				beforeManipulate(cu);
				position = -1;
				editedNode = edit2.getNode();
				if (!edit2.getOperationType().equals(EDIT.INSERT)) {
					currentNode = (Node) methodNode
							.lookforNodeBasedOnPosition(editedNode);
					if (currentNode != null) {
						currentASTNode = lookforASTNode(cu, currentNode);
					}
				}
				position = edit2.getLocation();

				currentParent = (Node) methodNode
						.lookforNodeBasedOnPosition((SubTreeModel) edit2
								.getParentNode());
				currentASTParent = lookforASTNode(cu, currentParent);
				switch (edit2.getOperationType()) {
				case INSERT: {
					ASTRewriteBasedInsert.apply(rewrite, currentASTParent,
							position, editedNode);
					changed = true;
				}
					break;
				case DELETE: {
					ASTRewriteBasedDelete.apply(rewrite, currentASTNode);
					changed = true;
				}
					break;
				case MOVE: {
					SubTreeMoveOperation move = (SubTreeMoveOperation) edit2;
					int newPosition = move.getNewSiblingsBefore().size();
					Node newParent = (Node) methodNode
							.lookforNodeBasedOnPosition(move.getNewParent());
					ASTNode newASTParent = lookforASTNode(cu, newParent);
					ASTRewriteBasedMove.apply(rewrite, currentASTNode,
							newPosition, newASTParent, editedNode);
					newASTParent = null;
					newParent = null;
					move = null;
					changed = true;
				}
					break;
				case UPDATE: {
					SubTreeUpdateOperation update = (SubTreeUpdateOperation) edit2;
					List<String> expressionStrings = AbstractExpressionRepresentationGenerator
							.createConcreteStringList(update.getNewNode()
									.getAbstractExpressions());
					SubTreeModel newNode = update.getNewNode();
					if (update.getNode().getNodeType() == newNode.getNodeType()) {
						ASTRewriteBasedUpdate.apply(rewrite, currentASTNode,
								expressionStrings);
					} else {
						ASTRewriteBasedUpdate.apply(rewrite, currentASTNode,
								newNode);
					}
					update = null;
					expressionStrings = null;
					newNode = null;
					changed = true;
				}
					break;
				}
				if (changed) {
					afterManipulate(cu);
				}
				cu = null;
				edit2 = null;
				md = null;
				methodNode = null;
				currentNode = null;
				currentASTNode = null;
				currentParent = null;
				currentASTParent = null;
			}
			editScriptOnCandidate = null;
			cu = helper.createCopy();
			if (ChangeAssistantMain.PRINT_INFO) {
				md = methodFinder.lookforMethod(cu, peer);
				methodNode = transformer.createMethodBodyTree(md);
				nStr = md.toString();
				nADT.range = new SourceCodeRange(md.getStartPosition(),
						md.getLength());
				// System.out.println("# of nodes in the method: " +
				// methodNode.countNodes());
				System.out.println(md.toString());
				result = new ChangedMethodADT2(oADT, nADT, oStr, nStr,
						ChangedMethodADT2.STATUS.NOT_APPLIED);
			}
			helper.clear();
		} catch (Exception e) {
			if (e instanceof RewriteException) {
				throw (RewriteException) e;
			}
			e.printStackTrace();
		}
	}

	private ASTNode lookforASTNode(CompilationUnit cu, Node node) {
		if (node == null)
			return null;
		ASTNodeFinder finder = new ASTNodeFinder();
		ASTNode astNode = finder.lookforASTNode(cu, node.getSourceCodeRange());
		if (node.getStrValue().equals("then:")
				|| node.getStrValue().equals("else:")) {
			if (astNode instanceof Block)
				return astNode;
			else {
				if (node.getStrValue().equals("then:")
						|| node.getStrValue().equals("else:")) {
					Block block = astNode.getAST().newBlock();
					ASTNode astNodeToMove = rewrite.createMoveTarget(astNode);
					rewrite.replace(astNode, block, null);
					ListRewrite listRewrite = rewrite.getListRewrite(block,
							Block.STATEMENTS_PROPERTY);
					listRewrite.insertAt(astNodeToMove, 0, null);
					listRewrite = null;
					return block;
				}
				return astNode.getParent();// this should be the if-statement
			}

		}
		return astNode;
	}

	public void rollBack(
			ProjectResource prLeft,
			ProjectResource prRight,
			List<AbstractTreeEditOperation2<SubTreeModel>> editScriptOnCandidate,
			ChangedMethodADT peer) {
		WorkspaceUtilities.copyFile(helper.backPath, helper.path);
	}
}
