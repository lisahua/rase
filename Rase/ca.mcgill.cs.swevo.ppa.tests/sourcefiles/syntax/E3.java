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

public class E3 extends E3Super {
	public void main() {
		a = "";
		E3a.b = "";
		E3z.E3y.E3b.E3c.E3d = "";
		E3z.E3y.E3b = "";
		b1 = new E3Anon() {
			public void main() {
				c1 = "";
			}
		}
		
		b2 = new p2.E3Anon2() {
			public void main() {
				c2 = "";
			}
		}
		
		String d = "";
		d.toto = "";
		
		m7().m8(2);
		
		Animal a = null;
		a.toto2 = 2;
		
		super.m10();
	}
}