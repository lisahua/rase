package changeassistant.multipleexample.contextualize;

import java.util.Map;

public class NameConstraint implements Constraint{

	public static final String METHOD_NAME = "MethodName";
	
	public static final String CLASS_NAME = "ClassName";
	
	public static final String PACKAGE_NAME = "PackageName";
	
	public static final String INTERFACE_NAME = "InterfaceName";
	
	public static final String SUPER_NAME = "SuperName";
	
	public static final String RELATIVE_FILE_PATH = "RelativeFilePath";
	
	private String label;
	
	private String value;
	public NameConstraint(String l, String v){
		this.label = l;
		this.value = v;
	}
	
}
