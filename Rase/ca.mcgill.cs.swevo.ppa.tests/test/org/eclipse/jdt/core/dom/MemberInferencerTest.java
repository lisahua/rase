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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.swevo.ppa.tests.NameVisitor;
import ca.mcgill.cs.swevo.ppa.tests.SetupTestUtil;

public class MemberInferencerTest {
	private static CompilationUnit cu1;
	private static CompilationUnit cu2;
	private static NameVisitor cu1Visitor;
	private static NameVisitor cu2Visitor;

	// private static PPATypeRegistry tRegistry;
	// private static PPADefaultBindingResolver resolver;

	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/syntax", "p1",
					"E3.java");
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);

			cu1 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu1Visitor = new NameVisitor();
			cu1.accept(cu1Visitor);

			file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/syntax", "p1", "E4.java");
			icu = (ICompilationUnit) JavaCore.create(file);

			cu2 = (CompilationUnit) PPABindingsUtil.getCUWithoutMethodBinding(icu);
			cu2Visitor = new NameVisitor();
			cu2.accept(cu2Visitor);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testFieldContainer() {
		Name nameA = cu1Visitor.getNames().get("a").get(0);
		Name nameC1 = cu1Visitor.getNames().get("c1").get(0);
		Name nameC2 = cu1Visitor.getNames().get("c2").get(0);

		IVariableBinding vBindingA = (IVariableBinding) nameA.resolveBinding();
		IVariableBinding vBindingC1 = (IVariableBinding) nameC1.resolveBinding();
		IVariableBinding vBindingC2 = (IVariableBinding) nameC2.resolveBinding();

		assertEquals("p1.E3Super", vBindingA.getDeclaringClass().getQualifiedName());
		assertEquals("p1.E3Anon", vBindingC1.getDeclaringClass().getQualifiedName());
		assertEquals("p2.E3Anon2", vBindingC2.getDeclaringClass().getQualifiedName());
	}

	public void testFieldFQNContainer() {
		Name nameB = cu1Visitor.getNames().get("b").get(0);
		Name nameB1 = cu1Visitor.getNames().get("E3b").get(0);
		Name nameD1 = cu1Visitor.getNames().get("E3d").get(0);

		IVariableBinding vBindingB = (IVariableBinding) nameB.resolveBinding();
		IVariableBinding vBindingB1 = (IVariableBinding) nameB1.resolveBinding();
		IVariableBinding vBindingD2 = (IVariableBinding) nameD1.resolveBinding();

		assertEquals("p1.E3a", vBindingB.getDeclaringClass().getQualifiedName());
		assertEquals("E3z.E3y", vBindingB1.getDeclaringClass().getQualifiedName());
		assertEquals(PPATypeRegistry.UNKNOWN_CLASS_FQN, vBindingD2.getDeclaringClass()
				.getQualifiedName());
	}

	@Test
	public void testFieldWrongContainer() {
		Name nameD = cu1Visitor.getNames().get("toto").get(0);
		IVariableBinding vBindingD = (IVariableBinding) nameD.resolveBinding();
		assertEquals("java.lang.String", vBindingD.getDeclaringClass()
				.getQualifiedName());
	}
	
	@Test
	public void testFieldGoodContainer() {
		Name nameD = cu1Visitor.getNames().get("toto2").get(0);
		IVariableBinding vBindingD = (IVariableBinding) nameD.resolveBinding();
		assertEquals("p1.Animal", vBindingD.getDeclaringClass()
				.getQualifiedName());
	}

	@Test
	public void testMethodWithMissingSuper() {
		Name m1 = cu2Visitor.getNames().get("m1").get(0);
		IMethodBinding mBindingM1 = (IMethodBinding) m1.resolveBinding();
		assertEquals(PPATypeRegistry.UNKNOWN_CLASS_FQN, mBindingM1.getReturnType()
				.getQualifiedName());
		assertEquals("p1.E4aSuper", mBindingM1.getDeclaringClass().getQualifiedName());
		assertEquals("int", mBindingM1.getParameterTypes()[0].getQualifiedName());
		assertEquals("int", mBindingM1.getParameterTypes()[1].getQualifiedName());
	}

	@Test
	public void testMethodWithMissing() {
		Name m2 = cu2Visitor.getNames().get("m2").get(1);
		IMethodBinding mBindingM2 = (IMethodBinding) m2.resolveBinding();
		assertEquals(PPATypeRegistry.UNKNOWN_CLASS_FQN, mBindingM2.getReturnType()
				.getQualifiedName());
		assertEquals("p1.E4", mBindingM2.getDeclaringClass().getQualifiedName());
		assertEquals(PPATypeRegistry.UNKNOWN_CLASS_FQN, mBindingM2.getParameterTypes()[0].getQualifiedName());
		assertEquals(PPATypeRegistry.UNKNOWN_CLASS_FQN, mBindingM2.getParameterTypes()[1].getQualifiedName());
	
		Name m3 = cu2Visitor.getNames().get("m3").get(0);
		IMethodBinding mBindingM3 = (IMethodBinding) m3.resolveBinding();
		assertEquals(PPATypeRegistry.UNKNOWN_CLASS_FQN, mBindingM3.getReturnType()
				.getQualifiedName());
		assertEquals("java.lang.String", mBindingM3.getDeclaringClass().getQualifiedName());
		assertEquals("int", mBindingM3.getParameterTypes()[0].getQualifiedName());
		
		Name m4 = cu2Visitor.getNames().get("m4").get(0);
		IMethodBinding mBindingM4 = (IMethodBinding) m4.resolveBinding();
		assertEquals(PPATypeRegistry.UNKNOWN_CLASS_FQN, mBindingM4.getReturnType()
				.getQualifiedName());
		assertEquals("p1.E4", mBindingM4.getDeclaringClass().getQualifiedName());
		assertEquals("int", mBindingM4.getParameterTypes()[0].getQualifiedName());
		assertEquals(PPATypeRegistry.UNKNOWN_CLASS_FQN, mBindingM4.getParameterTypes()[1].getQualifiedName());
	
	}

	@Test
	public void testMethodWithMatch() {
		Name m2 = cu2Visitor.getNames().get("m2").get(0);
		IMethodBinding mBindingM2 = (IMethodBinding) m2.resolveBinding();
		assertEquals("void", mBindingM2.getReturnType()
				.getQualifiedName());
		assertEquals("p1.E4", mBindingM2.getDeclaringClass().getQualifiedName());
		assertEquals("int", mBindingM2.getParameterTypes()[0].getQualifiedName());
		assertEquals("java.lang.Object", mBindingM2.getParameterTypes()[1].getQualifiedName());
		
		Name m5 = cu2Visitor.getNames().get("m5").get(0);
		IMethodBinding mBindingM5 = (IMethodBinding) m5.resolveBinding();
		assertEquals("void", mBindingM5.getReturnType()
				.getQualifiedName());
		assertEquals("p1.E4", mBindingM5.getDeclaringClass().getQualifiedName());
		assertEquals("java.lang.Object", mBindingM5.getParameterTypes()[0].getQualifiedName());
		assertEquals("java.lang.Object", mBindingM5.getParameterTypes()[1].getQualifiedName());
	}
	
	@Test
	public void testMethodWithUnknownContainer() {
		Name m8 = cu1Visitor.getNames().get("m8").get(0);
		IMethodBinding mBindingM8 = (IMethodBinding) m8.resolveBinding();
		assertEquals(PPATypeRegistry.UNKNOWN_CLASS_FQN,mBindingM8.getDeclaringClass().getQualifiedName());
		assertEquals("int", mBindingM8.getParameterTypes()[0].getQualifiedName());
	}
	
	@Test
	public void testMethodWithSuperContainer() {
		Name m10 = cu1Visitor.getNames().get("m10").get(0);
		IMethodBinding mBindingM10 = (IMethodBinding) m10.resolveBinding();
		assertEquals("p1.E3Super",mBindingM10.getDeclaringClass().getQualifiedName());
	}
}
