package changeassistant.changesuggestion.astrewrite.editcheckers;

import changeassistant.peers.comparison.Node;

public interface EditChecker<T> {

	public boolean check(T editOperation, Node node);
}
