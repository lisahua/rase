package changeassistant.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import changeassistant.peers.SourceCodeRange;

public class ASTNodeIncludedCollector extends ASTVisitor {
	Set<ASTNode> result;
	ASTElementSearcher searcher;
	SourceCodeRange scr;
	public ASTNodeIncludedCollector(MethodDeclaration d){
		searcher = new ASTElementSearcher(d);
	}
	
	@Override
	public void preVisit(ASTNode node){
		if(node.getStartPosition() == scr.startPosition &&
				node.getLength() == scr.length){
			if(node instanceof Block || node instanceof DoStatement ||
					node instanceof EnhancedForStatement || node instanceof ForStatement ||
					node instanceof IfStatement || node instanceof SwitchStatement ||
					node instanceof SynchronizedStatement || node instanceof ThrowStatement ||
					node instanceof TryStatement || node instanceof TypeDeclarationStatement ||
					node instanceof WhileStatement){
//				System.out.println("This node should not be included!");
				//do nothing
			}else{
				result.add(node);
			}
			//do nothing, do not collect the node itself
		}else{
			result.add(node);
		}
	}
	
	public Set<ASTNode> collectASTNodesWithin(SourceCodeRange scr){
		result = new HashSet<ASTNode>();
		this.scr = scr;
		ASTNode astNode = searcher.findElement(scr);
		astNode.accept(this);
		return result;
	}
	
//	public Set<ASTNode> collectASTNodesWithin(ASTNode astNode){
//		result = new HashSet<ASTNode>();
//		astNode.accept(this);
//		return result;
//	}
}
