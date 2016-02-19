package changeassistant.multipleexample.match;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;

public class PatternUtil {

	public static boolean isConcrete(Pattern pat) {
		String str = pat.toString();
		return !str.contains("+") && !str.contains("*");
	}

	public static void collectAllIdentifiers(Set<String> alphabetSet,
			SimpleASTNode sNode) {
		Term term = null;
		if (sNode.isLeaf()) {
			term = sNode.getTerm();
			if (term != null && !term.getTermType().equals(Term.TermType.Term)) {
				alphabetSet.add(term.getName());
			} else {// term == null, for u$ListLiteral
				String strValue = sNode.getStrValue();
				if (Term.U_List_Literal_Pattern.matcher(strValue).matches() || Term.U_Pattern.matcher(strValue).matches()) {
					alphabetSet.add(strValue);
				}
			}
		}
	}

	public static void collectAllIdentifiers(Set<String> alphabetSet,
			List<SimpleASTNode> sNodes) {
		Enumeration<SimpleASTNode> aEnum;
		SimpleASTNode aNode = null;
		for (SimpleASTNode sNode : sNodes) {
			aEnum = sNode.breadthFirstEnumeration();
			while (aEnum.hasMoreElements()) {
				aNode = aEnum.nextElement();
				collectAllIdentifiers(alphabetSet, aNode);
			}
		}
	}

	/**
	 * To collect terms used in sNodes
	 * 
	 * @param alphabetSet
	 * @param sNodes
	 */
	public static void collectTerms(Set<Term> alphabetSet,
			List<SimpleASTNode> sNodes) {
		Enumeration<SimpleASTNode> aEnum;
		SimpleASTNode aNode;
		Term term;
		String termName;
		String typeTermName;
		for (SimpleASTNode sNode : sNodes) {
			aEnum = sNode.breadthFirstEnumeration();
			while (aEnum.hasMoreElements()) {
				aNode = aEnum.nextElement();
				if (aNode.isLeaf()) {
					term = aNode.getTerm();
					if (term != null
							&& !term.getTermType().equals(Term.TermType.Term)) {
						termName = term.getName();
						if (!Term.ExactAbsPattern.matcher(termName).matches()) {
							if (term.getTermType().equals(
									Term.TermType.VariableTypeBindingTerm)) {
								typeTermName = ((VariableTypeBindingTerm) term)
										.getTypeNameTerm().getName();
								if (Term.T_Pattern.matcher(typeTermName)
										.matches()) {
									// if the variable itself is not generalized
									// but its type is generalized, this term
									// cannot be put into the alphabetSet as
									// well
									continue;
								}
							}
							alphabetSet.add(Term.normalize(term));
						}
					}
				}
			}
		}
	}

	public static void collectQualifiedNames(Set<String> qualifiedNames,
			List<SimpleASTNode> sNodes) {
		Enumeration<SimpleASTNode> aEnum = null;
		SimpleASTNode aNode = null;
		for (SimpleASTNode sNode : sNodes) {
			aEnum = sNode.breadthFirstEnumeration();
			while (aEnum.hasMoreElements()) {
				aNode = aEnum.nextElement();
				if (!aNode.isLeaf()
						&& aNode.getNodeType() == ASTNode.QUALIFIED_NAME) {
					qualifiedNames.add(aNode.constructStrValue());
				}
			}
		}
	}

	public static SimpleTreeNode createStringValues(SimpleTreeNode sTree,
			List<List<SimpleASTNode>> sNodesList, Set<String> stmtSet) {
		Enumeration<SimpleTreeNode> cEnum = null;
		SimpleTreeNode root = new SimpleTreeNode(sTree);
		SimpleTreeNode sTreeNode = null;
		List<SimpleTreeNode> simpleTreeNodes = new ArrayList<SimpleTreeNode>();
		List<SimpleASTNode> sNodes = null;
		String stmt = null;
		Enumeration<SimpleTreeNode> sEnum = sTree.breadthFirstEnumeration();
		while (sEnum.hasMoreElements()) {
			simpleTreeNodes.add((SimpleTreeNode) sEnum.nextElement().clone());
		}
		Queue<SimpleTreeNode> queue = new LinkedList<SimpleTreeNode>();
		queue.add(root);
		while (!queue.isEmpty()) {
			sTreeNode = queue.remove();
			sNodes = sNodesList.get(sTreeNode.getNodeIndex() - 1);
			stmt = createStrValue(sTreeNode.getNodeType(),
					sTreeNode.getStrValue(), sNodes);
			stmtSet.add(stmt);
			sTreeNode.setStrValue(stmt);
			cEnum = sTreeNode.children();
			while (cEnum.hasMoreElements()) {
				queue.add(cEnum.nextElement());
			}
		}
		return root;
	}

