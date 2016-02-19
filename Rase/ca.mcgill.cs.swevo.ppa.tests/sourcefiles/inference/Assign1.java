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

public class Assign1 extends Assign1Super {
	
	public void main() {
		a = "Hello World";
		
		Assign1a local1 = null;
		
		local1 = b;
		Assign1a local1b = b2;
		
		String local2;
		
		local2 = "";
		
		a5.b5.c5.d5 = "Hello World";
		
		a5.b5 = new Assign1a();
		
		e5 = new Assign1Package();
		e6 = new Assign1Unknown();
		e5 = e6;
		e7 = new Assign1();
		e8 = new Assign1Package();
		e7 = e8;
		
	}
	
}

class Assign1Package extends Assign1Super2 {
	
}