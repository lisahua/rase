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

public class G2 extends G2Super {
	
	public void main() {
		int i = 0;
		f1 = m1(i, f2);
		f3 = m2(f1, f4);
	}
	
	public int m1(int p1, int p2) {
		return 1;
	}
	
	public Object m1(Object p1, Object p2) {
		return null;
	}
	
	public int m2(int p1, int p2) {
		return 1;
	}
	
	public Object m2(Object p1, Object p2) {
		return null;
	}
	
}