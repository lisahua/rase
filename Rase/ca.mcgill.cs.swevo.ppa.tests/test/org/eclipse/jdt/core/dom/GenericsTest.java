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

public class GenericsTest {
	private static CompilationUnit cu1;
	private static NameVisitor cu1Visitor;

	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project,
					"sourcefiles/generics", "p1", "Generic1.java");
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);

			cu1 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu1Visitor = new NameVisitor();
			cu1.accept(cu1Visitor);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testConstructors() {
		Name f1 = cu1Visitor.getNames().get("bindingColumnNamePerTable").get(1);
		Name f2 = cu1Visitor.getNames().get("myMap").get(1);
		Name f3 = cu1Visitor.getNames().get("myContainer").get(1);

		IBinding f1Binding = f1.resolveBinding();
		IBinding f2Binding = f2.resolveBinding();
		IBinding f3Binding = f3.resolveBinding();

		assertEquals(
				"FBinding: java.util.Map<org.hibernate.mapping.Table,ExtendedMappings.ColumnNames> p1.Generic1:bindingColumnNamePerTable",
				PPABindingsUtil.getBindingText(f1Binding));

		// XXX The container is Configuration because there is a parsing error
		// when the first unknown generic parameter is encountered. If there was
		// no supertype, the container would be unknown.
		// At least, it is detected as a field.
		assertEquals(
				"FBinding: java.util.HashMap<java.lang.String,java.lang.String> p1.Configuration:myMap",
				PPABindingsUtil.getBindingText(f2Binding));
		assertEquals(
				"FBinding: p1.MyContainer<p1.DataColumn> p1.Configuration:myContainer",
				PPABindingsUtil.getBindingText(f3Binding));
	}

}
