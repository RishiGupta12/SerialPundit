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

package com.embeddedunveiled.serial.vendor;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.embeddedunveiled.serial.SerialComManager;

public class SerialComSLabsCP210xRuntimeTests {

	static SerialComManager scm;
	static int osType;
	static SerialComSLabsCP210xRuntime cprt = null;
	static String PORT;
	static long handle;
	static String libToLoad;
	static String libpath;

	@BeforeClass
	public static void preparePorts() throws Exception {
		scm = new SerialComManager();
		osType = scm.getOSType();
		if(osType == SerialComManager.OS_LINUX) { 
			// no lib port for linux from slabs
		}else if(osType == SerialComManager.OS_WINDOWS) {
			PORT = "COM51";
			libpath = "/home/r/ws-host-uart/tmp";
			libToLoad = "CP210xRuntime.dll";
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
			// no lib port for mac from slabs
		}else {
		}
		cprt = (SerialComSLabsCP210xRuntime) scm.getVendorLibInstance(SerialComVendorLib.VLIB_SLABS_CP210XRUNTIME, 
				libpath, libToLoad);
		handle = scm.openComPort(PORT, true, true, false);
	}

	@AfterClass
	public static void closePorts() throws Exception {
		scm.closeComPort(handle);
	}

	@Test(timeout=100)
	public void testreadLatch() throws Exception {
		long latchVal = -1;
		latchVal = cprt.readLatch(handle);
		assertTrue((-1) != latchVal);
	}

	@Test(timeout=100)
	public void testwriteLatch() throws Exception {
		long latchVal = 0x05;
		boolean ret = cprt.writeLatch(handle, latchVal, latchVal);
		assertTrue(ret == true);
		assertTrue(latchVal == cprt.readLatch(handle));
	}

	@Test(timeout=100)
	public void testgetPartNumber() throws Exception {
		String ret = cprt.getPartNumber(handle);
		System.out.println("pnum : " + ret);
		assertTrue(ret != null);
		assertTrue(ret.length() > 0);
		assertTrue(ret.equals("CP2102"));
	}

	@Test(timeout=100)
	public void testgetDeviceProductString() throws Exception {
		String ret = cprt.getDeviceProductString(handle);
		System.out.println("pnum1 : " + ret);
		assertTrue(ret != null);
		assertTrue(ret.length() > 0);
		assertTrue(ret.equals("CP2102 USB to UART Bridge Controller"));
	}

	@Test(timeout=100)
	public void testgetDeviceSerialNumber() throws Exception {
		String ret = cprt.getDeviceSerialNumber(handle);
		System.out.println("pnum2 : " + ret);
		assertTrue(ret != null);
		assertTrue(ret.length() > 0);
		assertTrue(ret.equals("0001"));
	}

	@Test(timeout=100)
	public void testgetDeviceInterfaceString() throws Exception {
		String ret = cprt.getDeviceInterfaceString(handle);
		System.out.println("pnum3 : " + ret);
		assertTrue(ret != null);
		assertTrue(ret.length() > 0);
		assertTrue(ret.equals("CP2102 USB to UART Bridge Controller"));
	}
}
