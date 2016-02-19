package changeassistant.main;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import changeassistant.change.group.ChangeGrouper;
import changeassistant.change.group.model.MatchingInfo;
import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.ChangeSuggestion2;
import changeassistant.changesuggestion.expression.representation.AbstractExpressionRepresentationGenerator;
import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.internal.SubTreeModelReader;
import changeassistant.model.TransformationRule;
import changeassistant.peers.PeerFinder2;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.DiffParser;
import changeassistant.versions.comparison.DiffUtil;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.ITreeMatcher;
import changeassistant.versions.treematching.MatchFactory;
import changeassistant.versions.treematching.NodePair;

public class ChangeAssistantMain {

	IProject fProjectLeft = null;
	IProject fProjectRight = null;

	public static String METHOD_BEING_DEBUGGED = "";

	static ProjectResource prLeft = null;
	static ProjectResource prRight = null;
	public static boolean UsePPA = false;

	public static boolean PRINT_INFO = true;
	public static boolean DEBUG = false;

	static List<MethodModification> methodModifications = null;
	// static Map<MethodModification, Set<MethodPair>> methodPairMap = null;

	String outputPath = "E:\\experiment\\6.16";
	String leftFilePath1, leftFilePath2, rightFilePath1, rightFilePath2;
	String leftClassName1, leftClassName2, rightClassName1, rightClassName2;
	String leftMethodName1, leftMethodName2, rightMethodName1,
			rightMethodName2;

	IFile leftFile = null;
	IFile rightFile = null;

	String sProjectRight = null;
	String sProjectLeft = null;

	String src = null;

	private void execute() {
		prLeft = new ProjectResource(fProjectLeft, src, leftFilePath1,
				rightFilePath1);
		prRight = new ProjectResource(fProjectRight, src, leftFilePath2,
				rightFilePath2);
		ClassContext cc1Left, cc2Left, cc1Right, cc2Right;
		DiffParser parser = new DiffParser();
		cc1Left = prLeft.findClassContext(leftClassName1);
		cc2Left = prLeft.findClassContext(rightClassName1);

		cc1Right = prRight.findClassContext(leftClassName2);
		cc2Right = prRight.findClassContext(rightClassName2);

		// MethodModification concernedMM =
		// parser.extractMM(cc1Left, cc1Right, leftMethodName1,
		// leftMethodName2);

		METHOD_BEING_DEBUGGED = leftMethodName1.substring(0,
				leftMethodName1.indexOf('('));

		List<MethodModification> mmList = // get edit script for all changed
											// methods in the class
		parser.compareClassContext(prLeft, prRight, cc1Left, cc1Right);
		// filter the mmList so that it only contains the method concerned
		MethodModification concernedMM = null;
		if (leftMethodName1.contains(".")) {
			leftMethodName1 = DiffUtil.simplifySig(leftMethodName1);
		}
		if (rightMethodName1.contains(".")) {
			rightMethodName1 = DiffUtil.simplifySig(rightMethodName1);
		}
		for (MethodModification mm : mmList) {
			if (mm.originalMethod.methodSignature.equals(leftMethodName1)) {
				concernedMM = mm;
				break;
			}
		}
		if (concernedMM != null) {
			// group changes and parse out relevant subtrees
			ChangeGrouper grouper = new ChangeGrouper(prLeft, prRight);
			TransformationRule tr = grouper.execute(concernedMM).get(0);

			// DebugPrinter.printPath(tr.subTreeModel);
			ChangeSuggestion2 cs = new ChangeSuggestion2(prLeft);
			// look for subTrees contained in the peers
			PeerFinder2 pFinder = new PeerFinder2();
			// Node methodNode = cc1Left.getMethodNode(leftMethodName1);
			// SubTreeModel candidate = new SubTreeModel(methodNode, methodNode,
			// true, new AbstractExpressionRepresentationGenerator());
			// ChangedMethodADT peer = new ChangedMethodADT(leftMethodName1,
			// leftMethodName2, cc1Left.methodMap.get(leftMethodName1));
			Node methodNode = cc2Left.getMethodNode(rightMethodName1);
			SubTreeModel candidate = new SubTreeModel(methodNode, methodNode,
					true, new AbstractExpressionRepresentationGenerator());

			ChangedMethodADT peer = new ChangedMethodADT(rightClassName1,
					rightMethodName1, cc2Left.methodMap.get(rightMethodName1));

			MatchingInfo matchingInfo2 = pFinder.containSubTree(candidate,
					tr.subTreeModel);

			if (matchingInfo2 != null && !matchingInfo2.isEmpty()) {
				cs.apply(peer, tr, matchingInfo2);
			}
		}
	}

