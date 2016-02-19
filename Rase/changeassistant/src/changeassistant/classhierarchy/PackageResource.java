package changeassistant.classhierarchy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PackageResource {

	private String packageName;
	private Map<String, ClassContext> classMap; // used to store all class
												// contexts directly under
												// this package

	public PackageResource(String packageName) {
		this.classMap = new HashMap<String, ClassContext>();
		this.packageName = packageName;
	}

	public PackageResource() {
		classMap = new HashMap<String, ClassContext>();
	}

	public void addClassContext(ClassContext cc) {
		if (classMap.containsKey(cc.name))
			return;
		classMap.put(cc.name, cc);
	}

	public Iterator<ClassContext> classContextIterator() {
		return this.classMap.values().iterator();
	}

	public String getPackageName() {
		return this.packageName;
	}

	public ClassContext getClassContext(String className) {
		if (className == null)
			return null;
		return classMap.get(className);
	}

	public Map<String, ClassContext> getClassContexts() {
		return this.classMap;
	}

	public Iterator<ClassContext> getClassContextIterator() {
		return classMap.values().iterator();
	}

	/**
	 * This is only used by SearchController to create a TEMP packageResource
	 * when the package name is empty
	 * 
	 * @param map
	 */
	public void setClassMap(Map<String, ClassContext> map) {
		classMap = map;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String toString() {
		return packageName;
	}
}
