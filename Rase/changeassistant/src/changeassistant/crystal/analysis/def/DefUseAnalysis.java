package changeassistant.crystal.analysis.def;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.internal.ASTElementSearcher;
import changeassistant.internal.WorkspaceUtilities;
import changeassistant.peers.SourceCodeRange;
import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;
import edu.cmu.cs.crystal.tac.model.Variable;

public class DefUseAnalysis extends AbstractCrystalMethodAnalysis {

	// why using the only instance? this will make things complicated!
	// public static DefUseAnalysis instance;

	private TACFlowAnalysis<TupleLatticeElement<Integer, DefUseLE>> fa;

	private String methodSignature;

	private int startPosition, length;

	DefUseElementResult result;// since each DefUseAnalysis instance is created
								// only for one method analysis,

	// there should be only one analysis result

	public DefUseAnalysis() {// for Crystal to load this analysis, I must
								// provide a Constructor with no parameter
		super();
	}

	public DefUseAnalysis(String methodSignature, int startPosition, int length) {// There
																					// is
																					// only
																					// one
																					// DefUseAnalysis
																					// instance
		super();
		this.methodSignature = methodSignature;
		this.startPosition = startPosition;
		this.length = length;
	}

	@Override
	public void analyzeMethod(MethodDeclaration d) {// each method is given a
													// unique
													// DefUseTransferFunction
													// instance
		if (WorkspaceUtilities.getMethodSignatureFromASTNode(d).equals(
				methodSignature)
				&& d.getStartPosition() == this.startPosition
				&& d.getLength() == this.length) {
			// if the given method is the method we are concerned about
			CompilationUnitTACs tac = this.analysisInput.getComUnitTACs()
					.unwrap();
			// since a method declaration may include definitions of more than
			// one method,
			// therefore it is better to pass the TAC for the whole compilation
			// unit
			DefUseTransferFunction tf = new DefUseTransferFunction(tac, d);
			fa = new TACFlowAnalysis<TupleLatticeElement<Integer, DefUseLE>>(
					tf, this.analysisInput.getComUnitTACs().unwrap());
			result = new DefUseElementResult(d, fa, tac, tf.getFieldMap(),
					tf.getVarList());
		}
	}

	public void printLattice(TupleLatticeElement<Variable, DefUseLE> lattice) {
		ASTElementSearcher searcher = new ASTElementSearcher(
				result.getMethodDeclaration());
		for (Variable var : lattice.getKeySet()) {
			DefUseLE le = lattice.get(var);
			if (le.defs.size() > 0) {
				reporter.debugOut().println(
						var.getSourceString() + ": "
								+ "possible ASTNodes defining it");
				for (SourceCodeRange scr : le.defs) {
					reporter.debugOut().print(
							searcher.findElement(scr).toString() + " ");
				}
				reporter.debugOut().println();
			}
		}
		reporter.debugOut().println("\n\n");
	}

	public DefUseElementResult getResult() {
		return this.result;
	}
}
