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

public class MethodBindingTest {
	private static CompilationUnit cu1;
	private static NameVisitor cu1Visitor;


	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/methods", "p1",
					"G2.java");
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
		Name m1 = cu1Visitor.getNames().get("m1").get(0);
		Name m2 = cu1Visitor.getNames().get("m2").get(0);
		Name f1 = cu1Visitor.getNames().get("f1").get(0);
		Name f2 = cu1Visitor.getNames().get("f2").get(0);
		Name f3 = cu1Visitor.getNames().get("f3").get(0);
		Name f4 = cu1Visitor.getNames().get("f4").get(0);
		
		IMethodBinding m1Binding = (IMethodBinding) m1.resolveBinding();
		IMethodBinding m2Binding = (IMethodBinding) m2.resolveBinding();
		IVariableBinding f1VarBinding = (IVariableBinding) f1.resolveBinding();
		IVariableBinding f2VarBinding = (IVariableBinding) f2.resolveBinding();
		IVariableBinding f3VarBinding = (IVariableBinding) f3.resolveBinding();
		IVariableBinding f4VarBinding = (IVariableBinding) f4.resolveBinding();
		
		assertEquals("p1.G2",m1Binding.getDeclaringClass().getQualifiedName());
		assertEquals("p1.G2",m2Binding.getDeclaringClass().getQualifiedName());
		assertEquals("int",f1VarBinding.getType().getQualifiedName());
		assertEquals("int",f2VarBinding.getType().getQualifiedName());
		assertEquals("int",f3VarBinding.getType().getQualifiedName());
		assertEquals("int",f4VarBinding.getType().getQualifiedName());
	}
}
