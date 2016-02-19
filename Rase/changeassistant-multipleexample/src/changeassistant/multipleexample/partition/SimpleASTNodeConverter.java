package changeassistant.multipleexample.partition;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;

public class SimpleASTNodeConverter {

	public static String convertToString(List<SimpleASTNode> nodes,
			String knownStr) {
		if (nodes.size() == 0) {
			return knownStr;
		} else {
			if (nodes.size() == 1) {
				return nodes.get(0).getStrValue();
			} else { // nodes.size() > 1
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < nodes.size(); i++) {
					buffer.append(nodes.get(i).getStrValue()).append(";");
				}
				return buffer.toString();
			}
		}
	}

	public static List<List<Term>> convertToTermsList(
			List<SimpleASTNode> simpleExprs) {
		System.out.print("");
		List<List<Term>> expressions = new ArrayList<List<Term>>();
		SimpleASTNode simpleExpr = null;
		List<Term> terms = null;
		SimpleASTNode sNode = null;
		for (int j = 0; j < simpleExprs.size(); j++) {
			simpleExpr = simpleExprs.get(j);
			Enumeration<SimpleASTNode> dEnum = simpleExpr
					.depthFirstEnumeration();
			terms = new ArrayList<Term>();
			while (dEnum.hasMoreElements()) {
				sNode = dEnum.nextElement();
				if (sNode.isLeaf()) {
					if (sNode.getTerm() == null) {
						terms.add(new Term(sNode.getNodeType(), sNode
								.getStrValue()));
					} else {
						if (sNode.getStrValue().equals(
								SimpleASTNode.LIST_LITERAL)) {
							continue;
						}
						terms.add(sNode.getTerm());
					}
				}
			}
			expressions.add(terms);
		}
		return expressions;
	}
}
