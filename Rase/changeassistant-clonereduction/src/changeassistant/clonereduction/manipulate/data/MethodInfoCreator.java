package changeassistant.clonereduction.manipulate.data;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.clonereduction.manipulate.refactoring.IdGeneralizer;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.peers.SourceCodeRange;

public class MethodInfoCreator extends ASTVisitor{

	List<ASTNode> paramASTs;
	boolean needInst;
	String instTypeName;
	ASTNode astNode = null;
	
	public MethodInfo createForM(ASTNode astNode, String abstractName){
		this.astNode = astNode;
		paramASTs = new ArrayList<ASTNode>();
		needInst = false;
		instTypeName = null;
		astNode.accept(this);		
		int index = Term.parseInt(abstractName);
		return new MethodInfo("m"+index, paramASTs, astNode, abstractName, needInst, instTypeName);
	}
	
	public boolean visit(BooleanLiteral node){
		paramASTs.add(node);
		return false;
	}
	
	public boolean visit(CastExpression node){
		node.getExpression().accept(this);
		return false;
	}
	
	public boolean visit(MethodInvocation node){
		Expression expr = node.getExpression();
		if(expr == null){
			needInst = true;
			instTypeName = node.resolveMethodBinding().getDeclaringClass().getName();
		}else{
			instTypeName = expr.resolveTypeBinding().getName();
			if(instTypeName.equals(expr.toString())){//this is a static method
				instTypeName = null;
			}else{
				expr.accept(this);
			}			
		}
		visitList(node.arguments());
		return false;
	}
	
	private void visitList(List<ASTNode> list){
		for(ASTNode node : list){
			node.accept(this);
		}
	}
	
	public boolean visit(SimpleType node){
		return false;
	}
	
	public boolean visit(FieldAccess node){
		paramASTs.add(node);
		return false;
	}
	
	public boolean visit(SimpleName node){
		paramASTs.add(node);
		return false;
	}
}
