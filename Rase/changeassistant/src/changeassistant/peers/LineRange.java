package changeassistant.peers;

import org.eclipse.compare.internal.DocLineComparator;

public class LineRange {

	public int startLine;
	
	public int lineNum;
	
	public DocLineComparator docLineComparator;
	
	public LineRange(int startLine, int lineNum, DocLineComparator docLineComparator){
		this.startLine = startLine;
		this.lineNum = lineNum;
		this.docLineComparator = docLineComparator;
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof LineRange)) return false;
		LineRange other = (LineRange)obj;
		if(this.startLine != other.startLine) return false;
		if(this.lineNum != other.lineNum) return false;
		return true;
	}
	
	public int hashCode(){
		return this.startLine * 1000 + this.lineNum + this.docLineComparator.hashCode();
	}
	
	public String toString(){
		return "start line = " + this.startLine + " number of lines = " + this.lineNum;
	}
}
