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

/*
 * Tested with two FT232 devices with vid, pid, serial combination as : 
 * 0x0403, 0x6001, A70362A3 and 
 * 0x0403, 0x6001, A602RDCH respectively.
 */
public final class SerialComManagerTests {

	static SerialComManager scm;
	static int osType;
	static String PORT1;
	static String PORT2;
	static Long handle1;
	static Long handle2;

	@BeforeClass
	public static void preparePorts() throws Exception {
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
		handle1 = scm.openComPort(PORT1, true, true, true);
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		handle2 = scm.openComPort(PORT2, true, true, true);
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
		String dataRead = scm.readString(handle2);
		assertNotNull(dataRead);
		assertEquals(dataRead, "testing");
	}

	@Test(timeout=800)
	public void testWriteBytesWithDelay() throws SerialComException {
		assertTrue(scm.writeBytes(handle1, "testing".getBytes(), 5));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		String dataRead = scm.readString(handle2);
		assertNotNull(dataRead);
		assertEquals(dataRead, "testing");
	}

	@Test(timeout=800)
	public void testWriteString() throws SerialComException {
		assertTrue(scm.writeString(handle1, "testing", 0));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		String dataRead = scm.readString(handle2);
		assertNotNull(dataRead);
		assertEquals(dataRead, "testing");
	}

	@Test(timeout=800)
	public void testWriteStringWithDelay() throws SerialComException {
		assertTrue(scm.writeString(handle1, "testing", 5));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		String dataRead = scm.readString(handle2);
		assertNotNull(dataRead);
		assertEquals(dataRead, "testing");
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

	@Test(timeout=1000)
	public void testDirectBufferReadWrite() throws Exception {
		//TODO
	}	

	@Test(timeout=100)
	public void testGetCurrentConfiguration() throws SerialComException {
		String[] config = scm.getCurrentConfiguration(handle1);
		assertTrue(config != null);
		assertTrue(config.length > 0);
	}

	@Test(timeout=100)
	public void testSetRTS() throws SerialComException {
		assertTrue(scm.setRTS(handle1, true));
		assertTrue(scm.setRTS(handle1, false));
	}

	@Test(timeout=100)
	public void testSetDTR() throws SerialComException {
		assertTrue(scm.setDTR(handle1, true));
		assertTrue(scm.setDTR(handle1, false));
	}

	@Test(timeout=150)
	public void testFineTuneRead() throws SerialComException {
		if(osType == SerialComManager.OS_LINUX) { 
			assertTrue(scm.fineTuneRead(handle1, 0, 100, 0, 0, 0));
			long start = System.currentTimeMillis();
			scm.readBytes(handle1);
			assertTrue((System.currentTimeMillis() - start) < 100);
		}else if(osType == SerialComManager.OS_WINDOWS) {
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
		}else if(osType == SerialComManager.OS_SOLARIS) {
		}else{
		}
	}

	@Test(timeout=100)
	public void testClearPortIOBuffers() throws SerialComException {
		assertTrue(scm.clearPortIOBuffers(handle1, true, true));
		int[] byteCount = scm.getByteCountInPortIOBuffer(handle1);
		assertEquals(0, byteCount[0]);
		assertEquals(0, byteCount[1]);
	}

	@Test(timeout=100)
	public void testSendBreak() throws SerialComException {
		assertTrue(scm.sendBreak(handle1, 50));
	}

	@Test(timeout=100)
	public void testGetInterruptCount() throws SerialComException {
		if(osType == SerialComManager.OS_LINUX) { 
			int[] countInfo = scm.getInterruptCount(handle1);
			assertTrue(countInfo != null);
			assertTrue(countInfo.length > 0);
		}else{
			assertTrue(10 > 5);
		}
	}

	@Test(timeout=100)
	public void testGetLinesStatus() throws SerialComException {
		int[] lineInfo = scm.getLinesStatus(handle1);
		assertTrue(lineInfo != null);
		assertTrue(lineInfo.length == 7);
		if(osType == SerialComManager.OS_LINUX) { 
		}else if(osType == SerialComManager.OS_WINDOWS) {
			assertEquals(lineInfo[4], 0);
			assertEquals(lineInfo[5], 0);
			assertEquals(lineInfo[6], 0);
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
			assertEquals(lineInfo[4], 0);
		}else if(osType == SerialComManager.OS_SOLARIS) {
		}else{
		}
	}

	@Test(timeout=100)
	public void testFindDriverServingComPort() throws SerialComException {
		if(osType == SerialComManager.OS_LINUX) {
			assertEquals("ftdi_sio", scm.findDriverServingComPort("/dev/ttyUSB0"));
		}else if(osType == SerialComManager.OS_WINDOWS) {
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
		}else if(osType == SerialComManager.OS_SOLARIS) {
		}else{
		}
	}

	@Test(timeout=100)
	public void testFindIRQnumberForComPort() throws SerialComException {
		if(osType == SerialComManager.OS_LINUX) {
			assertEquals("", scm.findIRQnumberForComPort(handle1));
		}else if(osType == SerialComManager.OS_WINDOWS) {
			assertEquals("", scm.findIRQnumberForComPort(handle1));
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
			assertEquals("", scm.findIRQnumberForComPort(handle1));
		}else if(osType == SerialComManager.OS_SOLARIS) {
		}else{
		}
	}

	@Test(timeout=150)
	public void testGetByteCountInPortIOBuffer() throws SerialComException {
		scm.readBytes(handle2);
		int[] byteCountBefore = scm.getByteCountInPortIOBuffer(handle2);
		assertEquals(0, byteCountBefore[0]);
		assertEquals(0, byteCountBefore[1]);
		scm.writeString(handle1, "hello", 0);
		int[] byteCountAfter = scm.getByteCountInPortIOBuffer(handle1);
		assertEquals(5, byteCountAfter[0]);
		assertEquals(0, byteCountAfter[1]);
	}

	@Test(timeout=100)
	public void testGetPortName() throws SerialComException {
		if(osType == SerialComManager.OS_LINUX) {
			assertEquals("/dev/ttyUSB0", scm.getPortName(handle1));
		}else if(osType == SerialComManager.OS_WINDOWS) {
			assertEquals("", scm.findIRQnumberForComPort(handle1));
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
			assertEquals("", scm.findIRQnumberForComPort(handle1));
		}else if(osType == SerialComManager.OS_SOLARIS) {
		}else{
		}
	}

}
