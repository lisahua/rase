package changeassistant.multipleexample.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.main.ProjectMethodPair;
import changeassistant.multipleexample.contextualize.ContextController;
import changeassistant.multipleexample.contextualize.ContextualizeHelper2;
import changeassistant.multipleexample.datastructure.Pair;
import changeassistant.multipleexample.editfilter.RenameUpdateFilter;
import changeassistant.multipleexample.match.search.SearchController;
import changeassistant.multipleexample.partition.ChangeSummaryCreator;
import changeassistant.multipleexample.partition.PartitionController;
import changeassistant.multipleexample.partition.datastructure.AbstractCluster;
import changeassistant.multipleexample.partition.datastructure.BaseCluster;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.Java2XML;
import changeassistant.multipleexample.util.PropertyLoader;
import changeassistant.multipleexample.util.XML2Java;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.DiffParser;
import changeassistant.versions.comparison.MethodModification;

public class EnhancedChangeAssistantMain {

	public static boolean simpleInit = false;

	public boolean USE_CACHE = true;
	public static ProjectMethodGroup2 pmg = null;
	public static Map<String, IProject> prjMap = null;

	public static List<List<MethodModification>> resultMMs = null;
	public static List<List<ChangedMethodADT>> resultADTLists = null;

	public static List<Pair<ProjectResource>> prPairList = null;

	protected List<EditInCommonGroup> groups = null;

	public static int EXAMPLE_NUMBER = 2;

	public static boolean LIMIT_HOP_DISTANCE = false;

	public static boolean ABSTRACT_ALL = false;

	int STEP = 2; // when STEP = 1, we try to create cluster lattice

	// List<List<BitSet>> resultMMIndexes = null;

	Map<ChangedMethodADT, MethodModification> map = null;
	String src;
	String oldFilePath, newFilePath, oldClassName, newClassName, oldMethodName,
			newMethodName;
	IFile oldFile, newFile;

	IProject fProjectLeft = null;
	IProject fProjectRight = null;

	public static ProjectResource prLeft = null;
	ProjectResource prRight = null;

	String sProjectLeft = null;
	String sProjectRight = null;

	/**
	 * If a group does not contain any applicable cluster, do not consider this
	 * group any more
	 * 
	 * @param groups
	 * @return
	 */
	public List<EditInCommonGroup> createSysEdits(List<EditInCommonGroup> groups) {
		List<EditInCommonGroup> newGroups = new ArrayList<EditInCommonGroup>();
		EditInCommonGroup group = null;
		Iterator<EditInCommonCluster> iter = null;
		EditInCommonCluster eClus = null;
		for (int i = 0; i < groups.size(); i++) {
			// if (i != 14)
			// continue;
			group = groups.get(i);
			group.initializeIterator();
			iter = group.getIterator();
			if (!iter.hasNext()) {
				continue;
			} else {
				newGroups.add(group);
			}
			int counter = 0;
			while (iter.hasNext()) {
				eClus = iter.next();
				eClus.createEdits(group);
				counter++;
			}
			if (counter > 1) {
				System.out.println("More process is needed");
			}
		}
		return newGroups;
	}

	protected Set<Set<Integer>> enumeratePlans(int start, int end, int number) {
		Set<Set<Integer>> result = new HashSet<Set<Integer>>();
		Set<Integer> plan = null;
		if (number == 1) {
			for (int i = start; i <= end; i++) {
				plan = new HashSet<Integer>();
				plan.add(i);
				result.add(plan);
			}
		} else {
			Set<Set<Integer>> tmpResult = null;
			for (int i = start; i < end; i++) {
				tmpResult = enumeratePlans(i + 1, end, number - 1);
				for (Set<Integer> tmpPlan : tmpResult) {
					tmpPlan.add(i);
				}
				result.addAll(tmpResult);
			}
		}
		return result;
	}

