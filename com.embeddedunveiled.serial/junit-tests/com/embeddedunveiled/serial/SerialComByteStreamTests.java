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
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.SMODE;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

public final class SerialComByteStreamTests {

	static SerialComManager scm;
	static int osType;
	static String PORT1;
	static String PORT2;
	static Long handle1;
	static Long handle2;
	static SerialComInByteStream inStream;
	static SerialComOutByteStream outStream;

	@BeforeClass
	public static void openAndPrepareStream() throws Exception {
		scm = new SerialComManager();
		osType = scm.getOSType();
		if(osType == SerialComManager.OS_LINUX) { 
			PORT1 = "/dev/ttyUSB0";
			PORT2 = "/dev/ttyUSB1";
		}else if(osType == SerialComManager.OS_WINDOWS) {
			PORT1 = "COM51";
			PORT2 = "COM52";
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
			PORT1 = "/dev/cu.usbserial-A70362A3";
			PORT2 = "/dev/cu.usbserial-A602RDCH";
		}else if(osType == SerialComManager.OS_SOLARIS) {
			PORT1 = null;
			PORT2 = null;
		}else{

		}
		handle1 = scm.openComPort(PORT1, true, true, false);
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		handle2 = scm.openComPort(PORT2, true, true, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);

		inStream = scm.createInputByteStream(handle1, SMODE.NONBLOCKING);
		outStream = scm.createOutputByteStream(handle2, SMODE.NONBLOCKING);
	}

	@AfterClass
	public static void closeStreamAndSerialPorts() throws Exception {
		inStream.close();
		outStream.close();
		scm.closeComPort(handle1);
		scm.closeComPort(handle2);
	}

	@Test(timeout=100)
	public void testFlush() throws IOException {
		outStream.flush();
	}

	@Test(timeout=100)
	public void testAvailable() throws IOException {
		int numberOfBytesInInputBuffer = -1;
		assertTrue(scm.clearPortIOBuffers(handle1, true, true));
		numberOfBytesInInputBuffer = inStream.available();
		assertEquals(numberOfBytesInInputBuffer, 0);
		outStream.write(0x20);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		numberOfBytesInInputBuffer = inStream.available();
		assertEquals(numberOfBytesInInputBuffer, 1);
	}

	@Test(timeout=800)
	public void testReadWriteInt() throws IOException {
		scm.clearPortIOBuffers(handle1, true, true);
		outStream.write(0x20);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		assertEquals(0x20, (byte)inStream.read());
	}

	@Test(timeout=800)
	public void testReadWriteByteArray() throws IOException {
		scm.clearPortIOBuffers(handle1, true, true);
		outStream.write("test".getBytes());
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		byte[] dataRead = new byte[50];
		inStream.read(dataRead);
		assertEquals(dataRead[0], (byte)'t');
		assertEquals(dataRead[1], (byte)'e');
		assertEquals(dataRead[2], (byte)'s');
		assertEquals(dataRead[3], (byte)'t');
	}

	@Test(timeout=800)
	public void testReadWriteByteArrayWithOffset() throws IOException {
		byte[] writeBuffer = "test_string".getBytes();
		byte[] readBuffer = new byte[50];
		scm.clearPortIOBuffers(handle1, true, true);
		outStream.write(writeBuffer, 1, 5);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		inStream.read(readBuffer, 0, 5);
		assertEquals(readBuffer[0], (byte)'e');
		assertEquals(readBuffer[0], (byte)'s');
		assertEquals(readBuffer[0], (byte)'t');
		assertEquals(readBuffer[0], (byte)'_');
		assertEquals(readBuffer[0], (byte)'s');
	}

	@Test(timeout=100)
	public void testMark() throws IOException {
		inStream.mark(0);
	}

	@Test(timeout=100)
	public void testMarkSupported() throws IOException {
		assertTrue(inStream.markSupported() == false);
	}

	@Test(timeout=100)
	public void testReset() throws IOException {
		inStream.reset();
	}

	@Test(timeout=100)
	public void testSkip() throws IOException {
		assertTrue(inStream.skip(0) == 0);
	}

}
