package changeassistant.versions.comparison;

import changeassistant.internal.MethodADT;
import changeassistant.peers.SourceCodeRange;

public class ChangedMethodADT extends MethodADT {

	String projectName = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ChangedMethodADT(String classname, String methodSig,
			SourceCodeRange range, String prName) {
		this(classname, methodSig, range);
		projectName = prName;
	}

	public ChangedMethodADT(String classname, String methodSig,
			SourceCodeRange range) {
		super(classname, methodSig, range);
	}

	public ChangedMethodADT(String classname, String methodSig, String prName) {
		super(classname, methodSig, new SourceCodeRange(0, 0));
		projectName = prName;
	}

	public ChangedMethodADT(String classname, String methodSig) {
		super(classname, methodSig, new SourceCodeRange(0, 0));
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String prName) {
		projectName = prName;
	}
}
