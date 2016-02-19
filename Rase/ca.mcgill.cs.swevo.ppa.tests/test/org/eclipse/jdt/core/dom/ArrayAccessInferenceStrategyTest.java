package org.eclipse.jdt.core.dom;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.swevo.ppa.tests.NameVisitor;
import ca.mcgill.cs.swevo.ppa.tests.SetupTestUtil;

public class ArrayAccessInferenceStrategyTest {
	private static CompilationUnit cu1;
	private static NameVisitor cu1Visitor;


	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/inference", "p1",
					"ArrayAccess1.java");
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);

			cu1 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu1Visitor = new NameVisitor();
			cu1.accept(cu1Visitor);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testIndex() {
		Name name = cu1Visitor.getNames().get("f2").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("p1.A1[]", name.resolveTypeBinding().getQualifiedName());
		
		IVariableBinding varBinding = (IVariableBinding)name.resolveBinding();
		assertEquals("p1.A1[]", varBinding.getType().getQualifiedName());
		
		name = cu1Visitor.getNames().get("f3").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("p1.A1[]", name.resolveTypeBinding().getQualifiedName());
		
		varBinding = (IVariableBinding)name.resolveBinding();
		assertEquals("p1.A1[]", varBinding.getType().getQualifiedName());
		
		name = cu1Visitor.getNames().get("f5").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("boolean[]", name.resolveTypeBinding().getQualifiedName());
		
		varBinding = (IVariableBinding)name.resolveBinding();
		assertEquals("boolean[]", varBinding.getType().getQualifiedName());
		
		name = cu1Visitor.getNames().get("f6").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("boolean[]", name.resolveTypeBinding().getQualifiedName());
		
		varBinding = (IVariableBinding)name.resolveBinding();
		assertEquals("boolean[]", varBinding.getType().getQualifiedName());
	}
}
