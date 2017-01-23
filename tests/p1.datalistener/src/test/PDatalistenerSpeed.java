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

package test;

import java.util.concurrent.atomic.AtomicBoolean;

import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.ISerialComDataListener;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

class Reader implements ISerialComDataListener {

	int x = 0;

	@Override
	public void onDataListenerError(int arg0) {
		System.out.println("data error : " + arg0);
	}

	@Override
	public void onNewSerialDataAvailable(byte[] arg0) {
		System.out.println("data : " + ++x);
	}
}

class Writer implements Runnable {

	private final SerialComManager scm;
	private final long handle;

	AtomicBoolean ak;

	public  Writer(SerialComManager scm, long handle) {
		this.scm = scm;
		this.handle = handle;
	}

	@Override
	public void run() {
		while(true) {
			try {
				scm.writeSingleByte(handle, (byte)0x05);
			} catch (SerialComException e) {
				e.printStackTrace();
			}
		}
	}
}

public final class PDatalistenerSpeed {

	public static void main(String[] args) {
		try {
			SerialComManager scm = new SerialComManager();

			String PORT = "/dev/ttyUSB0";
			String PORT1 = "/dev/ttyUSB1";

			// instantiate class which is will implement ISerialComDataListener interface
			Reader dataListener = new Reader();

			// open and configure port that will listen data
			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

			System.out.println("registering data listener");
			scm.registerDataListener(handle, dataListener);    // register data listener for this port
			System.out.println("registered data listner");

			// open and configure port which will send data
			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

			Thread t = new Thread(new Writer(scm, handle1));
			t.start();

			Thread.sleep(1000000000);

			scm.unregisterDataListener(handle, dataListener);
			scm.closeComPort(handle);
			scm.closeComPort(handle1);
			System.out.println("done");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
