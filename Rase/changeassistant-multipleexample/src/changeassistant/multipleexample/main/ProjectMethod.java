package changeassistant.multipleexample.main;

public class ProjectMethod {

	public String src;
	public String oldProjectName, newProjectName;
	public String oldClassName, newClassName;
	public String oldFilePath, newFilePath;
	public String oldMethodName, newMethodName;

	public ProjectMethod(String src, String oldPName, String newPName,
			String oldCName, String newCName, String oldFPath, String newFPath,
			String oldMName, String newMName) {
		this.src = src;
		oldProjectName = oldPName;
		newProjectName = newPName;
		oldClassName = oldCName;
		newClassName = newCName;
		oldFilePath = oldFPath;
		newFilePath = newFPath;
		oldMethodName = oldMName;
		newMethodName = newMName;
	}
}
