package changeassistant.multipleexample.common;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TermsList;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.multipleexample.partition.MappingException;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.util.IdMapper;

public class CommonParser {

	protected Map<String, String> lTou = null;
	protected Map<String, String> rTou = null;
	protected List<Integer> leftCSIndexes, rightCSIndexes;

	protected int abstractUnknown = 0;
	protected int abstractVariable = 0;
	protected int abstractMethod = 0;
	protected int abstractType = 0;

	protected static final int UP = 1;
	protected static final int LEFT = 2;
	protected static final int DIAG = 3;

	public CommonParser() {
		lTou = new HashMap<String, String>();
		rTou = new HashMap<String, String>();
	}

	public void clear() {
		lTou.clear();
		rTou.clear();
		abstractUnknown = 0;
		abstractMethod = 0;
		abstractMethod = 0;
		abstractType = 0;
	}

	private void constructListLiteralName(StringBuffer buffer,
			SimpleASTNode node) {
		if (node.getChildCount() == 1
				&& Term.U_List_Literal_Pattern.matcher(
						((SimpleASTNode) node.getChildAt(0)).getStrValue())
						.matches()) {
			// do nothing
		} else {
			buffer.append(ASTExpressionTransformer.ARGS_PRE);
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			buffer.append(node.getChildAt(i));
		}
	}

	private SimpleASTNode createCommonSingle(SimpleASTNode other) {
		SimpleASTNode result = new SimpleASTNode(other.getNodeType(),
				other.getStrValue(), other.getScr().startPosition,
				other.getScr().length);
		if (other.getTerm() != null) {
			Term copyTerm = (Term) other.getTerm().clone();
			result.setTerm(copyTerm);
		} else {
			result.setTerm(new Term(other.getNodeType(), other.getStrValue()));
		}
		return result;
	}

	private String getAbstractIdentifier(String lName, String rName)
			throws MappingException {
		String uniName = lName;
		String luniName = lTou.get(lName), runiName = rTou.get(rName);
		if (luniName == null && runiName == null) {
			lTou.put(lName, uniName);
			rTou.put(rName, uniName);
			return uniName;
		}
		if (luniName != null && runiName != null && luniName.equals(runiName)) {
			return uniName;
		} else {
			throw new MappingException(lName + " cannot be mapped to " + rName
					+ " since they " + "are already mapped differently");
		}
	}

	public String getAbstractTypeIdentifier(TypeNameTerm lTerm,
			TypeNameTerm rTerm) throws MappingException {
		System.out.print("");
		String qName1 = lTerm.getQualifiedName();
		String qName2 = rTerm.getQualifiedName();
		List<TypeNameTerm> rterms = new ArrayList<TypeNameTerm>();
		List<TypeNameTerm> lterms = new ArrayList<TypeNameTerm>();
		rterms.add(rTerm);
		lterms.add(lTerm);
		String luniName = null;
		String runiName = null;
		String uniName = null;
		if (TermsList.isSubClass(qName1, rterms)
				|| TermsList.isSubClass(qName2, lterms)) {
			luniName = lTou.get(lTerm.getName());
			runiName = rTou.get(rTerm.getName());
			if (luniName != null) {
				uniName = luniName;
				rTou.put(rTerm.getName(), uniName);
				return uniName;
			} else if (runiName != null) {
				uniName = runiName;
				lTou.put(lTerm.getName(), uniName);
				return uniName;
			}
		}
		throw new MappingException(
				"The two type terms cannot be matched together "
						+ lTerm.toString() + "---" + rTerm.toString());
	}

