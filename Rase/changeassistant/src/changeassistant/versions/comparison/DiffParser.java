package changeassistant.versions.comparison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.compare.internal.DocLineComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.compare.structuremergeviewer.DiffContainer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.PackageResource;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.internal.WorkspaceUtilities;
import changeassistant.peers.LineRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.BestLeafMatchDifferencer;
import changeassistant.versions.treematching.ITreeDifferencer;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation;

/**
 * This class decides which kind of changes can be parsed out
 * 
 * @author ibm
 * 
 */
public class DiffParser {

	public static boolean DEBUG = false;

	public static final int LEFT = 1;
	public static final int RIGHT = 2;

	private static final String DOT = ".";

	private ProjectResource prLeft;
	private ProjectResource prRight;

	// private IProject projectLeft;
	// private IProject projectRight;

	private ClassContext ccLeft;
	private ClassContext ccRight;

	private int LIMIT = 10;

	// private String sProjectLeft;
	// private String sProjectRight;

	private ITreeDifferencer astDifferencer;
	private Set<MethodModification> methodModifications;

	public DiffParser() {
		this.astDifferencer = new BestLeafMatchDifferencer();
	}

	/**
	 * Only compare classContexts mentioned in the classNames Set
	 * 
	 * @param projectLeft
	 * @param prLeft
	 * @param projectRight
	 * @param prRight
	 * @param classNames
	 * @return
	 */
	public List<MethodModification> compareClasses(IProject projectLeft,
			ProjectResource prLeft, IProject projectRight,
			ProjectResource prRight, Set<String> classNames) {
		this.prLeft = prLeft;
		this.prRight = prRight;

		this.methodModifications = new HashSet<MethodModification>();

		Set<ClassContext> classContexts = new HashSet<ClassContext>();
		for (String className : classNames) {
			classContexts.add(prLeft.findClassContext(className));
		}
		compareClassContexts(classContexts.iterator());
		classContexts = null;
		return this.getMethodModifications();
	}

	/**
	 * It is difficult to compare two projects directly, since the API to
	 * compare two projects are not public available according to the definition
	 * in Compare plugin. So we still use the method to compare two files each
	 * time.
	 * 
	 * @param fProjectLeft
	 * @param fProjectRight
	 */
	public List<MethodModification> compareProjects(ProjectResource prLeft,
			ProjectResource prRight) {
		this.prLeft = prLeft;
		this.prRight = prRight;

		this.methodModifications = new HashSet<MethodModification>();

		compare();
		return this.getMethodModifications();
	}

