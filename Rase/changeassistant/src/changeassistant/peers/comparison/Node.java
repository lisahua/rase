package changeassistant.peers.comparison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.internal.ASTElementSearcher;
import changeassistant.model.AbstractNode;
import changeassistant.peers.SourceCodeRange;

public class Node extends AbstractNode implements Comparable {

	private static final long serialVersionUID = 1L;
	public static final String SEPARATOR = " ";
	public static final String COLON = ":";

	private List<SourceCodeRange> astExpressions;
	// private WeakReference<List<ASTNode>> astExpressions2;//this may be
	// expensive, so we only use WeakReference

	private boolean fMatched = false;
	private boolean fOrdered = false;
	private List<Integer> editIndexes;// since one node may experience more than
										// one change, this field should be able
										// to contain two elements
	private MethodDeclaration md;

	// private SoftReference<Map<Node, Integer>> nodeLineMap;

	public Node(int astNodeType, String defaultStrValue, SourceCodeRange range,
			Object[] expressions) {
		this.astNodeType = astNodeType;
		this.range = range;
		this.strValue = defaultStrValue; // the strValue is first set to the
											// defaultStrValue
		this.et = EDITED_TYPE.NONE_EDIT; // the node is not edited by default
		this.editIndexes = Collections.emptyList();// no edit index is related
		this.astExpressions = Collections.emptyList();// the astExpressions can
														// be empty but not null

		if (expressions.length == 1
				&& expressions[0] instanceof MethodDeclaration) {
			this.md = (MethodDeclaration) expressions[0];
		} else if (expressions.length == 1
				&& expressions[0] instanceof AnonymousClassDeclaration) {
			System.out
					.print("Node.new Node() creating a node for AnonymousClassDeclaration");
		} else {
			this.astExpressions = new ArrayList<SourceCodeRange>();
			StringBuffer buffer = new StringBuffer("");
			for (Object obj : expressions) {
				if (obj == null)
					continue; // do nothing
				if (obj instanceof ASTNode) {
					astExpressions.add(new SourceCodeRange(((ASTNode) obj)
							.getStartPosition(), ((ASTNode) obj).getLength()));
					buffer.append(((ASTNode) obj).toString() + SEPARATOR);
				} else {
					System.out
							.println("This passed-in object is not an ASTNode");
				}
			}
			astExpressions = Collections.unmodifiableList(astExpressions);
			if (!this.strValue.contains(COLON)) {
				this.strValue = this.strValue + COLON + buffer.toString();
			}
		}
	}

	public void clearMatchedHierarchical() {
		Enumeration<Node> enumeration = this.breadthFirstEnumeration();
		Node node = null;
		while (enumeration.hasMoreElements()) {
			node = enumeration.nextElement();
			if (node.isMatched()) {
				node.disenableMatched();
			}
		}
	}

	@Override
	public Object clone() {
		return super.clone();
		// Object result = super.clone();
		// ((Node)result).astNodeType = this.astNodeType;
		// ((Node)result).strValue = new String(this.strValue);
		// ((Node)result).range = new SourceCodeRange(this.range);
		// if(this.astExpressions.isEmpty()){
		// ((Node)result).astExpressions = Collections.emptyList();
		// }else{
		// List<SourceCodeRange> newAstExpressions = new
		// ArrayList<SourceCodeRange>(this.astExpressions.size());
		// for(int i = 0; i < newAstExpressions.size(); i ++){
		// newAstExpressions.add(new
		// SourceCodeRange(this.astExpressions.get(i)));
		// }
		// ((Node)result).astExpressions = newAstExpressions;
		// }
		// ((Node)result).fMatched = this.fMatched;
		// ((Node)result).fOrdered = this.fOrdered;
		// ((Node)result).et = this.et;
		// if(this.editIndexes.isEmpty()){
		// ((Node)result).editIndexes = Collections.emptyList();
		// }else{
		// List<Integer> newEditIndexes = new
		// ArrayList<Integer>(this.editIndexes.size());
		// for(int i = 0; i < newEditIndexes.size(); i ++){
		// newEditIndexes.add(this.editIndexes.get(i));
		// }
		// }
		// ((Node)result).md = this.md;
		// return result;
	}

	public boolean containMatchedChildren() {
		Enumeration<Node> children = this.children();
		Node node = null;
		while (children.hasMoreElements()) {
			node = children.nextElement();
			if (node.isMatched()) {
				return true;
			}
		}
		return false;
	}

	public boolean containMatchedDescendant() {
		Enumeration<Node> bEnumeration = this.breadthFirstEnumeration();
		Node node = null;
		while (bEnumeration.hasMoreElements()) {
			node = bEnumeration.nextElement();
			if (node.isMatched()) {
				return true;
			}
		}
		return false;
	}

