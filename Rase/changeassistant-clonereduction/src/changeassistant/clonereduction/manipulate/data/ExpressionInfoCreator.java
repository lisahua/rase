package changeassistant.clonereduction.manipulate.data;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.changesuggestion.expression.representation.Term;

public class ExpressionInfoCreator {

	public ExpressionInfo createForU(ASTNode astNode, String abstractName){
		int index = Term.parseInt(abstractName);
		return new ExpressionInfo("u"+index, astNode, abstractName);
	}
}
