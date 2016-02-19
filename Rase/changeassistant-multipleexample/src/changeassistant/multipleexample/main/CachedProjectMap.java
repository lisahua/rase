package changeassistant.multipleexample.main;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import changeassistant.classhierarchy.ProjectResource;

public class CachedProjectMap {

	protected static Map<String, ProjectResource> map = null;
	protected static Map<String, IProject> prjMap = null;

	public static void init() {
		map = new HashMap<String, ProjectResource>();
	}

	public static void setProjectMap(Map<String, IProject> projectMap) {
		prjMap = projectMap;
		map = new HashMap<String, ProjectResource>();
	}

	public static void clear() {
		map.clear();
	}

	public static boolean contains(String name) {
		return map.containsKey(name);
	}

	public static void put(String name, ProjectResource pr) {
		map.put(name, pr);
	}

	public static ProjectResource get(String name) {
		ProjectResource pr = map.get(name);
		if (pr == null) {
			if (prjMap != null) {
				pr = new ProjectResource(prjMap.get(name), name);
				map.put(name, pr);
			}
		}
		return pr;
	}
}
