package changeassistant.changesuggestion.astrewrite;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ASTRewriteBasedDelete {

	public static void apply(ASTRewrite rewrite, ASTNode astNode) throws RewriteException{
		//when deleting the single child of then or else statement, remember to create  
		//an empty block to take the place of the removed child
		try{
			ASTNode astParent = astNode.getParent();
			if(astParent != null){
				if(astParent instanceof IfStatement){
					IfStatement parent = (IfStatement)astParent;
					ASTNode newASTNode = astNode.getParent().getAST().newBlock();
				
						if(parent.getThenStatement() == astNode){
							rewrite.set(parent, IfStatement.THEN_STATEMENT_PROPERTY, newASTNode, null);
						}else{
							rewrite.set(parent, IfStatement.ELSE_STATEMENT_PROPERTY, newASTNode, null);
						}
						newASTNode = null;
				}else{
					rewrite.remove(astNode, null);
				}
		}else{
			rewrite.remove(astNode, null);	
		}
		astNode = null;
		}catch(Exception e){
			throw new RewriteException("The substitution with rewrite does not work well");
		}
	}
	
}
