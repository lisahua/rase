package changeassistant.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPlatformRunnable;

public class Application implements IPlatformRunnable{

	
	private static String xmlPath = "/Users/mn8247/Software/workspaceForStaticAnalysis/changeassistant/config/projectPair.xml";
	@Override
	public Object run(Object arg0) throws Exception {
		run();
		return null;
	}
	
	public List<ProjectMethodPair> readPairDescription(){
		return XMLReader.readXML(xmlPath);
	}

	public void run(){
		List<ProjectMethodPair> pairs = readPairDescription(); 
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		Map<String, IProject> map = new HashMap<String, IProject>();
		IProject iproject;
		String projectName;
		for(int i = 0; i < projects.length; i++){
			iproject = projects[i];
			projectName = iproject.getName();
			map.put(projectName, iproject);
		}
		ChangeAssistantMain cMain = new ChangeAssistantMain();
		cMain.run(map, pairs);
	}

}
