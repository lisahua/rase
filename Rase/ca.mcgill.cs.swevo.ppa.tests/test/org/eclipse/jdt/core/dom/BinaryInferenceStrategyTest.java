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

public class BinaryInferenceStrategyTest {
	private static CompilationUnit cu1;
	// private static CompilationUnit cu2;
	private static NameVisitor cu1Visitor;

	private static CompilationUnit cu2;
	 private static NameVisitor cu2Visitor;
//	 private static PPATypeRegistry tRegistry;
//	 private static PPADefaultBindingResolver resolver;

	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/inference", "p1",
					"Binary1.java");
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);

			cu1 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu1Visitor = new NameVisitor();
			cu1.accept(cu1Visitor);
			
			file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/inference", "p1",
			"Binary2.java");
			icu = (ICompilationUnit) JavaCore.create(file);
			
			cu2 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu2Visitor = new NameVisitor();
			cu2.accept(cu2Visitor);
			//
			// file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/syntax", "p1",
			// "E2.java");
			// icu = (ICompilationUnit) JavaCore.create(file);
			//
			// cu2 = (CompilationUnit) PPAUtil.getCU(icu);
			// cu2Visitor = new NameVisitor();
			// cu2.accept(cu2Visitor);

			// tRegistry = new PPATypeRegistry((JavaProject) JavaCore.create(icu
			// .getUnderlyingResource().getProject()));
			// resolver = PPAUtil.getResolver(cu1.getAST());

			// file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/util", "p1",
			// "B2.java");
			// icu = (ICompilationUnit) JavaCore.create(file);
			//
			// cu2 = (CompilationUnit)PPAUtil.getCU(icu);
			// cu2Visitor = new AnonymousVisitor();
			// cu2.accept(cu2Visitor);

			// file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/util", "p1",
			// "B3.java");
			// icu = (ICompilationUnit) JavaCore.create(file);
			//
			// cu3 = (CompilationUnit)PPAUtil.getCU(icu);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testIfForWhile() {
		Name name = cu1Visitor.getNames().get("f2").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("int", name.resolveTypeBinding().getQualifiedName());

		IVariableBinding varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("int", varBinding.getType().getQualifiedName());

		name = cu1Visitor.getNames().get("f3").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("boolean", name.resolveTypeBinding().getQualifiedName());

		varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("boolean", varBinding.getType().getQualifiedName());

		name = cu1Visitor.getNames().get("f4").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("double", name.resolveTypeBinding().getQualifiedName());

		varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("double", varBinding.getType().getQualifiedName());
		
		name = cu1Visitor.getNames().get("f5").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("short", name.resolveTypeBinding().getQualifiedName());

		varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("short", varBinding.getType().getQualifiedName());
		
		name = cu1Visitor.getNames().get("f7").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("int", name.resolveTypeBinding().getQualifiedName());

		varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("int", varBinding.getType().getQualifiedName());
		
		name = cu1Visitor.getNames().get("f8").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("int", name.resolveTypeBinding().getQualifiedName());

		varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("int", varBinding.getType().getQualifiedName());
		
		name = cu1Visitor.getNames().get("f9").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("int", name.resolveTypeBinding().getQualifiedName());

		varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("int", varBinding.getType().getQualifiedName());
		
		name = cu1Visitor.getNames().get("f10").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("int", name.resolveTypeBinding().getQualifiedName());

		varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("int", varBinding.getType().getQualifiedName());
		
		name = cu1Visitor.getNames().get("f11").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("double", name.resolveTypeBinding().getQualifiedName());

		varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("double", varBinding.getType().getQualifiedName());
		
		name = cu1Visitor.getNames().get("f12").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("short", name.resolveTypeBinding().getQualifiedName());

		varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("short", varBinding.getType().getQualifiedName());
		
		name = cu1Visitor.getNames().get("f15").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("int", name.resolveTypeBinding().getQualifiedName());

		varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("int", varBinding.getType().getQualifiedName());
		
		name = cu1Visitor.getNames().get("f16").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("int", name.resolveTypeBinding().getQualifiedName());

		varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("int", varBinding.getType().getQualifiedName());
	}

	@Test
	public void testDoFQN() {
		Name name = cu1Visitor.getNames().get("f6").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("java.lang.String", name.resolveTypeBinding().getQualifiedName());

		IVariableBinding varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("java.lang.String", varBinding.getType().getQualifiedName());
	}
	
	@Test
	public void testIntoBinary() {
		Name name = cu2Visitor.getNames().get("a1s").get(0);
		assertNotNull(name.resolveBinding());
		assertEquals("p1.A", name.resolveTypeBinding().getQualifiedName());

		IVariableBinding varBinding = (IVariableBinding) name.resolveBinding();
		assertEquals("p1.A", varBinding.getType().getQualifiedName());
	}
}