	private void execute(List<EditInCommonGroup> groups, String dir) {
		// List<List<List<MatchResult>>> mResultsLists = null;
		// List<EditInCommonGroup> newGroups = new
		// ArrayList<EditInCommonGroup>();
		// PartitionController pc = new PartitionController();
		// // GeneralizationController gc = new GeneralizationController();
		// ContextController cc = new ContextController();
		// SearchController sc = new SearchController();
		// EditApplyController ec = new EditApplyController();
		// TermsList.prLeft = prLeft;
		// TermsList.prRight = prRight;
		// System.out.print("");
		// newGroups = pc.partition(groups);
		// if (STEP == 1) {
		// save1(newGroups, dir);
		// return;
		// }
		// // System.out.print("");
		// this.groups = cc.contextualize(newGroups);
		// this.groups = createSysEdits(this.groups);
		//
		// mResultsLists = sc.searchForCandidates(this.groups);
		// ec.apply(mResultsLists, this.groups);
		// newGroups = ec.extend(newGroups);
	}

	private void execute(List<EditInCommonGroup> groups, int counter,
			long startTime) {
		List<EditInCommonGroup> newGroups = new ArrayList<EditInCommonGroup>();
		PartitionController pc = new PartitionController();
		// GeneralizationController gc = new GeneralizationController();
		ContextController cc = new ContextController();
		SearchController sc = new SearchController();
		System.out.print("");
		newGroups = pc.partition(groups);
		System.out.print("");
		this.groups = cc.contextualize(newGroups);
		this.groups = createSysEdits(this.groups);

		sc.searchForCandidates(this.groups, counter, startTime);
	}

	protected void executeForSingleExample(List<EditInCommonGroup> groups,
			int counter, long startTime) {
		EditInCommonGroup group = null;
		List<AbstractCluster> baseClusters = null;
		List<EditInCommonCluster> eClusters = null;
		EditInCommonCluster eClus = null;
		BaseCluster bClus = null;
		List<Integer> indexes = null;
		SimpleTreeNode sTree = null;
		SimpleTreeNode sTree2 = null;
		List<List<SimpleTreeNode>> forests = null;
		Node node = null;
		for (int i = 0; i < groups.size(); i++) {
			group = groups.get(i);
			eClusters = new ArrayList<EditInCommonCluster>();
			baseClusters = (List<AbstractCluster>) group.createBaseClusters();
			for (int j = 0; j < baseClusters.size(); j++) {
				bClus = (BaseCluster) baseClusters.get(j);
				eClus = new EditInCommonCluster(bClus.getConChgSum(),
						bClus.getChgSumStr(), bClus.getAbsChgSumStr());
				indexes = new ArrayList<Integer>();
				for (int k = 0; k < bClus.getMM().getEdits().size() - 2; k++) {
					indexes.add(k);
				}
				eClus.addIncoming(bClus, indexes, new HashMap<String, String>());
				node = (Node) bClus.getMM().getEdits().get(0).getParentNode()
						.getRoot();
				sTree = new SimpleTreeNode(node, true, 1);
				node = bClus.getMM().getEdits()
						.get(bClus.getMM().getEdits().size() - 1).getNode();
				sTree2 = new SimpleTreeNode(node, true, 1);
				bClus.setSTree(sTree);
				bClus.encodeSequence();
				eClus.addSTree(sTree);
				eClus.addSTree2(sTree2);
				eClus.setSequence(bClus.getSequence());
				eClus.addSequence(bClus.getSequence());
				eClus.setSimpleASTNodesList(bClus.getSimpleASTNodesList());
				eClus.setExprsLists(bClus.getSimpleExprsLists());
				eClus.enableForestOrder();
				forests = new ArrayList<List<SimpleTreeNode>>();
				forests.add(eClus.getSTrees());
				eClus.setForests(forests);
				eClusters.add(eClus);
			}
			group.setEditInCommonClusters(eClusters);
			ContextualizeHelper2 cHelper2 = new ContextualizeHelper2(eClusters,
					group.getMMList());
			cHelper2.refineCommonContext();
		}
		SearchController sc = new SearchController();
		this.groups = createSysEdits(groups);
		sc.searchForCandidates(this.groups, counter, startTime);
	}

