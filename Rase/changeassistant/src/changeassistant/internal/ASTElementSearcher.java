package changeassistant.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.peers.SourceCodeRange;

public class ASTElementSearcher extends ASTVisitor{
	
	MethodDeclaration md = null;
	
	int startPosition;
	int length;
	
	public ASTElementSearcher(MethodDeclaration md){
		this.md = md;
	}
	
	public void setMethodDeclaration(MethodDeclaration d){
		this.md = d;
	}
	
	@Override
	public void preVisit(ASTNode node){
		if(node.getStartPosition() == startPosition &&
				node.getLength() == length){
			this.astNode = node;
		}
	}
	
	private ASTNode astNode = null;
	
	public ASTNode findElement(SourceCodeRange scr){
		this.startPosition = scr.startPosition;
		this.length = scr.length;
		md.accept(this);
		return astNode;
	}
}
