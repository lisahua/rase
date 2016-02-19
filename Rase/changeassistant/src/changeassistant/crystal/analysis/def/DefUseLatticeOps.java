package changeassistant.crystal.analysis.def;

import edu.cmu.cs.crystal.simple.SimpleLatticeOperations;

public class DefUseLatticeOps extends SimpleLatticeOperations<DefUseLE> {

	/**
	 * Compares analysis information for precision; more precisely, Notice that
	 * in a lattice, the bottom lattice element will be more precise than
	 * everything, and top element will be less precise than everything. That
	 * is, {@code atLeastAsPrecise(bottom, x) == true} and
	 * {@code atLeastAsPrecise(x, top) == true}.
	 */
	@Override
	public boolean atLeastAsPrecise(DefUseLE left, DefUseLE right) {
		// to judge whether the right contains the right totally
		return right.defs.containsAll(left.defs)
				&& right.uses.containsAll(left.uses)
				&& right.fieldsAlreadyDefined
						.containsAll(left.fieldsAlreadyDefined);
	}

	@Override
	public DefUseLE bottom() {
		// TODO Auto-generated method stub
		return DefUseLE.bottom();
	}

	@Override
	public DefUseLE copy(DefUseLE original) {// the defs are spread, however,
												// uses are not
		return new DefUseLE(original);
	}

	@Override
	public DefUseLE join(DefUseLE left, DefUseLE right) {
		DefUseLE copy = new DefUseLE();
		copy.defs.addAll(left.defs);
		copy.defs.addAll(right.defs);
		copy.uses.addAll(left.uses);
		copy.uses.addAll(right.uses);
		copy.fieldsAlreadyDefined.addAll(left.fieldsAlreadyDefined);
		copy.fieldsAlreadyDefined.addAll(right.fieldsAlreadyDefined);
		return copy;
	}

}
