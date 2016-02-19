package changeassistant.change.group;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.model.TransformationRule;
import changeassistant.versions.comparison.MethodModification;

public class ChangeGrouper {

	public static boolean DEBUG = true;

	// private IProject projectLeft, projectRight;

	private ProjectResource prLeft, prRight;

	private List<TransformationRule> subTreeList;

	public static int EDIT_NUMBER_THRES = 200;

	public ChangeGrouper(ProjectResource prLeft, ProjectResource prRight) {
		this.prLeft = prLeft;
		this.prRight = prRight;
		this.subTreeList = new ArrayList<TransformationRule>();
	}

	public ChangeGrouper(IProject projectLeft, ProjectResource prLeft,
			IProject projectRight, ProjectResource prRight) {
		this.prLeft = prLeft;
		this.prRight = prRight;
		this.subTreeList = new ArrayList<TransformationRule>();
	}

	public GroupManager createGroupManager(MethodModification mm) {
		return new GroupManager(mm, prLeft, prRight);
	}

	public void partition(MethodModification mm, GroupManager gManager) {
		if (mm.getEdits().size() > EDIT_NUMBER_THRES) {
			return;
		}
		if (DEBUG)
			;
		System.out.println("create sub tree for "
				+ mm.originalMethod.toString());
		try {
			// Step 1: get all changed nodes in the tree most similar to the new
			// revision
			gManager.groupBasedOnNewRevision();
			// Step 2: get DELETED/UPDATED/MOVED nodes and relevant context
			// nodes not included in the latest updated tree
			gManager.refineGroupsBasedOnOldRevision();
			// if(ChangeAssistantMain.PRINT_INFO){
			// System.out.println("# of control dependence edges:" +
			// gManager.cEdges.size());
			// System.out.println("# of data dependence edges:" +
			// gManager.dEdges.size());
			// gManager.cEdges = gManager.dEdges = null;
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<TransformationRule> execute(MethodModification mm) {
		GroupManager gManager = createGroupManager(mm);

		partition(mm, gManager);
		List<TransformationRule> result = new ArrayList<TransformationRule>();
		// Step3: construct subtree with known nodes in each group
		gManager.modelSubTree();

		ChangeGroup changeGroup = gManager.getChangeGroups().get(0);
		SubTreeModel temp = changeGroup.subTreeModel;
		// PatternNode patternNode =
		// graphHelper.refineSubGraph(changeGroup.subTreeModel);
		TransformationRule tr = new TransformationRule(temp,
				changeGroup.editScriptOnSubTree, mm);
		result.add(tr);

		gManager = null;
		return result;
	}

	// public List<SubTreeModel> execute2(final MethodModification mm){
	// List<SubTreeModel> result = new ArrayList<SubTreeModel>();
	// GroupManager gManager = null;
	// gManager = new GroupManager(mm, prLeft, prRight);
	// //Step 1: get all changed nodes in the tree most similar to the new
	// revision
	// gManager.groupBasedOnNewRevision();
	// //Step 2: get DELETED/UPDATED/MOVED nodes and relevant context nodes not
	// included in the latest updated tree
	// gManager.refineGroupsBasedOnOldRevision2();
	// if(ChangeAssistantMain.PRINT_INFO){
	// System.out.println("# of control dependence edges:" +
	// gManager.cEdges.size());
	// System.out.println("# of data dependence edges:" +
	// gManager.dEdges.size());
	// }
	// //Step3: construct subtree with known nodes in each group
	// gManager.modelSubTree2();
	// if(gManager.getChangeGroups().isEmpty()){
	// //do nothing, since no change group is included in the GroupManager
	// }else{
	// for(ChangeGroup changeGroup : gManager.getChangeGroups()){
	// SubTreeModel temp = new SubTreeModel(changeGroup.subTreeModel);
	// result.add(temp);
	// }
	// List<ChangeGroup> cgs = gManager.getChangeGroups();
	// if(ChangeAssistantMain.DEBUG){
	// if(cgs.size() == 1){
	// System.out.println(cgs.get(0));
	// }else{
	// for(int i = 0; i < cgs.size(); i ++){
	// System.out.println("Group " + i);
	// System.out.println(cgs.get(i));
	// }
	// }
	// }
	// }
	// return result;
	// }

	/**
	 * This method only allows the size of the argument list to be 1. This
	 * method is out of date.
	 * 
	 * @param methodModifications
	 * @return
	 */
	@Deprecated
	public List<TransformationRule> execute(
			final List<MethodModification> methodModifications) {
		int counter = 0;
		subTreeList = new ArrayList<TransformationRule>();
		for (MethodModification mm : methodModifications) {
			try {
				System.out.println(counter++);
				List<TransformationRule> tr = execute(mm);
				if (tr != null)
					subTreeList.addAll(execute(mm));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		List<TransformationRule> result = subTreeList;
		subTreeList = null;
		return result;
	}

	// private int getDeepHashCode(SubTreeModel temp){
	// int hashCode = 0;
	// Enumeration<SubTreeModel> enumeration = temp.breadthFirstEnumeration();
	// int counter = 1;
	// while(enumeration.hasMoreElements()){
	// hashCode += enumeration.nextElement().hashCode() * (counter++);
	// }
	// return hashCode;
	// }
}
