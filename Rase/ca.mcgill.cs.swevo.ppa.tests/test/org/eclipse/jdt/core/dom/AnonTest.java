package org.eclipse.jdt.core.dom;

import static junit.framework.Assert.assertEquals;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.swevo.ppa.tests.NameVisitor;
import ca.mcgill.cs.swevo.ppa.tests.SetupTestUtil;

public class AnonTest {
	private static CompilationUnit cu1;
	private static NameVisitor cu1Visitor;

	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/anon", "p1", "Anon1.java");
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);

			cu1 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu1Visitor = new NameVisitor();
			cu1.accept(cu1Visitor);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test1() {
		Name m1 = cu1Visitor.getNames().get("doubleValue").get(0);

		IMethodBinding m1Binding = (IMethodBinding) m1.resolveBinding();

		assertEquals("java.lang.Double", m1Binding.getDeclaringClass().getQualifiedName());
	}

}
