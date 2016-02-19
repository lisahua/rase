package changeassistant.change.group.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.AbstractExpressionRepresentationGenerator;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TermsList;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.model.AbstractNode;
import changeassistant.model.AbstractNode.EDITED_TYPE;
import changeassistant.peers.MatchWorker;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

/**
 * The reason why this new kind of node is defined is that the original Node
 * class already has too much fields and functionality
 * 
 * @author ibm
 * 
 */
public class SubTreeModel extends AbstractNode {

	private static final long serialVersionUID = 1L;

	public static String METHOD_DECLARATION = "method declaration";

	private List<List<Term>> abstractExpressions;

	private int location;

	private boolean fMatched;

	private int matchingIndex;// help subTreeModel remember which node to match;
								// since neither range of position can be used
								// here

	// private int index;//this is used to uniquely identify each node in the
	// tree
	/**
	 * To initialize a subTreeModel instance with a known subTreeModel instance.
	 * Notice: this is a little different from deepCopy(), since the product is
	 * not exactly the same as the original one
	 * 
	 * @param other
	 */
	public SubTreeModel(SubTreeModel other) {
		init(other.editTypes, other.astNodeType, other.getStrValue());
		this.abstractExpressions = other.abstractExpressions;
		this.fMatched = other.fMatched;
		this.range = other.range;

		Stack<SubTreeModel> stackOriginal = new Stack<SubTreeModel>();
		Stack<SubTreeModel> stackNew = new Stack<SubTreeModel>();
		stackOriginal.push(other);
		stackNew.push(this);

		SubTreeModel originalNode, newNode;
		while (!stackOriginal.isEmpty()) {
			originalNode = stackOriginal.pop();
			newNode = stackNew.pop();
			Enumeration<SubTreeModel> children = originalNode.children();
			while (children.hasMoreElements()) {
				SubTreeModel child = children.nextElement();
				SubTreeModel childCopy = (SubTreeModel) child.clone();
				newNode.add(childCopy);
				stackOriginal.push(child);
				stackNew.push(childCopy);
			}
		}
	}

	private void init(Set<EDIT> editTypes, int astNodeType,
			String defaultStrValue) {
		this.editTypes = editTypes;
		this.astNodeType = astNodeType;
		this.abstractExpressions = new ArrayList<List<Term>>();
		this.fMatched = false;
		this.strValue = defaultStrValue;
		this.location = 0; // the default value of each child is 0
		this.matchingIndex = -1;
		this.range = new SourceCodeRange(0, 0);
	}

	private void init2(Node subTree, Node methodNode,
			AbstractExpressionRepresentationGenerator generator) {
		if (subTree.getNodeType() == ASTNode.METHOD_DECLARATION) {
			this.setStrValue(METHOD_DECLARATION);
			List<ASTNode> ASTExpressions = new ArrayList<ASTNode>();
			MethodDeclaration md = methodNode.getMethodDeclaration();
			if (md.getReturnType2() != null) {
				ASTExpressions.add(md.getReturnType2());
			}
			ASTExpressions.addAll(md.parameters());
			this.abstractExpressions = generator
					.getTokenizedRepresentation(ASTExpressions);
			// special process for method name
			this.abstractExpressions.add(generator.getTokenizedRepresentation(
					md.getName().getIdentifier(), md.getNodeType()));
			ASTExpressions = null;
		} else {
			this.setStrValue(subTree.getStrValue());
			List<List<Term>> tokenizedRepresentations = generator
					.getTokenizedRepresentation(methodNode
							.lookforNodeBasedOnRange(subTree)
							.getASTExpressions2());
			this.setAbstractExpressions(tokenizedRepresentations);
		}
		this.setSourceCodeRange(subTree.getSourceCodeRange());
	}

	
	public SubTreeModel(Node subTree, List<List<Term>> expressions){
		init(subTree.getEDITset(), subTree.getNodeType(), subTree.getStrValue());
		this.abstractExpressions = expressions;
	}
	
	public SubTreeModel(Node subTree, Node methodNode,
			AbstractExpressionRepresentationGenerator generator) {
		// deal with the default value
		init(subTree.getEDITset(), subTree.getNodeType(), subTree.getStrValue());
		init2(subTree, methodNode, generator);
	}

