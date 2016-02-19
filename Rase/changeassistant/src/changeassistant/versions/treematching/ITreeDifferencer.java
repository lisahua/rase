package changeassistant.versions.treematching;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;

import changeassistant.peers.LineRange;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation;

public interface ITreeDifferencer {

	public List<ITreeEditOperation> getEditScript();
	
	public void calculateEditScript(Node left, Node right);
	
	public void calculateEditScript(Node left, Node right, 
			Map<LineRange, LineRange> noChangeRangePairs,
			Map<LineRange, LineRange> updateRangePairs,
			CompilationUnit leftCU,
			CompilationUnit rightCU);

}