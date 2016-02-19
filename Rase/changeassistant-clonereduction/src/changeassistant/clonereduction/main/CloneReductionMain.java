package changeassistant.clonereduction.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonedetection.ccfinder.CCFinderHelper;
import changeassistant.clonedetection.util.DeltaCodeSizeCounter;
import changeassistant.clonereduction.datastructure.Wildcard;
import changeassistant.clonereduction.helper.BenefitEstimateHelper;
import changeassistant.clonereduction.manipulate.CloneReductionController;
import changeassistant.clonereduction.manipulate.OperationCollection;
import changeassistant.clonereduction.manipulate.SystematicEditController;
import changeassistant.clonereduction.manipulate.refactoring.RefactoringMetaData;
import changeassistant.clonereduction.search.CloneReductionMatchResult;
import changeassistant.main.ProjectMethod;
import changeassistant.main.ProjectMethodGroup;
import changeassistant.main.ProjectMethodPair;
import changeassistant.multipleexample.common.CommonParserMulti;
import changeassistant.multipleexample.main.CachedProjectMap;
import changeassistant.multipleexample.main.Oracle;
import changeassistant.multipleexample.match.MatchResult;
import changeassistant.multipleexample.partition.ChangeSummaryCreator;
import changeassistant.multipleexample.partition.PartitionController;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.DiffParser;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;

public class CloneReductionMain {
	Map<ChangedMethodADT, MethodModification> map = null;
	String src;
	String oldFilePath, newFilePath, oldClassName, newClassName, oldMethodName,
			newMethodName;
	IFile oldFile, newFile;
	public static int counter;
	public static Map<String, IProject> prjMap = null;
	public static OperationCollection refEdits = null;
	public static DeltaCodeSizeCounter deltaCounter = null;
	
	protected List<EditInCommonGroup> editGroups = null;

	IProject fProjectLeft = null;
	IProject fProjectRight = null;

	public static ProjectResource prLeft = null;
	public static ProjectResource prRight = null;

	String sProjectLeft = null;
	String sProjectRight = null;
	
	public static boolean runPair = false;
	public static boolean onlyHasOneSubtree = false;
	public static boolean refactoringOld = false;
	public static boolean expandContext = true;
	public static boolean refactoringAll = false;
	public static boolean hasMultiExamples = true;
	
