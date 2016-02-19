package changeassistant.crystal.analysis;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.crystal.analysis.postdominate.PostDominateAnalysis;
import changeassistant.crystal.analysis.postdominate.PostDominateElementResult;
import changeassistant.internal.WorkspaceUtilities;
import edu.cmu.cs.crystal.IAnalysisInput;
import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.internal.StandardAnalysisReporter;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;
import edu.cmu.cs.crystal.util.Option;

public class PostDominateAnalysisFactory extends AnalysisFactory{

	private static PostDominateAnalysisFactory instance = new PostDominateAnalysisFactory();
	
	public static PostDominateAnalysisFactory getInstance(){
		return instance;
	}
	
	public PostDominateElementResult getAnalysisResultForMethod(ICompilationUnit icu, MethodDeclaration md){
		String methodSignature = WorkspaceUtilities.getMethodSignatureFromASTNode(md);
		CompilationUnit ast_comp_unit = (CompilationUnit)WorkspaceUtilities.getASTNodeFromCompilationUnit(icu);
		final CompilationUnitTACs compUnitTacs = new CompilationUnitTACs();
		IAnalysisInput input = new IAnalysisInput() {
			private Option<IProgressMonitor> mon = 
				Option.wrap(null);
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
		PostDominateAnalysis analysis = new PostDominateAnalysis(methodSignature, md.getStartPosition(), md.getLength());
		analysis.runAnalysis(new StandardAnalysisReporter(), input, icu, ast_comp_unit);
		ast_comp_unit = null;
		return analysis.getResult();
	}
}