	private void findMethodModifications() {
		resultMMs = new ArrayList<List<MethodModification>>();
		List<MethodModification> mms = null;
		MethodModification mm = null;
		List<ChangedMethodADT> adtList = null;
		ChangedMethodADT adt = null;
		map = new HashMap<ChangedMethodADT, MethodModification>();
		for (int i = 0; i < resultADTLists.size(); i++) {
			mms = new ArrayList<MethodModification>();
			adtList = resultADTLists.get(i);
			for (int j = 0; j < adtList.size(); j++) {
				adt = adtList.get(j);
				mms.add(getMM(adt));
			}
			resultMMs.add(mms);
		}
		map = null;
	}

	public List<EditInCommonGroup> getGroups() {
		return groups;
	}

	private MethodModification getMM(ChangedMethodADT adt) {
		MethodModification mm = map.get(adt);
		ClassContext oldCC = null, newCC = null;
		if (mm == null) {
			DiffParser diffParser = new DiffParser();
			oldCC = prLeft.findClassContext(adt.classname);
			System.out.print("");
			newCC = prRight.findClassContext(adt.classname);
			List<MethodModification> mmList = diffParser.compareClassContext(
					prLeft, prRight, oldCC, newCC);
			for (MethodModification elem : mmList) {
				map.put(elem.originalMethod, elem);
			}
			mm = map.get(adt);
		}
		mm.originalMethod.setProjectName(adt.getProjectName());
		return mm;
	}

	private MethodModification getMMWithoutCache(ChangedMethodADT adt) {
		ClassContext oldCC = null;
		ClassContext newCC = null;
		DiffParser diffParser = new DiffParser();
		oldCC = prLeft.findClassContext(adt.classname);
		newCC = prRight.findClassContext(adt.classname);
		List<MethodModification> mmList = diffParser.compareClassContext(
				prLeft, prRight, oldCC, newCC);
		System.out.print("");
		for (MethodModification elem : mmList) {
			if (elem.originalMethod.equals(adt)) {
				elem.originalMethod.setProjectName(sProjectLeft);
				elem.newMethod.setProjectName(sProjectRight);
				return elem;
			}
		}
		return null;
	}

	private MethodModification getMM(ChangedMethodADT adt1,
			ChangedMethodADT adt2) {
		MethodModification mm = null;
		ClassContext oldCC = null, newCC = null;
		DiffParser diffParser = new DiffParser();
		oldCC = prLeft.findClassContext(adt1.classname);
		newCC = prRight.findClassContext(adt2.classname);
		List<MethodModification> mmList = diffParser.compareClassContext(
				prLeft, prRight, oldCC, newCC);
		for (MethodModification elem : mmList) {
			if (elem.originalMethod.equals(adt1)) {
				mm = elem;
				break;
			}
		}
		mm.originalMethod.setProjectName(adt1.getProjectName());
		mm.newMethod.setProjectName(adt2.getProjectName());
		return mm;
	}

