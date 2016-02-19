package changeassistant.classhierarchy;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import changeassistant.internal.WorkspaceUtilities;
import changeassistant.peers.SourceCodeRange;

/**
 * Notice: a source code file may contain multiple type declarations
 * 
 * @author ibm
 * 
 */
public class ClassContextVisitor extends ASTVisitor {

	// the reference used to callback
	private ProjectResource pr;

	private String packageName;

	private CompilationUnit cu;

	private String relativeFilePath;

	private Stack<ClassContext> classContextStack;

	private ClassContext currentClassContext;

	private List<ClassContext> classContexts = null;

	private boolean notResolved = false;

	public ClassContextVisitor(ProjectResource pr, IPath filePath) {
		this.pr = pr;
		relativeFilePath = filePath.toOSString().substring(
				this.pr.projectFullPath.length());
		classContexts = new ArrayList<ClassContext>();
		classContextStack = new Stack<ClassContext>();
		packageName = "";
		currentClassContext = null;
	}

	public List<ClassContext> getClassContexts() {
		return this.classContexts;
	}

	@Override
	public boolean visit(CompilationUnit node) {
		this.cu = node;
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		List<VariableDeclarationFragment> frags = node.fragments();
		for (VariableDeclarationFragment frag : frags) {
			currentClassContext.fieldMap.put(
					frag.getName().getIdentifier(),
					new SourceCodeRange(node.getStartPosition(), node
							.getLength()));
		}
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		currentClassContext.methodMap.put(
				WorkspaceUtilities.getMethodSignatureFromASTNode(node),
				new SourceCodeRange(node.getStartPosition(), node.getLength()));
		return false;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		return false;
	}

	/**
	 * Here I use a conservative way: not using resolveBinding to get all supers
	 * and all interfaces for fear that it may throw exception
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		try {
			System.out.println(node.resolveBinding().getQualifiedName());
		} catch (Exception e) {// the type cannot be resolved
			notResolved = true;
			return false;
		}
		// ClassContext cc = new
		// ClassContext(node.resolveBinding().getQualifiedName(), cu, pr,
		// relativeFilePath);
		String className = node.resolveBinding().getQualifiedName();
		ClassContext cc = null;
		if (packageName.isEmpty() || className.startsWith(packageName))
			cc = new ClassContext(className, pr, relativeFilePath);
		else {
			cc = new ClassContext(packageName + "." + className, pr,
					relativeFilePath);
		}
		cc.isInterface = node.isInterface();
		if (node.getSuperclassType() != null) {
			if (node.getSuperclassType().resolveBinding() == null) {
				cc.superName = node.getSuperclassType().toString();
			} else {
				cc.superName = node.getSuperclassType().resolveBinding()
						.getQualifiedName();
			}
		}
		if (node.superInterfaceTypes() != null) {
			if (cc.interfaces.isEmpty()) {
				cc.interfaces = new ArrayList<String>();
			}
			for (Type type : (List<Type>) node.superInterfaceTypes()) {
				try {
					cc.interfaces.add(type.resolveBinding().getQualifiedName());
				} catch (Exception e) {
					cc.interfaces.add(type.toString());
				}
			}
		}
		// if this is the out most type declaration, then there is no need to
		// use stack
		if (currentClassContext == null) {
			currentClassContext = cc;
		} else {
			classContextStack.push(currentClassContext);
			currentClassContext = cc;
		}
		if (node.isInterface()) {// there is no need to visit the content of an
									// interface
			return false;
		}
		return true;
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		if (notResolved) {
			notResolved = false;
		} else {
			// the current class context is finished
			this.classContexts.add(currentClassContext);
			// if this is not the outmost type declaration, recover the
			// currentClassContext to the outer type decla
			if (!classContextStack.isEmpty()) {
				currentClassContext = classContextStack.pop();
			}
		}
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		packageName = node.getName().getFullyQualifiedName();
		return true;
	}
}
