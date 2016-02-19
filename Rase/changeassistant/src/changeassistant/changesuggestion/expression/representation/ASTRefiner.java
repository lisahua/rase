package changeassistant.changesuggestion.expression.representation;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class ASTRefiner extends ASTVisitor{

	private List<ASTNode> method1Specific;
	private List<ASTNode> method2Specific;
	
	public ASTRefiner(List<ASTNode> method1Specific, List<ASTNode> method2Specific){
		this.method1Specific = method1Specific;
		this.method2Specific = method2Specific;
	}
}