	private List<EditInCommonGroup> initialize1() {
		List<EditInCommonGroup> groups = new ArrayList<EditInCommonGroup>();
		List<MethodModification> mmList = null;
		List<MethodModification> newMMList = null;
		List<List<ChangeSummary>> chgSumsList = null;
		List<ChangeSummary> chgSums = null;
		ChangeSummary chgSum = null;
		List<List<String>> chgSumStrs = null;
		List<List<String>> absChgSumStrs = null;
		List<String> chgSumStr = null;
		List<String> absChgSumStr = null;
		ChangeSummaryCreator csCreator = new ChangeSummaryCreator();
		for (int i = 0; i < resultMMs.size(); i++) {
			mmList = resultMMs.get(i);
			newMMList = new ArrayList<MethodModification>();
			chgSumsList = new ArrayList<List<ChangeSummary>>();
			chgSumStrs = new ArrayList<List<String>>();
			absChgSumStrs = new ArrayList<List<String>>();
			for (int j = 0; j < mmList.size(); j++) {
				chgSums = csCreator.createSummary(mmList.get(j));
				if (RenameUpdateFilter.filterOut(chgSums))
					continue;

				chgSumStr = new ArrayList<String>();
				absChgSumStr = new ArrayList<String>();
				for (int k = 0; k < chgSums.size(); k++) {
					chgSum = chgSums.get(k);
					chgSumStr.add(csCreator.createChgSumStr1(chgSum));
					absChgSumStr.add(csCreator.createChgSumStr2(chgSum));
				}
				newMMList.add(mmList.get(j));
				chgSumsList.add(chgSums);
				chgSumStrs.add(chgSumStr);
				absChgSumStrs.add(absChgSumStr);
			}
			if (newMMList.size() != 0)
				groups.add(new EditInCommonGroup(newMMList, chgSumsList,
						chgSumStrs, absChgSumStrs));
		}
		return groups;
	}

	private List<EditInCommonGroup> initialize2(String dir) {
		List<EditInCommonGroup> groups = new ArrayList<EditInCommonGroup>();
		List<MethodModification> mmList = null;
		List<MethodModification> newMMList = null;
		List<List<ChangeSummary>> chgSumsList = null;
		List<ChangeSummary> chgSums = null;
		ChangeSummary chgSum = null;
		List<List<String>> chgSumStrs = null;
		List<List<String>> absChgSumStrs = null;
		List<String> chgSumStr = null;
		List<String> absChgSumStr = null;
		ChangeSummaryCreator csCreator = new ChangeSummaryCreator();
		for (int i = 0; i < resultMMs.size(); i++) {
			mmList = resultMMs.get(i);
			newMMList = new ArrayList<MethodModification>();
			chgSumsList = new ArrayList<List<ChangeSummary>>();
			chgSumStrs = new ArrayList<List<String>>();
			absChgSumStrs = new ArrayList<List<String>>();
			for (int j = 0; j < mmList.size(); j++) {
				chgSums = csCreator.createSummary(mmList.get(j));
				if (RenameUpdateFilter.filterOut(chgSums))
					continue;

				chgSumStr = new ArrayList<String>();
				absChgSumStr = new ArrayList<String>();
				for (int k = 0; k < chgSums.size(); k++) {
					chgSum = chgSums.get(k);
					chgSumStr.add(csCreator.createChgSumStr1(chgSum));
					absChgSumStr.add(csCreator.createChgSumStr2(chgSum));
				}
				newMMList.add(mmList.get(j));
				chgSumsList.add(chgSums);
				chgSumStrs.add(chgSumStr);
				absChgSumStrs.add(absChgSumStr);
			}
			if (newMMList.size() != 0)
				groups.add(new EditInCommonGroup(newMMList, chgSumsList,
						chgSumStrs, absChgSumStrs));
		}
		return groups;
	}

	public void run(List<IProject> elements) {
		String dir = (String) PropertyLoader.props
				.getProperty("Primitive_Result_Path");
		if (elements.size() < 2) {
			System.out
					.println("There are not enough elements selected to derive systematic changes");
		} else if (prLeft == null) {
			CachedProjectMap.init();
			fProjectLeft = elements.get(0);
			sProjectLeft = fProjectLeft.getFullPath().toOSString().substring(1);

			fProjectRight = elements.get(1);
			sProjectRight = fProjectRight.getFullPath().toOSString()
					.substring(1);

			Set<String> filePaths = new HashSet<String>();
			if (STEP == 1)
				resultADTLists = XML2Java.readXML(dir + sProjectLeft + "__"
						+ sProjectRight + ".xml", filePaths);
			else if (STEP == 2)
				resultADTLists = XML2Java.readGroupXML(dir + sProjectLeft
						+ "__" + sProjectRight + "__groups.xml", filePaths);
			String[] filePathsArray = filePaths.toArray(new String[filePaths
					.size()]);
			prLeft = new ProjectResource(fProjectLeft, filePathsArray);
			prRight = new ProjectResource(fProjectRight, filePathsArray);
			CachedProjectMap.put(sProjectLeft, prLeft);
			CachedProjectMap.put(sProjectRight, prRight);
			// prLeft = new ProjectResource(fProjectLeft);
			// prRight = new ProjectResource(fProjectRight);

			fProjectLeft = fProjectRight = null;
			findMethodModifications();// to set resultMMs
		}

		List<EditInCommonGroup> groups = null;
		if (STEP == 1)
			groups = initialize1();
		else if (STEP == 2)
			groups = initialize2(dir);
		execute(groups, dir);
	}

