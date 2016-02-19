package changeassistant.multipleexample.match.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.PackageResource;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.internal.WorkspaceUtilities;
import changeassistant.multipleexample.apply.EditApplyController;
import changeassistant.multipleexample.main.CachedProjectMap;
import changeassistant.multipleexample.main.EnhancedChangeAssistantMain;
import changeassistant.multipleexample.match.CodePattern;
import changeassistant.multipleexample.match.ContextMatcher;
import changeassistant.multipleexample.match.MatchResult;
import changeassistant.multipleexample.match.NamingPatternCreator;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.MethodModification;

public class SearchController {

	private ProjectResource pr = null;

	protected List<List<List<MatchResult>>> findCandidates(
			List<EditInCommonGroup> groups, int counter) {
		List<List<List<MatchResult>>> mResultsLists = new ArrayList<List<List<MatchResult>>>();
		List<List<MatchResult>> mResultsList = null;
		List<MatchResult> mResults = null;
		EditInCommonGroup group = null;
		EditInCommonCluster eClus = null;
		Iterator<EditInCommonCluster> iter = null;
		CodePattern pat = null;
		List<PackageResource> packageResources = null;
		List<ClassContext> classContexts = null;
		List<ChangedMethodADT> adtList = null;
		List<ChangedMethodADT> knownADTList = null;
		List<ChangedMethodADT> knownADTList2 = null;
		int localCounter = 0;
		for (int i = 0; i < groups.size(); i++) {
			// if (i != 9)
			// continue;
			group = groups.get(i);
			knownADTList = getADTs(group);
			iter = group.getIterator();
			mResultsList = new ArrayList<List<MatchResult>>();
			localCounter = 0;
			while (iter.hasNext()) {
				eClus = iter.next();
				knownADTList2 = new ArrayList<ChangedMethodADT>();
				for (Integer inst : eClus.getInstances()) {
					knownADTList2.add(knownADTList.get(inst));
				}
				pat = eClus.getCodePattern();
				// if (pat.getSequence().getNodeIndexes().size() < 4) {
				// mResults = Collections.EMPTY_LIST;
				// mResultsList.add(mResults);
				// continue;
				// }

				// packageResources =
				// processPrName(pat.getPackageNamingPattern());
				// // // System.out.print("");
				// classContexts = filterClassContexts(packageResources,
				// pat.getPackageNamingPattern(),
				// pat.getFileNamingPattern(), pat.getClassNamingPattern());
				// // adtList = filterMethods(pat.getMethodNamingPattern(),
				// // classContexts, knownADTList2);
				// mResults = matchMethods2(pat, classContexts, pr);

				// --- match within the knownADTList2 ranges ---
				// knownADTList2.add(new ChangedMethodADT(
				// "org.eclipse.swt.widgets.Spinner",
				// "getSelectionText()", new SourceCodeRange(0, 0),
				// "16738-win32"));
				// mResults = matchMethods(pat, knownADTList2);

				// --- match within the whole project ---
				mResults = matchMethods(pat, pr);
				if (mResults.isEmpty()) {
					System.out.println("No suggestion for new place");
				} else {
					System.out.println("Interesting suggestions!!!");
					// StringBuffer path = new StringBuffer(
					// "/Users/mn8247/Software/workspaceForStaticAnalysis/changeassistant.multipleexample/tmp/");
					// path.append(counter).append("_").append(i)
					// .append(localCounter++).append(pr.projectName)
					// .append(".txt");
					// File f = new File(path.toString());
					// try {
					// BufferedWriter output = new BufferedWriter(
					// new FileWriter(f));
					// for (MatchResult mr : mResults) {
					// output.write(mr.getADT().toString() + "\n");
					// }
					// // output.write("Time spent is "
					// // + (System.currentTimeMillis() - startTime)
					// // + "\n");
					// output.close();
					// } catch (IOException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
				}
				mResultsList.add(mResults);
			}
			mResultsLists.add(mResultsList);
		}
		return mResultsLists;
	}

	public void searchForCandidates(List<EditInCommonGroup> groups,
			int counter, long startTime) {
		if (groups.size() == 0) {
			return;
		}
		System.out.print("");
		List<List<List<MatchResult>>> mResultsLists = new ArrayList<List<List<MatchResult>>>();
		EditApplyController ec = new EditApplyController();
		if (EnhancedChangeAssistantMain.pmg == null
				|| EnhancedChangeAssistantMain.pmg.candidateProjects.isEmpty()) {
			// if (EnhancedChangeAssistantMain.simpleInit) {
			pr = EnhancedChangeAssistantMain.prLeft;
			mResultsLists = findCandidates(groups, counter);
			ec.apply(mResultsLists, groups, counter);
			// EnhancedChangeAssistantMain.prLeft = pr;
			// EnhancedChangeAssistantMain.simpleInit = true;
			// } else {
			// pr = EnhancedChangeAssistantMain.prLeft;
			// }
		} else {
			// if (NewAction.mode.equals(MODE.MULTIPLE))
			CachedProjectMap.clear();
			for (String prjName : EnhancedChangeAssistantMain.pmg.candidateProjects) {
				pr = CachedProjectMap.get(prjName);
				mResultsLists = findCandidates(groups, counter);
				ec.apply(mResultsLists, groups, counter);
			}
		}
		return;
	}

