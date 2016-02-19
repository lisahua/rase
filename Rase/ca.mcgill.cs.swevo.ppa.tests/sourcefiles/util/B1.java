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

public class B1 {


}

class C extends D {
	public void main() {
	}
}

interface B1c {
	Object m1(Object p1, Object p2);
	
}

class B1a {
	Object m4(Object p1, Object p2) {
		return null;
	}
	
	Object m3(Object p1) {
		return null;
	}
	
	private int m2(int p1) {
		return 0;
	}
	
	Object m1(Object p1, Object p2) {
		return null;
	}
}

class B1b extends B1a implements B1c {

	Object m4(Object p1, int p2) {
		return null;
	}
	
	int m3(int p1) {
		return null;
	}
	
	int m2(int p1) {
		return 0;
	}
	
	Object m1(Object p1, Object p2) {
		return null;
	}
}

