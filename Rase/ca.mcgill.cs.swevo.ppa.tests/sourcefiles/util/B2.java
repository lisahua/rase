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

public class B2 {

	public void main() {
		Runnable r1 = new Runnable() {
			public void run() {
				
			}
		};
		
		Object r2 = new B2c() {
			
		};
		
		Runnable r3 = new java.lang.Runnable() {
			public void run() {
				
			}
		};
	}

	class B2a {
		
	}
	
}

public class B2b extends B2z {
	public void main() {
		Runnable r1 = new Runnable() {
			public void run() {
				
			}
		};
		
		Object r2 = new B2d() {
			
		};
	}

	class B2e {
		
	}
	
	class B2f extends B2g {
		
	}
}

public class B2h implements B2i {
	
	class B2j {
		
	}
	
}