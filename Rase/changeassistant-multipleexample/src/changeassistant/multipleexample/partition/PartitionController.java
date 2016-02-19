package changeassistant.multipleexample.partition;

import java.util.ArrayList;
import java.util.List;

import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;

public class PartitionController {

	// private ParitionHelper1 p1;

	// private PartitionHelper2 p2;

	public PartitionController() {
		// p1 = new ParitionHelper1();
		// p2 = new PartitionHelper2();
	}
	
	public List<EditInCommonGroup> partitionWithoutContext(List<EditInCommonGroup> groups){
		boolean flag = true;
		EditInCommonGroup editGroup = null;
		for(int i = 0; i < groups.size(); i++){
			editGroup = groups.get(i);
			flag = false;
			if(!editGroup.createClusterLatticeWithoutContext()){
				flag = false;
			}
		}
		if(!flag){
			List<EditInCommonGroup> newGroups = new ArrayList<EditInCommonGroup>();
			for (EditInCommonGroup group : groups) {
				if (group.getClusters() != null) {
					newGroups.add(group);
				}
			}
			return newGroups;
		}
		return groups;
	}

	public List<EditInCommonGroup> partition(List<EditInCommonGroup> groups) {
		// List<EditInCommonGroup> editGroups = p1.partitionBasedOnType(groups);
		boolean flag = true;
		// int counter = -1;
		EditInCommonGroup editGroup = null;
		for (int i = 0; i < groups.size(); i++) {
			editGroup = groups.get(i);
			flag = false;
			// counter++;
			// if (i != 10)
			// continue;
			// System.out.println(counter);
			// try {
			if (!editGroup.createClusterLattice()) {
				flag = false;
			}
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
		}
		if (!flag) {
			List<EditInCommonGroup> newGroups = new ArrayList<EditInCommonGroup>();
			for (EditInCommonGroup group : groups) {
				if (group.getClusters() != null) {
					newGroups.add(group);
				}
			}
			return newGroups;
		}
		return groups;
	}
}
