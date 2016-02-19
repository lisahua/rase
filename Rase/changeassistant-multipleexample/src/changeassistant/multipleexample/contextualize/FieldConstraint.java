package changeassistant.multipleexample.contextualize;

public class FieldConstraint implements Constraint{

	private String fieldName;
	
	private String fieldType;
	
	private boolean isSuper;
	
	public FieldConstraint(String fieldName){
		this.fieldName = fieldName;
		this.isSuper = false;
	}
	
	public void setSuper(){
		isSuper = true;
	}
}
