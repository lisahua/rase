package changeassistant.crystal.analysis;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.crystal.analysis.def.DefUseAnalysis;
import changeassistant.crystal.analysis.def.DefUseElementResult;
import changeassistant.internal.WorkspaceUtilities;
import edu.cmu.cs.crystal.IAnalysisInput;
import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.internal.StandardAnalysisReporter;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;
import edu.cmu.cs.crystal.util.Option;

/**
 * Used to store all analysis results of DefUseAnalysis
 * 
 * @author ibm
 * 
 */
public class DefUseAnalysisFactory extends AnalysisFactory {

	private static DefUseAnalysisFactory instance = new DefUseAnalysisFactory();

	// private final Set<String> enabled;

	private DefUseAnalysisFactory() {
		// this.enabled = new HashSet<String>();
		// this.enabled.add("DefUseAnalysis");
	}

	public static DefUseAnalysisFactory getInstance() {
		return instance;
	}

	public DefUseElementResult getAnalysisResultForMethod(ICompilationUnit icu,
			MethodDeclaration md) {
		String methodSignature = WorkspaceUtilities
				.getMethodSignatureFromASTNode(md);
		CompilationUnit ast_comp_unit = (CompilationUnit) WorkspaceUtilities
				.getASTNodeFromCompilationUnit(icu);
		final CompilationUnitTACs compUnitTacs = new CompilationUnitTACs();
		IAnalysisInput input = new IAnalysisInput() {
			private Option<IProgressMonitor> mon = Option.wrap(null);

			public AnnotationDatabase getAnnoDB() {
				return null;
			}

			public Option<CompilationUnitTACs> getComUnitTACs() {
				return Option.some(compUnitTacs);
			}

			public Option<IProgressMonitor> getProgressMonitor() {
				return mon;
			}
		};
		DefUseAnalysis analysis = new DefUseAnalysis(methodSignature,
				md.getStartPosition(), md.getLength());
		analysis.runAnalysis(new StandardAnalysisReporter(), input, icu,
				ast_comp_unit);
		return analysis.getResult();
	}
}
