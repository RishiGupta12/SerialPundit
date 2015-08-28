/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 *
 * The 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
 */

package com.embeddedunveiled.serial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SerialComGenericHidTests {

	static SerialComManager scm;
	static SerialComHID sch;
	static String HIDDEVPATH;
	static Long handle;

	@BeforeClass
	public static void preparePorts() throws Exception {
		scm = new SerialComManager();
		int osType = scm.getOSType();
		if(osType == SerialComManager.OS_LINUX) { 
			HIDDEVPATH = "/dev/hidraw0";
		}else if(osType == SerialComManager.OS_WINDOWS) {
			HIDDEVPATH = "/dev/hidraw0";
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
			HIDDEVPATH = "/dev/hidraw0";
		}else if(osType == SerialComManager.OS_SOLARIS) {
			HIDDEVPATH = "/dev/hidraw0";
		}else{

		}
		sch = scm.getSerialComHIDInstance(SerialComHID.HID_GENERIC, null, null);
		handle = sch.openHidDevice(HIDDEVPATH);
	}

	@AfterClass
	public static void closePorts() throws Exception {
		sch.closeHidDevice(handle);
	}

	@Test
	public void testListHIDdevicesWithInfo() throws Exception {
		SerialComHIDdevice[] devices = sch.listHIDdevicesWithInfo();
		assertTrue(devices != null);
		assertTrue(devices.length > 0);
	}

	@Test
	public void testFormatReportToHex() throws Exception {
		assertEquals("68 65 6C 6C 6F", sch.formatReportToHex("hello".getBytes()));
	}

	@Test
	public void testGetReportDescriptorSize() throws Exception {
		assertEquals(16, sch.getReportDescriptorSize(handle));
	}

	@Test
	public void testWriteOutputReport() throws Exception {
		byte[] report = { };
		assertEquals(16, sch.writeOutputReport(handle, (byte)0x00, report));
	}

	@Test
	public void testReadInputReport() throws Exception {
		byte[] reportBuffer = new byte[17];
		assertEquals(17, sch.readInputReport(handle, reportBuffer, reportBuffer.length));
	}

	@Test
	public void testReadInputReportWithTimeout() throws Exception {
		byte[] reportBuffer = new byte[17];
		assertEquals(17, sch.readInputReportWithTimeout(handle, reportBuffer, reportBuffer.length, 150));
	}

	@Test
	public void testSendFeatureReport() throws Exception {
		byte[] report = { };
		assertEquals(16, sch.sendFeatureReport(handle, (byte)0x00, report));
	}

	@Test
	public void testGetFeatureReport() throws Exception {
		byte[] reportBuffer = new byte[17];
		assertEquals(17, sch.readInputReport(handle, reportBuffer, reportBuffer.length));
	}

	@Test
	public void testGetManufacturerString() throws Exception {
		assertEquals("68 65 6C 6C 6F", sch.getManufacturerString(handle));
	}

	@Test
	public void testGetProductString() throws Exception {
		assertEquals("68 65 6C 6C 6F", sch.getProductString(handle));
	}

	@Test
	public void testGetSerialNumberString() throws Exception {
		assertEquals("68 65 6C 6C 6F", sch.getSerialNumberString(handle));
	}

	@Test
	public void testGetIndexedString() throws Exception {
		assertEquals("68 65 6C 6C 6F", sch.getIndexedString(handle, 2));
	}
}