	/**
	 * This is for unknown identifiers and operators
	 * 
	 * @param lTerm
	 * @param rTerm
	 * @param abstractName
	 * @return
	 * @throws MappingException
	 */
	public String getAbstractSpecialIdentifier(int lType, int rType,
			String lName, String rName, String abstractName)
			throws MappingException {
		String uniName = null;

		System.out.print("");
		uniName = getMappedIdentifier(lName, rName);
		if (uniName == null) {
			if (abstractName.equals(ASTExpressionTransformer.ABSTRACT_VARIABLE)) {
				uniName = Term.createAbstractName(abstractName,
						abstractVariable++);
			} else if (abstractName
					.equals(ASTExpressionTransformer.ABSTRACT_METHOD)) {
				uniName = Term.createAbstractName(abstractName,
						abstractMethod++);
			} else if (abstractName
					.equals(ASTExpressionTransformer.ABSTRACT_TYPE)) {
				uniName = Term.createAbstractName(abstractName, abstractType++);
			} else {// for the operators, we will give different prefix
					// for others, we will append something special
				uniName = Term.createAbstractName(abstractName,
						abstractUnknown++);
				if (lType == ASTExpressionTransformer.OPERATOR
						&& rType == ASTExpressionTransformer.OPERATOR) {
					uniName = uniName + Term.createOpSuffix(lName, rName);
				} else {
					uniName = uniName
							+ Term.createExprSuffix(lType, rType, lName, rName);
				}
			}
		}
		return uniName;
	}

	public String getAbstractIdentifier(String lName, String rName,
			String abstractName) throws MappingException {
		String uniName = getMappedIdentifier(lName, rName);
		if (uniName == null) {
			if (abstractName.equals(ASTExpressionTransformer.ABSTRACT_VARIABLE)) {
				uniName = Term.createAbstractName(abstractName,
						abstractVariable++);
			} else if (abstractName
					.equals(ASTExpressionTransformer.ABSTRACT_METHOD)) {
				uniName = Term.createAbstractName(abstractName,
						abstractMethod++);
			} else if (abstractName
					.equals(ASTExpressionTransformer.ABSTRACT_TYPE)) {
				uniName = Term.createAbstractName(abstractName, abstractType++);
			} else {// for the operators, we will give different prefix
					// for others, we will append something special
				uniName = Term.createAbstractName(abstractName,
						abstractUnknown++);
			}
		}
		return uniName;
	}

	public List<List<SimpleASTNode>> getCommon(
			List<List<SimpleASTNode>> exprsList1,
			List<List<SimpleASTNode>> exprsList2) throws MappingException {
		List<List<SimpleASTNode>> sASTnodesList = new ArrayList<List<SimpleASTNode>>();
		List<SimpleASTNode> sASTNodes = null;
		List<SimpleASTNode> sASTNodes1 = null, sASTNodes2 = null;
		SimpleASTNode sASTNode = null;
		if (exprsList1 == null || exprsList2 == null)
			return null;
		// System.out.print("");
		for (int i = 0; i < exprsList1.size(); i++) {
			sASTNodes1 = exprsList1.get(i);
			sASTNodes2 = exprsList2.get(i);
			sASTNodes = new ArrayList<SimpleASTNode>();
			if (sASTNodes1.size() == sASTNodes2.size()) {
				for (int j = 0; j < sASTNodes1.size(); j++) {
					// System.out.print("");
					sASTNode = getCommon(sASTNodes1.get(j), sASTNodes2.get(j));
					if (sASTNode == null)
						return null;
					sASTNodes.add(sASTNode);
				}
			}
			sASTnodesList.add(sASTNodes);
		}
		return sASTnodesList;
	}

