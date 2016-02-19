package changeassistant.crystal.analysis.postdominate;

import changeassistant.crystal.analysis.DominateLE;
import edu.cmu.cs.crystal.simple.SimpleLatticeOperations;

public class PostDominateLatticeOps extends SimpleLatticeOperations<DominateLE> {

	@Override
	public boolean atLeastAsPrecise(DominateLE left, DominateLE right) {
		return left.ranges.containsAll(right.ranges);
	}

	@Override
	public DominateLE bottom() {
		return DominateLE.bottom();
	}

	@Override
	public DominateLE copy(DominateLE original) {
		return new DominateLE(original);
	}

	/**
	 * Get the intersection between two DominateLE as the join function
	 */
	@Override
	public DominateLE join(DominateLE left, DominateLE right) {
		DominateLE copy = new DominateLE(left);
		copy.ranges.retainAll(right.ranges);
		return copy;
	}
}
