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

package com.embeddedunveiled.serial.vendor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.embeddedunveiled.serial.SerialComManager;

/* save the original settings after each tests to keep device in original state. */

public class SerialComSLabsCP210xManufacturingTests {

	static SerialComManager scm;
	static int osType;
	static SerialComSLabsCP210xManufacturing cpman = null;
	static String PORT;
	static long handle;
	static String vendorSuppliedLib;
	static String libpath;

	@BeforeClass
	public static void preparePorts() throws Exception {
		scm = new SerialComManager();
		osType = scm.getOSType();
		if(osType == SerialComManager.OS_LINUX) { 
			PORT = "/dev/ttyUSB0";
			libpath = "/home/r/ws-host-uart/tmp";
			vendorSuppliedLib = "libcp210xmanufacturing.so.1.0";
		}else if(osType == SerialComManager.OS_WINDOWS) {
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
		}else {
		}
		cpman = (SerialComSLabsCP210xManufacturing) scm.getVendorLibInstance(SerialComVendorLib.VLIB_SLABS_CP210XMANUFACTURING, 
				libpath, vendorSuppliedLib);
		handle = cpman.open(0);
	}

	@AfterClass
	public static void closePorts() throws Exception {
		cpman.close(handle);
	}

	@Test(timeout=100)
	public void testgetNumDevices() throws Exception {
		int ret = cpman.getNumDevices();
		assertTrue(ret >= 1);
	}

	@Test(timeout=100)
	public void testgetProductString() throws Exception {
		String ret = cpman.getProductString(0, SerialComSLabsCP210xManufacturing.CP210x_RETURN_SERIAL_NUMBER);
		assertTrue(ret != null);
		assertTrue(ret.length() > 0);
	}

	@Test(timeout=100)
	public void testgetProductString1() throws Exception {
		String ret = cpman.getProductString(0, SerialComSLabsCP210xManufacturing.CP210x_RETURN_DESCRIPTION);
		assertTrue(ret != null);
		assertTrue(ret.length() > 0);
	}

	@Test(timeout=100)
	public void testgetProductString2() throws Exception {
		String ret = cpman.getProductString(0, SerialComSLabsCP210xManufacturing.CP210x_RETURN_FULL_PATH);
		assertTrue(ret != null);
		assertTrue(ret.length() > 0);
	}

	@Test(timeout=100)
	public void testgetPartNumber() throws Exception {
		String ret = cpman.getPartNumber(handle);
		assertTrue(ret != null);
		assertTrue(ret.length() > 0);
	}

	//	driver will not be able to identify
	@Test(timeout=100)
	public void testsetVid() throws Exception {
		//		boolean ret = cpman.setVid(handle, 0x0222);
		//		assertEquals(ret, true);
	}
	@Test(timeout=100)
	public void testsetPid() throws Exception {
		//		boolean ret = cpman.setPid(handle, 0x0111);
		//		assertEquals(ret, true);
	}

	@Test(timeout=100)
	public void testsetProductString() throws Exception {
		// save original string
		String defaultVal = cpman.getProductString(0, SerialComSLabsCP210xManufacturing.CP210x_RETURN_DESCRIPTION);
		assertNotNull(defaultVal);
		assertTrue(defaultVal.length() > 0);

		// push test string
		assertEquals(true, cpman.setProductString(handle, "scm uart product"));

		// check test string pushed successfully
		String val = cpman.getProductString(0, SerialComSLabsCP210xManufacturing.CP210x_RETURN_DESCRIPTION);
		assertNotNull(val);
		assertEquals(val, "scm uart product");

		// restore original
		assertEquals(true, defaultVal);
	}

	@Test(timeout=100)
	public void testsetSerialNumber() throws Exception {
		String defaultVal = cpman.getProductString(0, SerialComSLabsCP210xManufacturing.CP210x_RETURN_SERIAL_NUMBER);
		assertNotNull(defaultVal);
		assertTrue(defaultVal.length() > 0);

		assertEquals(true, cpman.setSerialNumber(handle, "12121985"));

		String val = cpman.getProductString(0, SerialComSLabsCP210xManufacturing.CP210x_RETURN_SERIAL_NUMBER);
		assertNotNull(val);
		assertEquals(val, "12121985");

		assertEquals(true, defaultVal);
	}

	@Test(timeout=100)
	public void testsetInterfaceString() throws Exception {
		String defaultVal = cpman.getDeviceInterfaceString(handle, (byte)0);
		assertNotNull(defaultVal);
		assertTrue(defaultVal.length() > 0);

		assertEquals(true, cpman.setInterfaceString(handle, (byte)0, "iStr"));
		assertEquals("iStr", cpman.getDeviceInterfaceString(handle, (byte)0));

		assertEquals(true, cpman.setInterfaceString(handle, (byte)0, defaultVal));
		assertEquals(defaultVal, cpman.getDeviceInterfaceString(handle, (byte)0));
	}

	@Test(timeout=100)
	public void testsetSelfPower() throws Exception {
		boolean defaultVal = cpman.getSelfPower(handle);

		assertEquals(true, cpman.setSelfPower(handle, true));
		assertEquals(true, cpman.getSelfPower(handle));

		assertEquals(true, cpman.setSelfPower(handle, defaultVal));
		assertEquals(defaultVal, cpman.getSelfPower(handle));
	}

	@Test(timeout=100)
	public void testsetMaxPower() throws Exception {
		byte defaultVal = cpman.getMaxPower(handle);

		assertEquals(true, cpman.setMaxPower(handle, (byte)80));
		assertEquals(80, cpman.getMaxPower(handle));

		assertEquals(true, cpman.setMaxPower(handle, defaultVal));
		assertEquals(defaultVal, cpman.getMaxPower(handle));
	}