	/**
	 * get common things between two SimpleASTNodes in a hierarchical way
	 * 
	 * @param s1
	 * @param s2
	 * @return may be null when the two conflict with the known identifier
	 *         mappings
	 * @throws MappingException
	 */
	public SimpleASTNode getCommon(SimpleASTNode s1, SimpleASTNode s2)
			throws MappingException {
		SimpleASTNode commonPart = null;
		SimpleASTNode commonTree = null;
		Enumeration<SimpleASTNode> childEnum1, childEnum2;
		SimpleASTNode node3Parent = null;
		SimpleASTNode node1 = null, node2 = null, node3 = null, child1, child2;
		commonTree = parseCommon(s1, s2);
		// if (!sameType(commonTree)) {
		// return null;
		// }
		Queue<SimpleASTNode> queue1 = new LinkedList<SimpleASTNode>();
		Queue<SimpleASTNode> queue2 = new LinkedList<SimpleASTNode>();
		Queue<SimpleASTNode> queue3 = new LinkedList<SimpleASTNode>();
		queue1.add(s1);
		queue2.add(s2);
		queue3.add(commonTree);
		while (!queue1.isEmpty()) {
			node1 = queue1.remove();
			node2 = queue2.remove();
			node3 = queue3.remove();
			if (node1.getStrValue().equals(SimpleASTNode.LIST_LITERAL)
					&& node2.getStrValue().equals(SimpleASTNode.LIST_LITERAL)
					|| node1.getNodeType() == ASTExpressionTransformer.LIST
					&& node2.getNodeType() == ASTExpressionTransformer.LIST) {
				int childCount1 = node1.getChildCount();
				int childCount2 = node2.getChildCount();
				if (childCount1 != childCount2
						|| (childCount1 == 1 && ((Term.U_List_Literal_Pattern
								.matcher(((SimpleASTNode) (node1.getChildAt(0)))
										.getStrValue()).matches()) || Term.U_List_Literal_Pattern
								.matcher(
										((SimpleASTNode) (node2.getChildAt(0)))
												.getStrValue()).matches()))) {
					StringBuffer buffer1 = new StringBuffer();
					StringBuffer buffer2 = new StringBuffer();
					constructListLiteralName(buffer1, node1);
					constructListLiteralName(buffer2, node2);
					SimpleASTNode unifiedNode = null;
					String abstractName = getAbstractSpecialIdentifier(
							ASTExpressionTransformer.LIST_LITERAL,
							ASTExpressionTransformer.LIST_LITERAL,
							buffer1.toString(), buffer2.toString(),
							ASTExpressionTransformer.ABSTRACT_UNKNOWN);
					if (node1.getStrValue().equals(SimpleASTNode.LIST_LITERAL)) {
						unifiedNode = new SimpleASTNode(-1, abstractName, 0, 0);
					} else {
						unifiedNode = new SimpleASTNode(node1.getNodeType(),
								abstractName, 0, 0);
					}
					lTou.put(buffer1.toString(), abstractName);
					rTou.put(buffer2.toString(), abstractName);
					unifiedNode.setMarked();
					node3.add(unifiedNode);
					node3.setRecalc();
					continue;
				}
			} else {
				int childCount1 = node1.getChildCount();
				int childCount2 = node2.getChildCount();
				// The two nodes belong to the same AST node type, but do
				// not share any content
				if (childCount1 != childCount2) {
					continue;
				}
			}
			childEnum1 = node1.children();
			childEnum2 = node2.children();
			while (childEnum1.hasMoreElements() && childEnum2.hasMoreElements()) {
				child1 = childEnum1.nextElement();
				child2 = childEnum2.nextElement();
				System.out.print("");
				commonPart = parseCommon(child1, child2);
				if (sameType(commonPart)) {
					if (commonPart.getStrValue().equals(
							SimpleASTNode.LIST_LITERAL)
							|| Term.Abs_And_Exact_Pattern.matcher(
									commonPart.getStrValue()).matches()) {
						if (Term.U_List_Literal_Pattern.matcher(
								commonPart.getStrValue()).matches()) {
							continue;
						}
						commonPart.setMarked();
					}
					queue1.add(child1);
					queue2.add(child2);
					queue3.add(commonPart);
				}
				// }
				if (commonPart.hasMark()) {
					node3.setRecalc();
				}
				node3.add(commonPart);
			}
		}
		Enumeration<SimpleASTNode> dEnum = commonTree.depthFirstEnumeration();
		Set<SimpleASTNode> cached = new HashSet<SimpleASTNode>();
		while (dEnum.hasMoreElements()) {
			node3 = dEnum.nextElement();
			if (node3.hasMark()) {
				node3Parent = (SimpleASTNode) node3.getParent();
				if (node3Parent != null
						&& cached.add(node3Parent)
						&& !node3Parent.getStrValue().equals(
								SimpleASTNode.LIST_LITERAL)) {
					node3Parent.constructStrValue(lTou, rTou);
				}
				node3.clearMarked();
			}
		}
		return commonTree;
	}

	public Map<String, String> getLtoU() {
		return lTou;
	}

	public Map<String, String> getRtoU() {
		return rTou;
	}

