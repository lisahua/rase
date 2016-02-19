package changeassistant.multipleexample.util;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;

public class IdGeneralizer {

	private int abstractMethod = -1;
	private int abstractType = -1;
	private int abstractVariable = -1;

	public void generalize(Map<String, String> specificToUnified,
			List<List<SimpleASTNode>> nodesList) {
		IdMapper.calcMaxAbsCounter(specificToUnified.values());
		abstractMethod = IdMapper.abstractMethod;
		abstractType = IdMapper.abstractType;
		abstractVariable = IdMapper.abstractVariable;
		List<SimpleASTNode> nodes = null;
		for (int i = 0; i < nodesList.size(); i++) {
			nodes = nodesList.get(i);
			generalizeSingleNode(specificToUnified, nodes);
		}
	}

	private void generalizeSingleNode(Map<String, String> specificToUnified,
			List<SimpleASTNode> nodes) {
		SimpleASTNode node = null;
		Enumeration<SimpleASTNode> sEnum = null;
		SimpleASTNode sTmp = null;
		String valueName = null;
		String key = null;
		Term term = null;
		VariableTypeBindingTerm vTerm = null;
		for (int j = 0; j < nodes.size(); j++) {
			node = nodes.get(j);
			sEnum = node.depthFirstEnumeration();
			while (sEnum.hasMoreElements()) {
				sTmp = sEnum.nextElement();
				if (sTmp.getTerm() != null) {
					term = sTmp.getTerm();
					key = term.getName();
					valueName = specificToUnified.get(key);
					if (valueName != null
							&& !Term.ExactAbsPattern.matcher(valueName)
									.matches() || valueName == null) {
						switch (term.getTermType()) {
						case MethodNameTerm: {
							valueName = Term.createAbstractName(
									ASTExpressionTransformer.ABSTRACT_METHOD,
									++abstractMethod);
							specificToUnified.put(key, valueName);
						}
							break;
						case TypeNameTerm: {
							valueName = Term.createAbstractName(
									ASTExpressionTransformer.ABSTRACT_TYPE,
									++abstractType);
							specificToUnified.put(key, valueName);
						}
							break;
						case VariableTypeBindingTerm: {
							valueName = Term.createAbstractName(
									ASTExpressionTransformer.ABSTRACT_VARIABLE,
									++abstractVariable);
							specificToUnified.put(key, valueName);
							vTerm = (VariableTypeBindingTerm) term;
							term = vTerm.getTypeNameTerm();
							key = term.getName();
							String newValueName = specificToUnified.get(key);
							if (newValueName != null) {
								if (!newValueName.equals(valueName)) {
									valueName = newValueName;
									if (!Term.T_Pattern.matcher(valueName)
											.matches()) {
										valueName = Term
												.createAbstractName(
														ASTExpressionTransformer.ABSTRACT_TYPE,
														++abstractType);
										specificToUnified.put(key, valueName);
										specificToUnified.put(key, valueName);
									}
								}
							}
						}
							break;
						}
					}
				}
			}
		}
	}
}
