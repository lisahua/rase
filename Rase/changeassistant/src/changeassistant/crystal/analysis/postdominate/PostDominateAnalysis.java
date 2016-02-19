package changeassistant.crystal.analysis.postdominate;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.crystal.analysis.DominateLE;
import changeassistant.internal.WorkspaceUtilities;
import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;
import edu.cmu.cs.crystal.tac.model.Variable;

public class PostDominateAnalysis extends AbstractCrystalMethodAnalysis {

	/**
	 * To make things simple, we use TupleLatticeElement although we actually
	 * don't need to save any variable. What we need to save is just all source
	 * code ranges dominating current node.
	 */
	private TACFlowAnalysis<TupleLatticeElement<Variable, DominateLE>> fa;

	private String methodSignature;

	private int startPosition, length;

	PostDominateElementResult result;

	public PostDominateAnalysis() {
		super();
	}

	public PostDominateAnalysis(String methodSignature, int startPosition,
			int length) {
		super();
		this.methodSignature = methodSignature;
		this.startPosition = startPosition;
		this.length = length;
	}

	@Override
	public void analyzeMethod(MethodDeclaration d) {
		if (WorkspaceUtilities.getMethodSignatureFromASTNode(d).equals(
				methodSignature)
				&& d.getStartPosition() == this.startPosition
				&& d.getLength() == this.length) {
			CompilationUnitTACs tac = this.analysisInput.getComUnitTACs()
					.unwrap();
			PostDominateTransferFunction tf = new PostDominateTransferFunction(
					tac, d);
			fa = new TACFlowAnalysis<TupleLatticeElement<Variable, DominateLE>>(
					tf, this.analysisInput.getComUnitTACs().unwrap());
			result = new PostDominateElementResult(d, fa, tac);
		}

	}

	public PostDominateElementResult getResult() {
		return this.result;
	}

}
