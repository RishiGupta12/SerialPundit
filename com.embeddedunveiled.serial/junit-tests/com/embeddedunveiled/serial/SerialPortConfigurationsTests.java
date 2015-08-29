package com.embeddedunveiled.serial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

/*
 * Tested with two FT232 devices with vid, pid, serial combination as : 
 * 0x0403, 0x6001, A70362A3 and 
 * 0x0403, 0x6001, A602RDCH respectively.
 */
public final class SerialPortConfigurationsTests {

	static SerialComManager scm;
	static int osType;
	static String PORT1;
	static String PORT2;
	static Long handle1;
	static Long handle2;

	@BeforeClass
	public static void startup() throws Exception {
		scm = new SerialComManager();
		osType = scm.getOSType();
	}

	@AfterClass
	public static void shutdown() throws Exception {
	}

	@Before
	public void openPort() throws Exception {
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
		handle1 = scm.openComPort("/dev/pts/1", true, true, true);
		handle2 = scm.openComPort("/dev/pts/3", true, true, true);
	}

	@After
	public void validataDataAndclosePort() throws Exception {
		assertTrue(scm.writeBytes(handle1, "testing".getBytes(), 0));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		String dataRead = scm.readString(handle2);
		assertNotNull(dataRead);
		assertEquals(dataRead, "testing");
		scm.closeComPort(handle1);
		scm.closeComPort(handle2);
	}

	/* No flow control, 8N1 - with different baud rates */

	@Test(timeout=100)
	public void test8N10() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N150() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test8N175() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1110() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1134() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test8N1150() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1300() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test8N1600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N12400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N14800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N19600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N114400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N119200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test8N128800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N138400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N156000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N157600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1115200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1128000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1153600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1230400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1256000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1460800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1576000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1921600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11152000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N12000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N12500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N13000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N13500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N14000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	/* No flow control, 5N1 - with different baud rates */

	@Test(timeout=100)
	public void test5N10() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N150() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test5N175() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1110() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1134() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test5N1150() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1300() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test5N1600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N12400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N14800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N19600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N114400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N119200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test5N128800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N138400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N156000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N157600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1115200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1128000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1153600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1230400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1256000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1460800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1576000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1921600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11152000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N12000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N12500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N13000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N13500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N14000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	/* No flow control, 6N1 - with different baud rates */


	@Test(timeout=100)
	public void test6N10() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N150() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test6N175() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1110() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1134() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test6N1150() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1300() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test6N1600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N12400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N14800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N19600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N114400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N119200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test6N128800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N138400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N156000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N157600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1115200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1128000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1153600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1230400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1256000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1460800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1576000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1921600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11152000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N12000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N12500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N13000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N13500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N14000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	/* No flow control, 7N1 - with different baud rates */

