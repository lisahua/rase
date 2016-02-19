package changeassistant.multipleexample.extend;

import java.util.ArrayList;
import java.util.List;

import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;

public class ExtendController {

	public List<EditInCommonGroup> extend(List<EditInCommonGroup> groups){
		List<EditInCommonGroup> editGroups = new ArrayList<EditInCommonGroup>();
		for(EditInCommonGroup group : editGroups){
			group.contextualizeClusters();
		}
		return editGroups;
	}
}
