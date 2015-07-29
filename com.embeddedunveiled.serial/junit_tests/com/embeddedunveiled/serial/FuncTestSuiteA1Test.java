package com.embeddedunveiled.serial;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.usb.SerialComUSB;
import com.embeddedunveiled.serial.usb.SerialComUSBdevice;

import static org.junit.Assert.*;

public final class FuncTestSuiteA1Test {

	static SerialComManager scm;	
	static String PORT1;
	static String PORT2;
	static Long handle1;
	static Long handle2;

	@BeforeClass
	public static void preparePorts() throws Exception {
		try {
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
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		}
	}

	@AfterClass
	public static void closePorts() throws Exception {
		scm.closeComPort(handle1);
		scm.closeComPort(handle2);
	}

	@Test(timeout=100)
	public void getLibraryVersions() throws SerialComException {
		String version = scm.getLibraryVersions();
		assertNotNull("getLibraryVersions() : ", version);
	}

	@Test(timeout=100)
	public void getOSType() {
		int type = scm.getOSType();
		assertTrue(type != SerialComManager.OS_UNKNOWN);
	}

	@Test(timeout=100)
	public void getCPUArchitecture() {
		int arch = scm.getCPUArchitecture();
		assertTrue(arch != SerialComManager.ARCH_UNKNOWN);
	}

	@Test(timeout=100)
	public void listAvailableComPorts() throws SerialComException {
		String[] ports = scm.listAvailableComPorts();
		assertTrue(ports != null);
	}

	@Test(timeout=100)
	public void listUSBdevicesWithInfo() throws SerialComException {
		SerialComUSBdevice[] devices = scm.listUSBdevicesWithInfo(SerialComUSB.V_ALL);
		assertTrue(devices != null);
	}

	@Test(timeout=100)
	public void listComPortFromUSBAttributes() throws SerialComException {
		String[] ports = scm.listComPortFromUSBAttributes(0x0403, 0x6001, "A70362A3");
		assertTrue(ports != null);
	}

	@Test(timeout=100)
	public void isUSBDevConnected() throws SerialComException {
		assertTrue(scm.isUSBDevConnected(0x0403, 0x6001));
	}

	@Test(timeout=800)
	public void writeBytesA() throws SerialComException {
		assertTrue(scm.writeBytes(handle1, "testing".getBytes(), 0));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		assertNotNull(scm.readString(handle2));
	}

	@Test(timeout=800)
	public void writeStringA() throws SerialComException {
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
	public void writeSingleByteA() throws SerialComException {
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

}











































