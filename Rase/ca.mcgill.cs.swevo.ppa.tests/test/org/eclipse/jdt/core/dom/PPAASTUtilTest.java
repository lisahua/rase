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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.swevo.ppa.PPAASTUtil;
import ca.mcgill.cs.swevo.ppa.tests.AnonymousVisitor;
import ca.mcgill.cs.swevo.ppa.tests.NameVisitor;
import ca.mcgill.cs.swevo.ppa.tests.SetupTestUtil;

@SuppressWarnings("restriction")
public class PPAASTUtilTest {
	private static CompilationUnit cu1;
	private static CompilationUnit cu2;
	private static CompilationUnit cu3;
	private static NameVisitor cu1Visitor;
	private static AnonymousVisitor cu2Visitor;
	private static NameVisitor cu3Visitor;
	private static PPATypeRegistry tRegistry;
//	private static PPADefaultBindingResolver resolver;

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
//			resolver = PPAASTUtil.getResolver(cu1.getAST());

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
	public void testGetContainer() {
		TypeDeclaration firstTypeNode = (TypeDeclaration) cu2.types().get(0);
		MethodDeclaration firstMethod = ((TypeDeclaration) firstTypeNode).getMethods()[0];
		ASTNode node = cu2Visitor.getAnons().get(firstMethod).get(0);
		TypeDeclaration secondTypeNode = (TypeDeclaration) firstTypeNode.getTypes()[0];
		assertNull(PPAASTUtil.getFieldContainer(cu2, true, false));
		assertNull(PPAASTUtil.getFieldContainer(firstTypeNode, true, false));
		assertEquals(PPAASTUtil.getFieldContainer(secondTypeNode, true, false), firstTypeNode);
		assertEquals(PPAASTUtil.getFieldContainer(node, true, false), firstTypeNode);
	}

	@Test
	public void testGetFieldContainer() {
		SimpleName sName1 = (SimpleName) cu3Visitor.getNames().get("d").get(0);
		SimpleName sName2 = (SimpleName) cu3Visitor.getNames().get("e").get(0);
		SimpleName sName3 = (SimpleName) cu3Visitor.getNames().get("f").get(0);
		SimpleName sName4 = (SimpleName) cu3Visitor.getNames().get("g").get(0);
		SimpleName sName5 = (SimpleName) cu3Visitor.getNames().get("y").get(0);
		SimpleName sName6 = (SimpleName) cu3Visitor.getNames().get("i").get(0);
	
		assertTrue(PPAASTUtil.getFieldContainer(sName1, true, true) instanceof SimpleName);
		assertEquals("d", PPAASTUtil.getFieldContainer(sName1, true, true).toString());
		assertTrue(PPAASTUtil.getFieldContainer(sName2, true, false) instanceof TypeDeclaration);
		assertTrue(PPAASTUtil.getFieldContainer(sName3, true, false) instanceof MethodInvocation);
		assertTrue(PPAASTUtil.getFieldContainer(sName4, true, false) instanceof TypeDeclaration);
		assertTrue(PPAASTUtil.getFieldContainer(sName5, true, false) instanceof Name);
		assertEquals("z", PPAASTUtil.getFieldContainer(sName5, true, false).toString());
		assertTrue(PPAASTUtil.getFieldContainer(sName6, true, false) instanceof AnonymousClassDeclaration);
	}

	@Test
	public void testGetFieldContainer2() {
		SimpleName sName1 = (SimpleName) cu3Visitor.getNames().get("d").get(0);
		SimpleName sName2 = (SimpleName) cu3Visitor.getNames().get("e").get(0);
		SimpleName sName3 = (SimpleName) cu3Visitor.getNames().get("f").get(0);
		SimpleName sName4 = (SimpleName) cu3Visitor.getNames().get("g").get(0);
		assertTrue(PPAASTUtil.getFieldContainer(sName1, false, false) instanceof TypeDeclaration);
		assertTrue(PPAASTUtil.getFieldContainer(sName1, true, false) instanceof QualifiedName);
		assertEquals("a.b.c", PPAASTUtil.getFieldContainer(sName1, true, false).toString());
		assertTrue(PPAASTUtil.getFieldContainer(sName1, false, false) instanceof TypeDeclaration);
		assertTrue(PPAASTUtil.getFieldContainer(sName2, false, false) instanceof TypeDeclaration);
		assertTrue(PPAASTUtil.getFieldContainer(sName3, false, false) instanceof MethodInvocation);
		assertTrue(PPAASTUtil.getFieldContainer(sName4, false, false) instanceof TypeDeclaration);
		// assertTrue(PPAUtil.getFieldContainer(sName5, false, false) instanceof
		// AnonymousClassDeclaration);
		// assertTrue(PPAUtil.getFieldContainer(sName5, false, true) instanceof
		// AnonymousClassDeclaration);
	}

