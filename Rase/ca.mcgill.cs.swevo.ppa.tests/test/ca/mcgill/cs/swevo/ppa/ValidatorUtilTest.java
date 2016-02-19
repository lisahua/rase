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
package ca.mcgill.cs.swevo.ppa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ValidatorUtilTest {

	@Test
	public void testValidateNull() {
		Object o1 = null;
		Object o2 = new Object();
		assertFalse(ValidatorUtil.validateNull(o1, "foo", false));
		assertTrue(ValidatorUtil.validateNull(o2, "foo", false));
		try {
			assertFalse(ValidatorUtil.validateNull(o1, "foo", true));
			fail("Should have thrown an exception.");
		} catch(IllegalArgumentException iae) {
			assertTrue(iae.getMessage().contains("foo"));
		}
	}
	
	@Test
	public void testValidateNull2() {
		Object[] o1 = null;
		Object[] o2 = {new Object(), null, new Object()};
		Object[] o3 = {new Object(), new Object()};
		assertFalse(ValidatorUtil.validateNull(o1, "foo", false));
		assertFalse(ValidatorUtil.validateNull(o2, "foo", false));
		assertTrue(ValidatorUtil.validateNull(o3, "foo", false));
		try {
			assertFalse(ValidatorUtil.validateNull(o1, "foo", true));
			fail("Should have thrown an exception.");
		} catch(IllegalArgumentException iae) {
			assertTrue(iae.getMessage().contains("foo"));
		}
	}
	
	@Test
	public void testValidateEmpty() {
		String s1 = null;
		String s2 = "  ";
		String s3 = "";
		String s4 = "a";
		assertFalse(ValidatorUtil.validateEmpty(s1, "foo", false));
		assertFalse(ValidatorUtil.validateEmpty(s2, "foo", false));
		assertFalse(ValidatorUtil.validateEmpty(s3, "foo", false));
		assertTrue(ValidatorUtil.validateEmpty(s4, "foo", false));
		
		try {
			assertFalse(ValidatorUtil.validateEmpty(s1, "foo", true));
			fail("Should have thrown an exception.");
		} catch(IllegalArgumentException iae) {
			assertTrue(iae.getMessage().contains("foo"));
		}
	}
	
}
