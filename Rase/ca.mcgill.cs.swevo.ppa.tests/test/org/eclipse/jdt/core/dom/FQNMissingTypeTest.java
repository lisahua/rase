package org.eclipse.jdt.core.dom;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.swevo.ppa.tests.NameVisitor;
import ca.mcgill.cs.swevo.ppa.tests.SetupTestUtil;

public class FQNMissingTypeTest {
	private static CompilationUnit cu1;
	private static NameVisitor cu1Visitor;
	private static CompilationUnit cu2;
	private static NameVisitor cu2Visitor;
	private static CompilationUnit cu3;
	private static NameVisitor cu3Visitor;

	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/fqn", "p1",
					"TypeBinding.java");
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);

			cu1 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu1Visitor = new NameVisitor();
			cu1.accept(cu1Visitor);

			file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/fqn", "p1",
					"TextAreaPainter.java");
			icu = (ICompilationUnit) JavaCore.create(file);

			cu2 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu2Visitor = new NameVisitor();
			cu2.accept(cu2Visitor);

			file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/fqn", "",
					"A6.java");
			icu = (ICompilationUnit) JavaCore.create(file);

			cu3 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu3Visitor = new NameVisitor();
			cu3.accept(cu3Visitor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test1() {
		Name m1 = cu1Visitor.getNames().get("kind").get(0);
		Name t1 = cu1Visitor.getNames().get("TypeBinding").get(1);
		Name f1 = cu1Visitor.getNames().get("binding").get(1);

		IMethodBinding m1Binding = (IMethodBinding) m1.resolveBinding();
		ITypeBinding t1VarBinding = (ITypeBinding) t1.resolveBinding();
		IVariableBinding f1VarBinding = (IVariableBinding) f1.resolveBinding();

		assertEquals("org.eclipse.jdt.internal.compiler.lookup.TypeBinding", m1Binding
				.getDeclaringClass().getQualifiedName());
		assertEquals("org.eclipse.jdt.internal.compiler.lookup.TypeBinding", t1VarBinding
				.getQualifiedName());
		assertEquals("org.eclipse.jdt.internal.compiler.lookup.TypeBinding", f1VarBinding.getType()
				.getQualifiedName());
	}

	@Test
	public void test2() {
		// Just to ensure that this is not a loop!
		assertFalse(cu2Visitor.getNames().get("tabSize").isEmpty());
	}
	
	@Test
	public void testDefaultPackage() {
		Name n1 = cu3Visitor.getNames().get("FooHello").get(0);
		ITypeBinding typeBinding = (ITypeBinding) n1.resolveBinding();
		assertEquals("FooHello", typeBinding.getQualifiedName());
	}
}
