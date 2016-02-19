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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.PPATypeBindingOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.core.JavaProject;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.swevo.ppa.PPAASTUtil;
import ca.mcgill.cs.swevo.ppa.tests.AnonymousVisitor;
import ca.mcgill.cs.swevo.ppa.tests.NameVisitor;
import ca.mcgill.cs.swevo.ppa.tests.SetupTestUtil;

@SuppressWarnings("restriction")
public class PPABindingsUtilTest {

	private static CompilationUnit cu1;
	private static CompilationUnit cu2;
	private static CompilationUnit cu3;
	private static NameVisitor cu1Visitor;
	private static AnonymousVisitor cu2Visitor;
	private static NameVisitor cu3Visitor;
	private static PPATypeRegistry tRegistry;
	private static PPADefaultBindingResolver resolver;

	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/util", "p1",
					"B1.java");
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);
			tRegistry = new PPATypeRegistry((JavaProject) JavaCore.create(icu
					.getUnderlyingResource().getProject()));
			cu1 = (CompilationUnit) PPABindingsUtil.getCU(icu, tRegistry);
			cu1Visitor = new NameVisitor();
			cu1.accept(cu1Visitor);
			resolver = PPABindingsUtil.getResolver(cu1.getAST());

			file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/util", "p1", "B2.java");
			icu = (ICompilationUnit) JavaCore.create(file);

			cu2 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu2Visitor = new AnonymousVisitor();
			cu2.accept(cu2Visitor);

			file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/util", "p1", "B3.java");
			icu = (ICompilationUnit) JavaCore.create(file);

			cu3 = (CompilationUnit) PPABindingsUtil.getCU(icu);
			cu3Visitor = new NameVisitor();
			cu3.accept(cu3Visitor);

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
	public void testIsProblematicType() {
		ITypeBinding bBinding = (ITypeBinding) cu1Visitor.getNames().get("B1").get(0)
				.resolveBinding();
		ITypeBinding cBinding = (ITypeBinding) cu1Visitor.getNames().get("C").get(0)
				.resolveBinding();
		ITypeBinding dBinding = (ITypeBinding) cu1Visitor.getNames().get("D").get(0)
				.resolveBinding();
		assertFalse(PPABindingsUtil.isProblemType(bBinding));
		assertFalse(PPABindingsUtil.isProblemType(cBinding));
		assertFalse(PPABindingsUtil.isProblemType(dBinding));
	}

	@Test
	public void testIsMissingType() {
		ITypeBinding bBinding = (ITypeBinding) cu1Visitor.getNames().get("B1").get(0)
				.resolveBinding();
		ITypeBinding cBinding = (ITypeBinding) cu1Visitor.getNames().get("C").get(0)
				.resolveBinding();
		ITypeBinding dBinding = (ITypeBinding) cu1Visitor.getNames().get("D").get(0)
				.resolveBinding();
		assertFalse(PPABindingsUtil.isMissingType(bBinding));
		assertFalse(PPABindingsUtil.isMissingType(cBinding));
		assertTrue(PPABindingsUtil.isMissingType(dBinding));
	}

	@Test
	public void testIsUnknownType() {
		ITypeBinding bBinding = (ITypeBinding) cu1Visitor.getNames().get("B1").get(0)
				.resolveBinding();
		ITypeBinding cBinding = (ITypeBinding) cu1Visitor.getNames().get("C").get(0)
				.resolveBinding();
		ITypeBinding dBinding = (ITypeBinding) cu1Visitor.getNames().get("D").get(0)
				.resolveBinding();
		assertFalse(PPABindingsUtil.isUnknownType(bBinding));
		assertFalse(PPABindingsUtil.isUnknownType(cBinding));
		assertFalse(PPABindingsUtil.isUnknownType(dBinding));
	}

	@Test
	public void testIsSuperMissingType() {
		ITypeBinding bBinding = (ITypeBinding) cu1Visitor.getNames().get("B1").get(0)
				.resolveBinding();
		ITypeBinding cBinding = (ITypeBinding) cu1Visitor.getNames().get("C").get(0)
				.resolveBinding();
		ITypeBinding dBinding = (ITypeBinding) cu1Visitor.getNames().get("D").get(0)
				.resolveBinding();
		assertFalse(PPABindingsUtil.isSuperMissingType(bBinding));
		assertTrue(PPABindingsUtil.isSuperMissingType(cBinding));
		assertFalse(PPABindingsUtil.isSuperMissingType(dBinding));
	}

	@Test
	public void testIsFullType() {
		ITypeBinding bBinding = (ITypeBinding) cu1Visitor.getNames().get("B1").get(0)
				.resolveBinding();
		ITypeBinding cBinding = (ITypeBinding) cu1Visitor.getNames().get("C").get(0)
				.resolveBinding();
		ITypeBinding dBinding = (ITypeBinding) cu1Visitor.getNames().get("D").get(0)
				.resolveBinding();
		assertTrue(PPABindingsUtil.isFullType(bBinding));
		assertFalse(PPABindingsUtil.isFullType(cBinding));
		assertFalse(PPABindingsUtil.isFullType(dBinding));
	}

	@Test
	public void testIsConventionalClassName() {
		String n1 = "Animal";
		String n2 = "ANIMAL";
		String n3 = "animal";
		assertTrue(PPABindingsUtil.isConventionalClassName(n1));
		assertFalse(PPABindingsUtil.isConventionalClassName(n2));
		assertFalse(PPABindingsUtil.isConventionalClassName(n3));
	}

	@Test
	public void testIsComplexName() {
		String n1 = "ca.mcgill.A";
		String n2 = "ca";
		assertTrue(PPABindingsUtil.isComplexName(n1));
		assertFalse(PPABindingsUtil.isComplexName(n2));
	}

	@Test
	public void testGetSimpleName() {
		String n1 = "ca.mcgill.A";
		String n2 = "ca.mcgill.swevo";
		String n3 = "ca";

		assertEquals(PPABindingsUtil.getSimpleName(n1), "A");
		assertEquals(PPABindingsUtil.getSimpleName(n2), "swevo");
		assertEquals(PPABindingsUtil.getSimpleName(n3), "ca");
	}

	@Test
	public void testGetPackage() {
		String n1 = "ca.mcgill.A";
		String n2 = "ca.mcgill.swevo.*";
		String n3 = "ca";
		assertEquals(PPABindingsUtil.getPackage(n1), "ca.mcgill");
		assertEquals(PPABindingsUtil.getPackage(n2), "ca.mcgill.swevo");
		assertNull(PPABindingsUtil.getPackage(n3));
	}

	@Test
	public void testGetArrayFromName() {
		String p1 = "ca.mcgill.swevo";
		char[][] p = PPABindingsUtil.getArrayFromName(p1);
		assertEquals(p.length, 3);
		assertEquals(new String(p[0]), "ca");
		assertEquals(new String(p[1]), "mcgill");
		assertEquals(new String(p[2]), "swevo");
	}

	@Test
	public void testIsStarImport() {
		String p1 = "ca.mcgill.*";
		String p2 = "ca.mcgill.Swevo";

		assertTrue(PPABindingsUtil.isStarImport(p1));
		assertFalse(PPABindingsUtil.isStarImport(p2));
	}

	@Test
	public void testGetFirstFieldContainerMissingSuperType1() {
		TypeDeclaration firstTypeNode = (TypeDeclaration) cu2.types().get(0);
		MethodDeclaration firstMethod = ((TypeDeclaration) firstTypeNode).getMethods()[0];
		ASTNode node1 = cu2Visitor.getAnons().get(firstMethod).get(0);
		ASTNode node2 = cu2Visitor.getAnons().get(firstMethod).get(1);
		TypeDeclaration secondTypeNode = (TypeDeclaration) firstTypeNode.getTypes()[0];

		// Null for null.
		assertNull(PPABindingsUtil.getFirstFieldContainerMissingSuperType(null));
		// B2 does not have any missing type
		assertNull(PPABindingsUtil.getFirstFieldContainerMissingSuperType(firstTypeNode));
		// Same for runnable
		assertNull(PPABindingsUtil.getFirstFieldContainerMissingSuperType(node1));
		// Same for B2a
		assertNull(PPABindingsUtil.getFirstFieldContainerMissingSuperType(secondTypeNode));
		// But B2c is not known!
		assertEquals(PPABindingsUtil.getFirstFieldContainerMissingSuperType(node2).getQualifiedName(),
				"p1.B2c");
	}

	@Test
	public void testGetFirstFieldContainerMissingSuperType2() {
		TypeDeclaration firstTypeNode = (TypeDeclaration) cu2.types().get(1);
		MethodDeclaration firstMethod = ((TypeDeclaration) firstTypeNode).getMethods()[0];
		ASTNode node1 = cu2Visitor.getAnons().get(firstMethod).get(0);
		ASTNode node2 = cu2Visitor.getAnons().get(firstMethod).get(1);
		TypeDeclaration secondTypeNode = (TypeDeclaration) firstTypeNode.getTypes()[0];
		TypeDeclaration thirdTypeNode = (TypeDeclaration) firstTypeNode.getTypes()[1];

		// B2b has a missing type (extend)
		assertEquals(PPABindingsUtil.getFirstFieldContainerMissingSuperType(firstTypeNode)
				.getQualifiedName(), "p1.B2z");
		// Same for runnable
		assertEquals(PPABindingsUtil.getFirstFieldContainerMissingSuperType(node1).getQualifiedName(),
				"p1.B2z");
		// Anon is unknown (B2d)
		assertEquals(PPABindingsUtil.getFirstFieldContainerMissingSuperType(node2).getQualifiedName(),
				"p1.B2d");
		// B2e has a missing type (container)
		assertEquals(PPABindingsUtil.getFirstFieldContainerMissingSuperType(secondTypeNode)
				.getQualifiedName(), "p1.B2z");
		// B2f has a missing type (extend)
		assertEquals(PPABindingsUtil.getFirstFieldContainerMissingSuperType(thirdTypeNode)
				.getQualifiedName(), "p1.B2g");
	}

	@Test
	public void testGetFirstFirstContainerMissingSuperType3() {
		TypeDeclaration firstTypeNode = (TypeDeclaration) cu2.types().get(2);
		TypeDeclaration secondTypeNode = (TypeDeclaration) firstTypeNode.getTypes()[0];

		// But B2h has a missing type (implements)
		assertEquals(PPABindingsUtil.getFirstFieldContainerMissingSuperType(firstTypeNode)
				.getQualifiedName(), "p1.B2i");
		// But B2j has a missing type (container)
		assertEquals(PPABindingsUtil.getFirstFieldContainerMissingSuperType(secondTypeNode)
				.getQualifiedName(), "p1.B2i");
	}

	@Test
	public void testGetFirstFieldContainerMissingSuperType4() {
		SimpleName sName2 = (SimpleName) cu3Visitor.getNames().get("e").get(0);
		SimpleName sName4 = (SimpleName) cu3Visitor.getNames().get("g").get(0);
		SimpleName sName5 = (SimpleName) cu3Visitor.getNames().get("i").get(0);
		SimpleName sName6 = (SimpleName) cu3Visitor.getNames().get("k").get(0);
		SimpleName sName7 = (SimpleName) cu3Visitor.getNames().get("l").get(0);

		ITypeBinding binding = PPABindingsUtil.getFirstFieldContainerMissingSuperType(PPAASTUtil
				.getFieldContainer(sName2, false, false));
		assertEquals("p1.B3a", binding.getQualifiedName());

		binding = PPABindingsUtil.getFirstFieldContainerMissingSuperType(PPAASTUtil.getFieldContainer(sName4,
				false, false));
		assertEquals("p1.B3a", binding.getQualifiedName());

		binding = PPABindingsUtil.getFirstFieldContainerMissingSuperType(PPAASTUtil.getFieldContainer(sName5,
				false, false));
		assertEquals("p1.B3d", binding.getQualifiedName());

		binding = PPABindingsUtil.getFirstFieldContainerMissingSuperType(PPAASTUtil.getFieldContainer(sName6,
				false, false));
		assertEquals("p1.B3c", binding.getQualifiedName());

		binding = PPABindingsUtil.getFirstFieldContainerMissingSuperType(PPAASTUtil.getFieldContainer(sName7,
				false, false));
		assertEquals("p1.B3a", binding.getQualifiedName());
	}



	@Test
	public void testIsPrimitive() {
		String name1 = "String";
		String name2 = "char";
		String name3 = "double";

		assertFalse(PPABindingsUtil.isPrimitiveName(name1));
		assertTrue(PPABindingsUtil.isPrimitiveName(name2));
		assertTrue(PPABindingsUtil.isPrimitiveName(name3));
	}

	@Test
	public void testIsNullType() {
		TypeBinding tBinding1 = BaseTypeBinding.NULL;
		TypeBinding tBinding2 = BaseTypeBinding.DOUBLE;

		assertTrue(PPABindingsUtil.isNullType(tBinding1));
		assertFalse(PPABindingsUtil.isNullType(tBinding2));
	}

	@Test
	public void testCompatibleTypes() {
		ITypeBinding unknown = tRegistry.getUnknownBinding(resolver);
		ITypeBinding string = tRegistry.getTypeBinding(cu1, "java.lang.String", resolver, false, new PPATypeBindingOptions());
		ITypeBinding object = tRegistry.getTypeBinding(cu1, "java.lang.Object", resolver, false, new PPATypeBindingOptions());
		ITypeBinding intType = tRegistry.getTypeBinding(cu1, "int", resolver, false, new PPATypeBindingOptions());
		ITypeBinding booleanType = tRegistry.getTypeBinding(cu1, "boolean", resolver, false, new PPATypeBindingOptions());
		ITypeBinding b1 = tRegistry.getTypeBinding(cu1, "p1.B1", resolver, false, new PPATypeBindingOptions());
		ITypeBinding c = tRegistry.getTypeBinding(cu1, "p1.C", resolver, false, new PPATypeBindingOptions());
		ITypeBinding d = tRegistry.getTypeBinding(cu1, "p1.D", resolver, false, new PPATypeBindingOptions());
		ITypeBinding voidType = tRegistry.getPrimitiveBinding("void", resolver);

		// Unknown test
		assertTrue(PPABindingsUtil.compatibleTypes(unknown, unknown));
		assertTrue(PPABindingsUtil.compatibleTypes(unknown, string));

		// Test booleans
		assertTrue(PPABindingsUtil.compatibleTypes(booleanType, booleanType));

		// Test primitives
		assertTrue(PPABindingsUtil.compatibleTypes(intType, intType));

		// Test primitive and booleans
		assertFalse(PPABindingsUtil.compatibleTypes(intType, booleanType));

		// Test primitive with type
		assertFalse(PPABindingsUtil.compatibleTypes(intType, b1));
		assertFalse(PPABindingsUtil.compatibleTypes(string, intType));

		// Formal known, but not the other
		assertTrue(PPABindingsUtil.compatibleTypes(b1, c));
		assertFalse(PPABindingsUtil.compatibleTypes(c, b1));
		assertFalse(PPABindingsUtil.compatibleTypes(b1, intType));
		assertFalse(PPABindingsUtil.compatibleTypes(string, d));
		assertTrue(PPABindingsUtil.compatibleTypes(object, d));

		// Subtype test
		assertTrue(PPABindingsUtil.compatibleTypes(object, string));
		assertFalse(PPABindingsUtil.compatibleTypes(string, object));
		assertFalse(PPABindingsUtil.compatibleTypes(b1, string));

		// Void test
		assertTrue(PPABindingsUtil.compatibleTypes(voidType, voidType));
		assertTrue(PPABindingsUtil.compatibleTypes(voidType, unknown));
		assertFalse(PPABindingsUtil.compatibleTypes(voidType, intType));
		assertFalse(PPABindingsUtil.compatibleTypes(voidType, object));
		assertFalse(PPABindingsUtil.compatibleTypes(voidType, d));
		assertFalse(PPABindingsUtil.compatibleTypes(d, voidType));
	}

	@Test
	public void testFindAcceptableMethodsName() {
		ITypeBinding system = tRegistry.getTypeBinding(cu1, "java.lang.System", resolver, false, new PPATypeBindingOptions());
		ITypeBinding unknown = tRegistry.getUnknownBinding(resolver);
		ITypeBinding b1b = tRegistry.getTypeBinding(cu1, "p1.B1b", resolver, false, new PPATypeBindingOptions());

		List<IMethodBinding> m1s = PPABindingsUtil.findAcceptableMethods("m1", 2, b1b);
		assertEquals(3, m1s.size());

		List<IMethodBinding> m2s = PPABindingsUtil.findAcceptableMethods("m2", 1, b1b);
		assertEquals(1, m2s.size());

		m2s = PPABindingsUtil.findAcceptableMethods("m2", 2, b1b);
		assertEquals(0, m2s.size());
		
		m2s = PPABindingsUtil.findAcceptableMethods("m2", 2, unknown);
		assertEquals(0, m2s.size());
		
		List<IMethodBinding> methods =  PPABindingsUtil.findAcceptableMethods("currentTimeMillis", 0, system);
		assertEquals(1, methods.size());
	}

	@Test
	public void testFindAcceptableMethodsParams() {
		ITypeBinding unknown = tRegistry.getUnknownBinding(resolver);
		ITypeBinding string = tRegistry.getTypeBinding(cu1, "java.lang.String", resolver, false, new PPATypeBindingOptions());
		ITypeBinding intType = tRegistry.getTypeBinding(cu1, "int", resolver, false, new PPATypeBindingOptions());
		ITypeBinding b1b = tRegistry.getTypeBinding(cu1, "p1.B1b", resolver, false, new PPATypeBindingOptions());
		
		ITypeBinding[] typeBindings1 = {unknown, unknown};
		ITypeBinding[] typeBindings2 = {string, string};
		ITypeBinding[] typeBindings3 = {b1b, intType};
		
		List<IMethodBinding> methods = PPABindingsUtil.findAcceptableMethods("m4", typeBindings1, b1b);
		assertEquals(2,methods.size());
		
		methods = PPABindingsUtil.findAcceptableMethods("m4", typeBindings2, b1b);
		assertEquals(1,methods.size());
		
		methods = PPABindingsUtil.findAcceptableMethods("m4", typeBindings3, b1b);
		assertEquals(1,methods.size());
	}

	@Test
	public void testFindAcceptableMethodsReturn() {
		ITypeBinding unknown = tRegistry.getUnknownBinding(resolver);
		ITypeBinding string = tRegistry.getTypeBinding(cu1, "java.lang.String", resolver, false, new PPATypeBindingOptions());
		ITypeBinding intType = tRegistry.getTypeBinding(cu1, "int", resolver, false, new PPATypeBindingOptions());
		ITypeBinding b1b = tRegistry.getTypeBinding(cu1, "p1.B1b", resolver, false, new PPATypeBindingOptions());
		
		ITypeBinding[] typeBindings1 = {unknown};
		
		List<IMethodBinding> methods = PPABindingsUtil.findAcceptableMethods("m3", typeBindings1, intType, b1b);
		assertEquals(1,methods.size());
		assertEquals("int",methods.get(0).getReturnType().getQualifiedName());
		
		methods = PPABindingsUtil.findAcceptableMethods("m3", typeBindings1, unknown, b1b);
		assertEquals(2,methods.size());
		
		methods = PPABindingsUtil.findAcceptableMethods("m3", typeBindings1, string, b1b);
		assertEquals(0,methods.size());
	}

	@Test
	public void testFilterMethods() {
		ITypeBinding b1b = tRegistry.getTypeBinding(cu1, "p1.B1b", resolver, false, new PPATypeBindingOptions());
		ITypeBinding unknown = tRegistry.getUnknownBinding(resolver);
		
		List<IMethodBinding> m1s = PPABindingsUtil.findAcceptableMethods("m1", 2, b1b);
		assertEquals(3, m1s.size());
		List<IMethodBinding> m1sFiltered = PPABindingsUtil.filterMethods(m1s, false);
		assertEquals(1, m1sFiltered.size());
		assertEquals("p1.B1b", m1sFiltered.get(0).getDeclaringClass().getQualifiedName());
		
		ITypeBinding[] typeBindings1 = {unknown, unknown};
		List<IMethodBinding> m4s = PPABindingsUtil.findAcceptableMethods("m4", typeBindings1, b1b);
		m1sFiltered = PPABindingsUtil.filterMethods(m4s, false);
		assertEquals(2,m4s.size());
		assertEquals(2, m1sFiltered.size());

	}
}
