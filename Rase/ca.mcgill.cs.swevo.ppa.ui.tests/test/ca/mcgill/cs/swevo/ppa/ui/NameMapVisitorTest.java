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
package ca.mcgill.cs.swevo.ppa.ui;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import ca.mcgill.cs.swevo.ppa.PPAOptions;

public class NameMapVisitorTest {
	private String snippet4;
	private File snippet4f;

	@Before
	public void setUp() {
		String mainPath = null;
		try {
			Bundle ppaTestBundle = Platform.getBundle("ca.mcgill.cs.swevo.ppa.ui.tests");
			mainPath = FileLocator.toFileURL(ppaTestBundle.getEntry("sourcefiles/snippets"))
					.getFile();
			snippet4f = new File(mainPath, "java4.java");
			FileInputStream fis = new FileInputStream(snippet4f);
			snippet4 = IOUtils.toString(fis);
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@After
	public void tearDown() {
		PPAUtil.cleanUpAll();
	}
	
	@Test
	public void testAnnotation() {
		CompilationUnit cu = PPAUtil.getCU(snippet4, new PPAOptions());
		NameMapVisitor visitor = new NameMapVisitor(true, true, true);
		cu.accept(visitor);
		visitor.print();
	}
	
}