	public void disenableMatched() {
		this.fMatched = false;
	}

	public void enableInOrder() {
		this.fOrdered = true;
	}

	public void enableMatched() {
		this.fMatched = true;
	}

	public void enableOutOfOrder() {
		this.fOrdered = false;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Node))
			return false;
		Node other = (Node) obj;
		if (!this.range.equals(other.range))
			return false;
		return true;
	}

	public List<SourceCodeRange> getASTExpressions() {
		return this.astExpressions;
	}

	public List<ASTNode> getASTExpressions2() {
		List<ASTNode> list = new ArrayList<ASTNode>();
		ASTElementSearcher searcher = new ASTElementSearcher(
				this.getMethodDeclaration());
		for (SourceCodeRange scr : this.astExpressions) {
			ASTNode astExpression = searcher.findElement(scr);
			if (astExpression == null) {
				System.out.println("The marked AST Expression is not found!");
			} else {
				list.add(astExpression);
			}
		}
		return list;
	}

	public List<SourceCodeRange> getAllExpressionContained() {
		List<SourceCodeRange> expressions = new ArrayList<SourceCodeRange>();
		Enumeration<Node> bfEnumeration = this.breadthFirstEnumeration();
		while (bfEnumeration.hasMoreElements()) {
			expressions.addAll(bfEnumeration.nextElement().astExpressions);
		}
		return expressions;
	}

	public List<Integer> getEditIndexes() {
		return this.editIndexes;
	}

	public MethodDeclaration getMethodDeclaration() {
		if (this.md != null)
			return this.md;
		return ((Node) this.getRoot()).getMethodDeclaration();
	}

	public String getNodeTypeString() {
		return this.strValue.substring(0, strValue.indexOf(":") + 1);
	}

	public Set<Node> getNodesIncluded() {
		Set<Node> result = new HashSet<Node>();
		Enumeration<Node> bEnum = this.breadthFirstEnumeration();
		while (bEnum.hasMoreElements()) {
			result.add(bEnum.nextElement());
		}
		return result;
	}

	/**
	 * The nearest sibling comes first
	 * 
	 * @return
	 */
	public List<Node> getPrevSiblings() {
		List<Node> result = new ArrayList<Node>();
		Node temp = this;
		while (temp.getPreviousSibling() != null) {
			temp = (Node) temp.getPreviousSibling();
			result.add(temp);
		}
		return result;
	}

	@Override
	public int hashCode() {
		return astNodeType * 10000 + strValue.hashCode() * 100
				+ range.hashCode();
	}

	public boolean isDeepEquivalentTo(Node node) {
		Node otherNode, thisNode;
		Enumeration<Node> otherEnum = node.depthFirstEnumeration();
		Enumeration<Node> thisEnum = this.depthFirstEnumeration();
		while (otherEnum.hasMoreElements()) {
			otherNode = otherEnum.nextElement();
			thisNode = thisEnum.nextElement();
			if (!otherNode.isEquivalentTo(thisNode))
				return false;
		}
		return true;
	}

	public boolean isEquivalentTo(Node node) {
		if (this.astNodeType != node.astNodeType)
			return false;
		if (!this.strValue.equals(node.strValue))
			return false;
		return true;
	}

	public boolean isInOrder() {
		return this.fOrdered;
	}

	public boolean isMatched() {
		return this.fMatched;
	}

	public int locationInParent() {
		if (this.getParent() == null)
			return 0;
		return this.getParent().getIndex(this);
	}

	// look for node based on source code range
	public Node lookforNodeBasedOnRange(Node target) {
		List<Node> candidates = this.lookforNodeBasedOnRange(target
				.getSourceCodeRange());
		for (Node candidate : candidates) {
			if (candidate.getStrValue().equals(target.getStrValue())) {
				return candidate;
			}
		}
		return null;
	}

	// public Node lookforNodeByAmbiguousLine(CompilationUnit cu, int line){
	// Enumeration<Node> depthFirstEnumeration = this.depthFirstEnumeration();
	// Node temp = null, result = null;
	// Integer tempLine = null;
	// while(depthFirstEnumeration.hasMoreElements()){
	// temp = depthFirstEnumeration.nextElement();
	// try{
	// tempLine = nodeLineMap.get().get(temp);
	// }catch(Exception e){
	// nodeLineMap = new SoftReference<Map<Node, Integer>>(new HashMap<Node,
	// Integer>());
	// }
	// if(tempLine == null){
	// tempLine = cu.getLineNumber(temp.getSourceCodeRange().startPosition);
	// nodeLineMap.get().put(temp, tempLine);
	// }
	// if(tempLine.equals(line)){
	// result = temp;
	// }
	// }
	// return result;
	// }

	/**
	 * look for stmt node based on ambiguous matching
	 * 
	 * @param start
	 * @param length
	 * @return
	 */
	public Node lookforNodeByAmbiguousRange(int start, int length,
			List<Node> nodes) {
		Node result = nodes.get(0);
		double originalSquareDiff = Math.pow(
				result.getSourceCodeRange().startPosition - start, 2)
				+ Math.pow(result.getSourceCodeRange().length - length, 2);
		double squareDiff = originalSquareDiff;
		int tempStart = 0, tempLength = 0;
		Node temp = null;
		for (int i = 1; i < nodes.size(); i++) {
			temp = nodes.get(i);
			if (temp.isMatched() || !temp.isLeaf()) {
				continue;
			}
			tempStart = temp.getSourceCodeRange().startPosition;
			tempLength = temp.getSourceCodeRange().length;
			if (tempStart == start && tempLength == length) {
				return temp;
			} else {
				long diffOffset = temp.getSourceCodeRange().startPosition
						- start;
				long diffLength = temp.getSourceCodeRange().length - length;
				if (diffOffset * diffOffset + diffLength * diffLength < squareDiff) {
					squareDiff = diffOffset * diffOffset + diffLength
							* diffLength;
					result = temp;
				}
			}
		}
		// if(Math.abs(originalSquareDiff - squareDiff) > 1)
		return result;
		// return null;
	}

	/**
	 * Try to look for all the nodes matching the given scr
	 * 
	 * @param scr
	 * @return
	 */
	public List<Node> lookforNodeBasedOnRange(SourceCodeRange scr) {
		List<Node> candidates = new ArrayList<Node>();
		Node temp = null;
		Enumeration<Node> depthFirstEnumeration = this.depthFirstEnumeration();
		while (depthFirstEnumeration.hasMoreElements()) {
			temp = depthFirstEnumeration.nextElement();
			if (temp.getSourceCodeRange().equals(scr)) {
				candidates.add(temp);
			}
		}
		if (candidates.isEmpty()) {
			// System.out.println("The node is not found surprisingly!");
		}
		return candidates;
	}

	// public Node lookforNodeBasedOnPositionAndRange(Node target){
	// Node foundNode = null;
	// Node node = null;
	// Node node2 = null;
	// int knownLocation = 0;
	// if(target.getParent() != null){
	// knownLocation = target.locationInParent();
	// }
	// Enumeration<Node> depthFirstEnumeration = this.depthFirstEnumeration();
	// while(depthFirstEnumeration.hasMoreElements()){
	// Node temp = depthFirstEnumeration.nextElement();
	// if(temp.getNodeType() == target.getNodeType() &&
	// temp.locationInParent() == target.locationInParent()){
	// Enumeration<Node> path =
	// target.pathFromAncestorEnumeration(target.getRoot());
	// Enumeration<Node> path2 = temp.pathFromAncestorEnumeration(this);
	// while(path2.hasMoreElements() && path.hasMoreElements()){
	// node2 = path2.nextElement();
	// node = path.nextElement();
	// if(node2.getNodeType() == node.getNodeType()){
	// //do nothing
	// }else{
	// break;
	// }
	// }
	// if(path2.hasMoreElements() || path.hasMoreElements()){
	// continue;
	// }else if(node2.getNodeType() != node.getNodeType()){
	// continue;
	// }else{//get to the right child generation or level
	// int location = temp.locationInParent();
	// if(location == knownLocation){
	// foundNode = temp;
	// break;
	// }else{
	// continue;
	// }
	// }
	// }else{
	// //do nothing
	// }
	// }
	// return foundNode;
	// }

	public void setEditIndex(int index) {
		try {
			this.editIndexes.add(index);
		} catch (Exception e) {
			this.editIndexes = new ArrayList<Integer>();
			editIndexes.add(index);
		}
	}

	/**
	 * This is especially used by InsertOperation
	 * 
	 * @param md
	 */
	public void setMethodDeclaration(MethodDeclaration md) {
		this.md = md;
	}

	// public Node shallowCopy(){
	// Node newNode = new Node(this.astNodeType, "", new SourceCodeRange(-1,
	// -1), null);
	// return newNode;
	// }

	@Override
	public String toString() {
		return this.strValue;
	}

	@Override
	public int compareTo(Object o) {
		Node other = (Node) o;
		SourceCodeRange range2 = other.range;
		if (this.range.startPosition < range2.startPosition)
			return -1;
		if (this.range.startPosition > range2.startPosition)
			return 1;
		return 0;
	}
}
