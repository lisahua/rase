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
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

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
public class PPATypeRegistry_MethodTest {
	private static CompilationUnit cu1;
	private static PPATypeRegistry tRegistry;
	private static ICompilationUnit icu;
	private static PPADefaultBindingResolver resolver;
	private static ITypeBinding stringBinding;
	private static ITypeBinding unknownBinding;
	private static ITypeBinding myType;

	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/methods", "p1",
					"G1.java");
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
			myType = tRegistry.getTypeBinding(cu1, "MyType", resolver, false, new PPATypeBindingOptions());
			unknownBinding = tRegistry.getUnknownBinding(resolver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetUnknownConstructor() {
		String voidName = "void";
		IMethodBinding cBindingString1 = tRegistry.getUnknownConstructorBinding(stringBinding, 0, resolver);
		IMethodBinding cBindingString2 = tRegistry.getUnknownConstructorBinding(stringBinding, 5, resolver);
		IMethodBinding cBindingUnknown1 = tRegistry.getUnknownConstructorBinding(unknownBinding, 2, resolver);
		IMethodBinding cBindingMyType1 = tRegistry.getUnknownConstructorBinding(myType, 0, resolver);
		IMethodBinding cBindingMyType2 = tRegistry.getUnknownConstructorBinding(myType, 2, resolver);

		assertNotNull(cBindingString1);
		assertNotNull(cBindingString2);
		assertNotNull(cBindingUnknown1);
		assertNotNull(cBindingMyType1);
		assertNotNull(cBindingMyType2);

		assertEquals(voidName, cBindingString1.getReturnType().getName());
		assertEquals(voidName, cBindingString2.getReturnType().getName());
		assertEquals(voidName, cBindingUnknown1.getReturnType().getName());
		assertEquals(voidName, cBindingMyType1.getReturnType().getName());
		assertEquals(voidName, cBindingMyType2.getReturnType().getName());

		assertEquals(0, cBindingString1.getParameterTypes().length);
		assertEquals(5, cBindingString2.getParameterTypes().length);
		assertEquals(2, cBindingUnknown1.getParameterTypes().length);
		assertEquals(0, cBindingMyType1.getParameterTypes().length);
		assertEquals(2, cBindingMyType2.getParameterTypes().length);

		assertTrue(stringBinding.isEqualTo(cBindingString1.getDeclaringClass()));
		assertTrue(stringBinding.isEqualTo(cBindingString2.getDeclaringClass()));
		assertTrue(unknownBinding.isEqualTo(cBindingUnknown1.getDeclaringClass()));
		assertTrue(myType.isEqualTo(cBindingMyType1.getDeclaringClass()));
		assertTrue(myType.isEqualTo(cBindingMyType2.getDeclaringClass()));

		assertTrue(unknownBinding.isEqualTo(cBindingString2.getParameterTypes()[0]));
		assertTrue(unknownBinding.isEqualTo(cBindingUnknown1.getParameterTypes()[1]));
		assertTrue(unknownBinding.isEqualTo(cBindingMyType2.getParameterTypes()[1]));
	}

	@Test
	public void testCreateUnknownMethod() {
		IMethodBinding cBindingString1 = tRegistry.getUnknownMethodBinding("test".toCharArray(), 0,
				resolver);
		IMethodBinding cBindingUnknown1 = tRegistry.getUnknownMethodBinding("test".toCharArray(),
				2, resolver);
		IMethodBinding cBindingMyType2 = tRegistry.getUnknownMethodBinding("test".toCharArray(), 2,
				resolver);

		assertNotNull(cBindingString1);
		assertNotNull(cBindingUnknown1);
		assertNotNull(cBindingMyType2);

		assertTrue(unknownBinding.isEqualTo(cBindingString1.getReturnType()));
		assertTrue(unknownBinding.isEqualTo(cBindingString1.getReturnType()));
		assertTrue(unknownBinding.isEqualTo(cBindingString1.getReturnType()));

		assertEquals(0, cBindingString1.getParameterTypes().length);
		assertEquals(2, cBindingUnknown1.getParameterTypes().length);
		assertEquals(2, cBindingMyType2.getParameterTypes().length);

		assertTrue(unknownBinding.isEqualTo(cBindingString1.getDeclaringClass()));
		assertTrue(unknownBinding.isEqualTo(cBindingUnknown1.getDeclaringClass()));
		assertTrue(unknownBinding.isEqualTo(cBindingMyType2.getDeclaringClass()));

		assertTrue(unknownBinding.isEqualTo(cBindingUnknown1.getParameterTypes()[0]));
		assertTrue(unknownBinding.isEqualTo(cBindingMyType2.getParameterTypes()[1]));
	}

	// @Test
	// public void testGetField() {
	// ITypeBinding d1Type = ((TypeDeclaration)cu1.types().get(0)).resolveBinding();
	// IVariableBinding fBinding1 = tRegistry.getFieldBinding("f1", d1Type, stringBinding,
	// resolver);
	// assertEquals(fBinding1.getName(),"f1");
	//		
	// ITypeBinding d2Type = tRegistry.getTypeBinding(cu1, "D1a", resolver);
	// IVariableBinding fBinding2 = tRegistry.getFieldBinding("f2", d2Type, unknownBinding,
	// resolver);
	// assertEquals(fBinding2.getName(),"f2");
	//		
	// // Cache hit?
	// IVariableBinding fBinding3 = tRegistry.getFieldBinding("f2", d2Type, unknownBinding,
	// resolver);
	// assertEquals(fBinding3.getName(),"f2");
	//		
	// // Field in hierarchy
	// ITypeBinding d1zType = ((TypeDeclaration)cu1.types().get(2)).resolveBinding();
	// IVariableBinding fBinding4 = tRegistry.getFieldBinding("f3", d1zType, stringBinding,
	// resolver);
	// assertEquals(fBinding4.getName(),"f3");
	// assertEquals(fBinding4.getDeclaringClass().getName(),"D1w");
	// }
}
