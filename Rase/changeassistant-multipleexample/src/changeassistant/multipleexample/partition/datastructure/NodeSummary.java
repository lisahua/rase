package changeassistant.multipleexample.partition.datastructure;

import java.util.ArrayList;
import java.util.List;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.Term;

public class NodeSummary implements Cloneable {

	public String nodeStr;
	public List<List<Term>> expressions;
	public int nodeType;

	public NodeSummary() {
	}

	public NodeSummary(String nodeStr, List<List<Term>> termsList, int nodeType) {
		this();
		this.nodeStr = nodeStr;
		if (termsList.size() == 1 && termsList.get(0).size() == 0) {
			this.expressions = new ArrayList<List<Term>>();
		} else {
			this.expressions = termsList;
		}
		this.nodeType = nodeType;
	}

	public Object clone() {
		NodeSummary obj;
		List<Term> terms;
		List<Term> terms2;
		try {
			obj = (NodeSummary) super.clone();
			List<List<Term>> result = new ArrayList<List<Term>>();
			for (int i = 0; i < this.expressions.size(); i++) {
				terms = expressions.get(i);
				terms2 = new ArrayList<Term>();
				for (int j = 0; j < terms.size(); j++) {
					terms2.add((Term) terms.get(j).clone());
				}
				result.add(terms2);
			}
			obj.expressions = result;
			return obj;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// the definition of equals() and hashCode() can allow some difference in
	// supporting instances of a systematic change
	public boolean equals(Object obj) {
		if (!(obj instanceof NodeSummary))
			return false;
		NodeSummary other = (NodeSummary) obj;
		if (!this.nodeStr.equals(other.nodeStr))
			return false;

		if (!this.expressions.equals(other.expressions))
			return false;
		if (this.expressions == null) {
			if (other.expressions != null)
				return false;
		} else if (!this.expressions.equals(other.expressions))
			return false;

		return true;
	}

	public int hashCode() {
		return this.nodeStr.hashCode() * 10000 + this.expressions.hashCode();
	}

	public String toAbstractString() {
		StringBuffer buffer = new StringBuffer();
		StringBuffer aExpr = new StringBuffer();
		List<Term> terms;
		for (int i = 0; i < expressions.size(); i++) {
			buffer = new StringBuffer();
			terms = expressions.get(i);
			for (int j = 0; j < terms.size(); j++) {
				buffer.append(terms.get(j).getAbstractNameWithoutIndex());
			}
			aExpr.append(buffer.toString());
		}
		return aExpr.toString();
	}

	public String toAbstractString2() {
		StringBuffer buffer = new StringBuffer();
		StringBuffer aExpr = new StringBuffer();
		List<Term> terms;
		for (int i = 0; i < expressions.size(); i++) {
			buffer = new StringBuffer();
			terms = expressions.get(i);
			for (int j = 0; j < terms.size(); j++) {
				buffer.append(terms.get(j).getName());
				if (j != terms.size() - 1)
					buffer.append(' ');
			}
		}
		return aExpr.toString();
	}

	public static String toAbstractString(SubTreeModel model) {
		StringBuffer buffer = new StringBuffer();
		StringBuffer aExpr = new StringBuffer();
		List<List<Term>> termsList = model.getAbstractExpressions();
		String tmp = model.toString();
		if (tmp.equals("method declaration")) {
			aExpr.append(tmp);
		} else {
			aExpr.append(tmp.substring(0, tmp.indexOf(":") + 1));
			if (termsList.size() == 0) {
				// do nothing
			} else {
				List<Term> terms;
				for (int i = 0; i < termsList.size(); i++) {
					buffer = new StringBuffer();
					terms = termsList.get(i);
					for (int j = 0; j < terms.size(); j++) {
						buffer.append(terms.get(j)
								.getAbstractNameWithoutIndex());
					}
					aExpr.append(buffer.toString());
				}
			}
		}
		return aExpr.toString();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		StringBuffer result = new StringBuffer(nodeStr + " ");
		List<Term> terms;
		for (int i = 0; i < expressions.size(); i++) {
			buffer = new StringBuffer();
			terms = expressions.get(i);
			for (int j = 0; j < terms.size(); j++) {
				buffer.append(terms.get(j).getName());
			}
			result.append(buffer.toString());
		}
		return result.toString();
	}
}
