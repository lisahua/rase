package changeassistant.changesuggestion.astrewrite;

import java.util.List;

public abstract class Differencer<T> {

	protected EditScript<T> fEditScript;
	
	protected static final int UP = 1;
	protected static final int LEFT = 2;
	protected static final int DIAG = 3;
	
	abstract public T apply(T original, EditScript<T> editScripts);
	
	abstract public void editScript(T left, T right);
	
	public EditScript<T> getEditScript(){
		return this.fEditScript;
	}
	
	abstract protected boolean isApplicable(T original, EditScript<T> editScripts, List<T> unchangedElements);
	
	abstract protected List<CommonADT<T>> longestCommonSubsequence(T left, T right);
	
	abstract protected void extractLCS(int[][]b, T l, T r, int i, int j, List<CommonADT<T>> lcs); 
}