	@Test(timeout=100)
	public void test7N10() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N150() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test7N175() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1110() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1134() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test7N1150() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1300() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test7N1600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N12400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N14800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N19600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N114400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N119200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test7N128800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N138400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N156000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N157600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1115200() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1128000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1153600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1230400() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1256000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1460800() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1576000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1921600() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11152000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N12000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N12500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N13000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N13500000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N14000000() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.NONE, 'x', 'x', false, false);
	}

	/* Hardware flow control, 8N1 - with different baud rates */

	@Test(timeout=100)
	public void test8N10H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N150H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test8N175H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1110H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1134H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test8N1150H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1300H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test8N1600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N12400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N14800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N19600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N114400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N119200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test8N128800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N138400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N156000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N157600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1115200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1128000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1153600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1230400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1256000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1460800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1576000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1921600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11152000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N12000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N12500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N13000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N13500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N14000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	/* Hardware flow control, 5N1 - with different baud rates */

	@Test(timeout=100)
	public void test5N10H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N150H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test5N175H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1110H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1134H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test5N1150H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1300H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test5N1600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N12400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N14800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N19600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N114400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N119200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N128800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N138400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N156000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N157600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1115200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1128000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1153600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1230400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1256000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1460800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1576000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1921600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11152000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N12000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N12500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N13000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N13500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N14000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	/* Hardware flow control, 6N1 - with different baud rates */

	@Test(timeout=100)
	public void test6N10H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N150H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test6N175H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1110H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1134H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test6N1150H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1300H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test6N1600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N12400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N14800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N19600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N114400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N119200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test6N128800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N138400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N156000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N157600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1115200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1128000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1153600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1230400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1256000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1460800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1576000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1921600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11152000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N12000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N12500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N13000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N13500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N14000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	/* Hardware flow control, 7N1 - with different baud rates */

	@Test(timeout=100)
	public void test7N10H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N150H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test7N175H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1110H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1134H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test7N1150H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1300H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test7N1600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N12400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N14800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N19600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N114400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N119200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test7N128800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N138400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N156000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N157600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1115200H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1128000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1153600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1230400H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1256000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1460800H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1576000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1921600H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11152000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N12000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N12500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N13000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N13500000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N14000000H() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	/* Software flow control,, 8N1 - with different baud rates */

	@Test(timeout=100)
	public void test8N10S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test8N175S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1110S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1134S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test8N1150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1300S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test8N1600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N12400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N14800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N19600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N114400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N119200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test8N128800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N138400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N156000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N157600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1115200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1128000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1153600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1230400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1256000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1460800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1576000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1921600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11152000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N12000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N12500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N13000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N13500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N14000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	/* Software flow control,, 5N1 - with different baud rates */

	@Test(timeout=100)
	public void test5N10S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test5N175S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1110S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1134S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test5N1150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1300S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test5N1600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N12400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N14800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N19600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N114400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N119200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test5N128800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N138400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N156000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N157600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1115200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1128000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1153600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1230400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1256000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1460800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1576000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1921600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11152000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N12000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N12500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N13000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N13500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N14000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	/* Software flow control,, 6N1 - with different baud rates */


	@Test(timeout=100)
	public void test6N10S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test6N175S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1110S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1134S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test6N1150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1300S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test6N1600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N12400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N14800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N19600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N114400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N119200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test6N128800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N138400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N156000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N157600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1115200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1128000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1153600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1230400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1256000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1460800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1576000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1921600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11152000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N12000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N12500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N13000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N13500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N14000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	/* Software flow control,, 7N1 - with different baud rates */

	@Test(timeout=100)
	public void test7N10S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test7N175S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1110S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1134S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test7N1150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1300S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test7N1600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N12400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N14800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N19600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N114400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N119200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test7N128800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N138400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N156000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N157600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1115200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1128000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1153600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1230400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1256000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1460800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1576000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1921600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11152000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N12000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N12500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N13000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N13500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N14000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
	}

	/* Software flow control,, 8N1 - with different baud rates */

	@Test(timeout=100)
	public void test8N10S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test8N175S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1110S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1134S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test8N1150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1300S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test8N1600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N12400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N14800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N19600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N114400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N119200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test8N128800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N138400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N156000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N157600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1115200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1128000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1153600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1230400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1256000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1460800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1576000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N1921600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11152000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N11500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N12000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N12500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N13000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N13500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test8N14000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	/* Software flow control,, 5N1 - with different baud rates */

	@Test(timeout=100)
	public void test5N10S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test5N175S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1110S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1134S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test5N1150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1300S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test5N1600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N12400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N14800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N19600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N114400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N119200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test5N128800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N138400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N156000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N157600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1115200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1128000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1153600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1230400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1256000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1460800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1576000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N1921600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11152000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N11500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N12000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N12500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N13000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N13500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test5N14000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_5, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	/* Software flow control,, 6N1 - with different baud rates */


	@Test(timeout=100)
	public void test6N10S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test6N175S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1110S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1134S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test6N1150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1300S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test6N1600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N12400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N14800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N19600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N114400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N119200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test6N128800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N138400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N156000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N157600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1115200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1128000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1153600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1230400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1256000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1460800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1576000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N1921600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11152000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N11500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N12000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N12500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N13000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N13500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test6N14000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_6, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	/* Software flow control,, 7N1 - with different baud rates */

	@Test(timeout=100)
	public void test7N10S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B0, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B50, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test7N175S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B75, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1110S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B110, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1134S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B134, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test7N1150S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B150, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1300S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B300, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}
	@Test(timeout=100)
	public void test7N1600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N12400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N14800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N19600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N114400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B14400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N119200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B19200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}


	@Test(timeout=100)
	public void test7N128800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B28800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N138400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B38400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N156000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B56000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N157600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B57600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1115200S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1128000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B128000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1153600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B153600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1230400S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B230400, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1256000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B256000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1460800S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B460800, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1576000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B576000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N1921600S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B921600, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11152000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1152000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N11500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B1500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N12000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N12500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N13000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N13500000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B3500000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}

	@Test(timeout=100)
	public void test7N14000000S() throws SerialComException {
		scm.configureComPortData(handle1, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle1, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
		scm.configureComPortData(handle2, DATABITS.DB_7, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B4000000, 0);
		scm.configureComPortControl(handle2, FLOWCONTROL.SOFTWARE, 'x', 'x', false, false);
	}	
}