	/**
	 * Create a SubTreeModel from a known subTree of a method node
	 * 
	 * @param subTree
	 */
	public SubTreeModel(Node subTree, Node methodNode, boolean copyHierarchy,
			AbstractExpressionRepresentationGenerator generator) {
		init(subTree.getEDITset(), subTree.getNodeType(), subTree.getStrValue());

		init2(subTree, methodNode, generator);

		if (copyHierarchy) {
			Stack<Node> stackOriginal = new Stack<Node>();
			Stack<SubTreeModel> stackNew = new Stack<SubTreeModel>();
			stackOriginal.push(subTree);
			stackNew.push(this);

			while (!stackOriginal.isEmpty()) {
				Node originalNode = stackOriginal.pop();
				SubTreeModel newNode = stackNew.pop();
				Enumeration<Node> children = originalNode.children();
				int indexOfChild = 0;
				while (children.hasMoreElements()) {
					Node child = children.nextElement();
					SubTreeModel childCopy = new SubTreeModel(child,
							methodNode, generator);
					childCopy.setLocation(indexOfChild);// each child remember
														// its index
					indexOfChild++;
					newNode.add(childCopy);
					stackOriginal.push(child);
					stackNew.push(childCopy);
				}
			}
			stackOriginal = null;
		}
		methodNode = null;
		subTree = null;
		generator = null;
	}

	public void clearMatched(boolean clearAll) {
		if (clearAll) {
			Enumeration<SubTreeModel> 
				bEnumeration = this.breadthFirstEnumeration();
			while (bEnumeration.hasMoreElements()) {
				bEnumeration.nextElement().clearMatched(false);
			}
		} else {
			this.fMatched = false;
		}
	}
	
	@Override
	public Object clone(){
		SubTreeModel obj = (SubTreeModel)super.clone();
		obj.abstractExpressions = new ArrayList<List<Term>>(this.abstractExpressions.size());
		List<Term> terms;
		List<Term> oTerms;
		for(int i = 0; i < abstractExpressions.size(); i++){
			oTerms = this.abstractExpressions.get(i);
			terms = new ArrayList<Term>();
			for(int j = 0; j < oTerms.size(); j++){
				terms.add((Term)oTerms.get(j).clone());
			}
			obj.abstractExpressions.add(terms);
		}
		return obj;
	}
	
	public SubTreeModel createCopy(){
		SubTreeModel newRoot = new SubTreeModel((SubTreeModel)this.getRoot());
		SubTreeModel temp = null;
		Enumeration<SubTreeModel> enumeration = newRoot.breadthFirstEnumeration();
		while(enumeration.hasMoreElements()){
			temp = enumeration.nextElement();
			if(temp.equals(this))
				return temp;
		}
		return null;
	}

	public void disableMatched() {
		this.fMatched = false;
	}

