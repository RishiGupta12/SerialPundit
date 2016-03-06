/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
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
