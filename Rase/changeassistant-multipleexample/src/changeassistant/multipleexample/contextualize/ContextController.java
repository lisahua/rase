package changeassistant.multipleexample.contextualize;

import java.util.ArrayList;
import java.util.List;

import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;

public class ContextController {

	public List<EditInCommonGroup> contextualize(List<EditInCommonGroup> groups) {
		List<EditInCommonGroup> newGroups = new ArrayList<EditInCommonGroup>();
		EditInCommonGroup group = null;
		// try {
		for (int i = 0; i < groups.size(); i++) {
			// if (i != 11)
			// continue;
			// System.out.println("i == " + i);
			group = groups.get(i);
			group.contextualizeClusters();
			if (!group.getClusters().isEmpty())
				newGroups.add(group);

		}
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		return newGroups;
	}
}
