package changeassistant.multipleexample.contextualize;

public class MethodConstraint implements Constraint{

	private String methodName;
	
	private boolean isSuper;

	public MethodConstraint(String strValue){
		this.methodName = strValue;
		this.isSuper = false;
	}
	
	public void setSuper(){
		isSuper = true;
	}
}
