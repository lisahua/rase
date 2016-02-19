package changeassistant.internal;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class ASTChildFinder extends ASTVisitor{
	
	private int counter = 0;
	
	private int position = 0;
	
	private ASTNode parent = null;
	
	private ASTNode node = null;
	
	@Override
	public void preVisit(ASTNode node){
		if(node.getParent().equals(parent)){
			if(this.counter == this.position){
				this.node = node;
			}else{
				counter++;
			}
		}
	}
	
	public ASTNode lookforChild(ASTNode parent, int position){
		counter = 0;
		this.position = position;
		parent.accept(this);
		return this.node;
	}
}
