package com.embeddedunveiled.serial;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.ENDIAN;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.NUMOFBYTES;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.usb.SerialComUSB;
import com.embeddedunveiled.serial.usb.SerialComUSBdevice;

import static org.junit.Assert.*;

public final class SerialComManagerTests {

	static SerialComManager scm;	
	static String PORT1;
	static String PORT2;
	static Long handle1;
	static Long handle2;

	@BeforeClass
	public static void preparePorts() throws Exception {
		scm = new SerialComManager();
		int osType = scm.getOSType();
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
	}

	@AfterClass
	public static void closePorts() throws Exception {
		scm.closeComPort(handle1);
		scm.closeComPort(handle2);
	}

	@Test(timeout=100)
	public void testGetLibraryVersions() throws SerialComException {
		String version = scm.getLibraryVersions();
		assertNotNull("getLibraryVersions() : ", version);
		assertEquals(version, "Java lib version: 1.0.4 \nNative lib version: 1.0.4");
	}

	@Test(timeout=100)
	public void testGetOSType() {
		int type = scm.getOSType();
		assertTrue(type != SerialComManager.OS_UNKNOWN);
	}

	@Test(timeout=100)
	public void testGetCPUArchitecture() {
		int arch = scm.getCPUArchitecture();
		assertTrue(arch != SerialComManager.ARCH_UNKNOWN);
	}

	@Test(timeout=100)
	public void testListAvailableComPorts() throws SerialComException {
		String[] ports = scm.listAvailableComPorts();
		assertTrue(ports != null);
		assertTrue(ports.length > 0);
	}

	@Test(timeout=100)
	public void testListUSBdevicesWithInfo() throws SerialComException {
		SerialComUSBdevice[] devices = scm.listUSBdevicesWithInfo(SerialComUSB.V_ALL);
		assertTrue(devices != null);
		assertTrue(devices.length > 0);
	}

	@Test(timeout=100)
	public void testListComPortFromUSBAttributes() throws SerialComException {
		String[] ports = scm.listComPortFromUSBAttributes(0x0403, 0x6001, "A70362A3");
		assertTrue(ports != null);
		assertTrue(ports.length > 0);
	}

	@Test(timeout=100)
	public void testIsUSBDevConnected() throws SerialComException {
		assertTrue(scm.isUSBDevConnected(0x0403, 0x6001));
	}

	@Test(timeout=800)
	public void testWriteBytes() throws SerialComException {
		assertTrue(scm.writeBytes(handle1, "testing".getBytes(), 0));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		assertNotNull(scm.readString(handle2));
	}
	
	@Test(timeout=800)
	public void testWriteBytesWithDelay() throws SerialComException {
		assertTrue(scm.writeBytes(handle1, "testing".getBytes(), 5));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		assertNotNull(scm.readString(handle2));
	}

	@Test(timeout=800)
	public void testWriteString() throws SerialComException {
		assertTrue(scm.writeString(handle1, "testing", 0));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		String data = scm.readString(handle2);
		assertNotNull(data);
		assertEquals(data, "testing");
	}
	
	@Test(timeout=800)
	public void testWriteStringWithDelay() throws SerialComException {
		assertTrue(scm.writeString(handle1, "testing", 5));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		String data = scm.readString(handle2);
		assertNotNull(data);
		assertEquals(data, "testing");
	}

	@Test(timeout=800)
	public void testWriteSingleByte() throws SerialComException {
		assertTrue(scm.writeSingleByte(handle1, (byte) 0x41));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		byte[] data = scm.readSingleByte(handle2);
		assertNotNull(data);
		assertTrue(data.length == 1);
		assertEquals(0x41, (byte)data[0]);
	}
	
	@Test(timeout=800)
	public void testWriteSingleIntLittleEndian2() throws SerialComException {
		assertTrue(scm.writeSingleInt(handle1, 0x41, 0, ENDIAN.E_LITTLE, NUMOFBYTES.NUM_2));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		byte[] data = scm.readBytes(handle2);
		assertNotNull(data);
		assertTrue(data.length == 2);
		assertEquals(0x41, (byte)data[0]);
		assertEquals(0x00, (byte)data[1]);
	}
	
	@Test(timeout=800)
	public void testWriteSingleIntLittleEndian4() throws SerialComException {
		assertTrue(scm.writeSingleInt(handle1, 0x380006BF, 0, ENDIAN.E_LITTLE, NUMOFBYTES.NUM_4));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		byte[] data = scm.readBytes(handle2);
		assertNotNull(data);
		assertTrue(data.length == 4);
		assertEquals(0xBF, (byte)data[0]);
		assertEquals(0x06, (byte)data[1]);
		assertEquals(0x00, (byte)data[2]);
		assertEquals(0x38, (byte)data[3]);
	}
	
	@Test(timeout=800)
	public void testWriteSingleIntBigEndian2() throws SerialComException {
		assertTrue(scm.writeSingleInt(handle1, 0x41, 0, ENDIAN.E_BIG, NUMOFBYTES.NUM_2));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		byte[] data = scm.readBytes(handle2);
		assertNotNull(data);
		assertTrue(data.length == 2);
		assertEquals(0x00, (byte)data[0]);
		assertEquals(0x41, (byte)data[1]);
	}
	
	@Test(timeout=800)
	public void testWriteSingleIntBigEndian4() throws SerialComException {
		assertTrue(scm.writeSingleInt(handle1, 0x380006BF, 0, ENDIAN.E_BIG, NUMOFBYTES.NUM_4));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		byte[] data = scm.readBytes(handle2);
		assertNotNull(data);
		assertTrue(data.length == 4);
		assertEquals(0x38, (byte)data[0]);
		assertEquals(0x00, (byte)data[1]);
		assertEquals(0x06, (byte)data[2]);
		assertEquals(0xBF, (byte)data[3]);
	}

}
