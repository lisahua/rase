package changeassistant.internal;

import java.io.Serializable;

import changeassistant.peers.SourceCodeRange;

public class MethodADT implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String classname;
	public String methodSignature;
	public SourceCodeRange range;

	public MethodADT(String filepath, String methodSig, SourceCodeRange range) {
		this.classname = filepath;
		this.methodSignature = methodSig;
		this.range = range;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof MethodADT))
			return false;
		MethodADT mADT = (MethodADT) obj;
		if (!this.classname.equals(mADT.classname))
			return false;
		if (!this.methodSignature.equals(mADT.methodSignature))
			return false;
		// if(!this.range.equals(mADT.range)) return false;
		return true;
	}

	// return method name without parameters
	public static String getMethodName2(String methodSignature) {
		String result = methodSignature.substring(0,
				methodSignature.indexOf('('));
		return result;
	}

	public int hashCode() {
		return this.classname.hashCode() * 10000
				+ this.methodSignature.hashCode() * 100 /*
														 * +
														 * this.range.hashCode()
														 */;
	}

	public String toString() {
		return this.classname + "  " + this.methodSignature + "  (" + range
				+ ")";
	}
}