	public void runForPairs2(Map<String, IProject> projectMap, List<ProjectMethodPair> pairs, List<Oracle> oracles){

		try {
 			Oracle oracle = null;
			counter = 0;
			map = new HashMap<ChangedMethodADT, MethodModification>();
			prjMap = projectMap;
			CachedProjectMap.setProjectMap(prjMap);
			RefactoringMetaData.clear();
			List<Integer> counterList = Arrays.asList(new Integer[]{/*1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,*/
					19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46,
					47, 48, 49, 50, 51, 52, 53, 54, 55, 56});
			ProjectMethodGroup tmpGroup = null;
			for (ProjectMethodPair pair : pairs) {
				editGroups = new ArrayList<EditInCommonGroup>();
				oracle = oracles.get(counter);
				counter++;
//				System.out.print("");
//				if(!counterList.contains(counter))
				if (counter != 48)
					continue;
				RefactoringMetaData.clear();
				refEdits = new OperationCollection();
				deltaCounter = new DeltaCodeSizeCounter();
				tmpGroup = new ProjectMethodGroup(pair.src, pair.leftProjectName, pair.rightProjectName, 
						pair.leftClassName1, pair.leftClassName2, pair.leftFilePath1, pair.leftFilePath2,
						pair.leftMethodName1, pair.leftMethodName2);
				tmpGroup.addProjectMethod(new ProjectMethod(pair.rightClassName1, pair.rightClassName2, pair.rightFilePath1, 
						pair.rightFilePath2, pair.rightMethodName1, pair.rightMethodName2));
				executeForGroup(tmpGroup, oracle, counter);
			}
			map = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void runForPairs(Map<String, IProject> projectMap, List<ProjectMethodPair> pairs, List<Oracle> oracles){
		try {
			Oracle oracle = null;
			counter = 0;
			map = new HashMap<ChangedMethodADT, MethodModification>();
			prjMap = projectMap;
			CachedProjectMap.setProjectMap(prjMap);
			RefactoringMetaData.clear();
			List<Integer> counterList = Arrays.asList(new Integer[]{1, 2, 4, 6, 8, 9, 10, 11, 12, 13, 16, 18,
					19, 21, 22, 29, 32, 34, 35, 36, 38, 40, 45, 46, 47, 48, 53, 54, 55, 56});
			for (ProjectMethodPair pair : pairs) {
				editGroups = new ArrayList<EditInCommonGroup>();
				oracle = oracles.get(counter);
				counter++;
				System.out.print("");
				if(!counterList.contains(counter))
//				if (counter != 48)
					continue;
				RefactoringMetaData.clear();
				refEdits = new OperationCollection();
				deltaCounter = new DeltaCodeSizeCounter();
				executeForPair(pair, oracle, counter);
			}
			map = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void runForGroups(Map<String, IProject> projectMap,
			List<ProjectMethodGroup> groups, List<Oracle> oracles) {
		try {			
			Oracle oracle = null;
			counter = 0;			
			map = new HashMap<ChangedMethodADT, MethodModification>();
			prjMap = projectMap;
			CachedProjectMap.setProjectMap(prjMap);
			RefactoringMetaData.clear();
			List<Integer> counterList = Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
			16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30});
			for (ProjectMethodGroup group : groups) {
				editGroups = new ArrayList<EditInCommonGroup>();
				oracle = oracles.get(counter);
				counter++;
//				System.out.print("");
				if(counter != 26)
//				if(!counterList.contains(counter))
					continue;
				RefactoringMetaData.clear();
				refEdits = new OperationCollection();
				deltaCounter = new DeltaCodeSizeCounter();
				executeForGroup(group, oracle, counter);
			}
			map = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void executeForPair(ProjectMethodPair pair, Oracle oracle, int counter){
		EditInCommonGroup editGroup = null;
		List<MethodModification> newMMList = null;
		CachedProjectMap.clear();
		newMMList = new ArrayList<MethodModification>();
		List<List<ChangeSummary>> chgSumsList = null;
		List<List<String>> chgSumStrs = null;
		List<List<String>> absChgSumStrs = null;
		List<ChangeSummary> chgSums = null;
		List<String> chgSumStr = null;
		List<String> absChgSumStr = null;
		ChangeSummaryCreator csCreator = new ChangeSummaryCreator();
		ChangeSummary chgSum = null;
		src = pair.src.trim();
		sProjectLeft = pair.leftProjectName;
		sProjectRight = pair.rightProjectName;
		prLeft = CachedProjectMap.get(sProjectLeft);
		prRight = CachedProjectMap.get(sProjectRight);
		newMMList.add(getMM(new ChangedMethodADT(pair.leftClassName1,
				pair.leftMethodName1, sProjectLeft), new ChangedMethodADT(
				pair.leftClassName2, pair.leftMethodName2, sProjectRight)));
		newMMList.add(getMM(new ChangedMethodADT(pair.rightClassName1, pair.rightMethodName1, sProjectLeft), 
					new ChangedMethodADT(pair.rightClassName2, pair.rightMethodName2, sProjectRight)));
		chgSumsList = new ArrayList<List<ChangeSummary>>();
		chgSumStrs = new ArrayList<List<String>>();
		absChgSumStrs = new ArrayList<List<String>>();
		for (int j = 0; j < newMMList.size(); j++) {
			chgSums = csCreator.createSummary(newMMList.get(j));
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
			editGroup = new EditInCommonGroup(newMMList, chgSumsList, chgSumStrs,
					absChgSumStrs);
		editGroups.add(editGroup);
		List<EditInCommonGroup> newGroups = new ArrayList<EditInCommonGroup>();
		PartitionController pc = new PartitionController();

		newGroups = pc.partition(editGroups);
		CloneReductionController crc = new CloneReductionController(prLeft, prRight);
		SystematicEditController sec = new SystematicEditController();
		Wildcard wData = new Wildcard();
		try {
			boolean canSystematicEdit = sec.check(prLeft, newGroups, oracle,
					prRight);
			List<MatchResult> oResults = sec.getMatchResults();
			if (canSystematicEdit) {
				wData.collectOldWildcards(oResults, newMMList.get(0).originalMethod);
				wData.collectEditedWildcards(sec.getCluster());
			}
			System.out.print("");
			boolean canReduceClone = false;
			if(CloneReductionMain.refactoringOld){
				canReduceClone = crc.check2(editGroups.get(0), wData, oResults, oracle);
			}else{
				canReduceClone = crc.check(editGroups.get(0), wData, oResults, oracle);
			}	
			List<CloneReductionMatchResult> nResults = crc.getMatchResults();			
			if (canReduceClone && canSystematicEdit) {
					crc.apply(wData);
			}else if (canReduceClone) {
			 crc.apply(wData);
			} else if (canSystematicEdit) {
			 sec.apply();
			}
		} catch (Exception e) {
			System.err.println(Integer.toString(counter) + ": "
					+ e.getMessage());
			e.printStackTrace();
//			 System.exit(1);
		}
	}

	private void executeForGroup(ProjectMethodGroup group, Oracle oracle, int counter) {
		System.out.print("");
		EditInCommonGroup editGroup = null;
		List<MethodModification> newMMList = null;
		CachedProjectMap.clear();
		newMMList = new ArrayList<MethodModification>();
		List<List<ChangeSummary>> chgSumsList = null;
		List<List<String>> chgSumStrs = null;
		List<List<String>> absChgSumStrs = null;
		List<ChangeSummary> chgSums = null;
		List<String> chgSumStr = null;
		List<String> absChgSumStr = null;
		ChangeSummaryCreator csCreator = new ChangeSummaryCreator();
		ChangeSummary chgSum = null;
		src = group.src.trim();
		sProjectLeft = group.leftProjectName;
		sProjectRight = group.rightProjectName;
		prLeft = CachedProjectMap.get(sProjectLeft);
		prRight = CachedProjectMap.get(sProjectRight);
		newMMList.add(getMM(new ChangedMethodADT(group.leftClassName1,
				group.leftMethodName1, sProjectLeft), new ChangedMethodADT(
				group.leftClassName2, group.leftMethodName2, sProjectRight)));
		for(ProjectMethod pm : group.rightMethods){
			newMMList.add(getMM(new ChangedMethodADT(pm.className1, pm.methodName1, sProjectLeft), 
					new ChangedMethodADT(pm.className2, pm.methodName2, sProjectRight)));
		}
		chgSumsList = new ArrayList<List<ChangeSummary>>();
		chgSumStrs = new ArrayList<List<String>>();
		absChgSumStrs = new ArrayList<List<String>>();
		for (int j = 0; j < newMMList.size(); j++) {
			chgSums = csCreator.createSummary(newMMList.get(j));
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
			editGroup = new EditInCommonGroup(newMMList, chgSumsList, chgSumStrs,
					absChgSumStrs);
		editGroups.add(editGroup);
		List<EditInCommonGroup> newGroups = new ArrayList<EditInCommonGroup>();
		PartitionController pc = new PartitionController();

		newGroups = pc.partition(editGroups);
		CloneReductionController crc = new CloneReductionController(prLeft, prRight);
		SystematicEditController sec = new SystematicEditController();
		Wildcard wData = new Wildcard();
		try {
			List<MatchResult> oResults = null;
			boolean canReduceClone = false;
			boolean canSystematicEdit = false;
			System.out.print("");	
			if(!hasMultiExamples){
				canSystematicEdit = sec.check(prLeft, newGroups, oracle,
						prRight);
				oResults = sec.getMatchResults();
				if (canSystematicEdit) {
					wData.collectOldWildcards(oResults, newMMList.get(0).originalMethod);
					wData.collectEditedWildcards(sec.getCluster());
				}							
				if(CloneReductionMain.refactoringOld){
					canReduceClone = crc.check(editGroups.get(0), wData, oResults, oracle);
				}else{
					canReduceClone = crc.check(editGroups.get(0), wData, oResults, oracle);
				}	
			}else{				
					EditInCommonGroup tmpEditGroup = editGroups.get(0);
					if(tmpEditGroup.getClusters() != null)
						canReduceClone = crc.check(tmpEditGroup, wData, oResults, oracle);				
			}			
			if (canReduceClone && canSystematicEdit) {
				crc.apply(wData);
			}else if (canReduceClone) {
			 crc.apply(wData);
			} else if (canSystematicEdit) {
			 sec.apply();
			}
		} catch (Exception e) {
			System.err.println(Integer.toString(counter) + ": "
					+ e.getMessage());
			e.printStackTrace();
//			 System.exit(1);
		}
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
}