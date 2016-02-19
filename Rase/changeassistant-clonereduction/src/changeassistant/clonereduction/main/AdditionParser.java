package changeassistant.clonereduction.main;

import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.peers.LineRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.DiffUtil;

public class AdditionParser {

	ProjectResource oldPr, newPr;
	ClassContext oldCC, newCC;
	ChangedMethodADT oldADT, newADT;
	// RunDiff runner;
	Set<LineRange> lineRanges;

	public AdditionParser(ProjectResource oldPr, ProjectResource newPr) {
		this.oldPr = oldPr;
		this.newPr = newPr;
		// this.runner = new RunDiff();
	}

	public Set<LineRange> parseAddition(ChangedMethodADT oldADT,
			ChangedMethodADT newADT) {
		this.oldADT = oldADT;
		this.newADT = newADT;
		oldCC = oldPr.findClassContext(oldADT.classname);
		newCC = newPr.findClassContext(newADT.classname);
		// String oldMethod = oldCC.getMethodAST(oldADT.methodSignature)
		// .toString();
		// String newMethod = newCC.getMethodAST(newADT.methodSignature)
		// .toString();
		// String path1 = FileUtil.createTmpFile(oldMethod, "tmp1.txt");
		// String path2 = FileUtil.createTmpFile(newMethod, "tmp2.txt");
		// String diffDesc = runner.runDiff(path1, path2);
		lineRanges = new HashSet<LineRange>();
		compareClassContext();
		Set<Node> affectedNodes = getAffectedNodes(newCC
				.getMethodNode(newADT.methodSignature));
		return lineRanges;
	}

	private void compareClassContext() {
		IFile oldFile = oldPr.getFile(oldCC.relativeFilePath);
		IFile newFile = newPr.getFile(newCC.relativeFilePath);
		DiffNode diff = DiffUtil.compare(oldFile, newFile);
		if (diff != null) {
			List<DiffNode> classNodes = DiffUtil.findClass(diff.getChildren());
			for (DiffNode classNode : classNodes) {
				if (classNode != null) {
					if (classNode.getKind() != Differencer.ADDITION
							&& classNode.getKind() != Differencer.DELETION) {
						processClassContainer(classNode, oldADT.methodSignature);
					}
				}
			}
		}
	}

	private Set<Node> getAffectedNodes(Node node) {
		Set<Node> set = new HashSet<Node>();
		CompilationUnit cu = newCC.getCU();

		return set;
	}

	private void processClassContainer(DiffNode classNode,
			String methodSignature) {
		addChange(classNode, methodSignature);
		IDiffElement[] elements = classNode.getChildren();
		for (IDiffElement element : elements) {
			if (element instanceof DiffContainer) {
				DiffContainer container = (DiffContainer) element;
				if (container instanceof DiffNode) {
					DiffNode dn = (DiffNode) container;
					if (dn.getId() instanceof DocumentRangeNode) {
						DocumentRangeNode drn = (DocumentRangeNode) dn.getId();
						if (DiffUtil.isAttribute(drn)
								|| DiffUtil.isMethodOrConstructor(drn)) {
							addChange(dn, methodSignature);
						}
						if (DiffUtil.isClassOrInterface(drn)) {
							processClassContainer(dn, methodSignature);
						}
					}
				}
			}
		}
	}

	private void addChange(DiffNode diffNode, String methodSignature) {
		if (!DiffUtil.isUsable(diffNode))
			return;
		if (DiffUtil.isInsert(diffNode)) {
			// do nothing currently
		} else if (DiffUtil.isDeletion(diffNode)) {
			// do nothing currently
		} else if (DiffUtil.isChange(diffNode)) {
			extractAdditions(diffNode, methodSignature);
		}
	}

	private void extractAdditions(DiffNode diffNode, String methodSignature) {
		if (DiffUtil.isMethodOrConstructor(diffNode)
				&& diffNode.getName().equals(methodSignature)) {
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

			DocLineComparator sLeft = new DocLineComparator(lDoc, lRegion, true);
			// ignore white space
			DocLineComparator sRight = new DocLineComparator(rDoc, rRegion,
					true);// ignore white space

			DocLineComparator sAncestor = null;
			Object result = RangeDifferencer.findRanges(null, sAncestor, sLeft,
					sRight);

			RangeDifference[] e = (RangeDifference[]) result;
			RangeDifference es = null;
			// System.out.print("");
			for (int i = 0; i < e.length; i++) {
				es = e[i];
				if (es.kind() == RangeDifference.CHANGE) {
					// there is a change
					try {
						String leftStr = getPureCode(es.leftStart(),
								es.leftEnd(), lDoc);
						String rightStr = getPureCode(es.rightStart(),
								es.rightEnd(), rDoc);
						if (!leftStr.equals(rightStr)) {
							lineRanges.add(new LineRange(es.rightStart(), es
									.rightLength(), sRight));
						}
					} catch (Exception excep) {
						excep.printStackTrace();
					}
				}
			}
		}
	}

	private String getPureCode(int start, int end, IDocument doc) {
		int offset = 0;
		int length = 0;
		String lineStr = null;
		StringBuffer buffer = new StringBuffer();
		for (int j = start; j < end; j++) {
			try {
				offset = doc.getLineOffset(j);
				length = doc.getLineLength(j);
				lineStr = doc.get(offset, length).trim();
				if (lineStr.startsWith("//")) {
					continue;
				} else if (lineStr.startsWith("/*")) {
					for (; j < end; j++) {
						offset = doc.getLineOffset(j);
						length = doc.getLineLength(j);
						lineStr = doc.get(offset, length).trim();
						if (lineStr.endsWith("*/")) {
							break;
						}
					}
				} else {
					buffer.append(lineStr);
				}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return buffer.toString();
	}
}
