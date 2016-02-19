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

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

public class NameVisitor extends ASTVisitor {

	private Map<String,List<Name>> names = new HashMap<String, List<Name>>();

	@Override
	public boolean visit(SimpleName node) {
		addName(node);
		return super.visit(node);
	}
	
	@Override
	public boolean visit(QualifiedName node) {
		addName(node);
		return super.visit(node);
	}



	private void addName(Name node) {
		String key = node.toString();
		List<Name> nameList = names.get(key);
		if (nameList == null) {
			nameList = new ArrayList<Name>();
			names.put(key,nameList);
		}
		nameList.add(node);
	}
	
	public Map<String, List<Name>> getNames() {
		return names;
	}
	
	
	
}