	private void compare() {
		try {
			compareClassContexts(prLeft.classContextIterator());

			Iterator<PackageResource> packageResourceIterator = prLeft
					.packageResourceIterator();
			while (packageResourceIterator.hasNext()) {
				compareClassContexts(packageResourceIterator.next()
						.classContextIterator());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<MethodModification> compareClassContext(ProjectResource prLeft,
			ProjectResource prRight, ClassContext ccLeft, ClassContext ccRight) {
		List<MethodModification> mmList = new ArrayList<MethodModification>();
		this.ccLeft = ccLeft;
		this.ccRight = ccRight;
		if (ccLeft != null && ccRight != null) {
			String leftPath = ccLeft.relativeFilePath;
			IFile leftFile = prLeft.getFile(leftPath);
			IFile rightFile = prRight.getFile(ccRight.relativeFilePath);
			DiffNode diff = DiffUtil.compare(leftFile, rightFile);
			if (diff != null) {
				List<DiffNode> classNodes = DiffUtil.findClass(diff
						.getChildren());
				for (DiffNode classNode : classNodes) {
					if (classNode != null) {
						if (classNode.getKind() != Differencer.ADDITION
								&& classNode.getKind() != Differencer.DELETION) {
							processClassContainer(classNode, mmList);
						}
					}
				}
			}
		}
		return mmList;
	}

	/**
	 * Assumption: the leftPath is always equal to rightPath
	 * 
	 * @param cIterator
	 *            --iterator of the left project
	 */
	private void compareClassContexts(Iterator<ClassContext> cIterator) {
		List<MethodModification> mmList;
		while (cIterator.hasNext()) {
			ccLeft = cIterator.next();
			ccRight = prRight.findClassContext(ccLeft.name);
			if (ccLeft != null && ccRight != null
					&& ccRight.relativeFilePath != null) {
				String leftPath = ccLeft.relativeFilePath;
				DiffNode diff = null;
				// if(cachedResults.containsKey(leftPath)){
				// this.methodModifications.addAll(cachedResults.get(leftPath));
				// }else{
				try {
					mmList = new ArrayList<MethodModification>();
					IFile leftFile = prLeft.getFile(leftPath);
					IFile rightFile = prRight.getFile(ccRight.relativeFilePath);
					diff = DiffUtil.compare(leftFile, rightFile);
					if (diff != null) {
						List<DiffNode> classNodes = DiffUtil.findClass(diff
								.getChildren());
						for (DiffNode classNode : classNodes) {
							if (classNode != null) {
								if (classNode.getKind() != Differencer.ADDITION
										&& classNode.getKind() != Differencer.DELETION) {
									processClassContainer(classNode, mmList);
								}
							}
						}
					}
					// if(cachedResults.size() + 1 < LIMIT){
					// cachedResults.put(leftPath, mmList);
					// }else{
					// cachedResults.clear();
					// cachedResults.put(leftPath, mmList);
					// }
					this.methodModifications.addAll(mmList);
					mmList = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
				// }
			}
		}
	}

	public MethodModification compareMethodContext(ProjectResource prLeft,
			ProjectResource prRight, ChangedMethodADT adt1,
			ChangedMethodADT adt2) {
		MethodModification mm = null;
		Node left = prLeft.findClassContext(adt1.classname).getMethodNode(
				adt1.methodSignature), right = prRight.findClassContext(
				adt2.classname).getMethodNode(adt2.methodSignature);
		if (left != null && right != null) {
			astDifferencer.calculateEditScript(left, right);
			// System.out.print("");
			List<ITreeEditOperation> tempOps = astDifferencer.getEditScript();
			List<AbstractTreeEditOperation> ops = new ArrayList<AbstractTreeEditOperation>();
			for (ITreeEditOperation op : tempOps) {
				ops.add((AbstractTreeEditOperation) op);
			}
			if (ops.size() > 0) {
				mm = new MethodModification(new ChangedMethodADT(
						adt1.classname, adt1.methodSignature,
						left.getSourceCodeRange(), adt1.getProjectName()),
						new ChangedMethodADT(adt2.classname,
								adt2.methodSignature, right
										.getSourceCodeRange(), adt2
										.getProjectName()), ops
				/*
				 * ,astDifferencer.getLeftToRightMatchPrime(),
				 * astDifferencer.getRightToLeftMatchPrime()
				 */);
			}
			tempOps = null;
			ops = null;
		}
		return mm;
	}

	public MethodModification compareMethodContext(ProjectResource prLeft,
			ProjectResource prRight, ChangedMethodADT adt) {
		MethodModification mm = null;
		Node left = prLeft.findClassContext(adt.classname).getMethodNode(
				adt.methodSignature), right = prRight.findClassContext(
				adt.classname).getMethodNode(adt.methodSignature);
		if (left != null && right != null) {
			astDifferencer.calculateEditScript(left, right);
			// System.out.print("");
			List<ITreeEditOperation> tempOps = astDifferencer.getEditScript();
			List<AbstractTreeEditOperation> ops = new ArrayList<AbstractTreeEditOperation>();
			for (ITreeEditOperation op : tempOps) {
				ops.add((AbstractTreeEditOperation) op);
			}
			if (ops.size() > 0) {
				mm = new MethodModification(
						new ChangedMethodADT(adt.classname,
								adt.methodSignature, left.getSourceCodeRange()),
						new ChangedMethodADT(adt.classname,
								adt.methodSignature, right.getSourceCodeRange()),
						ops
				/*
				 * ,astDifferencer.getLeftToRightMatchPrime(),
				 * astDifferencer.getRightToLeftMatchPrime()
				 */);
			}
			tempOps = null;
			ops = null;
		}
		return mm;
	}

	private int countLines(RangeDifference es, IDocument doc, int side)
			throws BadLocationException {
		if (side == LEFT && es.leftLength() == 1 || side == RIGHT
				&& es.rightLength() == 1)
			return 1;
		int offset = 0, length = 0;
		String lineStr = null;
		int lineLength = 0;
		int start = 0;
		int end = 0;
		if (side == LEFT) {
			lineLength = es.leftLength();
			start = es.leftStart();
			end = es.leftEnd();
		} else {// side == RIGHT
			lineLength = es.rightLength();
			start = es.rightStart();
			end = es.rightEnd();
		}

		for (int j = start; j < end; j++) {
			offset = doc.getLineOffset(j);
			length = doc.getLineLength(j);
			lineStr = doc.get(offset, length).trim();
			if (lineStr.startsWith("//")) {
				lineLength--;
			} else if (lineStr.startsWith("/*")) {
				int numOfCommentLines = 0;
				for (; j < es.leftEnd(); j++) {
					offset = doc.getLineOffset(j);
					length = doc.getLineLength(j);
					lineStr = doc.get(offset, length).trim();
					numOfCommentLines++;
					if (lineStr.endsWith("*/")) {
						break;
					}
				}
				lineLength -= numOfCommentLines;
			}
		}
		return lineLength;
	}

	private void processClassContainer(DiffNode classNode,
			List<MethodModification> methodModifications) {
		try {
			addChange(classNode, methodModifications);
			IDiffElement[] elements = classNode.getChildren();
			for (IDiffElement element : elements) {
				if (element instanceof DiffContainer) {
					DiffContainer container = (DiffContainer) element;
					if (container instanceof DiffNode) {
						DiffNode dn = (DiffNode) container;
						if (dn.getId() instanceof DocumentRangeNode) {
							DocumentRangeNode drn = (DocumentRangeNode) dn
									.getId();
							if (DiffUtil.isAttribute(drn)
									|| DiffUtil.isMethodOrConstructor(drn)) {
								addChange(dn, methodModifications);
							}
							if (DiffUtil.isClassOrInterface(drn)) {
								processClassContainer(dn, methodModifications);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addChange(DiffNode diffNode,
			List<MethodModification> methodModifications) {
		if (!DiffUtil.isUsable(diffNode))
			return;
		if (DiffUtil.isInsert(diffNode)) {
			// do nothing currently
		} else if (DiffUtil.isDeletion(diffNode)) {
			// do nothing currently
		} else if (DiffUtil.isChange(diffNode)) {
			extractBodyEdits(diffNode, methodModifications);
			extractDeclarationEdits(diffNode, methodModifications);
		}
	}

	public MethodModification extractMM(ClassContext ccLeft,
			ClassContext ccRight, String leftMethodSignature,
			String rightMethodSignature) {
		List<AbstractTreeEditOperation> ops = new ArrayList<AbstractTreeEditOperation>();
		Node left = ccLeft.getMethodNode(leftMethodSignature);
		Node right = ccRight.getMethodNode(rightMethodSignature);
		if (left == null && right == null) {
			left = ccLeft.getAmbiguousMethodNode(leftMethodSignature);
			right = ccRight.getAmbiguousMethodNode(rightMethodSignature);
		}
		if (left != null && right != null) {
			astDifferencer.calculateEditScript(left, right);
			List<ITreeEditOperation> tempOps = astDifferencer.getEditScript();
			for (ITreeEditOperation op : tempOps) {
				ops.add((AbstractTreeEditOperation) op);
			}
		}
		if (ops.size() > 0) {
			return new MethodModification(new ChangedMethodADT(ccLeft.name,
					leftMethodSignature, left.getSourceCodeRange()),
					new ChangedMethodADT(ccRight.name, rightMethodSignature,
							right.getSourceCodeRange()), ops);
		}
		return null;
	}

	private void extractBodyEdits(DiffNode diffNode,
			List<MethodModification> methodModifications) {
		if (DiffUtil.isMethodOrConstructor(diffNode)) {
			Map<LineRange, LineRange> noChangeRangePairs = new HashMap<LineRange, LineRange>();
			Map<LineRange, LineRange> updateRangePairs = new HashMap<LineRange, LineRange>();
			// if(diffNode.getId() instanceof DocumentRangeNode){
			DocumentRangeNode drnLeft = (DocumentRangeNode) diffNode.getLeft();
			DocumentRangeNode drnRight = (DocumentRangeNode) diffNode
					.getRight();

			IDocument lDoc = drnLeft.getDocument();
			IDocument rDoc = drnRight.getDocument();

			Region lRegion = new Region(drnLeft.getRange().getOffset(), drnLeft
					.getRange().getLength());
			Region rRegion = new Region(drnRight.getRange().getOffset(),
					drnRight.getRange().getLength());

			DocLineComparator sLeft = new DocLineComparator(lDoc, lRegion, true);// ignore
																					// white
																					// space
			DocLineComparator sRight = new DocLineComparator(rDoc, rRegion,
					true);// ignore white space

			DocLineComparator sAncestor = null;
			Object result = RangeDifferencer.findRanges(null, sAncestor, sLeft,
					sRight);

			RangeDifference[] e = (RangeDifference[]) result;
			RangeDifference es = null;
			boolean isChanged = false;
			System.out.print("");
			for (int i = 0; i < e.length; i++) {
				es = e[i];
				if (es.kind() == RangeDifference.NOCHANGE) {
					noChangeRangePairs.put(
							new LineRange(es.leftStart(), es.leftLength(),
									sLeft),
							new LineRange(es.rightStart(), es.rightLength(),
									sRight));
				} else {// there is a change
					isChanged = true;
					try {
						int leftLength = countLines(es, lDoc, LEFT);
						int rightLength = countLines(es, rDoc, RIGHT);
						if (leftLength == 1 && rightLength == 1) {
							updateRangePairs.put(
									new LineRange(es.leftStart(), es
											.leftLength(), sLeft),
									new LineRange(es.rightStart(), es
											.rightLength(), sRight));
						}
					} catch (Exception excep) {
						excep.printStackTrace();
					}
				}
			}
			if (!isChanged)
				return;
			// }
			String methodSignature = diffNode.getName();
			Node left = ccLeft.getMethodNode(methodSignature);
			Node right = ccRight.getMethodNode(methodSignature);
			if (left == null && right == null) {
				// since the method is not found, the methodSignature needs to
				// change
				left = ccLeft.getAmbiguousMethodNode(methodSignature);
				right = ccRight.getAmbiguousMethodNode(methodSignature);
				if (left == null || right == null)
					return;
				methodSignature = WorkspaceUtilities
						.getMethodSignatureFromASTNode(left
								.getMethodDeclaration());
			}
			List<AbstractTreeEditOperation> ops1 = new ArrayList<AbstractTreeEditOperation>();
			List<AbstractTreeEditOperation> ops2 = new ArrayList<AbstractTreeEditOperation>();
			if (left != null && right != null) {
				if (DEBUG) {
					astDifferencer.calculateEditScript(left, right);
					List<ITreeEditOperation> tempOps = astDifferencer
							.getEditScript();
					for (ITreeEditOperation op : tempOps) {
						ops1.add((AbstractTreeEditOperation) op);
					}
					left = ccLeft.getAmbiguousMethodNode(methodSignature);
					right = ccRight.getAmbiguousMethodNode(methodSignature);
					System.out.print("");
					astDifferencer.calculateEditScript(left, right,
							noChangeRangePairs, updateRangePairs,
							ccLeft.getCU(), ccRight.getCU());
					tempOps = astDifferencer.getEditScript();
					for (ITreeEditOperation op : tempOps) {
						ops2.add((AbstractTreeEditOperation) op);
					}
					if (ops1.size() != 0 && ops2.size() != 0) {
						if (ops1.size() < ops2.size()) {
							methodModifications.add(new MethodModification(
									new ChangedMethodADT(ccLeft.name,
											methodSignature, left
													.getSourceCodeRange()),
									new ChangedMethodADT(ccRight.name,
											methodSignature, right
													.getSourceCodeRange()),
									ops1
							/*
							 * ,astDifferencer.getLeftToRightMatchPrime(),
							 * astDifferencer.getRightToLeftMatchPrime()
							 */));
						} else {
							methodModifications.add(new MethodModification(
									new ChangedMethodADT(ccLeft.name,
											methodSignature, left
													.getSourceCodeRange()),
									new ChangedMethodADT(ccRight.name,
											methodSignature, right
													.getSourceCodeRange()),
									ops2));
							// if(diffNode.getName().contains(ChangeAssistantMain.METHOD_BEING_DEBUGGED)
							// && ops1.size() > ops2.size())
						}
						if (ops1.size() != ops2.size())
							System.out
									.println(diffNode.getName()
											+ " the new method has better performance: "
											+ (ops1.size() - 2) + " vs. "
											+ (ops2.size() - 2));
					}
				} else {
					System.out.print("");
					astDifferencer.calculateEditScript(left, right,
							noChangeRangePairs, updateRangePairs,
							ccLeft.getCU(), ccRight.getCU());
					List<ITreeEditOperation> tempOps = astDifferencer
							.getEditScript();
					// tempOps = astDifferencer.getEditScript();
					if (tempOps.size() <= 2)
						return;
					for (ITreeEditOperation op : tempOps) {
						ops2.add((AbstractTreeEditOperation) op);
					}
					methodModifications.add(new MethodModification(
							new ChangedMethodADT(ccLeft.name, methodSignature,
									left.getSourceCodeRange()),
							new ChangedMethodADT(ccRight.name, methodSignature,
									right.getSourceCodeRange()), ops2));
					System.out.println("compute edits for " + ccLeft.name + "."
							+ methodSignature);
				}
			}
		}
	}

	private void extractDeclarationEdits(DiffNode diffNode,
			List<MethodModification> methodModification) {
		// do nothing currently
	}

	public List<MethodModification> getMethodModifications() {
		return new ArrayList<MethodModification>(this.methodModifications);
	}
}