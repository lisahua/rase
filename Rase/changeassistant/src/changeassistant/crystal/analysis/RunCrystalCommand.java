package changeassistant.crystal.analysis;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;

import edu.cmu.cs.crystal.IAnalysisReporter;
import edu.cmu.cs.crystal.IRunCrystalCommand;
import edu.cmu.cs.crystal.internal.StandardAnalysisReporter;

public class RunCrystalCommand implements IRunCrystalCommand{
	
	final List<ICompilationUnit> compUnits;
	final Set<String> enabled;
	
	public RunCrystalCommand(List<ICompilationUnit> compUnits, Set<String> enabled){
		this.compUnits = compUnits;
		this.enabled = enabled;
	}

	@Override
	public Set<String> analyses() {		
		return enabled;
	}

	@Override
	public List<ICompilationUnit> compilationUnits() {
		return compUnits;
	}

	@Override
	public IAnalysisReporter reporter() {
		return new StandardAnalysisReporter(); 
	}

}
