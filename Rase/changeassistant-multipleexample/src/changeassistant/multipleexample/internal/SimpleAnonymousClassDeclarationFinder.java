package changeassistant.multipleexample.internal;

import java.util.Enumeration;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;

public class SimpleAnonymousClassDeclarationFinder {

	public SimpleASTNode find(SimpleASTNode node) {
		Enumeration<SimpleASTNode> astEnum = node.breadthFirstEnumeration();
		SimpleASTNode result = null;
		SimpleASTNode astTmp = null;
		while (astEnum.hasMoreElements()) {
			astTmp = astEnum.nextElement();
			if (astTmp.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION) {
				result = astTmp;
				break;
			}
		}
		return result;
	}
}
