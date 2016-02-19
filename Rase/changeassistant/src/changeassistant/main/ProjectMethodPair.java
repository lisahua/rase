package changeassistant.main;

public class ProjectMethodPair {
	public String src;
	public String leftProjectName, rightProjectName;
	public String leftClassName1, leftClassName2;
	public String rightClassName1, rightClassName2;
	public String leftFilePath1, leftFilePath2;
	public String rightFilePath1, rightFilePath2;
	public String leftMethodName1, leftMethodName2;
	public String rightMethodName1, rightMethodName2;
	
	public ProjectMethodPair(String src, 
			String leftProjectName, String rightProjectName,
			String leftClassName1, String leftClassName2, 
			String rightClassName1, String rightClassName2,
			String leftFilePath1, String leftFilePath2,
			String rightFilePath1, String rightFilePath2,
			String leftMethodName1, String leftMethodName2,
			String rightMethodName1, String rightMethodName2){
		this.src = src;
		this.leftProjectName = leftProjectName;
		this.rightProjectName = rightProjectName;
		this.leftClassName1 = leftClassName1;
		this.leftClassName2 = leftClassName2;
		this.rightClassName1 = rightClassName1;
		this.rightClassName2 = rightClassName2;
		this.leftFilePath1 = leftFilePath1;
		this.leftFilePath2 = leftFilePath2;
		this.rightFilePath1 = rightFilePath1;
		this.rightFilePath2 = rightFilePath2;
		this.leftMethodName1 = leftMethodName1;
		this.leftMethodName2 = leftMethodName2;
		this.rightMethodName1 = rightMethodName1;
		this.rightMethodName2 = rightMethodName2;
	}
}
