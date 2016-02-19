package changeassistant.crystal;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import edu.cmu.cs.crystal.tac.model.IVariableVisitor;
import edu.cmu.cs.crystal.tac.model.Variable;

public class FieldVariable extends Variable {

	private Variable objectAccessed;

	private IVariableBinding binding;

	private String fieldName;

	public FieldVariable(Variable objectAccessed, IVariableBinding binding,
			String fieldName) {
		this.objectAccessed = objectAccessed;
		this.binding = binding;
		this.fieldName = fieldName;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FieldVariable))
			return false;
		FieldVariable other = (FieldVariable) obj;
		if (other.objectAccessed == null && this.objectAccessed != null
				|| other.objectAccessed != null && this.objectAccessed == null)
			return false;
		if (other.objectAccessed == null && this.objectAccessed == null) {
			// do nothing
		} else if (!other.objectAccessed.equals(this.objectAccessed))
			return false;
		if (!other.fieldName.equals(this.fieldName))
			return false;
		return true;
	}

	public Variable getObjectAccessed() {
		return objectAccessed;
	}

	@Override
	public int hashCode() {
		if (this.objectAccessed == null)
			return this.fieldName.hashCode();
		return this.objectAccessed.hashCode() * 100 + this.fieldName.hashCode();
	}

	@Override
	public ITypeBinding resolveType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T dispatch(IVariableVisitor<T> visitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		if (objectAccessed == null) {
			return fieldName;
		} else
			return objectAccessed.getSourceString() + "." + fieldName;
	}

	public IVariableBinding getBinding() {
		return binding;
	}
}
