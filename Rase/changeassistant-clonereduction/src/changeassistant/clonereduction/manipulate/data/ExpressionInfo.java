package changeassistant.clonereduction.manipulate.data;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import changeassistant.changesuggestion.expression.representation.Term;

public class ExpressionInfo {
	public String name;
	public String type;
	public String content;
	public String abstractName;
	public List<String> otherKeys;
	
	public ExpressionInfo(String name, ASTNode astNode, String abstractName){
		this.abstractName = abstractName;
		this.name = name;
		if(astNode != null){
			this.type = ((Expression)astNode).resolveTypeBinding().getName();
			this.content = astNode.toString();
		}			
	}
	
	@Override
	public String toString(){
		return name + "(" + content + ")";
	}
}
