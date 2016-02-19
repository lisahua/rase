package changeassistant.clonereduction.manipulate.data;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class VariableInfo {

	public String name;
	public String abstractName;
	public String content;
	public String type;
	
	public VariableInfo(String name, String abstractName, String content, String type){
		this.name = name;
		this.abstractName = abstractName;
		this.content = content;
		this.type = type;
	}
	
	public VariableInfo(String name, ASTNode astNode, String abstractName){
		ASTNode parent = astNode.getParent();
		if(parent instanceof QualifiedName){
			content = parent.toString();
			type = ((QualifiedName) parent).resolveTypeBinding().getName();
		}else{
			content = astNode.toString();
			switch(astNode.getNodeType()){
			case ASTNode.SIMPLE_NAME: type = ((SimpleName)astNode).resolveTypeBinding().getName();
				break;
			case ASTNode.VARIABLE_DECLARATION_FRAGMENT: type = ((VariableDeclarationFragment)astNode)
				.resolveBinding().getType().getName();
				break;
			case ASTNode.QUALIFIED_NAME: type = ((QualifiedName)astNode).resolveTypeBinding().getName();
				break;
			case ASTNode.THIS_EXPRESSION: type = ((ThisExpression)astNode).resolveTypeBinding().getName();
				break;
			default:
				System.out.print("Need more process in VariableInfo(...)");
				break;
			}
		}
		this.abstractName = abstractName;
		this.name = name;
	}
	
	@Override
	public String toString(){
		return name + "(" + content + ")";
	}
}
