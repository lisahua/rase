package changeassistant.multipleexample.partition.datastructure;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.multipleexample.datastructure.MapList;
import changeassistant.multipleexample.main.Constants;
import changeassistant.peers.SourceCodeRange;

public class SimpleASTNode extends DefaultMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int UNDECIDED_NODE_TYPE = -1;

	public static final String UNDECIDED_STR_VALUE = "$UNKNOWN_SYDIT$";
	public static final String UNKNOWN_STATEMENT = "unknown statement";

	public static final String LIST_LITERAL = "$LIST_LITERAL_SYDIT$";

	private int astNodeType;

	private String strValue;

	private SourceCodeRange scr;

	private Term term = null;

	private boolean isGeneral = false;

	private boolean needsRecalc = false;

	public SimpleASTNode(Term term) {
		this(term.getNodeType(), UNKNOWN_STATEMENT, 0, 0);
		this.term = term;
	}

	public SimpleASTNode(SimpleASTNode other) {
		this(other.getNodeType(), other.getStrValue(),
				other.getScr().startPosition, other.getScr().length);
		if (other.term != null) {
			this.term = (Term) other.term.clone();
		}
		Queue<SimpleASTNode> queue1 = new LinkedList<SimpleASTNode>();
		Queue<SimpleASTNode> queue2 = new LinkedList<SimpleASTNode>();
		queue1.add(other);
		queue2.add(this);
		SimpleASTNode node1, node2, child1, child2;
		Enumeration<SimpleASTNode> childEnum = null;
		while (!queue1.isEmpty()) {
			node1 = queue1.remove();
			node2 = queue2.remove();
			childEnum = node1.children();
			while (childEnum.hasMoreElements()) {
				child1 = childEnum.nextElement();
				child2 = (SimpleASTNode) child1.clone();
				node2.add(child2);
				queue1.add(child1);
				queue2.add(child2);
			}
		}
	}

	/**
	 * according to the map, convert part of the node from leftName to rightName
	 * @param node
	 * @param map
	 */
	public static void convert(SimpleASTNode node, Map<String, String> map){
		Queue<SimpleASTNode> queue = new LinkedList<SimpleASTNode>();
		queue.add(node);
		SimpleASTNode sTmp = null, sTmp2 = null;
		String tmpStr = null;
		while(!queue.isEmpty()){
			sTmp = queue.remove();
			tmpStr = sTmp.getStrValue();
			if(map.containsKey(tmpStr)){
				sTmp.setStrValue(map.get(tmpStr));
				sTmp2 = (SimpleASTNode) sTmp.getParent();
				if(sTmp2 != null){
					sTmp2.setRecalcToRoot();
				}
				sTmp.removeAllChildren();
			}
			if(sTmp.getChildCount() > 0){
				for(int i = 0; i < sTmp.getChildCount(); i++){
					queue.add((SimpleASTNode) sTmp.getChildAt(i));
				}
			}
		}
		node.constructStrValue();
	}
	
	private boolean isMarked = false;

	/**
	 * When the nodeType is -1, we currently are creating a unifier for two
	 * argument lists which do not have the same size
	 * 
	 * @param nodeType
	 * @param strValue
	 * @param startPosition
	 * @param length
	 */
	public SimpleASTNode(int nodeType, String strValue, int startPosition,
			int length) {
		this.astNodeType = nodeType;
		this.strValue = strValue;
		this.scr = new SourceCodeRange(startPosition, length);
	}

	public void clearMarked() {
		isMarked = false;
	}

	public void clearRecalc() {
		needsRecalc = false;
	}

	public Object clone() {
		Object obj = super.clone();
		SimpleASTNode other = (SimpleASTNode) obj;
		other.scr = new SourceCodeRange(scr.startPosition,
				scr.length);
		if (term != null) {
			other.term = (Term) term.clone();
		}
		return obj;
	}

	public static boolean hasParentInvocation(SimpleASTNode node) {
		if (node.getParent() == null)
			return false;
		SimpleASTNode parent = (SimpleASTNode) node.getParent();
		int nodeType = parent.getNodeType();
		if (nodeType == ASTNode.METHOD_INVOCATION
				|| nodeType == ASTNode.SUPER_CONSTRUCTOR_INVOCATION
				|| nodeType == ASTNode.SUPER_METHOD_INVOCATION
				|| nodeType == ASTNode.CLASS_INSTANCE_CREATION
				|| nodeType == ASTNode.CONSTRUCTOR_INVOCATION)
			return true;
		return false;
	}

	/**
	 * update the strValue based on children's strValues
	 * 
	 * @return
	 */
	public String constructStrValue() {
		String tmpStr = null;
		System.out.print("");
		if (needsRecalc || strValue.equals(LIST_LITERAL)) {
			Enumeration<SimpleASTNode> childEnum = this.children();
			StringBuffer buffer = new StringBuffer();
			SimpleASTNode sTmp = null;
			while (childEnum.hasMoreElements()) {
				sTmp = childEnum.nextElement();
				if (sTmp.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION) {
					continue;
				}
				tmpStr = sTmp.constructStrValue();
				// The string corresponding to a method declaration's body is
				// ignored
				if (this.getNodeType() == ASTNode.METHOD_DECLARATION
						&& tmpStr.startsWith("{"))
					continue;
				if (tmpStr.trim().equals("instanceof"))
					buffer.append(" ").append("instanceof").append(" ");
				else {
					buffer.append(tmpStr);
				}
			}
			if (!strValue.equals(LIST_LITERAL) || !hasParentInvocation(this)) {
				strValue = buffer.toString();
			}
			needsRecalc = false;
			if (this.getParent() != null)
				((SimpleASTNode) this.getParent()).setRecalc();
			return buffer.toString();
		}
		return strValue;
	}

	public String constructStrValue(Map<String, String> lTou,
			Map<String, String> rTou) {
		if (needsRecalc && this.getChildCount() > 0
				&& !strValue.equals(LIST_LITERAL)) {
			Enumeration<SimpleASTNode> childEnum = this.children();
			StringBuffer buffer = new StringBuffer();
			while (childEnum.hasMoreElements()) {
				buffer.append(childEnum.nextElement().constructStrValue(lTou,
						rTou));
			}
			needsRecalc = false;
			if (Term.U_Pattern.matcher(strValue).matches()
					|| Term.U_Simple_Pattern.matcher(strValue).matches()) {
				for (Entry<String, String> entry : lTou.entrySet()) {
					if (entry.getValue().equals(strValue)) {
						lTou.remove(entry.getKey());
						break;
					}
				}
				for (Entry<String, String> entry : rTou.entrySet()) {
					if (entry.getValue().equals(strValue)) {
						rTou.remove(entry.getKey());
						break;
					}
				}
				if (this.term != null && term.getName().equals(strValue)) {
					this.term = null;
				}
			}
			if (!strValue.equals(LIST_LITERAL) || !hasParentInvocation(this))
				strValue = buffer.toString();
			else
				return buffer.toString();
		} else if (strValue.equals(LIST_LITERAL)) {
			Enumeration<SimpleASTNode> childEnum = this.children();
			StringBuffer buffer = new StringBuffer();
			while (childEnum.hasMoreElements()) {
				buffer.append(childEnum.nextElement().constructStrValue(lTou,
						rTou));
			}
			return buffer.toString();
		}
		return strValue;
	}

	public String constructStrValue(Map<String, String> lTou,
			List<Map<String, String>> rTous) {
		if (needsRecalc && this.getChildCount() > 0
				&& !strValue.equals(LIST_LITERAL)) {
			Enumeration<SimpleASTNode> childEnum = this.children();
			StringBuffer buffer = new StringBuffer();
			while (childEnum.hasMoreElements()) {
				buffer.append(childEnum.nextElement().constructStrValue(lTou,
						rTous));
			}
			needsRecalc = false;
			if (Term.U_Pattern.matcher(strValue).matches()) {
				for (Entry<String, String> entry : lTou.entrySet()) {
					if (entry.getValue().equals(strValue)) {
						lTou.remove(entry.getKey());
						break;
					}
				}
				Map<String, String> rTou = null;
				for (int i = 0; i < rTous.size(); i++) {
					rTou = rTous.get(i);
					for (Entry<String, String> entry : rTou.entrySet()) {
						if (entry.getValue().equals(strValue)) {
							rTou.remove(entry.getKey());
							break;
						}
					}
				}
				if (this.term != null && term.getName().equals(strValue)) {
					this.term = null;
				}
			}
			if (!strValue.equals(LIST_LITERAL) || !hasParentInvocation(this))
				strValue = buffer.toString();
			else
				return buffer.toString();
		} else if (strValue.equals(LIST_LITERAL)) {
			Enumeration<SimpleASTNode> childEnum = this.children();
			StringBuffer buffer = new StringBuffer();
			while (childEnum.hasMoreElements()) {
				buffer.append(childEnum.nextElement().constructStrValue(lTou,
						rTous));
			}
			return buffer.toString();
		}
		return strValue;
	}

	public String constructStrValue(MapList mapList){
		Enumeration<SimpleASTNode> childEnum = children();
		StringBuffer buffer = null;
		if(needsRecalc || strValue.equals(LIST_LITERAL)){
			buffer = new StringBuffer();
			while(childEnum.hasMoreElements()){
				buffer.append(childEnum.nextElement().constructStrValue(mapList));
			}
			needsRecalc = false;
//			if(Term.U_Pattern.matcher(strValue).matches()){
//				mapList.removeEntriesByValue(strValue);				
//				if(term != null && term.getName().equals(strValue)){
//					term = null;
//				}
//			}
			if(!strValue.equals(LIST_LITERAL) || !hasParentInvocation(this))
				strValue = buffer.toString();
			else
				return buffer.toString();
		}
		return strValue;
	}
	
	public static List<List<SimpleASTNode>> concretize(
			Map<String, String> unifiedToSpecific,
			List<List<SimpleASTNode>> sNodesList) {
		List<List<SimpleASTNode>> result = new ArrayList<List<SimpleASTNode>>();
		List<SimpleASTNode> nodes = null, newNodes = null;
		for (int i = 0; i < sNodesList.size(); i++) {
			nodes = sNodesList.get(i);
			newNodes = concretizeSingleNode(unifiedToSpecific, nodes);
			result.add(newNodes);
		}
		return result;
	}

	public static List<SimpleASTNode> concretizeSingleNode(
			Map<String, String> unifiedToSpecific, List<SimpleASTNode> nodes) {
		List<SimpleASTNode> newNodes = new ArrayList<SimpleASTNode>();
		SimpleASTNode node = null, newNode = null, sTmp = null, sTmp2 = null;
		Enumeration<SimpleASTNode> sEnum = null, sEnum2 = null;
		Term tmpTerm = null;
		String tmpArgs = null;
		String tmpStrValue = null;
		String tmpName = null;
		String speName = null;
		// System.out.print("");
		for (int j = 0; j < nodes.size(); j++) {
			node = nodes.get(j);
			newNode = new SimpleASTNode(node);
			sEnum = node.depthFirstEnumeration();
			sEnum2 = newNode.depthFirstEnumeration();
			while (sEnum.hasMoreElements()) {
				sTmp = sEnum.nextElement();
				sTmp2 = sEnum2.nextElement();
				if (sTmp.getStrValue().equals(LIST_LITERAL)
						|| sTmp.getNodeType() == ASTExpressionTransformer.LIST
						|| Term.U_List_Literal_Pattern.matcher(
								sTmp.getStrValue()).matches()) {
					tmpStrValue = sTmp2.constructStrValue();
					if (unifiedToSpecific.containsKey(tmpStrValue)) {
						tmpArgs = unifiedToSpecific.get(tmpStrValue);
						if (tmpArgs
								.startsWith(ASTExpressionTransformer.ARGS_PRE)) {
							tmpArgs = tmpArgs
									.substring(ASTExpressionTransformer.ARGS_PRE
											.length());
						}
						if (sTmp2.getChildCount() > 0) {
							sTmp2.children.clear();
						}
						if (sTmp.getStrValue().equals(LIST_LITERAL)) {
							SimpleASTNode sNode = new SimpleASTNode(-1,
									tmpArgs, 0, 0);
							sTmp2.add(sNode);
						} else {
							sTmp2.setStrValue(tmpArgs);
						}
						if (sTmp2.getParent() != null) {
							((SimpleASTNode) sTmp2.getParent()).setRecalc();
						}
					}
				} else {
					tmpName = sTmp2.constructStrValue();
					if (unifiedToSpecific.containsKey(tmpName)) {
						speName = unifiedToSpecific.get(tmpName);
						sTmp2.setStrValue(speName);
						if (sTmp2.getParent() != null) {
							((SimpleASTNode) sTmp2.getParent()).setRecalc();
						}
						if (sTmp2.getChildCount() > 1)
							// if sTmp2.getChildCount() == 1, the parent
							// node's
							// strValue is equal to the child node's
							// strValue
							sTmp2.children.clear();
						else if (sTmp2.getChildCount() == 1) {
							SimpleASTNode child = (SimpleASTNode) sTmp2.children
									.get(0);
							sTmp2.setTerm(child.getTerm());
							sTmp2.children.clear();
						}
						if (sTmp2.getTerm() != null) {
							tmpTerm = sTmp2.getTerm();
							tmpTerm.setName(speName);
							if (tmpTerm instanceof VariableTypeBindingTerm) {
								tmpTerm = ((VariableTypeBindingTerm) tmpTerm)
										.getTypeNameTerm();
								tmpName = tmpTerm.getName();
								if (unifiedToSpecific.containsKey(tmpName)) {
									speName = unifiedToSpecific.get(tmpName);
									tmpTerm.setName(speName);
								} else {
									// System.out
									// .println("More process is needed");
								}
							}
						}
					}
				}
			}
			newNodes.add(newNode);
		}
		return newNodes;
	}

	public static Map<String, String> createUMap(
			Map<String, String> specificToUnified) {
		Map<String, String> uMap = new HashMap<String, String>();
		String value = null;
		for (Entry<String, String> entry : specificToUnified.entrySet()) {
			value = entry.getValue();
			if (Term.U_Pattern.matcher(value).matches()) {
				uMap.put(entry.getKey(), value);
			}
		}
		return uMap;
	}

	/**
	 * Do not handle u_identifiers
	 * 
	 * @param specificToUnified
	 * @param nodes
	 * @return
	 */
	public static List<SimpleASTNode> customizeSingleNode2(
			Map<String, String> specificToUnified, List<SimpleASTNode> nodes) {
		List<SimpleASTNode> newNodes = new ArrayList<SimpleASTNode>();
		SimpleASTNode node = null, newNode = null;
		for (int j = 0; j < nodes.size(); j++) {
			node = nodes.get(j);
			newNode = customizeSingleSimpleASTNode(specificToUnified, node);
			newNodes.add(newNode);
		}
		return newNodes;
	}

	public static SimpleASTNode customizeSingleSimpleASTNode(
			Map<String, String> specificToUnified, SimpleASTNode node) {
		String tmpName = null;
		SimpleASTNode newNode = new SimpleASTNode(node);
		SimpleASTNode sTmp;
		SimpleASTNode sTmp2;
		SimpleASTNode tmp;
		Enumeration<SimpleASTNode> sEnum;
		Enumeration<SimpleASTNode> sEnum2;
		String uniName;
		Term tmpTerm;
		sEnum = node.depthFirstEnumeration();
		sEnum2 = newNode.depthFirstEnumeration();
		while (sEnum.hasMoreElements()) {
			sTmp = sEnum.nextElement();
			sTmp2 = sEnum2.nextElement();
			if (sTmp.getStrValue().equals(SimpleASTNode.LIST_LITERAL)) {
				tmpName = sTmp.getStrValue();
				sTmp2.setStrValue(tmpName);
				continue;
			}
			tmpName = sTmp2.constructStrValue();
			if (specificToUnified.containsKey(tmpName)) {
				uniName = specificToUnified.get(tmpName);
				sTmp2.setStrValue(uniName);
				tmp = sTmp2;
				while (tmp.getParent() != null) {
					tmp = (SimpleASTNode) tmp.getParent();
					tmp.setRecalc();
				}
				if (sTmp2.getChildCount() > 1)
					// if sTmp2.getChildCount() == 1, the parent
					// node's
					// strValue is equal to the child node's
					// strValue
					sTmp2.children.clear();
				else if (sTmp2.getChildCount() == 1) {
					SimpleASTNode child = (SimpleASTNode) sTmp2.children.get(0);
					sTmp2.setTerm(child.getTerm());
					sTmp2.children.clear();
				}
				// also customize term of the node because it will
				// be
				// used later in unifyCommon()
				if (sTmp2.getTerm() != null) {
					tmpTerm = sTmp2.getTerm();
					tmpTerm.setName(uniName);
					if (tmpTerm instanceof VariableTypeBindingTerm) {
						tmpTerm = ((VariableTypeBindingTerm) tmpTerm)
								.getTypeNameTerm();
						tmpName = tmpTerm.getName();
						if (specificToUnified.containsKey(tmpName)) {
							uniName = specificToUnified.get(tmpName);
							tmpTerm.setName(uniName);
						} else {
							// System.out
							// .println("More process is needed");
						}
					}
				}
			}
		}
		return newNode;
	}

	/**
	 * The customization is separated into two phases: 1. top-down phase to map
	 * u_... identifiers and trim the tree when necessary 2. bottom-up to map
	 * the left identifiers and construct upper-level values from bottom-level
	 * values
	 * 
	 * @param specificToUnified
	 * @param nodes
	 * @return
	 */
	public static List<SimpleASTNode> customizeSingleNode(
			Map<String, String> specificToUnified, Map<String, String> uMap,
			List<SimpleASTNode> nodes) {
		if (uMap.isEmpty())
			return customizeSingleNode2(specificToUnified, nodes);
		boolean needTopDownPhase = checkNeedTopDownPhase(uMap, nodes);
		if (!needTopDownPhase) {
			return customizeSingleNode2(specificToUnified, nodes);
		}
		List<SimpleASTNode> newNodes = new ArrayList<SimpleASTNode>();
		SimpleASTNode node = null, newNode = null, sTmp = null, sTmp2 = null;
		String tmpArgs = null;
		String tmpName = null;
		String uniName = null;
		Queue<SimpleASTNode> sQueue1 = new LinkedList<SimpleASTNode>();
		Queue<SimpleASTNode> sQueue2 = new LinkedList<SimpleASTNode>();
		// System.out.print("");
		for (int j = 0; j < nodes.size(); j++) {
			node = nodes.get(j);
			newNode = new SimpleASTNode(node);
			sQueue1.add(node);
			sQueue2.add(newNode);
			while (!sQueue1.isEmpty()) {
				sTmp = sQueue1.remove();
				sTmp2 = sQueue2.remove();
				if (sTmp.getStrValue().equals(LIST_LITERAL)
						|| sTmp.getNodeType() == ASTExpressionTransformer.LIST) {
					tmpArgs = ASTExpressionTransformer.ARGS_PRE
							+ sTmp.constructStrValue();
					if (uMap.containsKey(tmpArgs)) {
						if (sTmp2.getChildCount() > 0)
							sTmp2.children.clear();
						if (sTmp.getStrValue().equals(LIST_LITERAL)) {
							SimpleASTNode sNode = new SimpleASTNode(-1,
									specificToUnified.get(tmpArgs), 0, 0);
							sTmp2.add(sNode);
						} else {
							sTmp2.setStrValue(specificToUnified.get(tmpArgs));
						}
						if (sTmp2.getParent() != null) {
							((SimpleASTNode) sTmp2.getParent()).setRecalc();
						}
					}
				} else {
					tmpName = sTmp2.getStrValue();
					if (uMap.containsKey(tmpName)) {
						uniName = specificToUnified.get(tmpName);
						sTmp2.setStrValue(uniName);
						sTmp2.setTerm(null);
						if (sTmp2.getParent() != null) {
							((SimpleASTNode) sTmp2.getParent()).setRecalc();
							
						}
						if (sTmp2.getChildCount() > 1)
							// if sTmp2.getChildCount() == 1, the parent
							// node's
							// strValue is equal to the child node's
							// strValue
							sTmp2.children.clear();
						else if (sTmp2.getChildCount() == 1) {
							sTmp2.children.clear();
							sTmp2.setTerm(null);
						}
					}
				}
				if (sTmp2.getChildCount() != 0) {
					String tmpChildContent = null;
					if (sTmp2.getChildCount() == 1) {
						tmpChildContent = ((SimpleASTNode) sTmp2.getChildAt(0))
								.getStrValue();
					}
					if (sTmp.getStrValue().equals(LIST_LITERAL)
							&& tmpChildContent != null
							&& Term.U_List_Literal_Pattern.matcher(
									tmpChildContent).matches()) {
						// do nothing
					} else {
						sQueue1.addAll(sTmp.children);
						sQueue2.addAll(sTmp2.children);
					}
				}
			}
			newNode.constructStrValue();
			newNodes.add(newNode);
		}
		return customizeSingleNode2(specificToUnified, newNodes);
	}

	public static boolean checkNeedTopDownPhase(Map<String, String> uMap,
			List<SimpleASTNode> nodes) {
		boolean needTopDownPhase = false;
		for (String key : uMap.keySet()) {
			for (int i = 0; i < nodes.size(); i++) {
				if (key.isEmpty())
					continue;
				if (key.contains(ASTExpressionTransformer.ARGS_PRE)) {
					if (nodes
							.get(i)
							.getStrValue()
							.contains(
									key.substring(ASTExpressionTransformer.ARGS_PRE
											.length()))) {
						needTopDownPhase = true;
						break;
					}
				} else if (nodes.get(i).getStrValue().contains(key)) {
					needTopDownPhase = true;
					break;
				}
			}
			if (needTopDownPhase)
				break;
		}
		return needTopDownPhase;
	}
	
	public static List<List<SimpleASTNode>> customize(Map<String, String> specificToUnified, 
			List<List<SimpleASTNode>> nodesList){
		Map<String, String> uMap = SimpleASTNode.createUMap(specificToUnified);
		return customize(specificToUnified, uMap, nodesList);
	}

	public static List<List<SimpleASTNode>> customize(
			Map<String, String> specificToUnified, Map<String, String> uMap,
			List<List<SimpleASTNode>> nodesList) {
		List<List<SimpleASTNode>> result = new ArrayList<List<SimpleASTNode>>();
		List<SimpleASTNode> nodes = null, newNodes = null;
		for (int i = 0; i < nodesList.size(); i++) {
			nodes = nodesList.get(i);
			newNodes = customizeSingleNode(specificToUnified, uMap, nodes);
			result.add(newNodes);
		}
		return result;
	}

	public void disableRecalc() {
		needsRecalc = false;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof SimpleASTNode))
			return false;
		SimpleASTNode other = (SimpleASTNode) obj;
		if (this.astNodeType != other.astNodeType)
			return false;
		if (!this.strValue.equals(other.strValue))
			return false;
		return true;
	}

	public int getNodeType() {
		return astNodeType;
	}

	public Vector<SimpleASTNode> getChildren() {
		return this.children;
	}

	public String getStrValue() {
		return strValue;
	}

	public SourceCodeRange getScr() {
		return scr;
	}

	public Term getTerm() {
		return term;
	}

	public int hashCode() {
		Enumeration<SimpleASTNode> bEnum = this.breadthFirstEnumeration();
		SimpleASTNode sNode = null;
		int result = 0;
		while (bEnum.hasMoreElements()) {
			sNode = bEnum.nextElement();
			result += sNode.getNodeType() + sNode.getStrValue().hashCode();
		}
		return result;
	}

	public boolean hasGeneral() {
		return isGeneral;
	}

	public boolean hasMark() {
		return isMarked;
	}

	public boolean hasRecalc() {
		return needsRecalc;
	}

	public void setGeneral() {
		this.isGeneral = true;
	}

	public void setMarked() {
		this.isMarked = true;
	}

	public void setNodeType(int nodeType) {
		this.astNodeType = nodeType;
	}

	public void setRecalc() {
		needsRecalc = true;
	}
	
	public void setRecalcToRoot(){
		needsRecalc = true;
		SimpleASTNode tmp = (SimpleASTNode)parent;
		while(tmp != null){
			tmp.setRecalc();
			tmp = (SimpleASTNode)tmp.parent;
		}
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	@Override
	public String toString() {
		return strValue;
	}
}
