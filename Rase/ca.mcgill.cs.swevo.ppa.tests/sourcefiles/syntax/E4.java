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

public class E4 {
	
	public void main() {
		int i = 0;
		String s = "";
		E4a local1 = new E4a();
		
		local1.m1(i, i);
		m2(i,local1.getA());
		m2(local1.getE(), local1.getF());
		s.m3(i);
		m4(i,local1.getB());
		m5(local1.getC(),local1.getD());
		
	}
	
	public void m2(int p1, Object p2) {
		
	}
	
	public void m2(Object p1, Object p2) {
		
	}
	
	public void m4(Object p1, Object p2) {
		
	}
	
	public void m5(Object p1, Object p2) {
		
	}
	
	
	
}

class E4a extends E4aSuper {
	
	public void m1(int p1, int p2) {
		
	}
}