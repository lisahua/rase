package changeassistant.internal;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.peers.SourceCodeRange;

public class ASTNodeFinder extends ASTVisitor {
	private long offset, length;

	private boolean isMatched;

	public ASTNode node = null;

	private long squareDiff;

	private void setRange(SourceCodeRange range) {
		node = null;
		this.offset = (long) range.startPosition;
		this.length = (long) range.length;
		this.squareDiff = Long.MAX_VALUE;
		this.isMatched = false;
	}

	@Override
	public void preVisit(ASTNode node) {
		if (!isMatched) {
			if (node.getStartPosition() == this.offset
					&& node.getLength() == this.length) {
				this.squareDiff = 0;
				this.node = node;
				this.isMatched = true;
			} else {
				long diffOffset = this.offset - node.getStartPosition();
				long diffLength = this.length - node.getLength();
				// the new method node is closer to the given range
				if (diffOffset * diffOffset + diffLength * diffLength < this.squareDiff) {
					this.squareDiff = diffOffset * diffOffset + diffLength
							* diffLength;
					this.node = node;
				}
			}
		}
	}

	public ASTNode lookforASTNode(CompilationUnit unit, SourceCodeRange range) {
		this.setRange(range);
		unit.accept(this);
		return this.node;
	}

	public ASTNode lookforASTNode(MethodDeclaration d, SourceCodeRange range) {
		this.setRange(range);
		d.accept(this);
		return this.node;
	}
}
