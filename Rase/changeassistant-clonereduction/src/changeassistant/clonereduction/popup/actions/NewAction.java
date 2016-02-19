package changeassistant.clonereduction.popup.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.framework.Bundle;

import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.main.ProjectMethodGroup;
import changeassistant.main.ProjectMethodPair;
import changeassistant.main.XMLReader;
import changeassistant.multipleexample.main.Oracle;
import changeassistant.multipleexample.main.XMLReaderWithOracle;
import changeassistant.multipleexample.util.PropertyLoader;

public class NewAction implements IObjectActionDelegate {

	private ISelection selection;
	private Shell shell;
	private String cloneReductionPath;

	/**
	 * Constructor for Action1.
	 */
	public NewAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		Bundle bundle = Platform.getBundle("changeassistant.clonereduction");
		cloneReductionPath = bundle.getLocation();
		cloneReductionPath = cloneReductionPath.substring(cloneReductionPath
				.lastIndexOf(":") + 1);
		bundle = Platform.getBundle("changeassistant.multipleexample");
		String projectPath = bundle.getLocation();
		projectPath = projectPath.substring(projectPath.lastIndexOf(":") + 1);
		PropertyLoader.load(new File(projectPath + "config/properties"));
		PropertyLoader.props.setProperty("Project_Home_Path", projectPath);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection iSelection = (IStructuredSelection) selection;
			Object element;
			Map<String, IProject> map = new HashMap<String, IProject>();
			IProject iproject = null;
			for (Iterator<?> iterator = iSelection.iterator(); iterator
					.hasNext();) {
				element = iterator.next();
				if (element instanceof IProject) {
					iproject = (IProject) element;
					map.put(iproject.getName(), iproject);
				} else if (element instanceof IJavaProject) {
					iproject = ((IJavaProject) element).getProject();
					map.put(iproject.getName(), iproject);
				}
			}
			List<Oracle> oracles = new ArrayList<Oracle>();
			if(CloneReductionMain.hasMultiExamples){
				if(!CloneReductionMain.runPair){
					List<ProjectMethodGroup> pmgs = XMLReaderWithOracle.readGroupXML(cloneReductionPath + "config/projectGroup.xml", oracles);	
					createActionExecutable((action.getId())).runForGroups(map, pmgs, oracles);
				}else{
					List<ProjectMethodPair> pmps = XMLReaderWithOracle.readXML(cloneReductionPath + "config/projectPair.xml", oracles);
					createActionExecutable((action.getId())).runForPairs2(map, pmps, oracles);
				}											
			}else{
				List<ProjectMethodPair> pmps = XMLReaderWithOracle.readXML(cloneReductionPath + "config/projectPair.xml", oracles);
				createActionExecutable((action.getId())).runForPairs(map, pmps, oracles);
			}
			
		}
	}

	private CloneReductionMain createActionExecutable(String id) {
		if ("changeassistant.clonereduction".equals(id)) {
			return new CloneReductionMain();
		} else {
			throw new IllegalArgumentException(id);
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