	private String getMappedIdentifier(String lName, String rName)
			throws MappingException {
		String luniName = null;
		String runiName = null;
		String uniName = null;
		if (lName != null && lTou.get(lName) != null) {
			luniName = lTou.get(lName);
		}
		if (rName != null && rTou.get(rName) != null) {
			runiName = rTou.get(rName);
		}
		if (luniName != null && runiName != null) {
			if (luniName.equals(runiName)) {
				uniName = luniName;
			} else {
				throw new MappingException(rName + " cannot be mapped to "
						+ lName + " since they"
						+ "are already mapped differently");
			}
		} else if (luniName != null && runiName == null || luniName == null
				&& runiName != null) {
			if (luniName != null) {
				uniName = luniName;
				for (Entry<String, String> entry : rTou.entrySet()) {
					if (entry.getValue().equals(uniName)) {
						throw new MappingException(lName
								+ " cannot be mapped to " + rName + " since "
								+ lName + " has already been mapped to "
								+ entry.getKey());
					}

				}
			} else {
				uniName = runiName;
				for (Entry<String, String> entry : lTou.entrySet()) {
					if (entry.getValue().equals(uniName))
						throw new MappingException(rName
								+ " cannot be mapped to " + lName + " since "
								+ rName + " has already been mapped to "
								+ entry.getKey());
				}
			}
		}
		return uniName;
	}

