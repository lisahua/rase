package changeassistant.internal;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import changeassistant.versions.comparison.ChangedMethodADT;

public class ASTMethodFinder extends ASTVisitor {
	
	private String simpleClassName;

	private String methodSignature;
	
	 private MethodDeclaration node;
	 
	 private TypeDeclaration type;
	 
	 private boolean visitInnerMethod = false;
	@Override
	public boolean visit(MethodDeclaration node){
		if(!visitInnerMethod && this.node == null
				&& WorkspaceUtilities.getMethodSignatureFromASTNode(node).equals(this.methodSignature)
				&& node.getParent().equals(type)){
			this.node = node;
		}else if(visitInnerMethod && node.getParent() instanceof AnonymousClassDeclaration){
			if(WorkspaceUtilities.getMethodSignatureFromASTNode(node).equals(this.methodSignature)){
				this.node = node;
			}
		}
		return visitInnerMethod;
	}
	
	@Override
	public boolean visit(TypeDeclaration node){
		String typeName = node.getName().getIdentifier();
		if(typeName.equals(simpleClassName)){
			type = node;
			return true;
		}else if(simpleClassName.startsWith(typeName + ".")){
			simpleClassName = simpleClassName.substring(typeName.length() + 1);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean visit(PackageDeclaration node){
		if(!this.visitInnerMethod){
			String packageName = node.getName().getFullyQualifiedName();
			if(simpleClassName.contains(packageName))
				simpleClassName = simpleClassName.substring(packageName.length() + 1);
		}
		return true;
	}
	
	public MethodDeclaration lookforMethod(CompilationUnit unit,
			ChangedMethodADT peer){
//		System.out.print("");
		this.visitInnerMethod = false;
		this.simpleClassName = peer.classname;//at the beginning, the peer's classname is put in simpleClassName
		this.methodSignature = peer.methodSignature;
		this.node = null;
		this.type = null;
		unit.accept(this);
		while(this.node == null && simpleClassName.contains(".")){
			simpleClassName = simpleClassName.substring(simpleClassName.indexOf('.') + 1);
			String originalSimpleClassName = simpleClassName;
			unit.accept(this);
			simpleClassName = originalSimpleClassName;
		}
		if(this.node == null){
			this.visitInnerMethod = true;
			unit.accept(this);
		}
		return this.node;
	}
}
