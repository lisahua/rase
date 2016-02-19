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

import java.io.File;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaProject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import ca.mcgill.cs.swevo.ppa.tests.FileUtil;
import ca.mcgill.cs.swevo.ppa.tests.SetupTestUtil;

@SuppressWarnings("restriction")
public class PPATypeRegistry_FQNTest {

	private static CompilationUnit cu1;
	private static CompilationUnit cu2;
	private static CompilationUnit cu3;
	private static CompilationUnit cu4;
	private static CompilationUnit cu5;

	private static PPATypeRegistry typeRegistry;

	@BeforeClass
	public static void setupSuite() {
		String mainPath = null;
		Bundle ppaTestBundle = Platform.getBundle("ca.mcgill.cs.swevo.ppa.tests");
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();

			mainPath = FileLocator.toFileURL(ppaTestBundle.getEntry("sourcefiles/fqn")).getFile();
			typeRegistry = new PPATypeRegistry((JavaProject)javaProject);
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			parser.setSource(FileUtil.getContent(new File(mainPath, "A1.java")).toCharArray());
			cu1 = (CompilationUnit) parser.createAST(null);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			parser.setSource(FileUtil.getContent(new File(mainPath, "A2.java")).toCharArray());
			cu2 = (CompilationUnit) parser.createAST(null);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			parser.setSource(FileUtil.getContent(new File(mainPath, "A3.java")).toCharArray());
			cu3 = (CompilationUnit) parser.createAST(null);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			parser.setSource(FileUtil.getContent(new File(mainPath, "A4.java")).toCharArray());
			cu4 = (CompilationUnit) parser.createAST(null);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			parser.setSource(FileUtil.getContent(new File(mainPath, "A5.java")).toCharArray());
			cu5 = (CompilationUnit) parser.createAST(null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetFullName() {
		String fullName = "p1.p3.Type";
		String name = "Type";
		String javaName = "String";
		String primitiveName = "int";

		assertEquals(fullName, typeRegistry.getFullName(fullName, cu1));

		assertEquals("p1.Type", typeRegistry.getFullName(name, cu1));
		assertEquals("p1.Type", typeRegistry.getFullName(name, cu2));
		assertEquals("java.util.Collection", typeRegistry.getFullName("Collection", cu2));
		assertEquals("p2.p3.Type", typeRegistry.getFullName(name, cu3));
		assertEquals("Type", typeRegistry.getFullName(name, cu4));
		assertEquals(PPATypeRegistry.UNKNOWN_PACKAGE + ".Type", typeRegistry.getFullName(name, cu5));
		assertEquals("java.lang.String", typeRegistry.getFullName(javaName, cu1));
		assertEquals("p2.String", typeRegistry.getFullName(javaName, cu2));
		assertEquals("int", typeRegistry.getFullName(primitiveName, cu1));
		// To test the cache
		assertEquals("int", typeRegistry.getFullName(primitiveName, cu1));
	}

	@AfterClass
	public static void afterSuite() {
		typeRegistry.clear();
	}
	
	

}
