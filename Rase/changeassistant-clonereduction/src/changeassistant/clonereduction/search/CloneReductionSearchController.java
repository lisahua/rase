package changeassistant.clonereduction.search;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.PackageResource;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonereduction.main.CloneReductionException;
import changeassistant.clonereduction.manipulate.CloneReductionFilter;
import changeassistant.clonereduction.manipulate.SemanticsEqualConversion;
import changeassistant.clonereduction.manipulate.refactoring.RefactoringMetaData;
import changeassistant.clonereduction.pattern.CloneReductionContextMatcher;
import changeassistant.clonereduction.pattern.MethodExtractionPattern;
import changeassistant.multipleexample.match.ContextMatcher;
import changeassistant.multipleexample.match.MatchResult;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.IdMapper;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;

public class CloneReductionSearchController {

	private ProjectResource pr;
	private List<ChangedMethodADT> knownADTs;
	private CloneReductionFilter crFilter;
	private List<CloneReductionMatchResult> results;
	
	public List<CloneReductionMatchResult> findCandidates(
			CloneReductionFilter crFilter, ProjectResource pr,
			List<ChangedMethodADT> knownADTs) throws CloneReductionException {
		this.pr = pr;
		this.knownADTs = knownADTs;
		this.crFilter = crFilter;
		// System.out.print("");
		MethodExtractionPattern pat = new MethodExtractionPattern();
		pat.setpSNodes(crFilter.getpSNodes());
		pat.setSimpleASTNodesList(crFilter.getCustomizedNodesLists().get(0));
		pat.collectFeatures();
		results = new ArrayList<CloneReductionMatchResult>();
		// matchMethods(pat);
		matchMethods();
		return results;
	}
	
	public List<CloneReductionMatchResult> findCandidates(CloneReductionFilter crFilter, ProjectResource pr, 
			List<ChangedMethodADT> knownADTs, List<MatchResult> mResults, MethodExtractionPattern pat) throws CloneReductionException{
		this.pr = pr;
		this.knownADTs = knownADTs;
		this.crFilter = crFilter;
		results = new ArrayList<CloneReductionMatchResult>();
//		matchMethods(pat, mResults);
		matchMethods();
		return results;
	}

	/**
	 * fake method to search for candidates.
	 * @throws CloneReductionException 
	 */
	protected void matchMethods() throws CloneReductionException {
		CloneReductionMatchResult cResult = null;
		for (ChangedMethodADT adt : knownADTs) {
			System.out.println("method " + adt.methodSignature + " is matched");
			int index = knownADTs.indexOf(adt);
			ClassContext cc = pr.findClassContext(adt.classname);
			Map<String, String> specificToUnified = crFilter
					.getSpecificToUnifiedList().get(index);
			Map<String, String> unifiedToSpecific = IdMapper
					.createReverseMap(specificToUnified);
			cResult = new CloneReductionMatchResult(
					adt, 
					crFilter.getOriginalSNodes().get(index), 
					crFilter.getPartialSNodes().get(index),
					crFilter.getCustomizedNodesLists().get(index),					
					specificToUnified,
					unifiedToSpecific,
					(MethodDeclaration)cc.getMethodAST(adt.methodSignature));
			results.add(cResult);
		}
	}

//	protected void matchMethods(MethodExtractionPattern pat) {
//		Iterator<ClassContext> cIter = pr.classContextIterator();
//		matchMethods(pat, cIter, pr.projectName);
//		Iterator<PackageResource> prIter = pr.packageResourceIterator();
//		PackageResource pacResource = null;
//		while (prIter.hasNext()) {
//			pacResource = prIter.next();
//			cIter = pacResource.classContextIterator();
//			matchMethods(pat, cIter, pr.projectName);
//		}
//	}
	
