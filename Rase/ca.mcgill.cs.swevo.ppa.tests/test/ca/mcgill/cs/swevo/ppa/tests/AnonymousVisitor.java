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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class AnonymousVisitor extends ASTVisitor {
	private Map<MethodDeclaration,List<AnonymousClassDeclaration>> anons = new HashMap<MethodDeclaration, List<AnonymousClassDeclaration>>();
	
	private void addAnon(AnonymousClassDeclaration node) {
		MethodDeclaration key = getKey(node);
		if (key == null) {
			return;
		}
		List<AnonymousClassDeclaration> nameList = anons.get(key);
		if (nameList == null) {
			nameList = new ArrayList<AnonymousClassDeclaration>();
			anons.put(key,nameList);
		}
		nameList.add(node);
	}
	
	private MethodDeclaration getKey(AnonymousClassDeclaration node) {
		ASTNode parent = node.getParent();
		while (parent != null && !(parent instanceof MethodDeclaration)) {
			parent = parent.getParent();
		}
		
		MethodDeclaration mDeclaration = null;
		if (parent != null) {
			mDeclaration = (MethodDeclaration) parent;
		}
		
		return mDeclaration;
	}
	
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		addAnon(node);
		return super.visit(node);
	}

	public Map<MethodDeclaration, List<AnonymousClassDeclaration>> getAnons() {
		return anons;
	}
}
