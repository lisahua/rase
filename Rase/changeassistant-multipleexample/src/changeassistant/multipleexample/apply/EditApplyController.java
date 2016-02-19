package changeassistant.multipleexample.apply;

import java.util.Iterator;
import java.util.List;

import changeassistant.multipleexample.match.MatchResult;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;

public class EditApplyController {

	public void apply(List<List<List<MatchResult>>> mResultsLists,
			List<EditInCommonGroup> groups, int counter) {
		EditScriptApplier applier = new EditScriptApplier();
		EditInCommonGroup group = null;
		List<List<MatchResult>> mResultsList = null;
		List<MatchResult> mResults = null;
		Iterator<EditInCommonCluster> cIter = null;
		EditInCommonCluster cluster = null;
		int tmpCounter = 0;
		for (int i = 0; i < groups.size(); i++) {
			group = groups.get(i);
			mResultsList = mResultsLists.get(i);
			cIter = group.getIterator();
			tmpCounter = 0;
			while (cIter.hasNext()) {
				cluster = cIter.next();
				mResults = mResultsList.get(tmpCounter);
				if (mResults.isEmpty()) {
					// do nothing
				} else {
					applier.apply(mResults, counter, tmpCounter, cluster);
				}
				tmpCounter++;
			}
		}
	}
}
