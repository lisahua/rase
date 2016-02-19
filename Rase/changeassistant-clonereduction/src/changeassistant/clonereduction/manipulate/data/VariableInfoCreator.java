package changeassistant.clonereduction.manipulate.data;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.changesuggestion.expression.representation.Term;

public class VariableInfoCreator {

	public VariableInfo createForV(ASTNode astNode, String abstractName){
		int index = Term.parseInt(abstractName);
		return new VariableInfo("v"+index, astNode, abstractName);
	}
}
