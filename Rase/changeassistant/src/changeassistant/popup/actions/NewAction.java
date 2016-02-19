package changeassistant.popup.actions;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import changeassistant.main.ChangeAssistantMain;
import changeassistant.main.ProjectMethodGroup;
import changeassistant.main.ProjectMethodPair;
import changeassistant.main.XMLReader;
import changeassistant.util.PropertyLoader;

public class NewAction implements IObjectActionDelegate {

	private Shell shell;
	
	private ISelection selection;
	
	public boolean isMultiple = false;
	
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
//	public void run(IAction action) {
//		if(selection instanceof IStructuredSelection){
//			IStructuredSelection iSelection = (IStructuredSelection)selection;
//			List<IProject> elements = new LinkedList<IProject>();
//			for(Iterator<?> iterator = iSelection.iterator(); iterator.hasNext();){
//				Object element = iterator.next();
//				if(element instanceof IProject){
//					elements.add((IProject)element);					
//				}else if(element instanceof IJavaProject){
//					elements.add(((IJavaProject)element).getProject());
//				}
//			}
//			createActionExecutable((action.getId())).run(elements);
//		}
//	}
	
	public void processMultiple(IAction action, Map<String, IProject>elements){
		List<ProjectMethodGroup> pmgs = 
			XMLReader.readGroupXML("/Users/mn8247/Software/workspaceForStaticAnalysis/changeassistant/config/projectGroup.xml");
		createActionExecutable((action.getId())).run2(elements, pmgs);
	}
	
	public void run(IAction action) {
		if(selection instanceof IStructuredSelection){
			IStructuredSelection iSelection = (IStructuredSelection)selection;
			Object element = iSelection.getFirstElement();
			IProject[] projects = null;
			if(element instanceof IProject){
				projects = ((IProject) element).getWorkspace().getRoot().getProjects();
			}else if(element instanceof IJavaProject){
				projects = ((IJavaProject)element).getProject().getWorkspace().getRoot().getProjects();
			}
			Map<String, IProject> elements = new HashMap<String, IProject>();
			for(int i = 0; i < projects.length; i++){
				elements.put(projects[i].getName(), projects[i]);
			}
			if(isMultiple){
				processMultiple(action, elements);
			}else{
				List<ProjectMethodPair> pmps = 
					XMLReader.readXML("/Users/mn8247/Software/" +
							"workspaceForStaticAnalysis/changeassistant/" +
							"config/projectPair.xml");
				PropertyLoader.load(new File("/Users/mn8247/Software/" +
						"workspaceForStaticAnalysis/changeassistant/" +
						"config/properties"));
				createActionExecutable((action.getId())).run(elements, pmps);
			}
//		createActionExecutable((action.getId())).run(elements);
		}
	}
	
	private ChangeAssistantMain createActionExecutable(String id){
		if("changeassistant.newAction".equals(id)){
			return new ChangeAssistantMain();
		}else{
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
