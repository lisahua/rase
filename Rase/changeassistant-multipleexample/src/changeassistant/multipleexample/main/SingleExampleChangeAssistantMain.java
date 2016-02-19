package changeassistant.multipleexample.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import changeassistant.classhierarchy.ProjectResource;
import changeassistant.multipleexample.datastructure.Pair;
import changeassistant.multipleexample.partition.ChangeSummaryCreator;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.versions.comparison.MethodModification;

public class SingleExampleChangeAssistantMain extends
		EnhancedChangeAssistantMain {

	public void runSingleExample(Map<String, IProject> map,
			List<ProjectMethodGroup2> pmgs2) {
		int counter = 0;
		List<ProjectMethod> pms = null;
		List<MethodModification> mms = null;
		ChangeSummaryCreator csCreator = new ChangeSummaryCreator();
		prjMap = map;
		prPairList = new ArrayList<Pair<ProjectResource>>();
		groups = new ArrayList<EditInCommonGroup>();
		CachedProjectMap.init();
		assert EXAMPLE_NUMBER == 1 : "The number of examples should be exactly 1 in order to run SingleExampleChangeAssistantMain";
		int tmpCounter = 0;
		for (ProjectMethodGroup2 pmg2 : pmgs2) {
			counter++;
			if (counter != 8)
				continue;
			long startTime = System.currentTimeMillis();
			pmg = pmg2;
			pms = pmg2.getMembers();
			mms = new ArrayList<MethodModification>();
			for (int i = 0; i < pms.size(); i++) {
				tmpCounter++;
				if (tmpCounter < 6)
					continue;
				groups.clear();
				mms.clear();
				performDiff(map, pms, mms, i);
				groups.add(createGroup(csCreator, mms));
				executeForSingleExample(groups, tmpCounter, startTime);
			}
		}
	}
}
