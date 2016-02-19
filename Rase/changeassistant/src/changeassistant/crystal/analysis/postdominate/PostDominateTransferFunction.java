package changeassistant.crystal.analysis.postdominate;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.crystal.analysis.DominateLE;
import changeassistant.peers.SourceCodeRange;
import edu.cmu.cs.crystal.flow.AnalysisDirection;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.simple.AbstractingTransferFunction;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.simple.TupleLatticeOperations;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;
import edu.cmu.cs.crystal.tac.eclipse.EclipseTAC;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.TempVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

public class PostDominateTransferFunction extends
		AbstractingTransferFunction<TupleLatticeElement<Variable, DominateLE>> {

	private MethodDeclaration d;

	private CompilationUnitTACs cTac;

	private EclipseTAC tac;

	private TempVariable DEFAULT_KEY = new TempVariable(null);

	public PostDominateTransferFunction(CompilationUnitTACs tac,
			MethodDeclaration d) {
		this.d = d;
		this.cTac = tac;
		this.tac = tac.getMethodTAC(d);
	}

	private final TupleLatticeOperations<Variable, DominateLE> ops = new TupleLatticeOperations<Variable, DominateLE>(
			new PostDominateLatticeOps(), DominateLE.bottom());

	@Override
	public ILatticeOperations<TupleLatticeElement<Variable, DominateLE>> getLatticeOperations() {
		return ops;
	}

	@Override
	public TupleLatticeElement<Variable, DominateLE> createEntryValue(
			MethodDeclaration method) {
		return ops.getDefault();
	}

	@Override
	public AnalysisDirection getAnalysisDirection() {
		return AnalysisDirection.BACKWARD_ANALYSIS;
	}

	@Override
	public TupleLatticeElement<Variable, DominateLE> transfer(
			TACInstruction instr,
			TupleLatticeElement<Variable, DominateLE> value) {
		SourceCodeRange range = new SourceCodeRange(instr.getNode()
				.getStartPosition(), instr.getNode().getLength());
		DominateLE le = null;
		if (value.getKeySet().size() == 0) {
			le = new DominateLE();
			value.put(DEFAULT_KEY, le);
		} else {
			le = value.get(DEFAULT_KEY);
		}
		le.add(range);
		return value;
	}
}
