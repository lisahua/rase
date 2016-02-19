package changeassistant.clonereduction.pattern;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.match.CodePattern;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;

public class MethodExtractionPattern extends CodePattern {

	List<SimpleTreeNode> pSNodes = null;

	public void collectFeatures() {
		Enumeration<SimpleTreeNode> sEnum = null;
		SimpleTreeNode sNode = null;
		String stmt = null;
		for (SimpleTreeNode pSNode : pSNodes) {
			sEnum = pSNode.breadthFirstEnumeration();
			while (sEnum.hasMoreElements()) {
				sNode = sEnum.nextElement();
				stmt = sNode.getStrValue();
				PatternUtil.collectTerms(alphabetSet, simpleASTNodesList.get(sNode.getNodeIndex() - 1));
				if (!Term.Abs_And_Exact_Pattern.matcher(stmt).matches()) {
					stmtSet.add(stmt);
				}
			}
		}
		sequence = new Sequence(new ArrayList<Integer>());
		for (int i = 0; i < pSNodes.size(); i++) {
			sequence = sequence.concate(new Sequence(pSNodes.get(i)));
		}
	}

	public List<SimpleTreeNode> getpSNodes() {
		return pSNodes;
	}

	public void setpSNodes(List<SimpleTreeNode> pSNodes) {
		this.pSNodes = pSNodes;
	}

}