	@Test
	public void testGetQualifierPlusName() {
		SimpleName sName1 = (SimpleName) cu3Visitor.getNames().get("a").get(0);
		SimpleName sName2 = (SimpleName) cu3Visitor.getNames().get("b").get(0);
		SimpleName sName3 = (SimpleName) cu3Visitor.getNames().get("c").get(0);
		SimpleName sName4 = (SimpleName) cu3Visitor.getNames().get("d").get(0);
	
		assertEquals("a", PPAASTUtil.getQualifierPlusName(sName1));
		assertEquals("a.b", PPAASTUtil.getQualifierPlusName(sName2));
		assertEquals("a.b.c", PPAASTUtil.getQualifierPlusName(sName3));
		assertEquals("a.b.c.d", PPAASTUtil.getQualifierPlusName(sName4));
	}

	@Test
	public void testGetFQNFromAnyName() {
		SimpleName sName1 = (SimpleName) cu3Visitor.getNames().get("a").get(0);
		SimpleName sName2 = (SimpleName) cu3Visitor.getNames().get("b").get(0);
		SimpleName sName3 = (SimpleName) cu3Visitor.getNames().get("c").get(0);
		SimpleName sName4 = (SimpleName) cu3Visitor.getNames().get("d").get(0);
		QualifiedName qName1 = (QualifiedName) cu3Visitor.getNames().get("a.b").get(0);
		QualifiedName rootName = (QualifiedName) cu3Visitor.getNames().get("a.b.c.d").get(0);
		String fqn = rootName.getFullyQualifiedName();
		assertEquals(fqn, PPAASTUtil.getFQNFromAnyName(sName1));
		assertEquals(fqn, PPAASTUtil.getFQNFromAnyName(sName2));
		assertEquals(fqn, PPAASTUtil.getFQNFromAnyName(sName3));
		assertEquals(fqn, PPAASTUtil.getFQNFromAnyName(sName4));
		assertEquals(fqn, PPAASTUtil.getFQNFromAnyName(qName1));
	}
	
	@Test
	public void testIsContainer() {
		TypeDeclaration firstTypeNode = (TypeDeclaration) cu2.types().get(0);
		MethodDeclaration firstMethod = ((TypeDeclaration) firstTypeNode).getMethods()[0];
		ASTNode node = cu2Visitor.getAnons().get(firstMethod).get(0);
		assertFalse(PPAASTUtil.isFieldContainer(cu2, true));
		assertTrue(PPAASTUtil.isFieldContainer(firstTypeNode, true));
		assertFalse(PPAASTUtil.isFieldContainer(firstMethod, true));
		assertTrue(PPAASTUtil.isFieldContainer(node, true));
	}
	
	@Test
	public void testGetFQNFromAnon() {
		TypeDeclaration firstTypeNode = (TypeDeclaration) cu2.types().get(0);
		MethodDeclaration firstMethod = ((TypeDeclaration) firstTypeNode).getMethods()[0];
		ASTNode node1 = cu2Visitor.getAnons().get(firstMethod).get(0);
		ASTNode node2 = cu2Visitor.getAnons().get(firstMethod).get(1);
		ASTNode node3 = cu2Visitor.getAnons().get(firstMethod).get(2);
		assertEquals(PPAASTUtil.getNameFromAnon((AnonymousClassDeclaration) node1), "Runnable");
		assertEquals(PPAASTUtil.getNameFromAnon((AnonymousClassDeclaration) node2), "B2c");
		assertEquals(PPAASTUtil.getNameFromAnon((AnonymousClassDeclaration) node3),
				"java.lang.Runnable");
	}
	
	@Test
	public void testGetSpecificParentType() {
		SimpleName sName1 = (SimpleName) cu3Visitor.getNames().get("a").get(0);
		SimpleName sName2 = (SimpleName) cu3Visitor.getNames().get("l").get(0);

		assertTrue(PPAASTUtil.getSpecificParentType(sName1, ASTNode.COMPILATION_UNIT) instanceof CompilationUnit);
		assertTrue(PPAASTUtil.getSpecificParentType(sName1, ASTNode.METHOD_DECLARATION) instanceof MethodDeclaration);
		assertTrue(PPAASTUtil.getSpecificParentType(sName2, ASTNode.ANONYMOUS_CLASS_DECLARATION) instanceof AnonymousClassDeclaration);
		assertTrue(PPAASTUtil.getSpecificParentType(sName1, ASTNode.TYPE_DECLARATION) instanceof TypeDeclaration);
	}
}
