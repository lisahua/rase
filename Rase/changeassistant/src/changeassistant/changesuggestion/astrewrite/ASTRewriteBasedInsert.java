package changeassistant.changesuggestion.astrewrite;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import changeassistant.change.group.model.SubTreeModel;

public class ASTRewriteBasedInsert {
	
	public static void apply(ASTRewrite rewrite, ASTNode parentASTNode, 
			int position, SubTreeModel insertedNode) throws Exception{
		ASTNode astNodeToInsert = null;
		astNodeToInsert = ASTNodeGenerator2.createASTNode(parentASTNode.getAST(), insertedNode);
	    if(astNodeToInsert != null){
		    ASTRewriteHelper.insert(rewrite, astNodeToInsert, parentASTNode, position, insertedNode);
		}
		astNodeToInsert = null;
	}
}
