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
package p1;

public class Method1 extends Method1Super {
	
	
	
	public void main() {
		
		f4 = f1.m1(f2,f3);
		f1 = new Method1a();
		
		f5 = f4;
		f5 = f1.m2(f6,f7);
		
		Object o = new Object;
		f8 = f1.m2(o,f9);
		
	}
}

public class Method1a {
	public int m1(Object p1, double p2) {
		return 0;
	}
	
	public int m2(Object p1, Object p2) {
		
	}
	
	public boolean m2(String p1, String p2) {
		
	}
}