	public void enableMatched() {
		this.fMatched = true;
	}

	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		SubTreeModel other = (SubTreeModel) obj;
		if (abstractExpressions.size() != other.abstractExpressions.size())
			return false;
		List<Term> termList1, termList2;
		Term term1, term2;
		for (int i = 0; i < abstractExpressions.size(); i++) {
			termList1 = abstractExpressions.get(i);
			termList2 = other.abstractExpressions.get(i);
			if (termList1.size() != termList2.size())
				return false;
			
				for (int j = 0; j < termList1.size(); j++) {
					term1 = termList1.get(j);
					term2 = termList2.get(j);
					
					if(!term1.equals(term2))
						return false;
				}
		}
		if (this.location != other.location)
			return false;
		if (!this.getSourceCodeRange().equals(other.getSourceCodeRange()))
			return false;
		return true;
	}

	public List<List<Term>> getAbstractExpressions() {
		return this.abstractExpressions;
	}

	public int getMatchingIndex() {
		return this.matchingIndex;
	}

	public int hashCode() {
		return astNodeType * 10000 + strValue.hashCode() + range.hashCode();
	}

	public boolean isDeepCopyOf(SubTreeModel temp) {
		if (this.equals(temp)) {
			Queue<SubTreeModel> queueTemp = new LinkedList<SubTreeModel>();
			queueTemp.add(temp);
			Queue<SubTreeModel> queue = new LinkedList<SubTreeModel>();
			queue.add(this);
			SubTreeModel elemTemp, elem;
			Enumeration<SubTreeModel> childrenTemp, children;
			SubTreeModel childTemp, child;
			boolean flag = true;
			while (!queueTemp.isEmpty() && !queue.isEmpty()) {
				elemTemp = queueTemp.remove();
				elem = queue.remove();
				if (elemTemp.equals(elem)
						&& elemTemp.getChildCount() == elem.getChildCount()) {
					childrenTemp = elemTemp.children();
					children = elem.children();
					while (childrenTemp.hasMoreElements()
							&& children.hasMoreElements()) {
						queueTemp.add(childrenTemp.nextElement());
						queue.add(children.nextElement());
					}
				} else {
					return false;
				}
			}
			if (queueTemp.isEmpty() && queue.isEmpty() && flag) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public boolean isDeepEquivalentTo(SubTreeModel subTree){
		if(this.countNodes() != subTree.countNodes())
			return false;
		Enumeration<SubTreeModel> enumeration1 = this.breadthFirstEnumeration();
		Enumeration<SubTreeModel> enumeration2 = subTree.breadthFirstEnumeration();
		SubTreeModel subTree1, subTree2;
		while(enumeration1.hasMoreElements()){
			subTree1 = enumeration1.nextElement();
			subTree2 = enumeration2.nextElement();
			if(!MatchWorker.isEquivalentNode(subTree1, subTree2, false))
				return false;
		}
		return true;
	}
	
	/**
	 * to judge whether the two sub trees are equivalent
	 * when ignoring parameterized identifiers
	 * @param subTree
	 * @return
	 */
	public boolean isEquivalentTo(SubTreeModel subTree){
		boolean flag = false;
		if(this.getStrValue().equals(METHOD_DECLARATION)){
			if(subTree.getStrValue().equals(METHOD_DECLARATION)){
				flag = true;
			}
			return flag;
		}
		if(this.abstractExpressions.size() != subTree.abstractExpressions.size())
			return flag;
		if(this.abstractExpressions.size() == 0){
			if(this.strValue.equals(subTree.strValue)){
				flag = true;
			}else{
				//do nothing--return false;
			}
		}else{
			if(TermsList.isEquivalent(this.abstractExpressions, 
					subTree.abstractExpressions)){
				flag = true;
			}else{
				// do nothing
			}
		}
		return flag;
	}

	public boolean isMatched() {
		return this.fMatched;
	}

	public int locationInParent() {
		return this.location;
	}

	public List<SubTreeModel> lookforNodeBasedOnPositions(
			List<SubTreeModel> subTreeModelList) {
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		for (SubTreeModel sub : subTreeModelList) {
			SubTreeModel temp = (SubTreeModel) this
					.lookforNodeBasedOnPosition(sub);
			if (temp == null) {
				System.out.println("The mapping node is not found !");
			} else {
				result.add(temp);
			}
		}
		return result;
	}

	public void setAbstractExpressions(
			final List<List<Term>> abstractExpressions) {
		this.abstractExpressions = Collections
				.unmodifiableList(abstractExpressions);
		List<String> stringList = AbstractExpressionRepresentationGenerator
				.createStringList(abstractExpressions);
		if (!stringList.isEmpty()) {
			StringBuffer buffer = new StringBuffer(// the buffer starts with the													// default value
					this.strValue.substring(0,
							this.strValue.indexOf(Node.COLON) + 1));
			for (String str : stringList) {
				buffer.append(str + Node.SEPARATOR);
			}
			this.strValue = buffer.toString();
		}
	}

	public void setLocation(int location) {
		this.location = location;
	}

	public void setMatchingIndex(int index) {
		this.matchingIndex = index;
	}
	
	public String toConcreteString(){
		List<String> stringList = AbstractExpressionRepresentationGenerator
			.createConcreteStringList(abstractExpressions);
		if(!stringList.isEmpty()){
			StringBuffer buffer = new StringBuffer(
					this.strValue.substring(0, this.strValue.indexOf(Node.COLON) + 1));
			for(String str : stringList){
				buffer.append(str + Node.SEPARATOR);
			}
			return buffer.toString();
		}
		return this.strValue;
	}

	public String toString() {
		if (!this.strValue.equals("then:"))
			return this.strValue;
		StringBuffer result = new StringBuffer(strValue);
		Enumeration<SubTreeModel> children = this.children();
		while (children.hasMoreElements()) {
			result.append(children.nextElement().toString()).append(";");
		}
		return result.toString();
	}

	public void updateChildren() {
		Enumeration<SubTreeModel> children = this.children();
		int indexOfChild = 0;
		SubTreeModel child = null;
		while (children.hasMoreElements()) {
			child = children.nextElement();
			child.setLocation(indexOfChild);
			indexOfChild++;
		}
	}
}
