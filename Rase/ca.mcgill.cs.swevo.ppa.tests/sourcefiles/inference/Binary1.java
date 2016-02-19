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

public class Binary1 extends Binary1Super {
	
	
	public int doThat() {
		int i = 0;
		boolean b = true;
		long l = 2L;
		short s = 3;
		double d = 2.2;
		float f = 3.3f;
		
		f10 = i - f2;
		b = b & f3;
		f11 = f4 * d;
		f12 = s <= f5;
		f13 = x.y.z.f6 != "a";
		f14 = f7 & f8;
		f15 = f9 - f8;
		f16 = f15 * f17;
		int i = f8;
	}
	
	
}