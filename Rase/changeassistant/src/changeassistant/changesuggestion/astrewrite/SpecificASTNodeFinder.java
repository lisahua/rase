package changeassistant.changesuggestion.astrewrite;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class SpecificASTNodeFinder extends ASTVisitor {

	private int astNodeType;
	
	public ASTNode result;
	
	public SpecificASTNodeFinder(int astNodeType){
		this.astNodeType = astNodeType;
		result = null;
	}
	
	@Override
	public void preVisit(ASTNode node){
		if(result == null && node.getNodeType() == astNodeType){
			result = node;
		}
	}
	
}
