package changeassistant.changesuggestion.expression.representation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
/**
 * Target: only collect expression nodes since they will be useful when substituting ast nodes
 * @author ibm
 *
 */
public class ASTNodeCollector extends ASTVisitor {

	private List<ASTNode> nodeList;
	
	public ASTNodeCollector(){
		nodeList = new ArrayList<ASTNode>();		
	}
	
	public List<ASTNode> getNodeList(){
		return this.nodeList;
	}
	
	public void reset(){
		nodeList= new ArrayList<ASTNode>();
	}
	
	public void addToList(ASTNode ... astNodes){
		for(ASTNode astNode : astNodes){
			nodeList.add(astNode);
		}
	}
	
	@Override 
	public boolean visit(AssertStatement node){
		addToList(node.getExpression());
		return false;
	}
	
	@Override
	public boolean visit(BreakStatement node){//it is not convinent to only add node.getLabel() (sometimes it is null)
		// instead, add the node itself
		addToList(node);
		return false;
	}
	
	@Override
	public boolean visit(CatchClause node){
		addToList(node.getException());
		node.getBody().accept(this);
		return false;
	}
	
	@Override
	public boolean visit(ConstructorInvocation node){
		addToList(node);
		return false;
	}
	
	@Override
	public boolean visit(ContinueStatement node){
		addToList(node);
		return false;
	}
	
	@Override
	public boolean visit(DoStatement node){
		addToList(node.getExpression());
		return true;
	}
	
	@Override
	public boolean visit(EnhancedForStatement node){
		addToList(node.getParameter(), node.getExpression());		
		return true;
	}
	
	@Override
	public boolean visit(ExpressionStatement node){
		addToList(node.getExpression());
		return false;
	}
	
	@Override
	public boolean visit(ForStatement node){
		List<ASTNode> astNodes = new ArrayList<ASTNode>(node.initializers());
		astNodes.add(node.getExpression());
		astNodes.addAll(node.updaters());		
		addToList(astNodes.toArray(new ASTNode[astNodes.size()]));
		return true;
	}
	
	@Override
	public boolean visit(IfStatement node){
		addToList(node.getExpression());
		node.getThenStatement().accept(this);
		if(node.getThenStatement() != null)
			node.getThenStatement().accept(this);
		if(node.getElseStatement() != null)
			node.getElseStatement().accept(this);
		return false;
	}
	
	@Override
	public boolean visit(LabeledStatement node){
		addToList(node.getLabel());
		node.getBody().accept(this);
		return false;
	}
	
	@Override
	public boolean visit(ReturnStatement node){
		addToList(node.getExpression());
		return false;
	}
	
	@Override
	public boolean visit(SuperConstructorInvocation node){
		addToList(node);
		return false;
	}
	
	@Override
	public boolean visit(SwitchCase node){
		addToList(node.getExpression());
		return false;
	}
	
	@Override
	public boolean visit(SwitchStatement node){
		addToList(node.getExpression());
		visitList(node.statements());
		return false;
	}
	
	@Override
	public boolean visit(SynchronizedStatement node){
		addToList(node.getExpression());
		return true;
	}
	
	@Override
	public boolean visit(ThrowStatement node){
		addToList(node.getExpression());
		return false;
	}
	
	@Override
	public boolean visit(TryStatement node){
		node.getBody().accept(this);
		visitList(node.catchClauses());
		if(node.getFinally() != null){
			node.getFinally().accept(this);
		}
		return false;
	}
	
	@Override
	public boolean visit(VariableDeclarationStatement node){
		addToList(node);
		return false;
	}
	
	@Override
	public boolean visit(WhileStatement node){
		addToList(node.getExpression());
		return true;
	}
	
	public void visitList(List<ASTNode> list){
		for(ASTNode element : list){
			element.accept(this);
		}
	}
}
