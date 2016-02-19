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

public class EnumTest {
	private static CompilationUnit cu1;
	private static NameVisitor cu1Visitor;
	
	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/enum", "p1",
					"Modifier.java");
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);

			cu1 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu1Visitor = new NameVisitor();
			cu1.accept(cu1Visitor);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testFieldContainer() {
		Name nameA = cu1Visitor.getNames().get("name").get(0);

		IMethodBinding mBindingA = (IMethodBinding) nameA.resolveBinding();

		assertEquals("java.lang.Enum<p1.Modifier>", mBindingA.getDeclaringClass().getQualifiedName());
		assertEquals("java.lang.String", mBindingA.getReturnType().getQualifiedName());
	}
}
