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

package test43;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;
import com.serialpundit.serial.ISerialComDataListener;
import com.serialpundit.serial.ISerialComEventListener;
import com.serialpundit.serial.SerialComLineEvent;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

class DataListener extends Test43 implements ISerialComDataListener {

	int y = 0;

	@Override
	public void onNewSerialDataAvailable(byte[] arg0) {

		System.out.println("length : " + arg0.length + " data : " + new String(arg0));

		y = y + arg0.length;
		if(y >= 1) {
			exit.set(true);
		}
	}
	@Override
	public void onDataListenerError(int arg0) {
		System.out.println("error : " + arg0);
	}
}

// whole cycle create instance of scm, open, configure, write, listener, close repeated many times.
public class Test43 {

	protected static AtomicBoolean exit = new AtomicBoolean(false);

	public static void main(String[] args) throws SecurityException, IOException {

		SerialComManager scm = new SerialComManager();
		DataListener dataListener = new DataListener();

		long x = 0;
		for(x=0; x<5000; x++) {
			System.out.println("\n" + "Iteration : " + x);
			try {
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

				long handle = scm.openComPort(PORT, true, true, false);
				scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
				scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
				long handle1 = scm.openComPort(PORT1, true, true, false);
				scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
				scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

				System.out.println("main thread register  : " + scm.registerDataListener(handle, dataListener));
				scm.writeString(handle1, "HELLO", 0);

				// wait till data listener has received all the data
				while(exit.get() == false) {
					scm.writeString(handle1, "L", 0);
				}
				exit.set(false);

				System.out.println("main thread unregister : " + scm.unregisterDataListener(handle, dataListener));
				System.out.println("close handle : " + scm.closeComPort(handle));
				System.out.println("close handle1 : " + scm.closeComPort(handle1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
