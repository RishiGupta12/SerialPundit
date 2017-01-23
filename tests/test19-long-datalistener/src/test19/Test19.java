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

package test19;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.ISerialComDataListener;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.FTPPROTO;
import com.serialpundit.serial.SerialComManager.FTPVAR;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

public class Test19 implements ISerialComDataListener {

	static int x = 0;

	public static void main(String[] args) {

		try {
			SerialComManager scm = new SerialComManager();

			String PORT = null;
			String PORT1 = null;
			SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());

			int osType = scp.getOSType();
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

			// receiver
			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

			Test19 dataListener = new Test19();
			System.out.println("registering data listener");
			scm.registerDataListener(handle, dataListener);
			System.out.println("registered data listner");

			// sender
			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			byte[] buffer = new byte[2056];

			scm.writeBytes(handle1, buffer, 0);
			Thread.sleep(2000); // wait for data to be displayed on console

			System.out.println("\n");
			x = 0;

			scm.writeBytes(handle1, buffer, 0);
			Thread.sleep(2000); // wait for data to be displayed on console

			scm.unregisterDataListener(handle, dataListener);
			scm.closeComPort(handle);
			scm.closeComPort(handle1);
			System.out.println("done");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDataListenerError(int arg0) {
		System.out.println("data error : " + arg0);
	}

	@Override
	public void onNewSerialDataAvailable(byte[] data) {
		x = x + data.length;
		System.out.println("data length till now : " + x);
	}
}