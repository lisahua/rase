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

import static org.junit.Assert.assertEquals;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.lookup.PPATypeBindingOptions;
import org.eclipse.jdt.internal.core.JavaProject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.swevo.ppa.tests.SetupTestUtil;

@SuppressWarnings("restriction")
public class PPATypeRegistry_FieldTest {
	private static CompilationUnit cu1;
	private static PPATypeRegistry tRegistry;
	private static ICompilationUnit icu;
	private static PPADefaultBindingResolver resolver;
	private static ITypeBinding stringBinding;
	private static ITypeBinding unknownBinding;
	
	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/fields", "p1",
					"D1.java");
			icu = (ICompilationUnit) JavaCore.create(file);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Before
	public void setup() {
		try {
			tRegistry = new PPATypeRegistry((JavaProject) JavaCore.create(icu
					.getUnderlyingResource().getProject()));
			cu1 = (CompilationUnit) PPABindingsUtil.getCU(icu, tRegistry);
			resolver = PPABindingsUtil.getResolver(cu1.getAST());
			stringBinding = tRegistry.getTypeBinding(cu1, "String", resolver, false, new PPATypeBindingOptions());
			unknownBinding = tRegistry.getUnknownBinding(resolver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetField() {
		ITypeBinding d1Type = ((TypeDeclaration)cu1.types().get(0)).resolveBinding();
		IVariableBinding fBinding1 = tRegistry.getFieldBinding("f1", d1Type, stringBinding, resolver);
		assertEquals(fBinding1.getName(),"f1");
		
		ITypeBinding d2Type = tRegistry.getTypeBinding(cu1, "D1a", resolver, false, new PPATypeBindingOptions());
		IVariableBinding fBinding2 = tRegistry.getFieldBinding("f2", d2Type, unknownBinding, resolver);
		assertEquals(fBinding2.getName(),"f2");
		
		// Cache hit?
		IVariableBinding fBinding3 = tRegistry.getFieldBinding("f2", d2Type, unknownBinding, resolver);
		assertEquals(fBinding3.getName(),"f2");
		
		// Field in hierarchy
		ITypeBinding d1zType = ((TypeDeclaration)cu1.types().get(2)).resolveBinding();
		IVariableBinding fBinding4 = tRegistry.getFieldBinding("f3", d1zType, stringBinding, resolver);
		assertEquals(fBinding4.getName(),"f3");
		assertEquals(fBinding4.getDeclaringClass().getName(),"D1w");
	}
}
