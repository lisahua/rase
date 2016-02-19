package org.eclipse.jdt.core.dom;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.swevo.ppa.PPAASTUtil;
import ca.mcgill.cs.swevo.ppa.tests.NameVisitor;
import ca.mcgill.cs.swevo.ppa.tests.SetupTestUtil;

public class ConstructorTest {
	private static CompilationUnit cu1;
	private static NameVisitor cu1Visitor;

	private static CompilationUnit cu2;
	private static NameVisitor cu2Visitor;

	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/constructor", "p1",
					"Constructor.java");
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);

			cu1 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu1Visitor = new NameVisitor();
			cu1.accept(cu1Visitor);

			file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/methods", "p1",
					"Constructor1.java");
			icu = (ICompilationUnit) JavaCore.create(file);

			cu2 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu2Visitor = new NameVisitor();
			cu2.accept(cu2Visitor);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testFieldContainer() {
		Name nameA = cu1Visitor.getNames().get("String").get(1);
		ASTNode node = PPAASTUtil.getSpecificParentType(nameA, ASTNode.CLASS_INSTANCE_CREATION);
		IMethodBinding mBindingA = null;
		if (node != null) {
			ClassInstanceCreation cic = (ClassInstanceCreation) node;
			mBindingA = cic.resolveConstructorBinding();
		}

		assertEquals("String", mBindingA.getName());
		assertTrue(mBindingA.isConstructor());

		nameA = cu1Visitor.getNames().get("MyMy").get(1);
		node = PPAASTUtil.getSpecificParentType(nameA, ASTNode.CLASS_INSTANCE_CREATION);
		mBindingA = null;
		if (node != null) {
			ClassInstanceCreation cic = (ClassInstanceCreation) node;
			mBindingA = cic.resolveConstructorBinding();
		}

		assertEquals("MyMy", mBindingA.getName());
	}
	
	@Test
	public void testParameters() {
		Name name = cu2Visitor.getNames().get("Constructor1B1").get(3);
		ASTNode node = PPAASTUtil.getSpecificParentType(name, ASTNode.CLASS_INSTANCE_CREATION);
		IMethodBinding mBinding = null;
		if (node != null) {
			ClassInstanceCreation cic = (ClassInstanceCreation) node;
			mBinding = cic.resolveConstructorBinding();
		}

		assertEquals("Constructor1B1", mBinding.getName());
		assertEquals("int", mBinding.getParameterTypes()[0].getName());
		assertEquals("String", mBinding.getParameterTypes()[1].getName());
		assertEquals("Constructor1A1", mBinding.getParameterTypes()[2].getName());
		
		name = cu2Visitor.getNames().get("Constructor1C1").get(1);
		node = PPAASTUtil.getSpecificParentType(name, ASTNode.CLASS_INSTANCE_CREATION);
		mBinding = null;
		if (node != null) {
			ClassInstanceCreation cic = (ClassInstanceCreation) node;
			mBinding = cic.resolveConstructorBinding();
		}

		assertEquals("Constructor1C1", mBinding.getName());
		assertEquals("Constructor1B1", mBinding.getParameterTypes()[0].getName());
		assertEquals("String", mBinding.getParameterTypes()[1].getName());
		assertEquals("int", mBinding.getParameterTypes()[2].getName());
		
		name = cu2Visitor.getNames().get("Constructor1D1").get(1);
		node = PPAASTUtil.getSpecificParentType(name, ASTNode.CLASS_INSTANCE_CREATION);
		mBinding = null;
		if (node != null) {
			ClassInstanceCreation cic = (ClassInstanceCreation) node;
			mBinding = cic.resolveConstructorBinding();
		}

		assertEquals("Constructor1D1", mBinding.getName());
		assertEquals("Constructor1B1", mBinding.getParameterTypes()[0].getName());
		assertEquals("String", mBinding.getParameterTypes()[1].getName());
		
		name = cu2Visitor.getNames().get("Constructor1C2").get(1);
		node = PPAASTUtil.getSpecificParentType(name, ASTNode.CLASS_INSTANCE_CREATION);
		mBinding = null;
		if (node != null) {
			ClassInstanceCreation cic = (ClassInstanceCreation) node;
			mBinding = cic.resolveConstructorBinding();
		}

		assertEquals("Constructor1C2", mBinding.getName());
		assertEquals("int", mBinding.getParameterTypes()[0].getName());
		assertEquals("int", mBinding.getParameterTypes()[1].getName());
	}
}
