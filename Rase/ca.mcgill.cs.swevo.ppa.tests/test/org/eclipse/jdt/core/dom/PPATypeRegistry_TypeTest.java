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
import static org.junit.Assert.assertNull;

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
public class PPATypeRegistry_TypeTest {

	private static CompilationUnit cu1;
	private static PPATypeRegistry tRegistry;
	private static ICompilationUnit icu;
	private static PPADefaultBindingResolver resolver;

	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/types", "p1",
					"C1.java");
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetTypeBinding() {
		ITypeBinding tBinding1 = tRegistry.getTypeBinding(cu1, "String", resolver, false, new PPATypeBindingOptions());
		ITypeBinding tBinding2 = tRegistry.getTypeBinding(cu1, "Foo", resolver, false, new PPATypeBindingOptions());
		ITypeBinding tBinding3 = tRegistry.getTypeBinding(cu1, "ca.mcgill.Bar", resolver, false, new PPATypeBindingOptions());
		ITypeBinding tBinding4 = tRegistry.getTypeBinding(cu1, PPATypeRegistry.UNKNOWN_CLASS_FQN,
				resolver, false, new PPATypeBindingOptions());
		ITypeBinding tBinding5 = tRegistry.getTypeBinding(cu1, "boolean",
				resolver, false, new PPATypeBindingOptions());
		// To test cache
		ITypeBinding tBinding6 = tRegistry.getTypeBinding(cu1, "boolean",
				resolver, false, new PPATypeBindingOptions());
		// To test void
		ITypeBinding tBinding7 = tRegistry.getTypeBinding(cu1, "void",
				resolver, false, new PPATypeBindingOptions());

		assertEquals(tBinding1.getQualifiedName(), "java.lang.String");
		assertEquals(tBinding2.getQualifiedName(), "p1.Foo");
		assertEquals(tBinding3.getQualifiedName(), "ca.mcgill.Bar");
		assertEquals(tBinding4.getQualifiedName(), PPATypeRegistry.UNKNOWN_CLASS_FQN);
		assertEquals(tBinding5.getQualifiedName(), "boolean");
		assertEquals(tBinding6.getQualifiedName(), "boolean");
		assertEquals(tBinding7.getQualifiedName(), "void");
	}
	
	@Test
	public void testGetPrimitive() {
		ITypeBinding tBinding1 = tRegistry.getPrimitiveBinding("float", resolver);
		ITypeBinding tBinding2 = tRegistry.getPrimitiveBinding("long", resolver);
		ITypeBinding tBinding3 = tRegistry.getPrimitiveBinding("double", resolver);
		ITypeBinding tBinding4 = tRegistry.getPrimitiveBinding("String", resolver);
		assertEquals(tBinding1.getQualifiedName(), "float");
		assertEquals(tBinding2.getQualifiedName(), "long");
		assertEquals(tBinding3.getQualifiedName(), "double");
		assertNull(tBinding4);
	}
	
	@Test
	public void testGetUnknown() {
		ITypeBinding u1 = tRegistry.getUnknownBinding(resolver);
		ITypeBinding u2 = tRegistry.getUnknownBinding(resolver);
		assertEquals(u1.getQualifiedName(), PPATypeRegistry.UNKNOWN_CLASS_FQN);
		assertEquals(u2.getQualifiedName(), PPATypeRegistry.UNKNOWN_CLASS_FQN);
		
	}

}
