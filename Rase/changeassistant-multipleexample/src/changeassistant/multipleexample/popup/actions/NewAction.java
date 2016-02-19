package changeassistant.multipleexample.popup.actions;

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

import changeassistant.main.ProjectMethodPair;
import changeassistant.main.XMLReader;
import changeassistant.multipleexample.main.EnhancedChangeAssistantMain;
import changeassistant.multipleexample.main.ProjectMethodGroup2;
import changeassistant.multipleexample.main.SingleExampleChangeAssistantMain;
import changeassistant.multipleexample.main.XMLReader2;
import changeassistant.multipleexample.util.PropertyLoader;

public class NewAction implements IObjectActionDelegate {

	public static enum MODE {
		SEARCH, GIVEN, MULTIPLE, ENUMERATE, SINGLE
	};

	public static MODE mode = MODE.GIVEN;

	private Shell shell;

	private ISelection selection;

	private String projectPath;

	private String changeAssistantProjectPath;

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
		Bundle bundle = Platform.getBundle("changeassistant.multipleexample");
		projectPath = bundle.getLocation();
		projectPath = projectPath.substring(projectPath.lastIndexOf(":") + 1);
		bundle = Platform.getBundle("ChangeAssistant");
		changeAssistantProjectPath = bundle.getLocation();
		changeAssistantProjectPath = changeAssistantProjectPath
				.substring(changeAssistantProjectPath.lastIndexOf(":") + 1);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection iSelection = (IStructuredSelection) selection;
			Object element;
			List<IProject> projects = new ArrayList<IProject>();
			for (Iterator<?> iterator = iSelection.iterator(); iterator
					.hasNext();) {
				element = iterator.next();
				if (element instanceof IProject) {
					projects.add((IProject) element);
				}
				if (element instanceof IJavaProject) {
					projects.add(((IJavaProject) element).getProject());
				}
			}
			PropertyLoader.load(new File(projectPath + "config/properties"));
			PropertyLoader.props.setProperty("Project_Home_Path", projectPath);
			if (mode.equals(MODE.SEARCH)) {
				createActionExecutable((action.getId())).run(projects);
			} else {
				Map<String, IProject> map = new HashMap<String, IProject>();
				IProject iproject = null;
				for (int i = 0; i < projects.size(); i++) {
					iproject = projects.get(i);
					map.put(iproject.getName(), iproject);
				}
				if (mode.equals(MODE.MULTIPLE)) {
					List<ProjectMethodGroup2> pmg2 = XMLReader2
							.readXML(projectPath + "config/projectGroup.xml");
					createActionExecutable((action.getId())).run2(map, pmg2);
				} else if (mode.equals(MODE.ENUMERATE)) {
					List<ProjectMethodGroup2> pmg2 = XMLReader2
							.readXML(projectPath
									+ "config/projectGroupNewExpr.xml");
					createActionExecutable((action.getId())).run3(map, pmg2);
				} else if (mode.equals(MODE.SINGLE)) {
					List<ProjectMethodGroup2> pmg2 = XMLReader2
							.readXML(projectPath
									+ "config/projectGroupNewExpr.xml");
					(new SingleExampleChangeAssistantMain()).runSingleExample(
							map, pmg2);
				} else {// mode.equals(MODE.GIVEN)
					List<ProjectMethodPair> pmps = XMLReader
							.readXML(changeAssistantProjectPath
									+ "config/projectPair.xml");
					createActionExecutable((action.getId())).runForGiven(map,
							pmps);
				}
			}
		}
	}

	private EnhancedChangeAssistantMain createActionExecutable(String id) {
		if ("changeassistant.multipleexample.newAction".equals(id)) {
			return new EnhancedChangeAssistantMain();
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
