package changeassistant.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import changeassistant.peers.SourceCodeRange;

public class EnclosingASTFinder extends ASTVisitor {
	
	private List<ASTNode> result;
	
	private double sDiff;
	
	private int start, length;
	
	private boolean process(ASTNode node){
		if(node.getStartPosition() <= start && 
				node.getStartPosition() + node.getLength() >= start + length){
			double tmp = Math.pow(node.getStartPosition() - start, 2) + 
			Math.pow(node.getLength() - length, 2);
			if(tmp == sDiff)//this node has already been processed
				return true;
			if(tmp > sDiff) //this case does not seem to happen
				return false;
			result.add(node);//tmp <sDiff
			this.start = node.getStartPosition();//update all information about the smallest enclosing AST
			this.length = node.getLength();
			this.sDiff = tmp;
			return true;
		}else{
			return false;
		}
	}
	
	//only focus on Statement ASTNodes
	@Override
	public boolean visit(AssertStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(Block node){
		return process(node);
	}
	
	@Override
	public boolean visit(BreakStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(ConstructorInvocation node){
		return process(node);
	}
	
	@Override
	public boolean visit(ContinueStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(DoStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(EmptyStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(EnhancedForStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(ExpressionStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(ForStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(IfStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(LabeledStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(ReturnStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(SuperConstructorInvocation node){
		return process(node);
	}
	
	@Override
	public boolean visit(SwitchCase node){
		return process(node);
	}
	
	@Override
	public boolean visit(SwitchStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(SynchronizedStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(ThrowStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(TryStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(TypeDeclarationStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(VariableDeclarationStatement node){
		return process(node);
	}
	
	@Override
	public boolean visit(WhileStatement node){
		return process(node);
	}
	
	public List<ASTNode> lookforEnclosingAST(List<ASTNode> candies, SourceCodeRange scr){	
		ASTNode prime = null;
		double sDiff = Double.MAX_VALUE;
		int start = scr.startPosition;
		int length = scr.length;
		double tmp = 0;
		for(ASTNode candi : candies){
			if(candi.getStartPosition() <= start && 
					candi.getStartPosition() + candi.getLength() >= start + length){//this is enclosing node
				tmp = Math.pow(candi.getStartPosition() - start, 2) + 
						Math.pow(candi.getLength() - length, 2);
				if(tmp < sDiff){
					sDiff = tmp;
					prime = candi;
				}
			}
		}
		if(prime == null) return null;//not found in the siblings
		this.result = new ArrayList<ASTNode>();
		result.add(prime);
		this.start = start;
		this.length = length;
		this.sDiff = sDiff;
		prime.accept(this);		
		return this.result;
	}
}
