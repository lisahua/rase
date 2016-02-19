package changeassistant.multipleexample.main;

import java.util.List;

public class ProjectMethodGroup2 {

	public List<ProjectMethod> members = null;

	public List<String> candidateProjects = null;

	public ProjectMethodGroup2(List<ProjectMethod> pms, List<String> projects) {
		members = pms;
		candidateProjects = projects;
	}

	public List<ProjectMethod> getMembers() {
		return members;
	}
}
