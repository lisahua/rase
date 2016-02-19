package changeassistant.multipleexample.match;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;

public class ContextMatcher {

	public static SimpleTreeNode normalize(Node methodNode, Set<Term> terms,
			Set<String> stmtSet, List<List<SimpleASTNode>> simpleASTNodesList) {
		for (List<SimpleASTNode> sNodes : simpleASTNodesList) {
			PatternUtil.collectTerms(terms, sNodes);
		}
		SimpleTreeNode sTree = new SimpleTreeNode(methodNode, true, 1);
		sTree = PatternUtil.createStringValues(sTree, simpleASTNodesList,
				stmtSet);
		return sTree;
	}

	public static MatchResult match(SimpleTreeNode methodTree, Set<Term> terms,
			Set<String> stmtSet, Node methodNode, CodePattern pat,
			ChangedMethodADT adt, List<List<SimpleASTNode>> simpleASTNodesList) {
		Set<String> standardStmtSet = new HashSet<String>(pat.getStmtSet());
		Set<Term> standardTermSet = new HashSet<Term>(pat.getAlphabetSet());
		MatchResult result = null;

		// ProjectResource prLeft = EnhancedChangeAssistantMain
		// .getCachedProject(adt.getProjectName());
		// System.out.print("");
		// String className = adt.classname;
		// ClassContext cc = prLeft.findClassContext(className);
		// String fileName = WorkspaceUtilities
		// .getSimpleFileName(cc.relativeFilePath);
		// if (!pat.getFileNamingPattern().matcher(fileName).matches())
		// return result;
		// String packageName = WorkspaceUtilities.getPackageName(className);
		// if (!pat.getPackageNamingPattern().matcher(packageName).matches())
		// return result;
		// String simpleClassName = WorkspaceUtilities.getSimpleClassName(
		// className, packageName);
		// if (!pat.getClassNamingPattern().matcher(simpleClassName).matches())
		// return result;
		// String methodName = adt.methodSignature;
		// methodName = methodName.substring(0, methodName.indexOf('('));
		// if (!pat.getMethodNamingPattern().matcher(methodName).matches()) {
		// return result;
		// }

		standardStmtSet.removeAll(stmtSet);
		if (!standardStmtSet.isEmpty()) {
			return result;
		}
		standardTermSet.removeAll(terms);
		if (!standardTermSet.isEmpty()) {
			return result;
		}
		Sequence standardSequence = pat.getSequence();
		Sequence sequence = new Sequence(methodTree);

		result = CodePatternMatcher.matches(methodTree, simpleASTNodesList,
				sequence, pat.sTree, pat.getSimpleASTNodesList(),
				standardSequence);
		return result;
	}
}
