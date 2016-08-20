/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package test85;

import com.serialpundit.core.SerialComException;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

class UnblockBlocked extends Test85 implements Runnable {
	@Override
	public void run() {
		try {
			Thread.sleep(1000); // make sure closed is called after read is blocked
			// if this is commented out read will be kept blocked forever
			scm.unblockBlockingIOOperation(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


public class Test85  {

	public static SerialComManager scm = null;
	public static SerialComPlatform scp = null;
	public static int osType = 0;
	public static int ret = 0;
	public static String PORT = null;
	public static String PORT1 = null;
	public static long handle = 0;
	public static long handle1 = 1;
	public static long context = 0;
	public static byte[] buffer = new byte[2*1024];
	private static Thread mThread = null;

	public static void main(String[] args) {

		try {
			scm = new SerialComManager();
			scp = new SerialComPlatform(new SerialComSystemProperty());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		osType = scp.getOSType();
		if(osType == SerialComPlatform.OS_LINUX) {
			PORT = "/dev/ttyUSB0";
			PORT1 = "/dev/ttyUSB1";
		}else if(osType == SerialComPlatform.OS_WINDOWS) {
			PORT = "COM51";
			PORT1 = "COM52";
		}else if(osType == SerialComPlatform.OS_MAC_OS_X) {
			PORT = "/dev/cu.usbserial-A70362A3";
			PORT1 = "/dev/cu.usbserial-A602RDCH";
		}else if(osType == SerialComPlatform.OS_SOLARIS) {
			PORT = null;
			PORT1 = null;
		}else{
		}

		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~ TEST 1 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \n");

		try {
			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			System.out.println("opened handle : " + handle);

			handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
			System.out.println("opened handle1 : " + handle1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			scm.writeBytes(handle, "TESTSTRING".getBytes(), 0);
			Thread.sleep(100);
			// read : 8469838483848273787100
			scm.readBytes(handle1, buffer, 0, 11, -1, null);
			System.out.println("\nread : " + buffer[0] + buffer[1] + buffer[2] + buffer[3] + buffer[4] + buffer[5] + buffer[6] + 
					buffer[7] + buffer[8] + buffer[9] + buffer[10] + buffer[11]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\nclose : " + scm.closeComPort(handle));
			System.out.println("close1 : " + scm.closeComPort(handle1));
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~ TEST 2 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \n");

		try {
			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			System.out.println("opened handle : " + handle);

			handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
			System.out.println("opened handle1 : " + handle1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			scm.writeBytes(handle, "TESTSTRING".getBytes(), 0);

			context = scm.createBlockingIOContext();

			Thread.sleep(1000);

			// read : 8469838483848273787100
			scm.readBytes(handle1, buffer, 0, 11, context, null);
			System.out.println("\nread : " + buffer[0] + buffer[1] + buffer[2] + buffer[3] + buffer[4] + buffer[5] + buffer[6] + 
					buffer[7] + buffer[8] + buffer[9] + buffer[10] + buffer[11]);
			scm.destroyBlockingIOContext(context);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			System.out.println("\nclose : " + scm.closeComPort(handle));
			System.out.println("close1 : " + scm.closeComPort(handle1));
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~ TEST 3 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \n");

		try {
			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			System.out.println("opened handle : " + handle);

			handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
			System.out.println("opened handle1 : " + handle1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			context = scm.createBlockingIOContext();

			mThread = new Thread(new UnblockBlocked());
			mThread.start();
			System.out.println("Proccedding to call read which will block because of no data !");

			// read : 8469838483848273787100, it will block
			scm.readBytes(handle1, buffer, 0, 11, context, null);
			System.out.println("\nread : " + buffer[0] + buffer[1] + buffer[2] + buffer[3] + buffer[4] + buffer[5] + buffer[6] + 
					buffer[7] + buffer[8] + buffer[9] + buffer[10] + buffer[11]);
		} catch (Exception e) {
			if(SerialComManager.EXP_UNBLOCKIO.equals(((SerialComException) e).getExceptionMsg())) {
				System.out.println("\nUnblocked ....");
				try {
					scm.destroyBlockingIOContext(context);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}else {
				e.printStackTrace();
			}
		}

		try {
			System.out.println("\nclose : " + scm.closeComPort(handle));
			System.out.println("close1 : " + scm.closeComPort(handle1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