	public void run(Map<String, IProject> map, List<ProjectMethodPair> pairs) {
		try {

			int counter = 0;
			for (ProjectMethodPair pair : pairs) {
				counter++;
				// if (counter != 1)
				// continue;
				src = pair.src.trim();
				System.out.println("counter = " + counter);
				sProjectLeft = pair.leftProjectName;
				fProjectLeft = map.get(sProjectLeft);
				sProjectRight = pair.rightProjectName;
				fProjectRight = map.get(sProjectRight);
				leftClassName1 = pair.leftClassName1;
				leftClassName2 = pair.leftClassName2;
				rightClassName1 = pair.rightClassName1;
				rightClassName2 = pair.rightClassName2;

				leftMethodName1 = pair.leftMethodName1;
				leftMethodName2 = pair.leftMethodName2;
				rightMethodName1 = pair.rightMethodName1;
				rightMethodName2 = pair.rightMethodName2;

				leftFilePath1 = pair.leftFilePath1;
				leftFilePath2 = pair.leftFilePath2;
				rightFilePath1 = pair.rightFilePath1;
				rightFilePath2 = pair.rightFilePath2;

				// sProjectLeft = pair.leftProjectName;
				// fProjectLeft = map.get(sProjectLeft);
				// sProjectRight = pair.rightProjectName;
				// fProjectRight = map.get(sProjectRight);
				// leftClassName1 = pair.rightClassName1;
				// leftClassName2 = pair.rightClassName2;
				// rightClassName1 = pair.leftClassName1;
				// rightClassName2 = pair.leftClassName2;
				//
				// leftMethodName1 = pair.rightMethodName1;
				// leftMethodName2 = pair.rightMethodName2;
				// rightMethodName1 = pair.leftMethodName1;
				// rightMethodName2 = pair.leftMethodName2;
				//
				// leftFilePath1 = pair.rightFilePath1;
				// leftFilePath2 = pair.rightFilePath2;
				// rightFilePath1 = pair.leftFilePath1;
				// rightFilePath2 = pair.leftFilePath2;
				execute();

			}

			// prLeft = new ProjectResource(fProjectLeft);
			// prRight = new ProjectResource(fProjectRight);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	public void run2(Map<String, IProject> map, List<ProjectMethodGroup> groups) {
		int counter = 0;
		for (ProjectMethodGroup group : groups) {
			counter++;
			if (counter != 24)
				continue;
			src = group.src;
			sProjectLeft = group.leftProjectName;
			fProjectLeft = map.get(sProjectLeft);
			sProjectRight = group.rightProjectName;
			fProjectRight = map.get(sProjectRight);
			leftClassName1 = group.leftClassName1;
			leftClassName2 = group.leftClassName2;
			leftFilePath1 = group.leftFilePath1;
			leftFilePath2 = group.leftFilePath2;
			leftMethodName1 = group.leftMethodName1;
			leftMethodName2 = group.leftMethodName2;

			for (ProjectMethod right : group.rightMethods) {
				rightClassName1 = right.className1;
				rightClassName2 = right.className2;
				rightFilePath1 = right.filePath1;
				rightFilePath2 = right.filePath2;
				rightMethodName1 = right.methodName1;
				rightMethodName2 = right.methodName2;
				execute();
			}
		}
	}

	public void run(List<IProject> elements) {
		long startTime = System.currentTimeMillis();
		if (elements.size() < 2) {
			System.out.println("There are not enough elements selected");
		} else {
			fProjectLeft = ((IProject) elements.get(0)).getProject();
			fProjectRight = ((IProject) elements.get(1)).getProject();
			// execute();
			// execute2(); //construct subTrees interested
			// execute3(); //search for contexts which may be changed
			// simultaneously
			// execute4();
			execute5();
			// execute6();
		}
		System.out.println("The time spent is "
				+ (System.currentTimeMillis() - startTime) + " milli seconds");
	}

	public void execute4() {
		if (prLeft == null) {
			prLeft = new ProjectResource(fProjectLeft);
			prRight = new ProjectResource(fProjectRight);
		}
		Map<SubTreeModel, List<TransformationRule>> subTreeMaps = SubTreeModelReader
				.readFromFile2("/Users/mn8247/Software/experiment/subTreeMaps2.tmp");
		Map<SubTreeModel, Set<ChangedMethodADT>> map = SubTreeModelReader
				.readFromFile("/Users/mn8247/Software/experiment/subTreeMaps-search-result.tmp");

		// to collect all the classNames for the classContexts concerned
		Set<String> classNames = new HashSet<String>();
		Iterator<Set<ChangedMethodADT>> iter = map.values().iterator();
		Set<ChangedMethodADT> adtSet = null;
		while (iter.hasNext()) {
			adtSet = iter.next();
			for (ChangedMethodADT adt : adtSet) {
				classNames.add(adt.classname);
			}
		}

		// to focus on these concerned classes
		DiffParser diffParser = new DiffParser();
		methodModifications = diffParser.compareClasses(fProjectLeft, prLeft,
				fProjectRight, prRight, classNames);
		ChangeSuggestion2 cs = new ChangeSuggestion2(prLeft, prRight,
				methodModifications, subTreeMaps, map);
		// cs.suggestChanges();
	}

	public void execute5() {
		try {
			// Example 1
			// String className1 =
			// "org.eclipse.compare.contentmergeviewer.TextMergeViewer",
			// className2 =
			// "org.eclipse.compare.contentmergeviewer.TextMergeViewer.Diff";
			// String methodName1 = "getNewRange(char, Object)",
			// methodName2 = "getPosition(char)";
			// Example 2
			// String className1 =
			// "org.eclipse.compare.contentmergeviewer.TextMergeViewer.ContributorInfo",
			// className2 =
			// "org.eclipse.compare.contentmergeviewer.TextMergeViewer.ContributorInfo";
			// String methodName1 = "elementMoved(Object, Object)",
			// methodName2 = "elementDeleted(Object)";
			// Example 3
			// String className1 =
			// "org.eclipse.compare.internal.patch.PreviewPatchPage2",
			// className2 =
			// "org.eclipse.compare.internal.patch.PreviewPatchPage";
			// String methodName1 =
			// "guess(WorkspacePatcher, IProgressMonitor, int)",
			// methodName2 = "guess(WorkspacePatcher, IProgressMonitor, int)";
			// Example 4
			// String className1 =
			// "org.eclipse.compare.contentmergeviewer.ContentMergeViewer",
			// className2 =
			// "org.eclipse.compare.contentmergeviewer.ContentMergeViewer.SaveAction";
			// String methodName1 = "flush(IProgressMonitor)",
			// methodName2 = "run()";
			// Example 5
			// String className1 =
			// "org.eclipse.compare.internal.patch.PatcherCompareEditorInput.PatcherCompareEditorDecorator",
			// className2 =
			// "org.eclipse.compare.internal.patch.PreviewPatchLabelDecorator";
			// String methodName1 = "decorateImage(Image, Object)",
			// methodName2 = "decorateImage(Image, Object)";
			// Example 6
			// String className1 =
			// "org.eclipse.compare.contentmergeviewer.TextMergeViewer",
			// className2 =
			// "org.eclipse.compare.contentmergeviewer.TextMergeViewer";
			// String methodName1 = "getStrokeColor(Diff)",
			// methodName2 = "getFillColor(Diff)";
			// Example 7
			// String className1 =
			// "org.eclipse.compare.contentmergeviewer.ContentMergeViewer",
			// className2 =
			// "org.eclipse.compare.contentmergeviewer.ContentMergeViewer";
			// String methodName1 = "setLeftDirty(boolean)",
			// methodName2 = "setRightDirty(boolean)";
			// Example 8
			// String className1 = "org.eclipse.compare.CompareEditorInput",
			// className2 = "org.eclipse.compare.CompareEditorInput";
			// String methodName1 =
			// "addCompareInputChangeListener(ICompareInput, ICompareInputChangeListener)",
			// methodName2 =
			// "removeCompareInputChangeListener(ICompareInput, ICompareInputChangeListener)";
			// Example 9
			// String className1 =
			// "org.eclipse.compare.internal.CompareUIPlugin",
			// className2 = "org.eclipse.compare.internal.CompareUIPlugin";
			// String methodName1 =
			// "openCompareEditor(CompareEditorInput, IWorkbenchPage, IReusableEditor)",
			// methodName2 = "openCompareDialog(CompareEditorInput)";
			// Example 10
			// String className1 =
			// "org.eclipse.compare.internal.ContentChangeNotifier",
			// className2 = "org.eclipse.compare.BufferedContent";
			// String methodName1 = "fireContentChanged()",
			// methodName2 = "fireContentChanged()";
			// Example 11
			// String className1 =
			// "org.eclipse.compare.structuremergeviewer.DiffTreeViewer",
			// className2 =
			// "org.eclipse.compare.structuremergeviewer.DiffTreeViewer";
			// String methodName1 =
			// "DiffTreeViewer(Composite, CompareConfiguration)",
			// methodName2 = "DiffTreeViewer(Tree, CompareConfiguration)";
			// Example 12
			// String className1 = "org.eclipse.compare.CompareEditorInput",
			// className2 = "org.eclipse.compare.CompareEditorInput";
			// String methodName1 = "getActionBars()",
			// methodName2 = "getServiceLocator()";
			// Example 13
			// String className1 = "org.eclipse.compare.internal.patch.Patcher",
			// className2 =
			// "org.eclipse.compare.internal.patch.WorkspacePatcher";
			// String methodName1 = "applyAll(IProgressMonitor, Shell, String)",
			// methodName2 = "applyAll(IProgressMonitor, Shell, String)";
			// Example 14
			// String className1 =
			// "org.eclipse.compare.contentmergeviewer.TextMergeViewer.ContributorInfo",
			// className2 =
			// "org.eclipse.compare.contentmergeviewer.TextMergeViewer.ContributorInfo";
			// String methodName1 = "elementDirtyStateChanged(Object, boolean)",
			// methodName2 = "elementContentReplaced(Object)";
			// Example 15
			// String className1 =
			// "org.eclipse.compare.internal.CompareOutlinePage",
			// className2 = "org.eclipse.compare.internal.CompareOutlinePage";
			// String methodName1 =
			// "addSelectionChangedListener(ISelectionChangedListener)",
			// methodName2 =
			// "removeSelectionChangedListener(ISelectionChangedListener)";
			// Example 16
			// String className1 = "org.eclipse.compare.CompareEditorInput",
			// className2 = "org.eclipse.compare.CompareEditorInput";
			// String methodName1 =
			// "removeCompareInputChangeListener(ICompareInput, ICompareInputChangeListener)",
			// methodName2 =
			// "addCompareInputChangeListener(ICompareInput, ICompareInputChangeListener)";
			// Example 17
			// String className1 =
			// "org.eclipse.compare.internal.MergeSourceViewer",
			// className2 = "org.eclipse.compare.internal.MergeSourceViewer";
			// String methodName1 = "textChanged(TextEvent)",
			// methodName2 = "selectionChanged(SelectionChangedEvent)";
			// Example 18
			// String className1 = "org.eclipse.compare.CompareEditorInput",
			// className2 = "org.eclipse.compare.CompareEditorInput";
			// String methodName1 = "runAsynchronously(IRunnableWithProgress)",
			// methodName2 = "run(boolean, boolean, IRunnableWithProgress)";
			// Example 19
			// String className1 =
			// "org.eclipse.compare.contentmergeviewer.TextMergeViewer",
			// className2 =
			// "org.eclipse.compare.contentmergeviewer.TextMergeViewer";
			// String methodName1 = "getStrokeColor(Diff)",
			// methodName2 = "getFillColor(Diff)";
			// ---------------------------------------------------------------------------------------
			// Example 3rd-3802-4822
			// String className1 = "bsh.BSHLHSPrimarySuffix",
			// className2 = "bsh.BSHPrimarySuffix";
			// String methodName1 =
			// "doLHSSuffix(Object, CallStack, Interpreter)",
			// methodName2 = "doSuffix(Object, CallStack, Interpreter)";
			// Example 4th-3992-4448
			// String className1 = "org.gjt.sp.jedit.buffer.UndoManager",
			// className2 = "org.gjt.sp.jedit.buffer.UndoManager";
			// String methodName1 =
			// "contentInserted(int, int, String, boolean)",
			// methodName2 = "contentRemoved(int, int, String, boolean)";
			// Example 5th 3992-4448--cannot be processed, since we do not care
			// about method deletion
			// String className1 = "org.gjt.sp.jedit.gui.RecentDirectoriesMenu",
			// className2 = "org.gjt.sp.jedit.gui.RecentFilesMenu";
			// String methodName1 = "setPopupMenuVisible(boolean)",
			// methodName2 = "setPopupMenuVisible(boolean)";
			// Example 6th-3802-4448--cannot be processed, since the two method
			// bodies are very different
			// even the changed statements are very different
			// String className1 =
			// "org.gjt.sp.jedit.pluginmgr.InstallPluginsDialog",
			// className2 = "org.gjt.sp.jedit.pluginmgr.PluginManager";
			// String methodName1 =
			// "InstallPluginsDialog(JDialog, Vector, int)",
			// methodName2 = "PluginManager(View)",
			// methodName3 = "PluginManager(Frame)";
			// Example 7th-8th7163-7992(the same)--cannot be processed, since
			// the two methods are pulled out
			// to put them in the super class AbstractInputHandler
			// String className1 = "org.gjt.sp.jedit.gui.InputHandler",
			// className2 = "org.gjt.sp.jedit.input.TextAreaInputHandler";
			// String methodName1 =
			// "InstallPluginsDialog(JDialog, Vector, int)",
			// methodName2 = "PluginManager(View)",
			// methodName3 = "PluginManager(Frame)";
			// Example 9th-7163 8692
			// String className1 = "org.gjt.sp.jedit.textarea.JEditTextArea",
			// className2 = "org.gjt.sp.jedit.textarea.TextArea";
			// String methodName1 = "userInput(char)",
			// methodName2 = "userInput(char)";
			// Example 10th-3802-10791
			// String className1 =
			// "org.gjt.sp.jedit.gui.EnhancedCheckBoxMenuItem",
			// className2 = "org.gjt.sp.jedit.gui.EnhancedMenuItem",
			// className3 = "org.gjt.sp.jedit.menu.EnhancedCheckBoxMenuItem",
			// className4 = "org.gjt.sp.jedit.menu.EnhancedMenuItem";
			// String methodName1 = "getShortcut()",
			// methodName2 = "getShortcut()";
			// Example 11th-3992-11207
			// String className1 = "org.gjt.sp.jedit.options.ContextOptionPane",
			// className2 = "org.gjt.sp.jedit.options.ToolBarOptionPane";
			// String methodName1 = "_init()",
			// methodName2 = "_init()";
			// Example 12th-4926-12514
			// String className1 = "org.gjt.sp.jedit.buffer.PositionManager",
			// className2 = "org.gjt.sp.jedit.buffer.PositionManager";
			// String methodName1 = "contentInserted(int, int)",
			// methodName2 = "contentRemoved(int, int)";
			// Example 13th-3096-6221 eclipse.jdt.core--cannot deal with,
			// because confliction is found
			// this conflict is due to wrong edit script generated
			// String className1 =
			// "org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression",
			// className2 =
			// "org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression";
			// String methodName1 =
			// "generateOptimizedBoolean(BlockScope, CodeStream, Label, Label, boolean)",
			// methodName2 =
			// "generateOptimizedBoolean(BlockScope, CodeStream, Label, Label, boolean)";
			// String filePath1 =
			// "org/eclipse/jdt/internal/compiler/ast/AND_AND_Expression",
			// filePath2 =
			// "org/eclipse/jdt/internal/compiler/ast/OR_OR_Expression";
			// className2 = "control.dependence.Test";
			// String methodName1 = "test1()",
			// methodName2 = "test2()";
			// String filePath1 = "control/dependence/Test",
			// filePath2 = "control/dependence/Test";

			// prLeft= new ProjectResource(fProjectLeft);
			// prRight = new ProjectResource(fProjectRight);
			// Example 14th-3096-6221 eclipse.jdt.core--cannot be matched to
			// each other
			// String className1 =
			// "org.eclipse.jdt.internal.compiler.ast.BinaryExpression",
			// className2 =
			// "org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression";
			// String methodName1 =
			// "generateOptimizedLogicalOr(BlockScope, CodeStream, Label, Label, boolean)",
			// methodName2 =
			// "generateOptimizedBoolean(BlockScope, CodeStream, Label, Label, boolean)";
			// Example 15th-3096-6721 eclipse.jdt.core
			// String className1 =
			// "org.eclipse.jdt.internal.compiler.ast.AllocationExpression",
			// className2 =
			// "org.eclipse.jdt.internal.eval.CodeSnippetAllocationExpression";
			// String methodName1 = "resolveType(BlockScope)",
			// methodName2 = "resolveType(BlockScope)";
			// String filePath1 =
			// "org/eclipse/jdt/internal/compiler/ast/AllocationExpression",
			// filePath2 =
			// "org/eclipse/jdt/internal/eval/CodeSnippetAllocationExpression";
			// Example 16th--invalid example: the two methods are the same

			// Example 17th--3096-6721 eclipse.jdt.core 0.768 0.541--the best
			// match algorithm to compute edit script has 720 plans to try
			String className1 = "org.eclipse.jdt.internal.compiler.ast.EqualExpression", className2 = "org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression";
			String methodName1 = "areTypesCastCompatible(BlockScope, TypeBinding, TypeBinding)", methodName2 = "areTypesCastCompatible(BlockScope, TypeBinding, TypeBinding)", methodName4 = "checkCastTypesCompatibility(BlockScope, TypeBinding, TypeBinding)";
			String filePath1 = "org/eclipse/jdt/internal/compiler/ast/EqualExpression", filePath2 = "org/eclipse/jdt/internal/compiler/ast/InstanceOfExpression";

			// Example 18th -- 3096-12151 eclipse.jdt.core--solve but a "return"
			// is put at the wrong position
			// String className1 =
			// "org.eclipse.jdt.internal.compiler.ast.BinaryExpression",
			// className2 =
			// "org.eclipse.jdt.internal.compiler.ast.BinaryExpression";
			// String methodName1 =
			// "generateOptimizedGreaterThan(BlockScope, CodeStream, Label, Label, boolean)",
			// methodName2 =
			// "generateOptimizedGreaterThanOrEqual(BlockScope, CodeStream, Label, Label, boolean)";
			// // methodName4 =
			// "checkCastTypesCompatibility(BlockScope, TypeBinding, TypeBinding)";
			// String filePath1 =
			// "org/eclipse/jdt/internal/compiler/ast/BinaryExpression",
			// filePath2 =
			// "org/eclipse/jdt/internal/compiler/ast/BinaryExpression";

			// Example 19th -- 3096-5044
			// String className1 =
			// "org.eclipse.jdt.internal.codeassist.CompletionEngine",
			// className2 = "org.eclipse.jdt.internal.compiler.lookup.Scope";
			// String methodName1 =
			// "findIntefacesMethods(char[], TypeBinding[], ReferenceBinding," +
			// " ReferenceBinding[], Scope, ObjectVector, boolean, boolean, boolean, InvocationSite,"
			// +
			// " Scope, boolean)",
			// methodName2 =
			// "findMethodInSuperInterfaces(ReferenceBinding, char[], ObjectVector, MethodBinding)";
			// // methodName4 =
			// "checkCastTypesCompatibility(BlockScope, TypeBinding, TypeBinding)";
			// String filePath1 =
			// "org/eclipse/jdt/internal/codeassist/CompletionEngine",
			// filePath2 = "org/eclipse/jdt/internal/compiler/lookup/Scope";

			// Example 20th -- 3096-12742
			// String className1 =
			// "org.eclipse.jdt.internal.compiler.parser.RecoveredInitializer",
			// className2 =
			// "org.eclipse.jdt.internal.compiler.parser.RecoveredMethod";
			// String methodName1 = "add(FieldDeclaration, int)",
			// methodName2 = "add(FieldDeclaration, int)";
			// String filePath1 =
			// "org/eclipse/jdt/internal/compiler/parser/RecoveredInitializer",
			// filePath2 =
			// "org/eclipse/jdt/internal/compiler/parser/RecoveredMethod";
			String src = "src";
			prLeft = new ProjectResource(fProjectLeft, src, filePath1,
					filePath2);
			prRight = new ProjectResource(fProjectRight, src, filePath1,
					filePath2);
			ClassContext cc1Left, cc2Left, cc1Right, cc2Right;
			if (prLeft == null) {
				prLeft = new ProjectResource(fProjectLeft);
				prRight = new ProjectResource(fProjectRight);
			}
			DiffParser parser = new DiffParser();
			cc1Left = prLeft.findClassContext(className1);
			cc2Left = prLeft.findClassContext(className2);

			cc1Right = prRight.findClassContext(className1);
			cc2Right = prRight.findClassContext(className2);

			// cc1Right = prRight.findClassContext(className3);
			// cc2Right = prRight.findClassContext(className4);

			Node node1 = cc1Left.getMethodNode(methodName1);
			Node node2 = cc2Left.getMethodNode(methodName2);

			double similarity1 = computeSimilarity(node1, node2);
			System.out.println("similarity = " + similarity1
					+ " before changes");

			node1 = cc1Right.getMethodNode(methodName1);
			node2 = cc2Right.getMethodNode(methodName4);
			double similarity2 = computeSimilarity(node1, node2);
			System.out
					.println("similarity = " + similarity2 + " after changes");

			List<MethodModification> mmList = // get edit script for all changed
												// methods in the class
			parser.compareClassContext(prLeft, prRight, cc1Left, cc1Right);
			// filter the mmList so that it only contains the method concerned
			MethodModification concernedMM = null;
			for (MethodModification mm : mmList) {
				if (mm.originalMethod.methodSignature.equals(methodName1)) {
					concernedMM = mm;
					break;
				}
			}
			// System.out.print("");
			if (concernedMM != null) {
				// group changes and parse out relevant subtrees
				// mmList.clear();
				// mmList.add(concernedMM);
				ChangeGrouper grouper = new ChangeGrouper(prLeft, prRight);
				List<TransformationRule> subTreeList = grouper
						.execute(concernedMM);

				// look for subTrees contained in the peers
				PeerFinder2 pFinder = new PeerFinder2();
				Node methodNode = cc2Left.getMethodNode(methodName2);
				// Map<SubTreeModel, Set<ChangedMethodADT>> peers = new
				// HashMap<SubTreeModel, Set<ChangedMethodADT>>();
				// Set<SubTreeModel> subTrees = subTreeMaps.keySet();

				Set<ChangedMethodADT> set = new HashSet<ChangedMethodADT>();

				ChangedMethodADT peer = new ChangedMethodADT(className2,
						methodName2, cc2Left.methodMap.get(methodName2));
				set.add(peer);
				SubTreeModel candidate = new SubTreeModel(methodNode,
						methodNode, true,
						new AbstractExpressionRepresentationGenerator());
				// Map<SubTreeModel, MatchingInfo> matchingList = new
				// HashMap<SubTreeModel, MatchingInfo>();
				MatchingInfo matchingInfo;
				ChangeSuggestion2 cs = new ChangeSuggestion2(prLeft);
				// if(subTrees.size() > 0){
				// for(SubTreeModel subTree : subTrees){
				// matchingInfo = pFinder.containSubTree(candidate, subTree);
				// if(matchingInfo.getMatchedList().size() <
				// subTree.countNodes()){
				// //partially matched
				// System.out.println("The tree is partially matched!");
				// }
				// if(!matchingInfo.isEmpty()){
				// matchingList.put(subTree, matchingInfo);
				// cs.apply(peer, subTreeMaps.get(subTree).get(0),
				// matchingList.get(subTree));
				// }else{
				// System.out.println("The candidate is not appliable to the edits!");
				// }
				// }
				// }else{
				// System.out.println("The concerned method is not changed at all");
				// }
				SubTreeModel subTree;
				for (int i = 0; i < subTreeList.size(); i++) {
					subTree = subTreeList.get(i).subTreeModel;
					matchingInfo = pFinder.containSubTree(candidate, subTree);
					if (matchingInfo != null && !matchingInfo.isEmpty()) {
						cs.apply(peer, subTreeList.get(i), matchingInfo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * To debug abstract representation for change context
	 */
	public void execute6() {
		boolean flag = true;
		String src;
		String className1, className2, methodName1, methodName2, filePath1, filePath2;
		if (flag) {
			src = "compare";
			className1 = "org.eclipse.compare.internal.patch.Patcher";
			className2 = "org.eclipse.compare.internal.patch.WorkspacePatcher";
			methodName1 = "applyAll(IProgressMonitor, Shell, String)";
			methodName2 = "applyAll(IProgressMonitor, Shell, String)";
			filePath1 = "org/eclipse/compare/internal/patch/Patcher";
			filePath2 = "org/eclipse/compare/internal/patch/WorkspacePatcher";
		} else {
			src = "src";
			className1 = "control.dependence.Test";
			className2 = "control.dependence.Test";
			methodName1 = "test1()";
			methodName2 = "test2()";
			filePath1 = "control/dependence/Test";
			filePath2 = "control/dependence/Test";
		}

		// Example 1
		// String className1 =
		// "org.eclipse.compare.contentmergeviewer.TextMergeViewer",
		// className2 =
		// "org.eclipse.compare.contentmergeviewer.TextMergeViewer.Diff";
		// String methodName1 = "getNewRange(char, Object)",
		// methodName2 = "getPosition(char)";
		// String filePath1 =
		// "org/eclipse/compare/contentmergeviewer/TextMergeViewer";
		// String filePath2 =
		// "org/eclipse/compare/contentmergeviewer/TextMergeViewer";
		// Example 2
		// String className1 =
		// "org.eclipse.compare.contentmergeviewer.TextMergeViewer.ContributorInfo",
		// className2 =
		// "org.eclipse.compare.contentmergeviewer.TextMergeViewer.ContributorInfo";
		// String methodName1 = "elementMoved(Object, Object)",
		// methodName2 = "elementDeleted(Object)";
		// String filePath1 =
		// "org/eclipse/compare/contentmergeviewer/TextMergeViewer";
		// String filePath2 =
		// "org/eclipse/compare/contentmergeviewer/TextMergeViewer";
		// Example 3
		// String className1 =
		// "org.eclipse.compare.internal.patch.PreviewPatchPage2",
		// className2 = "org.eclipse.compare.internal.patch.PreviewPatchPage";
		// String methodName1 =
		// "guess(WorkspacePatcher, IProgressMonitor, int)",
		// methodName2 = "guess(WorkspacePatcher, IProgressMonitor, int)";
		// String filePath1 =
		// "org/eclipse/compare/internal/patch/PreviewPatchPage2",
		// filePath2 = "org/eclipse/compare/internal/patch/PreviewPatchPage";
		// Example 4
		// String className1 =
		// "org.eclipse.compare.contentmergeviewer.ContentMergeViewer",
		// className2 =
		// "org.eclipse.compare.contentmergeviewer.ContentMergeViewer.SaveAction";
		// String methodName1 = "flush(IProgressMonitor)",
		// methodName2 = "run()";
		// String filePath1 =
		// "org/eclipse/compare/contentmergeviewer/ContentMergeViewer",
		// filePath2 =
		// "org/eclipse/compare/contentmergeviewer/ContentMergeViewer";
		// Example 5
		// String className2 =
		// "org.eclipse.compare.internal.patch.PatcherCompareEditorInput.PatcherCompareEditorDecorator",
		// className1 =
		// "org.eclipse.compare.internal.patch.PreviewPatchLabelDecorator";
		// String methodName1 = "decorateImage(Image, Object)",
		// methodName2 = "decorateImage(Image, Object)";
		// String filePath2 =
		// "org/eclipse/compare/internal/patch/PatcherCompareEditorInput",
		// filePath1 =
		// "org/eclipse/compare/internal/patch/PreviewPatchLabelDecorator";

		// Example 6
		// String className1 =
		// "org.eclipse.compare.contentmergeviewer.TextMergeViewer",
		// className2 =
		// "org.eclipse.compare.contentmergeviewer.TextMergeViewer";
		// String methodName1 = "getStrokeColor(Diff)",
		// methodName2 = "getFillColor(Diff)";
		// String filePath1 =
		// "org/eclipse/compare/contentmergeviewer/TextMergeViewer",
		// filePath2 = "org/eclipse/compare/contentmergeviewer/TextMergeViewer";

		// Example 7
		// String className1 =
		// "org.eclipse.compare.contentmergeviewer.ContentMergeViewer",
		// className2 =
		// "org.eclipse.compare.contentmergeviewer.ContentMergeViewer";
		// String methodName1 = "setLeftDirty(boolean)",
		// methodName2 = "setRightDirty(boolean)";
		// String filePath1 =
		// "org/eclipse/compare/contentmergeviewer/ContentMergeViewer",
		// filePath2 =
		// "org/eclipse/compare/contentmergeviewer/ContentMergeViewer";

		// Example 8
		// String className1 = "org.eclipse.compare.CompareEditorInput",
		// className2 = "org.eclipse.compare.CompareEditorInput";
		// String methodName1 =
		// "addCompareInputChangeListener(ICompareInput, ICompareInputChangeListener)",
		// methodName2 =
		// "removeCompareInputChangeListener(ICompareInput, ICompareInputChangeListener)";
		// String filePath1 = "org/eclipse/compare/CompareEditorInput",
		// filePath2 = "org/eclipse/compare/CompareEditorInput";

		// Example 9
		// String className1 = "org.eclipse.compare.internal.CompareUIPlugin",
		// className2 = "org.eclipse.compare.internal.CompareUIPlugin";
		// String methodName1 =
		// "openCompareEditor(CompareEditorInput, IWorkbenchPage, IReusableEditor)",
		// methodName2 = "openCompareDialog(CompareEditorInput)";
		// String filePath1 = "org/eclipse/compare/internal/CompareUIPlugin",
		// filePath2 = "org/eclipse/compare/internal/CompareUIPlugin";
		// Example 10
		// String className2 =
		// "org.eclipse.compare.internal.ContentChangeNotifier",
		// className1 = "org.eclipse.compare.BufferedContent";
		// String methodName2 = "fireContentChanged()",
		// methodName1 = "fireContentChanged()";
		// String filePath1 = "org/eclipse/compare/BufferedContent",
		// filePath2 = "org/eclipse/compare/internal/ContentChangeNotifier";
		// Example 11
		// String className1 =
		// "org.eclipse.compare.structuremergeviewer.DiffTreeViewer",
		// className2 =
		// "org.eclipse.compare.structuremergeviewer.DiffTreeViewer";
		// String methodName1 =
		// "DiffTreeViewer(Composite, CompareConfiguration)",
		// methodName2 = "DiffTreeViewer(Tree, CompareConfiguration)";
		// String filePath1 =
		// "org/eclipse/compare/structuremergeviewer/DiffTreeViewer",
		// filePath2 =
		// "org/eclipse/compare/structuremergeviewer/DiffTreeViewer";
		// Example 12
		// String className1 = "org.eclipse.compare.CompareEditorInput",
		// className2 = "org.eclipse.compare.CompareEditorInput";
		// String methodName1 = "getActionBars()",
		// methodName2 = "getServiceLocator()";
		// String filePath1 = "org/eclipse/compare/CompareEditorInput",
		// filePath2 = "org/eclipse/compare/CompareEditorInput";
		// Example 13-unsolved
		// String className1 = "org.eclipse.compare.internal.patch.Patcher",
		// className2 = "org.eclipse.compare.internal.patch.WorkspacePatcher";
		// String methodName1 = "applyAll(IProgressMonitor, Shell, String)",
		// methodName2 = "applyAll(IProgressMonitor, Shell, String)";
		// String filePath1 = "org/eclipse/compare/internal/patch/Patcher",
		// filePath2 = "org/eclipse/compare/internal/patch/WorkspacePatcher";
		// Example 14
		// String className1 =
		// "org.eclipse.compare.contentmergeviewer.TextMergeViewer.ContributorInfo",
		// className2 =
		// "org.eclipse.compare.contentmergeviewer.TextMergeViewer.ContributorInfo";
		// String methodName1 = "elementDirtyStateChanged(Object, boolean)",
		// methodName2 = "elementContentReplaced(Object)";
		// String filePath1 =
		// "org/eclipse/compare/contentmergeviewer/TextMergeViewer",
		// filePath2 = "org/eclipse/compare/contentmergeviewer/TextMergeViewer";
		// Example 15
		// String className1 =
		// "org.eclipse.compare.internal.CompareOutlinePage",
		// className2 = "org.eclipse.compare.internal.CompareOutlinePage";
		// String methodName1 =
		// "addSelectionChangedListener(ISelectionChangedListener)",
		// methodName2 =
		// "removeSelectionChangedListener(ISelectionChangedListener)";
		// String filePath1 = "org/eclipse/compare/internal/CompareOutlinePage",
		// filePath2 = "org/eclipse/compare/internal/CompareOutlinePage";
		// Example 16
		// String className1 = "org.eclipse.compare.CompareEditorInput",
		// className2 = "org.eclipse.compare.CompareEditorInput";
		// String methodName1 =
		// "removeCompareInputChangeListener(ICompareInput, ICompareInputChangeListener)",
		// methodName2 =
		// "addCompareInputChangeListener(ICompareInput, ICompareInputChangeListener)";
		// String filePath1 = "org/eclipse/compare/CompareEditorInput",
		// filePath2 = "org/eclipse/compare/CompareEditorInput";
		// Example 17
		// String className1 = "org.eclipse.compare.internal.MergeSourceViewer",
		// className2 = "org.eclipse.compare.internal.MergeSourceViewer";
		// String methodName1 = "textChanged(TextEvent)",
		// methodName2 = "selectionChanged(SelectionChangedEvent)";
		// String filePath1 = "org/eclipse/compare/internal/MergeSourceViewer",
		// filePath2 = "org/eclipse/compare/internal/MergeSourceViewer";
		// Example 18
		// String className1 = "org.eclipse.compare.CompareEditorInput",
		// className2 = "org.eclipse.compare.CompareEditorInput";
		// String methodName1 = "runAsynchronously(IRunnableWithProgress)",
		// methodName2 = "run(boolean, boolean, IRunnableWithProgress)";
		// String filePath1 = "org/eclipse/compare/CompareEditorInput",
		// filePath2 = "org/eclipse/compare/CompareEditorInput";
		// Example 19
		// String className1 =
		// "org.eclipse.compare.contentmergeviewer.TextMergeViewer",
		// className2 =
		// "org.eclipse.compare.contentmergeviewer.TextMergeViewer";
		// String methodName1 = "getStrokeColor(Diff)",
		// methodName2 = "getFillColor(Diff)";
		// String filePath1 =
		// "org/eclipse/compare/contentmergeviewer/TextMergeViewer",
		// filePath2 = "org/eclipse/compare/contentmergeviewer/TextMergeViewer";
		// /////////////////Manual crafted
		// examples//////////////////////////////
		// String className1 = "control.dependence.Test",
		// className2 = "control.dependence.Test";
		// String methodName1 = "test1()",
		// methodName2 = "test2()";
		// String filePath1 = "control/dependence/Test",
		// filePath2 = "control/dependence/Test";

		prLeft = new ProjectResource(fProjectLeft, src, filePath1, filePath2);
		prRight = new ProjectResource(fProjectRight, src, filePath1, filePath2);
		// prLeft= new ProjectResource(fProjectLeft);
		// prRight = new ProjectResource(fProjectRight);

		ClassContext cc1Left, cc2Left, cc1Right/* , cc2Right */;

		DiffParser parser = new DiffParser();
		cc1Left = prLeft.findClassContext(className1);
		cc2Left = prLeft.findClassContext(className2);

		cc1Right = prRight.findClassContext(className1);
		// cc2Right = prRight.findClassContext(className2);

		List<MethodModification> mmList = // get edit script for all changed
											// methods in the class
		parser.compareClassContext(prLeft, prRight, cc1Left, cc1Right);
		// filter the mmList so that it only contains the method concerned
		MethodModification concernedMM = null;
		for (MethodModification mm : mmList) {
			if (mm.originalMethod.methodSignature.equals(methodName1)) {
				concernedMM = mm;
				break;
			}
		}
		// if(concernedMM != null){
		// //group changes and parse out relevant subtrees
		// mmList.clear();
		// mmList.add(concernedMM);
		// ChangeGrouper grouper = new ChangeGrouper(fProjectLeft, prLeft,
		// fProjectRight, prRight);
		// Map<SubTreeModel, List<TransformationRule>> subTreeMaps =
		// grouper.execute(mmList);
		//
		// //look for subTrees contained in the peers
		// PeerFinder2 pFinder = new PeerFinder2();
		// Node methodNode = cc2Left.getMethodNode(methodName2);
		// // Map<SubTreeModel, Set<ChangedMethodADT>> peers = new
		// HashMap<SubTreeModel, Set<ChangedMethodADT>>();
		// Set<SubTreeModel> subTrees = subTreeMaps.keySet();
		//
		// Set<ChangedMethodADT> set = new HashSet<ChangedMethodADT>();
		//
		// ChangedMethodADT peer = new ChangedMethodADT(className2,
		// methodName2, cc2Left.methodMap.get(methodName2));
		// set.add(peer);
		// SubTreeModel candidate = new SubTreeModel(methodNode, methodNode,
		// true,
		// new AbstractExpressionRepresentationGenerator());
		// Map<SubTreeModel, MatchingInfo> matchingList
		// = new HashMap<SubTreeModel, MatchingInfo>();
		// MatchingInfo matchingInfo;
		// ChangeSuggestion2 cs = new ChangeSuggestion2(prLeft);
		// if(subTrees.size() > 0){
		// for(SubTreeModel subTree : subTrees){
		// matchingInfo = pFinder.containSubTree(candidate, subTree);
		// if(matchingInfo.getMatchedList().size() < subTree.countNodes()){
		// //patially matched
		//
		// }
		// if(!matchingInfo.isEmpty()){
		// matchingList.put(subTree, matchingInfo);
		// cs.apply(peer, subTreeMaps.get(subTree).get(0),
		// matchingList.get(subTree));
		// }
		// else{
		// System.out.println("The candidate is not appliable to the edits!");
		// }
		// }
		// // if(peers.size() == 0){
		// // System.out.println("The two methods are not peers");
		// // return;
		// // }
		// }
		// }else{
		// System.out.println("The concerned method is not found to change at all");
		// }
	}

	public double computeSimilarity(Node node1, Node node2) {
		Set<NodePair> matchPairs = new HashSet<NodePair>();
		Set<Node> matchedLeftNodes = new HashSet<Node>();
		Set<Node> matchedRightNodes = new HashSet<Node>();
		ITreeMatcher dnm = MatchFactory.getMatcher(matchPairs, 1);
		int commonCounter = 0;
		dnm.match(node1, node2);
		for (NodePair matchPair : matchPairs) {
			if (matchPair.getLeft().getStrValue()
					.equals(matchPair.getRight().getStrValue())) {
				if (matchedLeftNodes.contains(matchPair.getLeft())
						|| matchedRightNodes.contains(matchPair.getRight())) {
					// do nothing
				} else if (matchPair.getLeft().toString()
						.equals("method declaration")) {
					// do nothing
				} else {
					commonCounter++;
					matchedLeftNodes.add(matchPair.getLeft());
					matchedRightNodes.add(matchPair.getRight());
				}
			}
		}
		double similarity;
		int counter1 = node1.countNodes();
		int counter2 = node2.countNodes();
		if (counter1 == 1 && counter2 == 1) {
			similarity = 1;
		} else if (counter1 == commonCounter && counter2 == commonCounter) {
			similarity = 1;
		} else {
			similarity = 2.0 * commonCounter
					/ (node1.countNodes() + node2.countNodes() - 2);
		}
		return similarity;
	}

	// public void execute(){
	// // if(methodModifications == null){
	// prLeft = new ProjectResource(fProjectLeft);
	// prRight = new ProjectResource(fProjectRight);
	// DiffParser diffParser = new DiffParser();
	// methodModifications =
	// diffParser.compareProjects(fProjectLeft, prLeft, fProjectRight, prRight);
	// List<MethodPair> methodPairs = new ArrayList<MethodPair>();
	// MethodPairCreator mpCreator = new MethodPairCreator();
	// methodPairMap = mpCreator.create(methodModifications, methodPairs,
	// prLeft, prRight);
	// // }
	// ChangeSuggestion cs = new ChangeSuggestion();
	// cs.suggestChange(methodPairMap, prLeft, prRight);
	// System.out.println("Done!");
	// // PeerFinder peerFinder = new PeerFinder(prLeft);
	// // List<Set<PeerMethodADT>> potentialMethodPeers =
	// peerFinder.findPotentialPeers();
	//
	// // MethodPairComparator comparator = new MethodPairComparator();
	// // comparator.compare(potentialMethodPeers, projectResource);
	// }

}
