package changeassistant.main;

import java.util.ArrayList;
import java.util.List;

public class ProjectMethodGroup {

	public String src;
	public String leftProjectName, rightProjectName;
	public String leftClassName1, leftClassName2;
	public String leftFilePath1, leftFilePath2;
	public String leftMethodName1, leftMethodName2;
	public List<ProjectMethod> rightMethods;
	
	public ProjectMethodGroup(String src, String leftProjectName, String rightProjectName,
			String leftClassName1, String leftClassName2, 
			String leftFilePath1, String leftFilePath2,
			String leftMethodName1, String leftMethodName2){
		rightMethods = new ArrayList<ProjectMethod>();
		this.src = src;
		this.leftProjectName = leftProjectName;
		this.rightProjectName = rightProjectName;
		this.leftClassName1 = leftClassName1;
		this.leftClassName2 = leftClassName2;
		this.leftFilePath1 = leftFilePath1;
		this.leftFilePath2 = leftFilePath2;
		this.leftMethodName1 = leftMethodName1;
		this.leftMethodName2 = leftMethodName2;
	}
	
	public void addProjectMethod(ProjectMethod pm){
		rightMethods.add(pm);
	}
}
