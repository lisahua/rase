package changeassistant.versions.treematching.edits;

import java.io.Serializable;

public interface ITreeEditOperation<T> extends Serializable {

	public enum EDIT {
		INSERT, //
		DELETE, //
		UPDATE, //
		MOVE, //
		EMPTY, //
		INSERT_METHOD, //
		INSERT_CLASS, //
		INSERT_FIELD, //
		MANUAL_REPLACE_V, //
		MANUAL_REPLACE_M, //
		MANUAL_REPLACE_T, //
		MANUAL_REPLACE_U, //
		INSERT_CONCRETE_CLASS, //
		INSERT_ABSTRACT_CLASS, //
		INSERT_OUTPUT_CLASS
	}

	public EDIT getOperationType();

	public void apply();

	public void apply(int index);

	public T getParentNode();

	public T getNode();
}