	private SimpleASTNode parseCommon(SimpleASTNode left, SimpleASTNode right)
			throws MappingException {
		SimpleASTNode commonPart = (SimpleASTNode) left.clone();
		if (right.getNodeType() != left.getNodeType()) {
			commonPart.setNodeType(SimpleASTNode.UNDECIDED_NODE_TYPE);
		}
		if (left.getStrValue().equals(SimpleASTNode.LIST_LITERAL)
				&& right.getStrValue().equals(SimpleASTNode.LIST_LITERAL))
			return commonPart;

		String uniName = null;
		String lName = null, rName = null;
		Term term = null;
		Term rTerm = right.getTerm();
		Term lTerm = left.getTerm();

		if (lTerm == null && left.getChildCount() == 1) {
			lTerm = ((SimpleASTNode) left.getChildAt(0)).getTerm();
		}
		if (rTerm == null && right.getChildCount() == 1) {
			rTerm = ((SimpleASTNode) right.getChildAt(0)).getTerm();
		}
		System.out.print("");
		if (rTerm == null)
			commonPart.setTerm(null);

		if (rTerm != null && lTerm != null) {
			lName = lTerm.getName();
			rName = rTerm.getName();
			if (rTerm.getTermType().equals(lTerm.getTermType())) {
				term = (Term) lTerm.clone();
				uniName = term.getName();
				if (!right.getStrValue().equals(left.getStrValue())
						|| Term.ExactAbsPattern.matcher(uniName).matches())
				/**
				 * uniName.contains("_") &&
				 * abstractNames.contains(uniName.substring(0,
				 * uniName.indexOf("_"))))
				 **/
				/* &&!uniName.equals(SimpleASTNode.LIST_LITERAL) */{
					switch (term.getTermType()) {
					case VariableTypeBindingTerm:
						uniName = getAbstractIdentifier(lName, rName,
								ASTExpressionTransformer.ABSTRACT_VARIABLE);
						break;
					case MethodNameTerm:
						uniName = getAbstractIdentifier(lName, rName,
								ASTExpressionTransformer.ABSTRACT_METHOD);
						break;
					case TypeNameTerm:
						try {
							uniName = getAbstractIdentifier(lName, rName,
									ASTExpressionTransformer.ABSTRACT_TYPE);
						} catch (Exception e) {
							uniName = getAbstractTypeIdentifier(
									(TypeNameTerm) lTerm, (TypeNameTerm) rTerm);
						}
						break;
					case Term:
						uniName = getAbstractSpecialIdentifier(
								lTerm.getNodeType(), rTerm.getNodeType(),
								lName, rName,
								ASTExpressionTransformer.ABSTRACT_UNKNOWN);
						break;
					}
					term.setName(uniName);
					commonPart.setGeneral();
					commonPart.setMarked();
					commonPart.setStrValue(uniName);
					lTou.put(lName, uniName);
					rTou.put(rName, uniName);
				} else if (right.getStrValue().equals(left.getStrValue())) {
					getAbstractIdentifier(lName, rName);
				}
				if (term instanceof VariableTypeBindingTerm) {
					TypeNameTerm ltTerm = ((VariableTypeBindingTerm) lTerm)
							.getTypeNameTerm();
					TypeNameTerm rtTerm = ((VariableTypeBindingTerm) rTerm)
							.getTypeNameTerm();
					TypeNameTerm ttTerm = ((VariableTypeBindingTerm) term)
							.getTypeNameTerm();
					lName = ltTerm.getName();
					rName = rtTerm.getName();
					if (lName.equals(rName)) {
						getAbstractIdentifier(lName, rName);
					} else {
						try {
							uniName = getAbstractIdentifier(lName, rName,
									ASTExpressionTransformer.ABSTRACT_TYPE);
						} catch (Exception e) {
							uniName = getAbstractTypeIdentifier(ltTerm, rtTerm);
						}
						lTou.put(lName, uniName);
						rTou.put(rName, uniName);
						ttTerm.setName(uniName);
					}
				}
			} else {
				if (Term.ExactAbsPattern.matcher(lName).matches()
						&& Term.ExactAbsPattern.matcher(rName).matches()
						&& !Term.U_Pattern.matcher(lName).matches()
						&& !Term.U_Pattern.matcher(rName).matches()) {
					// lName and rName are v$_n_, m$_n_, t$_n_
					if (lName.startsWith("v$_")) {
						uniName = getAbstractIdentifier(lName, rName,
								ASTExpressionTransformer.ABSTRACT_VARIABLE);
					} else if (lName.startsWith("m$_")) {
						uniName = getAbstractIdentifier(lName, rName,
								ASTExpressionTransformer.ABSTRACT_METHOD);
					} else {
						uniName = getAbstractIdentifier(lName, rName,
								ASTExpressionTransformer.ABSTRACT_TYPE);
					}
				} else {
					try {
						uniName = getAbstractIdentifier(lName, rName,
								ASTExpressionTransformer.ABSTRACT_UNKNOWN);
					} catch (Exception e) {
						uniName = getAbstractTypeIdentifier(
								(TypeNameTerm) lTerm, (TypeNameTerm) rTerm);
					}
					term = new Term(left.getNodeType(), getAbstractIdentifier(
							lName, rName, uniName));
				}

				commonPart.setGeneral();
				commonPart.setMarked();
				commonPart.setStrValue(uniName);
				lTou.put(lName, uniName);
				rTou.put(rName, uniName);
			}
			commonPart.setTerm(term);
		} else {// one or both of the terms do not have terms
			lName = left.getStrValue();
			rName = right.getStrValue();
			if (!rName.equals(lName)
					|| Term.ExactAbsPattern.matcher(lName).matches()) {
				// when the two strings represent different contents or they
				// have the same contents but both unified
				uniName = getAbstractSpecialIdentifier(left.getNodeType(),
						right.getNodeType(), lName, rName,
						ASTExpressionTransformer.ABSTRACT_UNKNOWN);
				commonPart.setGeneral();
				commonPart.setMarked();
				commonPart.setStrValue(uniName);
				commonPart.setTerm(new Term(-1, uniName));
				// for the U_identifiers, create an UNKNOWN term
				lTou.put(lName, uniName);
				rTou.put(rName, uniName);
			}
		}
		return commonPart;
	}

	private boolean sameType(SimpleASTNode commonTree) {
		return commonTree.getNodeType() != SimpleASTNode.UNDECIDED_NODE_TYPE
				|| !Term.U_Pattern.matcher(commonTree.getStrValue()).matches();
	}

	public void initCounter(int methodCounter, int typeCounter, int varCounter,
			int unknownCounter) {
		abstractMethod = methodCounter;
		abstractType = typeCounter;
		abstractVariable = varCounter;
		abstractUnknown = unknownCounter;
	}

	/**
	 * initialize the abstract index
	 * 
	 * @param specificToUnified
	 */
	public void initMap(Map<String, String> specificToUnified) {
		IdMapper.calcMaxAbsCounter(specificToUnified.values());
		abstractMethod = IdMapper.abstractMethod;
		abstractType = IdMapper.abstractType;
		abstractVariable = IdMapper.abstractVariable;
		abstractUnknown = IdMapper.abstractUnknown;

		// prepare the variables so that they will be used directly
		abstractMethod++;
		abstractType++;
		abstractVariable++;
		abstractUnknown++;
	}

	public void setMap(Map<String, String> lTou, Map<String, String> rTou) {
		this.lTou = lTou;
		this.rTou = rTou;
		initMap(lTou);
	}
}
