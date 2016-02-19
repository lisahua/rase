package changeassistant.clonereduction.manipulate.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.classhierarchy.ClassContext;
import changeassistant.multipleexample.main.CachedProjectMap;
import changeassistant.multipleexample.match.CodePattern;
import changeassistant.multipleexample.match.ContextMatcher;
import changeassistant.multipleexample.match.MatchResult;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.MethodModification;

public class CloneReductionUtil {

	public static List<ChangedMethodADT> getOldADTs(EditInCommonGroup group) {
		List<MethodModification> mmList = group.getMMList();
		List<ChangedMethodADT> adtList = new ArrayList<ChangedMethodADT>();
		for (int i = 0; i < mmList.size(); i++) {
			adtList.add(mmList.get(i).originalMethod);
		}
		return adtList;
	}

	public static List<ChangedMethodADT> getNewADTs(EditInCommonGroup group) {
		List<MethodModification> mmList = group.getMMList();
		List<ChangedMethodADT> adtList = new ArrayList<ChangedMethodADT>();
		for (int i = 0; i < mmList.size(); i++) {
			adtList.add(mmList.get(i).newMethod);
		}
		return adtList;
	}

	public static List<MatchResult> matchMethods(CodePattern pat,
			List<ChangedMethodADT> adts) {
		List<MatchResult> result = new ArrayList<MatchResult>();
		ChangedMethodADT adt = null;
		ClassContext cc = null;
		Node methodNode = null;
		Set<Term> terms = null;
		Set<String> stmtSet = null;
		// System.out.print("");
		SimpleASTCreator creator = new SimpleASTCreator();
		List<List<SimpleASTNode>> simpleASTNodesList = null;
		MatchResult mResult = null;
		SimpleTreeNode sTree = null;
		for (int i = 0; i < adts.size(); i++) {
			adt = adts.get(i);
			cc = CachedProjectMap.get(adt.getProjectName()).findClassContext(
					adt.classname);
			methodNode = cc.getMethodNode(adt.methodSignature);
			terms = new HashSet<Term>();
			stmtSet = new HashSet<String>();
			creator.init();
			simpleASTNodesList = creator.createSimpleASTNodesList(methodNode);
			sTree = ContextMatcher.normalize(methodNode, terms, stmtSet,
					simpleASTNodesList);
			mResult = ContextMatcher.match(sTree, terms, stmtSet, methodNode,
					pat, adt, simpleASTNodesList);
			if (mResult != null) {
				System.out.println("method " + adt.methodSignature
						+ " is matched");
				mResult.setADT(adt);
				result.add(mResult);
			} else {
				System.out.println("method " + adt.methodSignature
						+ " is not matched");
			}
		}
		return result;
	}
}
