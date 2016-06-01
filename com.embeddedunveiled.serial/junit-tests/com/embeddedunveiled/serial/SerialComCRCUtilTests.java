/*
 * This file is part of SerialPundit project and software.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.embeddedunveiled.serial;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SerialComCRCUtilTests {

	private String data = "The root cause of suffering is our identification with the mind.";

	private SerialComCRCUtil crcutil = new SerialComCRCUtil();

	@Test
	public void testGetChecksumValue() {
		assertEquals((byte)0xA3, crcutil.getChecksumValue(data.getBytes(), 0, (data.length() - 1)));
	}

	@Test
	public void testGetLRCCheckSum() {
		assertEquals((byte)0x4B, crcutil.getLRCCheckSum(data.getBytes(), 0, (data.length() - 1)));
	}

	@Test
	public void testGetCRC8Dallas1WireValue() {
		assertEquals(0x4D, crcutil.getCRC8Dallas1WireValue(data.getBytes(), 0, (data.length() - 1)));
	}

	@Test
	public void testGetCRC16Value() {
		assertEquals(0xAB97, crcutil.getCRC16Value(data.getBytes(), 0, (data.length() - 1)));
	}

	@Test
	public void testGetCRC16CCITTValue() {
		assertEquals(0x138A, crcutil.getCRC16CCITTValue(data.getBytes(), 0, (data.length() - 1)));
	}

	@Test
	public void testGetCRC16DNPValue() {
		assertEquals(0x55BD, crcutil.getCRC16DNPValue(data.getBytes(), 0, (data.length() - 1)));
	}

	@Test
	public void testGetCRC16IBMValue() {
		assertEquals(0x84D7, crcutil.getCRC16IBMValue(data.getBytes(), 0, (data.length() - 1)));
	}

}
