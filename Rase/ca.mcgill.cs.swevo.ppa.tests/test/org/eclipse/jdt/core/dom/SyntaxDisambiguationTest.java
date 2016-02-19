/*******************************************************************************
 * PPA - Partial Program Analysis for Java
 * Copyright (C) 2008 Barthelemy Dagenais
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library. If not, see 
 * <http://www.gnu.org/licenses/lgpl-3.0.txt>
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.swevo.ppa.tests.NameVisitor;
import ca.mcgill.cs.swevo.ppa.tests.SetupTestUtil;

@SuppressWarnings("restriction")
public class SyntaxDisambiguationTest {
	private static CompilationUnit cu1;
	private static CompilationUnit cu2;
	private static CompilationUnit cu3;
	private static NameVisitor cu1Visitor;
	private static NameVisitor cu2Visitor;
	private static NameVisitor cu3Visitor;
	private static PPATypeRegistry tRegistry;
	private static PPADefaultBindingResolver resolver;

	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project,
					"sourcefiles/syntax", "p1", "E1.java");
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);

			cu1 = (CompilationUnit) PPABindingsUtil
					.getCUWithoutMemberInference(icu);
			cu1Visitor = new NameVisitor();
			cu1.accept(cu1Visitor);

			file = SetupTestUtil.copyJavaSourceFile(project,
					"sourcefiles/syntax", "p1", "E2.java");
			icu = (ICompilationUnit) JavaCore.create(file);

			cu2 = (CompilationUnit) PPABindingsUtil
					.getCUWithoutMemberInference(icu);
			cu2Visitor = new NameVisitor();
			cu2.accept(cu2Visitor);
			
			file = SetupTestUtil.copyJavaSourceFile(project,
					"sourcefiles/syntax", "p1", "ProxyConfig.java");
			icu = (ICompilationUnit) JavaCore.create(file);

			cu3 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			
			file = SetupTestUtil.copyJavaSourceFile(project,
					"sourcefiles/syntax", "p1", "AdvisedSupport.java");
			icu = (ICompilationUnit) JavaCore.create(file);

			cu3 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu3Visitor = new NameVisitor();
			cu3.accept(cu3Visitor);

			tRegistry = new PPATypeRegistry((JavaProject) JavaCore.create(icu
					.getUnderlyingResource().getProject()));
			resolver = PPABindingsUtil.getResolver(cu1.getAST());

			// file = SetupTestUtil.copyJavaSourceFile(project,
			// "sourcefiles/util", "p1",
			// "B2.java");
			// icu = (ICompilationUnit) JavaCore.create(file);
			//
			// cu2 = (CompilationUnit)PPAUtil.getCU(icu);
			// cu2Visitor = new AnonymousVisitor();
			// cu2.accept(cu2Visitor);

			// file = SetupTestUtil.copyJavaSourceFile(project,
			// "sourcefiles/util", "p1",
			// "B3.java");
			// icu = (ICompilationUnit) JavaCore.create(file);
			//
			// cu3 = (CompilationUnit)PPAUtil.getCU(icu);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testArray() {
		Name name = cu2Visitor.getNames().get("arrayString").get(0);
		IBinding binding1 = name.resolveBinding();
		ITypeBinding binding2 = name.resolveTypeBinding();
		assertTrue(binding1 instanceof IVariableBinding);
		assertEquals("java.lang.String[]", binding2
				.getQualifiedName());
	
		// TODO This is a big one:
		// In anonymous classes, local variables are not propagated in the Eclipse compiler,
		// So when we receive a local variable, after it is declared, we have no clue that
		// it was defined earlier (it looks like a field from the anonymous class).
		// To fix this, we would have to search in the scope for a local declaration on the same name.
		// That's pretty bad and not trivial.
//		name = cu2Visitor.getNames().get("arrayString").get(1);
//		binding1 = name.resolveBinding();
//		binding2 = name.resolveTypeBinding();
//		assertTrue(binding1 instanceof IVariableBinding);
//		assertEquals("java.lang.String[]", binding2
//				.getQualifiedName());
	}
	
	@Test
	public void testLocals() {
		for (Name name : cu1Visitor.getNames().get("local1")) {
			IBinding binding1 = name.resolveBinding();
			ITypeBinding binding2 = name.resolveTypeBinding();
			assertTrue(binding1 instanceof IVariableBinding);
			assertFalse(binding1.isRecovered());
			assertNotNull(binding2);
			assertFalse(binding2.isRecovered());
		}
	}

	@Test
	public void testLocalFQN() {
		Name name = cu2Visitor.getNames().get("z4").get(0);
		IBinding binding1 = name.resolveBinding();
		ITypeBinding binding2 = name.resolveTypeBinding();
		assertTrue(binding1 instanceof IVariableBinding);
		assertEquals(PPATypeRegistry.UNKNOWN_CLASS_FQN, binding2
				.getQualifiedName());
		
		name = cu2Visitor.getNames().get("out").get(0);
		binding1 = name.resolveBinding();
		binding2 = name.resolveTypeBinding();
		assertTrue(binding1 instanceof IVariableBinding);
		assertEquals("java.io.PrintStream", binding2
				.getQualifiedName());
		
	}

	@Test
	public void testTypes() {
		for (Name name : cu1Visitor.getNames().get("E1b")) {
			IBinding binding1 = name.resolveBinding();
			ITypeBinding binding2 = name.resolveTypeBinding();
			assertTrue(binding1 instanceof ITypeBinding);
			assertFalse(binding1.isRecovered());
			assertNotNull(binding2);
			assertFalse(binding2.isRecovered());
		}
	}

	@Test
	public void testFieldOrType() {
		for (Name name : cu1Visitor.getNames().get("E1c")) {
			IBinding binding1 = name.resolveBinding();
			ITypeBinding binding2 = name.resolveTypeBinding();
			assertTrue(binding1 instanceof IVariableBinding);
			assertFalse(binding1.isRecovered());
			assertNotNull(binding2);
			assertFalse(binding2.isRecovered());
		}

		for (Name name : cu1Visitor.getNames().get("E1d")) {
			IBinding binding1 = name.resolveBinding();
			ITypeBinding binding2 = name.resolveTypeBinding();
			assertTrue(binding1 instanceof IVariableBinding);
			assertFalse(binding1.isRecovered());
			assertNotNull(binding2);
			assertFalse(binding2.isRecovered());
		}
	}

	@Test
	public void testQualifiedNames() {
		Name d1 = cu2Visitor.getNames().get("d1").get(0);
		Name c1 = cu2Visitor.getNames().get("c1").get(0);
		Name b1 = cu2Visitor.getNames().get("b1").get(0);
		Name a1 = cu2Visitor.getNames().get("a1").get(0);
		Name c3 = cu2Visitor.getNames().get("c3").get(0);
		Name a3 = cu2Visitor.getNames().get("a3").get(0);
		Name a4 = cu2Visitor.getNames().get("a4").get(0);
		Name c4 = cu2Visitor.getNames().get("c4").get(0);
		ITypeBinding unknownBinding = tRegistry.getUnknownBinding(resolver);

		assertNotNull(d1.resolveTypeBinding());
		assertNotNull(c1.resolveTypeBinding());
		assertNotNull(b1.resolveTypeBinding());
		assertNull(a1.resolveTypeBinding());
		assertNotNull(c3.resolveTypeBinding());
		assertNull(a3.resolveTypeBinding());
		assertNull(a4.resolveTypeBinding());
		assertNotNull(c4.resolveTypeBinding());

		IVariableBinding varBindingD1 = (IVariableBinding) d1.resolveBinding();
		IVariableBinding varBindingC1 = (IVariableBinding) c1.resolveBinding();
		IVariableBinding varBindingB1 = (IVariableBinding) b1.resolveBinding();
		ITypeBinding typeBindingC3 = (ITypeBinding) c3.resolveBinding();
		ITypeBinding typeBindingC4 = (ITypeBinding) c4.resolveBinding();

		assertTrue(varBindingD1.isField());
		assertTrue(varBindingC1.isField());
		assertTrue(varBindingB1.isField());
		assertEquals("a3.b3.c3", typeBindingC3.toString());
		assertEquals("a4.b4.c4", typeBindingC4.toString());

		assertTrue(unknownBinding.isEqualTo(varBindingD1.getDeclaringClass()));
		assertTrue(unknownBinding.isEqualTo(varBindingC1.getDeclaringClass()));
		assertTrue(unknownBinding.isEqualTo(varBindingB1.getDeclaringClass()));

		assertTrue(unknownBinding.isEqualTo(varBindingD1.getType()));
		assertTrue(unknownBinding.isEqualTo(varBindingC1.getType()));
		assertTrue(unknownBinding.isEqualTo(varBindingB1.getType()));
	}
	
	@Test
	public void testFinalObjectMethods() {
		// Test that getLog(getClass()) resolves to getLog(Class<...>) because getClass is declared in Object.
		Name getLogName = cu3Visitor.getNames().get("getLog").get(0);
		IMethodBinding methodBinding = ((MethodInvocation) getLogName.getParent()).resolveMethodBinding();
		assertEquals(PPABindingsUtil.getBindingText(methodBinding),"MBinding: p1.Log org.apache.commons.logging.LogFactory:getLog(java.lang.Class<>)");
	}
}