	private List<ClassContext> filterClassContexts(
			List<PackageResource> packageResources, Pattern prNamePattern,
			Pattern fileNamePattern, Pattern classNamePattern) {
		List<ClassContext> ccList = new ArrayList<ClassContext>();
		PackageResource packageResource = null;
		Iterator<ClassContext> ccIter = null;
		ClassContext cc = null;
		String classPatternName = null;
		String name = null;
		String packageName = null;
		if (classNamePattern.toString()
				.contains(NamingPatternCreator.RegForAny)
				|| classNamePattern.toString().contains(
						NamingPatternCreator.RegForAnyPac)) {
			// we need to match every
			for (int i = 0; i < packageResources.size(); i++) {
				packageResource = packageResources.get(i);
				packageName = packageResource.getPackageName();
				ccIter = packageResource.classContextIterator();
				while (ccIter.hasNext()) {
					cc = ccIter.next();
					name = cc.name;
					name = WorkspaceUtilities.getSimpleClassName(name,
							packageName);
					if (classNamePattern.matcher(name).matches()
							&& fileNamePattern
									.matcher(
											WorkspaceUtilities
													.getSimpleFileName(cc.relativeFilePath))
									.matches()) {
						ccList.add(cc);
					}
				}
			}
		} else {
			classPatternName = classNamePattern.toString();
			for (int i = 0; i < packageResources.size(); i++) {
				packageResource = packageResources.get(i);
				packageName = packageResource.getPackageName();
				ccIter = packageResource.classContextIterator();
				while (ccIter.hasNext()) {
					cc = ccIter.next();
					name = cc.name;
					name = WorkspaceUtilities.getSimpleClassName(name,
							packageName);
					if (classPatternName.equals(name)
							&& fileNamePattern
									.matcher(
											WorkspaceUtilities
													.getSimpleFileName(cc.relativeFilePath))
									.matches()) {
						ccList.add(cc);
					}
				}
			}
		}
		return ccList;
	}

	private List<ChangedMethodADT> filterMethods(Pattern namePattern,
			List<ClassContext> classContexts,
			List<ChangedMethodADT> knownADTList2) {
		List<ChangedMethodADT> adtList = new ArrayList<ChangedMethodADT>();
		Set<String> keys = null;
		String name = null;
		Map<String, SourceCodeRange> map = null;
		ChangedMethodADT adt = null;
		if (namePattern.equals(NamingPatternCreator.RegForAny)) {
			String key = null;
			for (ClassContext cc : classContexts) {
				name = cc.name;
				for (Entry<String, SourceCodeRange> entry : cc.methodMap
						.entrySet()) {
					key = entry.getKey();
					adt = new ChangedMethodADT(name, key, map.get(key));
					adtList.add(adt);
				}
			}
			// adtList.removeAll(knownADTList2);
		} else {
			for (ClassContext cc : classContexts) {
				name = cc.name;
				map = cc.methodMap;
				keys = map.keySet();
				for (String key : keys) {
					if (namePattern.matcher(key.substring(0, key.indexOf('(')))
							.matches()) {
						adt = new ChangedMethodADT(name, key, map.get(key));
						// if (!knownADTList2.contains(adt))
						adtList.add(adt);
					}
				}
			}
		}
		return adtList;
	}

	private List<ChangedMethodADT> getADTs(EditInCommonGroup group) {
		List<MethodModification> mmList = group.getMMList();
		List<ChangedMethodADT> adtList = new ArrayList<ChangedMethodADT>();
		for (int i = 0; i < mmList.size(); i++) {
			adtList.add(mmList.get(i).originalMethod);
		}
		return adtList;
	}

