package changeassistant.main;

import changeassistant.versions.comparison.ChangedMethodADT;

/**
 * The same method in two different projects
 * 
 * @author mn8247
 * 
 */
public class ProjectsMethodEntry {
	public String oldProjectName;
	public String newProjectName;
	public String relativeFilePath;

	public ChangedMethodADT adt = null;

	public ProjectsMethodEntry(String oldProjectName, String newProjectName,
			ChangedMethodADT adt, String relativeFilePath) {
		this.oldProjectName = oldProjectName;
		this.newProjectName = newProjectName;
		this.adt = adt;
		this.relativeFilePath = relativeFilePath;
	}

	public String toString() {
		return oldProjectName + " " + newProjectName + " " + relativeFilePath
				+ " " + adt.toString();
	}
}
