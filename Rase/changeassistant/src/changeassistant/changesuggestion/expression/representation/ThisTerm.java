package changeassistant.changesuggestion.expression.representation;

public class ThisTerm extends VariableTypeBindingTerm {

	private static final long serialVersionUID = -4591311275104898807L;

	public ThisTerm(int nodeType, String name) {
		super(nodeType, name);
	}

	public ThisTerm(int nodeType, String name, String abstractName) {
		super(nodeType, name, abstractName);
	}

	public ThisTerm(int nodeType, String name, String abstractName,
			TypeNameTerm t) {
		super(nodeType, name, abstractName, t);
	}
}
