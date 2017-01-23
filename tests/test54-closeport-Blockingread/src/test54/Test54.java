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

package test54;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComInByteStream;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.SMODE;
import com.serialpundit.serial.SerialComManager.STOPBITS;

class ClosePort extends Test54 implements Runnable {
	@Override
	public void run() {
		try {
			Thread.sleep(500); // make sure closed is called after read is blocked
			System.out.println("closing stream...");
			in.close();
			System.out.println("closed stream.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

// test if port is closed port while read was blocked 
public class Test54 {

	private static Thread mThread = null;
	public static long handle = 0;
	public static SerialComManager scm = null;
	public static SerialComPlatform scp = null;
	public static SerialComInByteStream in = null;
	public static int osType;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();
			scp = new SerialComPlatform(new SerialComSystemProperty());

			String PORT = null;
			String PORT1 = null;
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

			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

			// test 1
			in = (SerialComInByteStream) scm.getIOStreamInstance(SerialComManager.InputStream, handle, SMODE.BLOCKING);
			mThread = new Thread(new ClosePort());
			mThread.start();
			System.out.println("1- created input stream, proccedding to call read which will block because of no data !");
			in.read();
			System.out.println("main thread, in.read() returned from blocked read !");

			Thread.sleep(1000); // let the previous stream be closed and removed from information object in SerialComManager class

			// test 2
			in = (SerialComInByteStream) scm.getIOStreamInstance(SerialComManager.InputStream, handle, SMODE.BLOCKING);
			mThread = new Thread(new ClosePort());
			mThread.start();
			System.out.println("\n1- created input stream, proccedding to call read which will block because of no data !");
			byte[] b = new byte[50];
			in.read(b);
			System.out.println("main thread, in.read(b) returned from blocked read !");

			scm.closeComPort(handle);
			System.out.println("closed serial port.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