	@Test(timeout=100)
	public void testsetFlushBufferConfig() throws Exception {
		int defaultVal = cpman.getFlushBufferConfig(handle);

		assertEquals(true, cpman.setFlushBufferConfig(handle, SerialComSLabsCP210xManufacturing.FC_OPEN_TX));
		assertEquals(SerialComSLabsCP210xManufacturing.FC_OPEN_TX, cpman.getFlushBufferConfig(handle));

		assertEquals(true, cpman.setFlushBufferConfig(handle, SerialComSLabsCP210xManufacturing.FC_OPEN_RX));
		assertEquals(SerialComSLabsCP210xManufacturing.FC_OPEN_RX, cpman.getFlushBufferConfig(handle));

		assertEquals(true, cpman.setFlushBufferConfig(handle, SerialComSLabsCP210xManufacturing.FC_OPEN_TX | 
				SerialComSLabsCP210xManufacturing.FC_OPEN_RX));
		assertEquals((SerialComSLabsCP210xManufacturing.FC_OPEN_TX | SerialComSLabsCP210xManufacturing.FC_OPEN_RX), 
				cpman.getFlushBufferConfig(handle));

		assertEquals(true, cpman.setFlushBufferConfig(handle, SerialComSLabsCP210xManufacturing.FC_CLOSE_TX));
		assertEquals(SerialComSLabsCP210xManufacturing.FC_CLOSE_TX, cpman.getFlushBufferConfig(handle));

		assertEquals(true, cpman.setFlushBufferConfig(handle, SerialComSLabsCP210xManufacturing.FC_CLOSE_RX));
		assertEquals(SerialComSLabsCP210xManufacturing.FC_CLOSE_RX, cpman.getFlushBufferConfig(handle));

		assertEquals(true, cpman.setFlushBufferConfig(handle, SerialComSLabsCP210xManufacturing.FC_CLOSE_RX | 
				SerialComSLabsCP210xManufacturing.FC_CLOSE_TX));
		assertEquals((SerialComSLabsCP210xManufacturing.FC_CLOSE_RX | SerialComSLabsCP210xManufacturing.FC_CLOSE_TX), 
				cpman.getFlushBufferConfig(handle));

		assertEquals(true, cpman.setFlushBufferConfig(handle, (SerialComSLabsCP210xManufacturing.FC_CLOSE_RX | 
				SerialComSLabsCP210xManufacturing.FC_CLOSE_TX | SerialComSLabsCP210xManufacturing.FC_OPEN_TX | 
				SerialComSLabsCP210xManufacturing.FC_OPEN_RX)));
		assertEquals(cpman.getFlushBufferConfig(handle), (SerialComSLabsCP210xManufacturing.FC_CLOSE_RX | 
				SerialComSLabsCP210xManufacturing.FC_CLOSE_TX | SerialComSLabsCP210xManufacturing.FC_OPEN_TX | 
				SerialComSLabsCP210xManufacturing.FC_OPEN_RX));

		assertEquals(true, cpman.setFlushBufferConfig(handle, defaultVal));
		assertEquals(defaultVal, cpman.getFlushBufferConfig(handle));
	}

	@Test(timeout=100)
	public void testsetDeviceMode() throws Exception {
		byte[] defaultVal = cpman.getDeviceMode(handle);

		assertEquals(true, cpman.setDeviceMode(handle, defaultVal[0], defaultVal[1]));
	}

	@Test(timeout=100)
	public void testsetDeviceVersion() throws Exception {
		int defaultVal = cpman.getDeviceVersion(handle);

		assertEquals(true, cpman.setDeviceVersion(handle, 0x300));
		assertEquals(0x300, cpman.getDeviceVersion(handle));

		assertEquals(true, cpman.setDeviceVersion(handle, defaultVal));
		assertEquals(defaultVal, cpman.getDeviceVersion(handle));
	}

	@Test(timeout=100)
	public void testsetBaudRateConfig() throws Exception {
		CP210XbaudConfigs[] config = cpman.getBaudRateConfig(handle);

		assertNotNull(config);
		assertTrue(config.length > 0);
	}

	@Test(timeout=100)
	public void testsetDualPortConfig() throws Exception {
		int[] dualportconfig = cpman.getDualPortConfig(handle);
		assertNotNull(dualportconfig);
		assertTrue(dualportconfig.length == 6);
	}

	@Test(timeout=100)
	public void testsetQuadPortConfig() throws Exception {
	}

	@Test(timeout=100)
	public void testsetLockValue() throws Exception {
		// 0x00 means unlocked.
		assertEquals(0x00, cpman.getLockValue(handle));
	}

	@Test(timeout=100)
	public void testgetDeviceVid() throws Exception {
		assertEquals(0x10c4, cpman.getDeviceVid(handle));
	}

	@Test(timeout=100)
	public void testgetDevicePid() throws Exception {
		assertEquals(0xea60, cpman.getDevicePid(handle));
	}

	@Test(timeout=100)
	public void testgetDeviceProductString() throws Exception {
		assertEquals("CP2102 USB to UART Bridge Controller", cpman.getDeviceProductString(handle));
	}

	@Test(timeout=100)
	public void testgetDeviceSerialNumber() throws Exception {
		assertEquals("0001", cpman.getDeviceSerialNumber(handle));
	}

	@Test(timeout=100)
	public void testreset() throws Exception {
		assertEquals(true, cpman.reset(handle));
	}

	@Test(timeout=100)
	public void testcreateHexFile() throws Exception {
	}

	@Test(timeout=100)
	public void testgetDeviceManufacturerString() throws Exception {
		String defaultVal = cpman.getDeviceManufacturerString(handle);
		assertNotNull(defaultVal);
		assertTrue(defaultVal.length() > 0);
	}
}
