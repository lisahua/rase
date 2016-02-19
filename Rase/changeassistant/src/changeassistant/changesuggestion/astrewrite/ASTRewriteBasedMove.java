package changeassistant.changesuggestion.astrewrite;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import changeassistant.change.group.model.SubTreeModel;

public class ASTRewriteBasedMove {

	public static void apply(ASTRewrite rewrite, ASTNode astNodeToMove, 
			int newPosition, ASTNode newASTParent, SubTreeModel movedNode) 
			throws RewriteException{
		ASTNode newNode = rewrite.createCopyTarget(astNodeToMove);
		ASTRewriteBasedDelete.apply(rewrite, astNodeToMove);
//		ASTRewriteBasedInsert.apply(rewrite, newASTParent, newPosition, movedNode);
		ASTRewriteHelper.insert(rewrite, newNode, newASTParent, newPosition, movedNode);
		newNode = null;
	}
}
