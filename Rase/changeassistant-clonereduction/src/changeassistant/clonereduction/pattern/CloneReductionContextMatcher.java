package changeassistant.clonereduction.pattern;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.match.CodePatternMatcher;
import changeassistant.multipleexample.match.ContextMatcher;
import changeassistant.multipleexample.match.MatchResult;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;

public class CloneReductionContextMatcher extends ContextMatcher {
	public static MatchResult match(SimpleTreeNode methodTree, Set<Term> terms,
			Set<String> stmtSet, Node methodNode, MethodExtractionPattern pat,
			ChangedMethodADT adt, List<List<SimpleASTNode>> simpleASTNodesList) {
		Set<String> standardStmtSet = new HashSet<String>(pat.getStmtSet());
		MatchResult result = null;
		standardStmtSet.removeAll(stmtSet);
		if (!standardStmtSet.isEmpty()) {
			return result;
		}
		Set<Term> standardTermSet = new HashSet<Term>(pat.getAlphabetSet());
		standardTermSet.removeAll(terms);
		if (!standardTermSet.isEmpty()) {
			return result;
		}
		Sequence standardSequence = pat.getSequence();
		Sequence sequence = new Sequence(methodTree);
		result = CodePatternMatcher.matches(methodTree, simpleASTNodesList,
				sequence, pat.pSNodes, pat.getSimpleASTNodesList(),
				standardSequence);
		return result;
	}
}
