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

public class Field1 extends Field1Super {
	
	public void main() {
		field1 = "";
		field1.toto = true;
		
		field1a.field1b.field1c.field1d = "";
		field1a.field1b.field1c = new Field1X();

		if (field1e.field1f instanceof Object) {
			
		}
		
		if (field1e.field1z instanceof Object) {
			
		}
		
		field1e = new Field1Y();
		
		field1g.field1h = "";
		field1g = new Field1Z();
	
		field1l.field1i.myField = new Object();
		field1l = new Field1W();
		field1j.field1k = new Object();
		
		
		field1m.myField2 = new Object();
		field1m = new Field1X();
		
	}
	
}

class Field1W extends Field1WSuper {
	Field1X field1i;
}

class Field1X extends Field1XSuper {
	Field1XX myField;
	Field1XX myField2;
}

class Field1Y extends Field1YSuper {
	Integer field1z;
}