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
package ca.mcgill.cs.swevo.ppa.tests;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.osgi.framework.Bundle;

public class SetupTestUtil {

	public static IJavaProject setupJavaProject() throws Exception {
		IJavaProject javaProject = null;
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("TestPPAProject");
		if (!project.exists()) {
			NewJavaProjectWizardPageOne pageOne = new NewJavaProjectWizardPageOne();
			NewJavaProjectWizardPageTwo pageTwo = new NewJavaProjectWizardPageTwo(pageOne);
			pageOne.setProjectName("TestPPAProject");
			pageTwo.performFinish(new NullProgressMonitor());
			javaProject = pageTwo.getJavaProject();
		} else {
			javaProject = JavaCore.create(project);
		}

		return javaProject;
	}

	public static IFile copyJavaSourceFile(IProject project, String basePath, String packageName, String fileName) throws Exception {
		IFile file = null;

		String mainPath = null;
		Bundle ppaTestBundle = Platform.getBundle("ca.mcgill.cs.swevo.ppa.tests");
		mainPath = FileLocator.toFileURL(ppaTestBundle.getEntry(basePath)).getFile();
		IFolder p1Folder = project.getFolder("src").getFolder(packageName);
		if (!p1Folder.exists()) {
			p1Folder.create(true, true, new NullProgressMonitor());
		}

		file = p1Folder.getFile(fileName);
		if (!file.exists()) {
			file.create(new FileInputStream(new File(mainPath, fileName)), IFile.FORCE,
					new NullProgressMonitor());
		} else {
			file.setContents(new FileInputStream(new File(mainPath, fileName)), IFile.FORCE,
					new NullProgressMonitor());
		}

		return file;
	}

}
