package changeassistant.changesuggestion.expression.representation;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

public class ASTExpressionMatcher extends ASTMatcher{
	
	@Override
	public boolean match(QualifiedName node, Object other){
		return true;
	}
	
	@Override
	public boolean match(SimpleName node, Object other){
		return true;
	}
	
}