	// private List<MatchResult> matchMethods(CodePattern pat,
	// List<ChangedMethodADT> adts) {
	// List<MatchResult> result = new ArrayList<MatchResult>();
	// ChangedMethodADT adt = null;
	// ClassContext cc = null;
	// Node methodNode = null;
	// Set<Term> terms = null;
	// Set<String> stmtSet = null;
	// // System.out.print("");
	// SimpleASTCreator creator = new SimpleASTCreator();
	// List<List<SimpleASTNode>> simpleASTNodesList = null;
	// MatchResult mResult = null;
	// SimpleTreeNode sTree = null;
	// for (int i = 0; i < adts.size(); i++) {
	// // if (i != 0)
	// // continue;
	// adt = adts.get(i);
	// cc = CachedProjectMap.get(adt.getProjectName()).findClassContext(
	// adt.classname);
	// methodNode = cc.getMethodNode(adt.methodSignature);
	// terms = new HashSet<Term>();
	// stmtSet = new HashSet<String>();
	// creator.init();
	// simpleASTNodesList = creator.createSimpleASTNodesList(methodNode);
	// sTree = ContextMatcher.normalize(methodNode, terms, stmtSet,
	// simpleASTNodesList);
	// mResult = ContextMatcher.match(sTree, terms, stmtSet, methodNode,
	// pat, adt, simpleASTNodesList);
	// if (mResult != null) {
	// System.out.println("method " + adt.methodSignature
	// + " is matched");
	// mResult.setADT(adt);
	// result.add(mResult);
	// } else {
	// System.out.println("method " + adt.methodSignature
	// + " is not matched");
	// }
	// }
	// return result;
	// }

	// private List<MatchResult> matchMethods2(CodePattern pat,
	// List<ClassContext> classContexts, ProjectResource pr) {
	// List<MatchResult> result = new ArrayList<MatchResult>();
	// matchMethods(pat, classContexts.iterator(), result, pr.projectName);
	// return result;
	// }

	private void matchMethods(CodePattern pat, Iterator<ClassContext> cIter,
			List<MatchResult> result, String projectName) {
		System.out.println("");
		ClassContext cc = null;
		Map<String, SourceCodeRange> map = null;
		Set<Term> terms = null;
		Set<String> stmtSet = null;
		SimpleASTCreator creator = new SimpleASTCreator();
		Node methodNode = null;
		String key = null;
		List<List<SimpleASTNode>> simpleASTNodesList = null;
		SimpleTreeNode sTree = null;
		MatchResult mResult = null;
		ChangedMethodADT adt = null;
		while (cIter.hasNext()) {
			cc = cIter.next();
			map = cc.methodMap;
			for (Entry<String, SourceCodeRange> entry : map.entrySet()) {
				key = entry.getKey();
				methodNode = cc.getMethodNode(key);
				terms = new HashSet<Term>();
				stmtSet = new HashSet<String>();
				creator.init();
				simpleASTNodesList = creator
						.createSimpleASTNodesList(methodNode);
				sTree = ContextMatcher.normalize(methodNode, terms, stmtSet,
						simpleASTNodesList);
				adt = new ChangedMethodADT(cc.name, key, entry.getValue(),
						projectName);
				mResult = ContextMatcher.match(sTree, terms, stmtSet,
						methodNode, pat, adt, simpleASTNodesList);
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
		}
	}

	public List<MatchResult> matchMethods(CodePattern pat, ProjectResource pr) {
		if (pat.getSTree().countNodes() == 1
				&& pat.getSTree().getStrValue()
						.equals(SubTreeModel.METHOD_DECLARATION))
			return Collections.emptyList();
		List<MatchResult> result = new ArrayList<MatchResult>();
		Iterator<ClassContext> cIter = pr.classContextIterator();
		matchMethods(pat, cIter, result, pr.projectName);
		Iterator<PackageResource> prIter = pr.packageResourceIterator();
		PackageResource pacResource = null;
		while (prIter.hasNext()) {
			pacResource = prIter.next();
			cIter = pacResource.classContextIterator();
			matchMethods(pat, cIter, result, pr.projectName);
		}
		return result;
	}

	private List<PackageResource> processPrName(Pattern namePattern) {
		String patString = namePattern.toString();
		String tmp = null;
		PackageResource resource = null;
		List<PackageResource> resources = new ArrayList<PackageResource>();
		if (patString.isEmpty()) {
			resource = new PackageResource("");
			resource.setClassMap(pr.getClassMap());
			resources.add(resource);
		} else if (patString.contains(NamingPatternCreator.RegForAny)
				|| patString.contains(NamingPatternCreator.RegForAnyPac)) {
			Iterator<PackageResource> prIter = pr.packageResourceIterator();
			while (prIter.hasNext()) {
				resource = prIter.next();
				tmp = resource.getPackageName();
				if (namePattern.matcher(tmp).matches())
					resources.add(resource);
			}
		} else {
			patString = patString.replace(NamingPatternCreator.RegForDot, ".");
			resource = pr.findPackageResource(patString);
			resources.add(resource);
		}
		return resources;
	}
}
