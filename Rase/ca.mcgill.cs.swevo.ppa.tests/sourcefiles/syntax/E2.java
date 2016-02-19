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

public class E2 extends E2Super {
	
	public void main() {
		if (a1.b1.c1.d1 instanceof E2a) {
			
		}
		
		if (a1.b1 instanceof E2a) {
			
		}
		
		a3.b3.c3.hello();
		
		a2 = new a4.b4.c4() {
			
		}
		
		Z z1 = new Z();
		z1.z2 = z1.z3.z4;
		System.out.println("");
		E2a.b.c.e.g = "";
	
		CustomTestSetup wrapper = new CustomTestSetup() {
			protected void main1() {
				String[] arrayString = new String[2];
				boolean b10 = arrayString.length == 2;
			}
		};
	}
	
}

public class E2a {
	static E2b b;
}

public class E2b {
	static E2c c;
}

public class E2c extends E2d {
}