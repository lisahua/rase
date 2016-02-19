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

public class FieldInferenceStrategyTest {
	private static CompilationUnit cu1;
	// private static CompilationUnit cu2;
	private static NameVisitor cu1Visitor;

	// private static NameVisitor cu2Visitor;
	// private static PPATypeRegistry tRegistry;
	// private static PPADefaultBindingResolver resolver;

	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/inference", "p1",
					"Field1.java");
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);

			cu1 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu1Visitor = new NameVisitor();
			cu1.accept(cu1Visitor);
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
	public void testContainerMissingType() {
		Name h = cu1Visitor.getNames().get("field1h").get(0);
		Name k = cu1Visitor.getNames().get("field1k").get(0);

		IVariableBinding vBinding = (IVariableBinding) h.resolveBinding();
		assertEquals("p1.Field1Z", vBinding.getDeclaringClass().getQualifiedName());
		vBinding = (IVariableBinding) k.resolveBinding();
		assertEquals("p1.field1j", vBinding.getDeclaringClass().getQualifiedName());
	}

	@Test
	public void testContainerWrongType() {
		Name toto = cu1Visitor.getNames().get("toto").get(0);

		IVariableBinding vBinding = (IVariableBinding) toto.resolveBinding();
		assertEquals("java.lang.String", vBinding.getDeclaringClass().getQualifiedName());
	}

	@Test
	public void testContainerPartialSuperType() {
		Name f = cu1Visitor.getNames().get("field1f").get(0);
		Name d = cu1Visitor.getNames().get("field1d").get(0);
		Name z = cu1Visitor.getNames().get("field1z").get(0);
		Name my = cu1Visitor.getNames().get("myField").get(0);
		Name my2 = cu1Visitor.getNames().get("myField2").get(0);
		
		IVariableBinding vBinding = (IVariableBinding) f.resolveBinding();
		assertEquals("p1.Field1YSuper", vBinding.getDeclaringClass().getQualifiedName());

		vBinding = (IVariableBinding) d.resolveBinding();
		assertEquals("p1.Field1XSuper", vBinding.getDeclaringClass().getQualifiedName());
		assertEquals("java.lang.String", vBinding.getType().getQualifiedName());

		vBinding = (IVariableBinding) z.resolveBinding();
		assertEquals("p1.Field1Y", vBinding.getDeclaringClass().getQualifiedName());
		assertEquals("java.lang.Integer", vBinding.getType().getQualifiedName());

		vBinding = (IVariableBinding) my2.resolveBinding();
		assertEquals("p1.Field1X", vBinding.getDeclaringClass().getQualifiedName());
		// XXX This is because we want to converge to safe types only.
		assertEquals("p1.Field1XX", vBinding.getType().getQualifiedName());
		
		vBinding = (IVariableBinding) my.resolveBinding();
		assertEquals("p1.Field1X", vBinding.getDeclaringClass().getQualifiedName());
		// XXX This is because we want to converge to safe types only.
		assertEquals("p1.Field1XX", vBinding.getType().getQualifiedName());
	}
}
