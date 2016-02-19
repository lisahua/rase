package changeassistant.clonereduction.helper;

import java.util.ArrayList;
import java.util.List;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;

public class ClassContextHelper {

	/**
	 * To merge exceptions when possible
	 * 
	 * @param pr
	 * @param className
	 * @return
	 */
	public List<String> mergeExceptions(ProjectResource pr, String... className) {
		List<String> result = new ArrayList<String>();
		if (className.length == 0)
			return result;
		List<ClassContext> classContexts = new ArrayList<ClassContext>();
		for (String tmpClassName : className) {
			classContexts.add(pr.findClassContext(tmpClassName));
		}
		result.add(className[0]);
		if (className.length == 1)
			return result;
		ClassContext cc0 = classContexts.get(0), cc = null;
		for (int i = 1; i < classContexts.size(); i++) {
			cc = classContexts.get(i);
			if (cc.isSubClassOf(cc0.name)) {
				continue;
			} else if (cc0.isSubClassOf(cc.name)) {
				result.remove(cc0.name);
				result.add(cc.name);
			} else {
				result.add(cc.name);
			}
		}
		return result;
	}

	public List<String> getSuperClasses(ProjectResource pr, String... className) {
		List<String> supers = null;
		List<String> tmp = null;
		ClassContext cc = null;
		for (String cName : className) {
			cc = pr.findClassContext(cName);
			if (supers == null) {
				supers = new ArrayList<String>(cc.getAllSuperClassNames());
				supers.add(0, cName);
			} else {
				tmp = new ArrayList<String>(cc.getAllSuperClassNames());
				tmp.add(cName);
				supers.retainAll(tmp);
			}
		}
		return supers;
	}
}