	public void runForGiven(Map<String, IProject> projectMap,
			List<ProjectMethodPair> pairs) {
		try {
			List<EditInCommonGroup> groups = null;
			EditInCommonGroup group = null;
			List<MethodModification> newMMList = null;
			int counter = 0;
			map = new HashMap<ChangedMethodADT, MethodModification>();
			prjMap = projectMap;
			CachedProjectMap.setProjectMap(prjMap);
			List<List<ChangeSummary>> chgSumsList = null;
			List<List<String>> chgSumStrs = null;
			List<List<String>> absChgSumStrs = null;
			List<ChangeSummary> chgSums = null;
			List<String> chgSumStr = null;
			List<String> absChgSumStr = null;
			ChangeSummaryCreator csCreator = new ChangeSummaryCreator();
			ChangeSummary chgSum = null;
			for (ProjectMethodPair pair : pairs) {
				CachedProjectMap.clear();
				groups = new ArrayList<EditInCommonGroup>();
				counter++;
				if (counter != 1)
					continue;
				long startTime = System.currentTimeMillis();
				newMMList = new ArrayList<MethodModification>();
				src = pair.src.trim();
				sProjectLeft = pair.leftProjectName;
				fProjectLeft = projectMap.get(sProjectLeft);
				prLeft = new ProjectResource(fProjectLeft);
				prLeft.projectName = sProjectLeft;
				CachedProjectMap.put(sProjectLeft, prLeft);
				// prLeft = new ProjectResource(fProjectLeft, src,
				// pair.leftFilePath1, pair.rightFilePath1);
				sProjectRight = pair.rightProjectName;
				fProjectRight = projectMap.get(sProjectRight);
				// prRight = new ProjectResource(fProjectRight, src,
				// pair.leftFilePath2, pair.rightFilePath2/*
				// * ,
				// * "org/gjt/sp/jedit/gui/RolloverButton"
				// */);
				prRight = new ProjectResource(fProjectRight);
				prRight.projectName = sProjectRight;
				CachedProjectMap.put(sProjectRight, prRight);
				newMMList.add(getMM(new ChangedMethodADT(pair.leftClassName1,
						pair.leftMethodName1, sProjectLeft),
						new ChangedMethodADT(pair.leftClassName2,
								pair.leftMethodName2, sProjectRight)));
				if (EXAMPLE_NUMBER != 1)
					newMMList.add(getMM(new ChangedMethodADT(
							pair.rightClassName1, pair.rightMethodName1,
							sProjectLeft), new ChangedMethodADT(
							pair.rightClassName2, pair.rightMethodName2,
							sProjectRight)));
				// System.out.print("");
				chgSumsList = new ArrayList<List<ChangeSummary>>();
				chgSumStrs = new ArrayList<List<String>>();
				absChgSumStrs = new ArrayList<List<String>>();
				for (int j = 0; j < newMMList.size(); j++) {
					chgSums = csCreator.createSummary(newMMList.get(j));
					if (RenameUpdateFilter.filterOut(chgSums))
						continue;
					chgSumStr = new ArrayList<String>();
					absChgSumStr = new ArrayList<String>();
					for (int k = 0; k < chgSums.size(); k++) {
						chgSum = chgSums.get(k);
						chgSumStr.add(csCreator.createChgSumStr1(chgSum));
						absChgSumStr.add(csCreator.createChgSumStr2(chgSum));
					}
					chgSumsList.add(chgSums);
					chgSumStrs.add(chgSumStr);
					absChgSumStrs.add(absChgSumStr);
				}
				if (newMMList.size() != 0)
					group = new EditInCommonGroup(newMMList, chgSumsList,
							chgSumStrs, absChgSumStrs);
				groups.add(group);
				if (EXAMPLE_NUMBER == 1)
					executeForSingleExample(groups, counter, startTime);
				else
					execute(groups, counter, startTime);
			}
			map = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run2(Map<String, IProject> map, List<ProjectMethodGroup2> pmgs2) {
		prjMap = map;
		prPairList = new ArrayList<Pair<ProjectResource>>();
		groups = new ArrayList<EditInCommonGroup>();
		CachedProjectMap.setProjectMap(prjMap);
		int counter = 0;
		EditInCommonGroup group = null;
		List<ProjectMethod> pms = null;
		List<List<ChangeSummary>> chgSumsList = null;
		List<List<String>> chgSumStrs = null;
		List<List<String>> absChgSumStrs = null;
		List<ChangeSummary> chgSums = null;
		List<String> chgSumStr = null;
		List<String> absChgSumStr = null;
		ChangeSummaryCreator csCreator = new ChangeSummaryCreator();
		ChangeSummary chgSum = null;
		ProjectMethod pm = null;
		ChangedMethodADT adt = null;
		List<MethodModification> mms = null;
		long startTime = 0;
		// System.out.print("");
		for (ProjectMethodGroup2 pmg2 : pmgs2) {
			counter++;
			if (counter != 1)
				continue;
			pmg = pmg2;
			pms = pmg2.getMembers();
			if (EXAMPLE_NUMBER > pms.size())
				continue;
			mms = new ArrayList<MethodModification>();
			startTime = System.currentTimeMillis();
			for (int i = 0; i < EXAMPLE_NUMBER; i++) {
				pm = pms.get(i);
				oldFilePath = pm.oldFilePath;
				newFilePath = pm.newFilePath;
				src = pm.src;
				sProjectLeft = pm.oldProjectName;
				prLeft = CachedProjectMap.get(sProjectLeft);

				sProjectRight = pm.newProjectName;
				prRight = CachedProjectMap.get(sProjectRight);

				oldClassName = pm.oldClassName;
				newClassName = pm.newClassName;
				oldMethodName = pm.oldMethodName;
				newMethodName = pm.newMethodName;

				simpleInit = true;

				// extract code changes
				if (oldMethodName.equals(newMethodName)) {
					adt = new ChangedMethodADT(oldClassName, oldMethodName,
							sProjectLeft);
					mms.add(getMMWithoutCache(adt));
				} else {
					DiffParser parser = new DiffParser();
					mms.add(parser
							.compareMethodContext(prLeft, prRight,
									new ChangedMethodADT(oldClassName,
											oldMethodName, sProjectLeft),
									new ChangedMethodADT(newClassName,
											newMethodName, sProjectRight)));
				}
			}
			chgSumsList = new ArrayList<List<ChangeSummary>>();
			chgSumStrs = new ArrayList<List<String>>();
			absChgSumStrs = new ArrayList<List<String>>();
			for (int j = 0; j < mms.size(); j++) {
				chgSums = csCreator.createSummary(mms.get(j));
				if (RenameUpdateFilter.filterOut(chgSums))
					continue;
				chgSumStr = new ArrayList<String>();
				absChgSumStr = new ArrayList<String>();
				for (int k = 0; k < chgSums.size(); k++) {
					chgSum = chgSums.get(k);
					chgSumStr.add(csCreator.createChgSumStr1(chgSum));
					absChgSumStr.add(csCreator.createChgSumStr2(chgSum));
				}
				chgSumsList.add(chgSums);
				chgSumStrs.add(chgSumStr);
				absChgSumStrs.add(absChgSumStr);
			}
			if (mms.size() != 0)
				group = new EditInCommonGroup(mms, chgSumsList, chgSumStrs,
						absChgSumStrs);
			groups.add(group);
			execute(groups, counter, startTime);
		}
	}

	public void run3(Map<String, IProject> map, List<ProjectMethodGroup2> pmgs2) {
		prjMap = map;
		prPairList = new ArrayList<Pair<ProjectResource>>();
		groups = new ArrayList<EditInCommonGroup>();
		CachedProjectMap.setProjectMap(prjMap);
		int counter = 0;
		int tmpCounter = 0;
		List<ProjectMethod> pms = null;
		ChangeSummaryCreator csCreator = new ChangeSummaryCreator();
		List<MethodModification> mms = null;
		long startTime = 0;
		// System.out.print("");
		Set<Set<Integer>> exemplarPlans = null;
		// for (EXAMPLE_NUMBER = 4; EXAMPLE_NUMBER < 6; EXAMPLE_NUMBER++) {
		for (ProjectMethodGroup2 pmg2 : pmgs2) {
			counter++;
			if (counter != 1)
				continue;
			exemplarPlans = enumeratePlans(0, pmg2.getMembers().size() - 1,
					EXAMPLE_NUMBER);
			pmg = pmg2;
			pms = pmg2.getMembers();
			if (EXAMPLE_NUMBER > pms.size())
				continue;
			mms = new ArrayList<MethodModification>();
			startTime = System.currentTimeMillis();
			tmpCounter = 0;
			for (Set<Integer> plan : exemplarPlans) {
				groups.clear();
				mms.clear();
				tmpCounter++;
				// if (tmpCounter < 126)
				// continue;
				System.out.println(tmpCounter);
				for (Integer inst : plan) {
					performDiff(map, pms, mms, inst);
				}
				EditInCommonGroup group = createGroup(csCreator, mms);
				groups.add(group);
				execute(groups, tmpCounter, startTime);
			}
		}
		// }
	}

	public EditInCommonGroup createGroup(ChangeSummaryCreator csCreator,
			List<MethodModification> mms) {
		ChangeSummary chgSum;
		List<List<ChangeSummary>> chgSumsList = new ArrayList<List<ChangeSummary>>();
		List<List<String>> chgSumStrs = new ArrayList<List<String>>();
		List<List<String>> absChgSumStrs = new ArrayList<List<String>>();
		List<ChangeSummary> chgSums = null;
		List<String> chgSumStr = null;
		List<String> absChgSumStr = null;
		EditInCommonGroup group = null;
		for (int j = 0; j < mms.size(); j++) {
			chgSums = csCreator.createSummary(mms.get(j));
			if (RenameUpdateFilter.filterOut(chgSums))
				continue;
			chgSumStr = new ArrayList<String>();
			absChgSumStr = new ArrayList<String>();
			for (int k = 0; k < chgSums.size(); k++) {
				chgSum = chgSums.get(k);
				chgSumStr.add(csCreator.createChgSumStr1(chgSum));
				absChgSumStr.add(csCreator.createChgSumStr2(chgSum));
			}
			chgSumsList.add(chgSums);
			chgSumStrs.add(chgSumStr);
			absChgSumStrs.add(absChgSumStr);
		}
		if (mms.size() != 0)
			group = new EditInCommonGroup(mms, chgSumsList, chgSumStrs,
					absChgSumStrs);
		return group;
	}

	public void performDiff(Map<String, IProject> map, List<ProjectMethod> pms,
			List<MethodModification> mms, Integer inst) {
		ProjectMethod pm;
		ChangedMethodADT adt;
		pm = pms.get(inst);
		oldFilePath = pm.oldFilePath;
		newFilePath = pm.newFilePath;
		src = pm.src;
		sProjectLeft = pm.oldProjectName;
		if (USE_CACHE)
			prLeft = CachedProjectMap.get(sProjectLeft);
		else
			prLeft = null;
		if (prLeft == null) {
			fProjectLeft = map.get(sProjectLeft);
			prLeft = new ProjectResource(fProjectLeft, src, oldFilePath);
			// prLeft = new ProjectResource(fProjectLeft);
			prLeft.projectName = sProjectLeft;
			// cachedPrMap.put(sProjectLeft, prLeft);
		}

		sProjectRight = pm.newProjectName;
		if (USE_CACHE)
			prRight = CachedProjectMap.get(sProjectRight);
		else
			prRight = null;
		if (prRight == null) {
			fProjectRight = map.get(sProjectRight);
			prRight = new ProjectResource(fProjectRight, src, newFilePath);
			// prRight = new ProjectResource(fProjectRight);
			prRight.projectName = sProjectRight;
			// cachedPrMap.put(sProjectRight, prRight);
		}

		oldClassName = pm.oldClassName;
		newClassName = pm.newClassName;
		oldMethodName = pm.oldMethodName;
		newMethodName = pm.newMethodName;

		simpleInit = true;

		System.out.print("");
		// extract code changes
		if (oldMethodName.equals(newMethodName)) {
			adt = new ChangedMethodADT(oldClassName, oldMethodName,
					sProjectLeft);
			MethodModification mm = getMMWithoutCache(adt);
			if (mm != null)
				mms.add(getMMWithoutCache(adt));
			else
				System.out.print("The method is not found to change!");
		} else {
			DiffParser parser = new DiffParser();
			mms.add(parser.compareMethodContext(prLeft, prRight,
					new ChangedMethodADT(oldClassName, oldMethodName,
							sProjectLeft), new ChangedMethodADT(newClassName,
							newMethodName, sProjectRight)));
		}
	}

	private void save1(List<EditInCommonGroup> groups, String dir) {
		try {
			Java2XML.writeGroupXML(dir + sProjectLeft + "__" + sProjectRight
					+ "__groups.xml", prLeft, groups);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This is a dummy search for candidates in order to check whether the match
	 * function can match methods from which the code pattern generates for each
	 * detected systematic edit, find candidates to apply Currently, the temp
	 * implementation is aimed at establish matching for the methods from which
	 * the edit is created to check the pattern's usability
	 * 
	 * @param groups
	 */
	// private void searchForCandidates(List<EditInCommonGroup> groups) {
	// EditInCommonGroup group = null;
	// EditInCommonCluster eClus = null;
	// Iterator<EditInCommonCluster> iter = null;
	// List<Integer> insts = null;
	// List<MethodModification> mmList = null;
	// ChangedMethodADT adt = null;
	// MethodModification mm = null;
	// Node methodNode = null;
	// SimpleASTCreator creator = new SimpleASTCreator();
	// List<List<SimpleASTNode>> simpleASTNodesList = null;
	// for (int i = 0; i < groups.size(); i++) {
	// // if (i != 0)
	// // continue;
	// group = groups.get(i);
	// mmList = group.getMMList();
	// iter = group.getIterator();
	// while (iter.hasNext()) {
	// eClus = iter.next();
	// insts = eClus.getInstances();
	// for (Integer inst : insts) {
	// mm = mmList.get(inst);
	// methodNode = (Node) mm.getEdits().get(0).getParentNode()
	// .getRoot();
	// adt = mm.originalMethod;
	// Set<Term> terms = new HashSet<Term>();
	// Set<String> stmtSet = new HashSet<String>();
	// creator.init();
	// // System.out.print("");
	// simpleASTNodesList = creator
	// .createSimpleASTNodesList(methodNode);
	// SimpleTreeNode sTree = ContextMatcher.normalize(methodNode,
	// terms, stmtSet, simpleASTNodesList);
	// if (ContextMatcher.match(sTree, terms, stmtSet, methodNode,
	// eClus.getCodePattern(), adt, simpleASTNodesList)) {
	// System.out.println("method " + adt.methodSignature
	// + " is matched");
	// } else {
	// System.out.println("method " + adt.methodSignature
	// + " is not matched");
	// }
	//
	// }
	// }
	// }
	// }
}