	protected void matchMethods(MethodExtractionPattern pat, List<MatchResult> mResults) throws CloneReductionException{
		ChangedMethodADT adt = null;
		CloneReductionMatchResult cResult = null;
		Node methodNode = null;
		ClassContext cc = null;
		Set<Term> terms = null;
		Set<String> stmtSet = null;
		List<List<SimpleASTNode>> simpleASTNodesList = null;
		SimpleTreeNode sTree = null;
		SimpleASTCreator creator = new SimpleASTCreator();
		MethodDeclaration md = null;
		if(!mResults.isEmpty()){
			for(MatchResult mResult : mResults){
				adt = mResult.getADT();
				cc = pr.findClassContext(adt.classname);
				methodNode = cc.getMethodNode(adt.methodSignature);
				simpleASTNodesList = creator.createSimpleASTNodesList(methodNode);
				md = methodNode.getMethodDeclaration();
				terms = new HashSet<Term>();
				stmtSet = new HashSet<String>();
				creator.init();	
				sTree = CloneReductionContextMatcher.normalize(methodNode,
						terms, stmtSet, simpleASTNodesList);
				MatchResult tmpmResult = CloneReductionContextMatcher
					.match(sTree, terms, stmtSet, methodNode, pat, adt, simpleASTNodesList);
				if(tmpmResult != null){
					tmpmResult.setADT(adt);
					cResult = new CloneReductionMatchResult(tmpmResult, mResult, sTree, simpleASTNodesList, md);
					results.add(cResult);
				}
//				if(knownADTs.contains(adt)){				
//					System.out.println("method " + adt.methodSignature
//							+ " is matched");
//					int index = knownADTs.indexOf(adt);
//					results.add(convertMatchResult(index, adt, simpleASTNodesList, md));
//				}else{					
//					terms = new HashSet<Term>();
//					stmtSet = new HashSet<String>();
//					creator.init();					
//					sTree = CloneReductionContextMatcher.normalize(methodNode,
//							terms, stmtSet, simpleASTNodesList);
//					MatchResult tmpmResult = CloneReductionContextMatcher.match(sTree, terms, stmtSet, methodNode, pat, adt, simpleASTNodesList);
//					if(tmpmResult != null){
//						tmpmResult.setADT(adt);
//						cResult = new CloneReductionMatchResult(tmpmResult, mResult, sTree, simpleASTNodesList, md);
//						results.add(cResult);
//					}				
//				}
			}
		}		
	}
	
//	private CloneReductionMatchResult convertMatchResult(int index, ChangedMethodADT adt, List<List<SimpleASTNode>> simpleASTNodesList, MethodDeclaration md) 
//		throws CloneReductionException{
//		Map<String, String> specificToUnified = crFilter.getSpecificToUnifiedList().get(index);
//		Map<String, String> unifiedToSpecific = IdMapper
//			.createReverseMap(specificToUnified);
//		CloneReductionMatchResult cResult = new CloneReductionMatchResult(adt, crFilter.getOriginalSNodes().get(index), 
//				crFilter.getPartialSNodes().get(index), specificToUnified, 
//				unifiedToSpecific);	
//		cResult.createUnifiedToConcrete(simpleASTNodesList, md);
//		return cResult;
//	}

//	protected void matchMethods(MethodExtractionPattern pat,
//			Iterator<ClassContext> cIter, String projectName) {
//		ClassContext cc = null;
//		Map<String, SourceCodeRange> map = null;
//		Set<Term> terms = null;
//		Set<String> stmtSet = null;
//		SimpleASTCreator creator = new SimpleASTCreator();
//		Node methodNode = null;
//		String key = null;
//		List<List<SimpleASTNode>> simpleASTNodesList = null;
//		SimpleTreeNode sTree = null;
//		MatchResult mResult = null;
//		ChangedMethodADT adt = null;
//		SourceCodeRange range = null;
//		CloneReductionMatchResult cResult = null;
//		SemanticsEqualConversion converter = new SemanticsEqualConversion(pr);
//		while (cIter.hasNext()) {
//			cc = cIter.next();
//			map = cc.methodMap;
//			for (Entry<String, SourceCodeRange> entry : map.entrySet()) {
//				key = entry.getKey();
//				range = entry.getValue();
//				adt = new ChangedMethodADT(cc.name, key, entry.getValue(),
//						projectName);
//				if (knownADTs.contains(adt)) {
//					System.out.println("method " + adt.methodSignature
//							+ " is matched");
//					int index = knownADTs.indexOf(adt);
//					Map<String, String> specificToUnified = crFilter
//							.getSpecificToUnifiedList().get(index);
//					Map<String, String> unifiedToSpecific = IdMapper
//							.createReverseMap(specificToUnified);
//					cResult = new CloneReductionMatchResult(adt, crFilter
//							.getOriginalSNodes().get(index), crFilter
//							.getPartialSNodes().get(index), specificToUnified,
//							unifiedToSpecific);
//					results.add(cResult);
//				} else {
//					methodNode = cc.getMethodNode(key);
//					match(methodNode);
//					terms = new HashSet<Term>();
//					stmtSet = new HashSet<String>();
//					creator.init();
//					simpleASTNodesList = creator
//							.createSimpleASTNodesList(methodNode);
//					sTree = CloneReductionContextMatcher.normalize(methodNode,
//							terms, stmtSet, simpleASTNodesList);
//					mResult = CloneReductionContextMatcher.match(sTree, terms,
//							stmtSet, methodNode, pat, adt, simpleASTNodesList);
//					if (mResult != null) {
//						System.out.println("method " + adt.methodSignature
//								+ " is matched");
//						mResult.setADT(adt);
//						cResult = new CloneReductionMatchResult(mResult, sTree);
//						results.add(cResult);
//					} else {
//						System.out.println("method " + adt.methodSignature
//								+ " is not matched");
//					}
//				}
//			}
//		}
//	}
}