	public static String createStrValue(int nodeType, String strValue,
			List<SimpleASTNode> sNodes) {
		StringBuffer strBuffer = new StringBuffer();
		SimpleASTNode sTmp = null;
		if (sNodes.isEmpty()) {
			// to remove the ending ":" by setting a smaller length
			if (strValue.endsWith(":")) {
				if (strValue.equals("case:"))
					return "default:";
				if (strValue.equals("default:"))
					return strValue;
				return strValue.substring(0, strValue.length() - 1);
			}

			return strValue;
		} else if (strValue.endsWith(":") && !strValue.startsWith("case ")) {
			// sth like then:else:
			return strValue;
		}
		// Enumeration<SimpleASTNode> sEnum = null;
		// SimpleASTNode sTmp = null;
		// for (SimpleASTNode sNode : sNodes) {
		// sEnum = sNode.breadthFirstEnumeration();
		// while (sEnum.hasMoreElements()) {
		// sTmp = sEnum.nextElement();
		// if (!sTmp.isLeaf())
		// sTmp.setRecalc();
		// }
		// sNode.constructStrValue();
		// // recalculate the string value of this
		// // node to remove some unnecessary space
		// }
		String tmpStr = null;
		switch (nodeType) {
		case ASTNode.ANONYMOUS_CLASS_DECLARATION:
			strBuffer.append("new ");
			break;
		case ASTNode.ASSERT_STATEMENT:
			strBuffer.append("assert ")
					.append(trim(sNodes.get(0).getStrValue())).append(";");
			break;
		case ASTNode.BREAK_STATEMENT:
			strBuffer.append("break");
			tmpStr = trim(sNodes.get(0).getStrValue());
			if (!tmpStr.isEmpty()) {
				strBuffer.append(" ").append(tmpStr);
			}
			strBuffer.append(";");
			break;
		case ASTNode.CATCH_CLAUSE:
			strBuffer.append("catch( " + trim(sNodes.get(0).getStrValue())
					+ ")");
			break;
		case ASTNode.CONSTRUCTOR_INVOCATION:
			strBuffer.append("this(" + trim(sNodes.get(0).getStrValue()) + ")");
			break;
		case ASTNode.CONTINUE_STATEMENT:
			strBuffer.append("continue");
			tmpStr = trim(sNodes.get(0).getStrValue());
			if (!tmpStr.isEmpty()) {
				strBuffer.append(" ").append(tmpStr);
			}
			strBuffer.append(";");
			break;
		case ASTNode.DO_STATEMENT:
			strBuffer.append(sNodes.get(0).getStrValue());
			break;
		case ASTNode.ENHANCED_FOR_STATEMENT:
			strBuffer.append("for( " + trim(sNodes.get(0).getStrValue()) + ":" + trim(sNodes.get(1).getStrValue()) + ")");
			break;
		case ASTNode.EXPRESSION_STATEMENT:
			strBuffer.append(sNodes.get(0).getStrValue());
			break;
		case ASTNode.FOR_STATEMENT:
			strBuffer.append("for(");
			StringBuffer tmpBuf = new StringBuffer();
			for (SimpleASTNode sNode : sNodes) {
				tmpBuf.append(trim(sNode.getStrValue())).append(";");
			}
			if (sNodes.size() == 3)
				tmpBuf.setLength(tmpBuf.length() - 1);
			strBuffer.append(tmpBuf).append(")");
			break;
		case ASTNode.IF_STATEMENT:
			strBuffer.append("if(").append(trim(sNodes.get(0).getStrValue()))
					.append(")");
			break;
		case ASTNode.LABELED_STATEMENT:
			strBuffer.append(trim(sNodes.get(0).getStrValue())).append(":");
			break;
		case ASTNode.METHOD_DECLARATION:
			Enumeration<SimpleASTNode> cEnum = sNodes.get(0).children();
			while (cEnum.hasMoreElements()) {
				sTmp = cEnum.nextElement();
				if (sTmp.getNodeType() != ASTNode.BLOCK) {
					strBuffer.append(sTmp.getStrValue());
				} else {
					break;
				}
			}
			break;
		case ASTNode.RETURN_STATEMENT:
			tmpStr = trim(sNodes.get(0).getStrValue());
			strBuffer.append("return");
			if (!tmpStr.isEmpty()) {
				strBuffer.append(" " + tmpStr);
			}
			strBuffer.append(";");
			break;
		case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
			strBuffer.append(trim(sNodes.get(0).getStrValue()));
			break;
		case ASTNode.SWITCH_CASE:
			strBuffer.append("case ").append(trim(sNodes.get(0).getStrValue()))
					.append(":");
			break;
		case ASTNode.SWITCH_STATEMENT:
			strBuffer.append("switch(")
					.append(trim(sNodes.get(0).getStrValue())).append(")");
			break;
		case ASTNode.SYNCHRONIZED_STATEMENT:
			strBuffer.append("synchronized");
			tmpStr = trim(sNodes.get(0).getStrValue());
			if (!tmpStr.isEmpty()) {
				strBuffer.append("(" + tmpStr + ")");
			}
			break;
		case ASTNode.THROW_STATEMENT:
			strBuffer
					.append("throw " + trim(sNodes.get(0).getStrValue()) + ";");
			break;
		case ASTNode.TRY_STATEMENT:
			strBuffer.append("try");
			break;
		case ASTNode.VARIABLE_DECLARATION_STATEMENT:
			strBuffer.append(trim(sNodes.get(0).getStrValue()));
			break;
		case ASTNode.WHILE_STATEMENT:
			strBuffer
					.append("while(" + trim(sNodes.get(0).getStrValue()) + ")");
			break;
		default:
			System.out.println("No processing is defined");
			break;
		}
		return strBuffer.toString();
	}

	protected static String topDownConstructStrValue(SimpleASTNode sTmp) {
		StringBuffer buffer = new StringBuffer();
		Enumeration<SimpleASTNode> cEnum = sTmp.children();
		Enumeration<SimpleASTNode> cEnum2 = null;
		SimpleASTNode child = null;
		SimpleASTNode child2 = null;
		while (cEnum.hasMoreElements()) {
			child = cEnum.nextElement();
			switch (child.getNodeType()) {
			case ASTNode.ANONYMOUS_CLASS_DECLARATION:
				buffer.append("{");
				cEnum2 = child.children();
				while (cEnum2.hasMoreElements()) {
					child2 = cEnum2.nextElement();

				}
				buffer.append("}");
				break;
			}
		}
		return buffer.toString();
	}

	protected static String trim(String str) {
		String result = "";
		if (str != null) {
			result = str.trim();
		}
		return result;
	}

}
