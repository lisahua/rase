package changeassistant.crystal.analysis.def;

import java.util.HashSet;
import java.util.Set;

import changeassistant.peers.SourceCodeRange;

public class DefUseLE {
	public Set<SourceCodeRange> defs;
	public Set<SourceCodeRange> uses;
	public Set<String> fieldsAlreadyDefined;

	protected DefUseLE() {
		defs = new HashSet<SourceCodeRange>();
		uses = new HashSet<SourceCodeRange>();
		fieldsAlreadyDefined = new HashSet<String>();
	}

	public DefUseLE(DefUseLE le) {
		this.defs = new HashSet<SourceCodeRange>(le.defs);
		this.uses = new HashSet<SourceCodeRange>(le.uses);
		this.fieldsAlreadyDefined = new HashSet<String>(le.fieldsAlreadyDefined);
	}

	public static DefUseLE bottom() {
		return new DefUseLE();
	}

	public boolean containDef(SourceCodeRange range) {
		return this.defs.contains(range);
	}

	public boolean containUsage(SourceCodeRange range) {
		return this.uses.contains(range);
	}

	public boolean isEmpty() {
		return this.defs.isEmpty() && this.uses.isEmpty();
	}

	public String toString() {
		StringBuffer result = new StringBuffer("DEF: ");
		for (SourceCodeRange def : defs) {
			result.append(def + " ");
		}

		result.append("\n" + "USE: ");
		for (SourceCodeRange use : uses) {
			result.append(use + " ");
		}

		result.append("\n" + "Fields defined elsewhere: ");
		for (String f : fieldsAlreadyDefined) {
			result.append(f + " ");
		}

		return result.toString();
	}
}
