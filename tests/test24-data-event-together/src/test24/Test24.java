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

package test24;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.ISerialComDataListener;
import com.serialpundit.serial.ISerialComEventListener;
import com.serialpundit.serial.SerialComLineEvent;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

class DataListener0 implements ISerialComDataListener {
	@Override
	public void onNewSerialDataAvailable(byte[] arg0) {
		System.out.println("DCE GOT FROM DTE : " + new String(arg0));
	}

	@Override
	public void onDataListenerError(int arg0) {
	}
}

class DataListener1 implements ISerialComDataListener {
	@Override
	public void onNewSerialDataAvailable(byte[] arg0) {
		System.out.println("DTE GOT FROM DCE : " + new String(arg0));
	}

	@Override
	public void onDataListenerError(int arg0) {
	}
}

class EventListener0 implements ISerialComEventListener {
	@Override
	public void onNewSerialEvent(SerialComLineEvent lineEvent) {
		System.out.println("0eventCTS : " + lineEvent.getCTS());
		System.out.println("0eventDTR : " + lineEvent.getDSR());
	}
}

class EventListener1 implements ISerialComEventListener {
	@Override
	public void onNewSerialEvent(SerialComLineEvent lineEvent) {
		System.out.println("1eventCTS : " + lineEvent.getCTS());
		System.out.println("1eventDTR : " + lineEvent.getDSR());
	}
}

public class Test24 {

	public static void main(String[] args) {
		SerialComManager scm = null;
		String PORT = null;
		String PORT1 = null;
		int osType = 0;

		try {
			scm = new SerialComManager();
			SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());

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

			// Test 1
			try {
				EventListener0 eventListener0 = new EventListener0();
				EventListener0 eventListener1 = new EventListener0();
				DataListener0 dataListenerDTE0 = new DataListener0();
				DataListener1 dataListenerDCE1 = new DataListener1();

				// DTE terminal
				long handleDTE = scm.openComPort(PORT, true, true, true);
				scm.configureComPortData(handleDTE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
				scm.configureComPortControl(handleDTE, FLOWCONTROL.NONE, 'x', 'x', false, false);
				scm.registerDataListener(handleDTE, dataListenerDTE0);
				scm.registerLineEventListener(handleDTE, eventListener0);

				// DCE terminal
				long handleDCE = scm.openComPort(PORT1, true, true, true);
				scm.configureComPortData(handleDCE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
				scm.configureComPortControl(handleDCE, FLOWCONTROL.NONE, 'x', 'x', false, false);
				scm.registerDataListener(handleDCE, dataListenerDCE1);
				scm.registerLineEventListener(handleDCE, eventListener1);

				// Step 1
				scm.writeString(handleDTE, "str1", 0);
				Thread.sleep(50);
				scm.writeString(handleDTE, "str1", 0);
				Thread.sleep(50);
				scm.writeString(handleDCE, "str2", 0);
				Thread.sleep(50);
				scm.writeString(handleDCE, "str2", 0);

				Thread.sleep(200);

				scm.unregisterDataListener(handleDTE, dataListenerDTE0);
				scm.unregisterLineEventListener(handleDTE, eventListener0);
				scm.unregisterDataListener(handleDCE, dataListenerDCE1);
				scm.unregisterLineEventListener(handleDCE, eventListener1);

				scm.closeComPort(handleDTE);
				scm.closeComPort(handleDCE);
				System.out.println("done 1!");
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Test 2
			// register/unregister data/event in various permutation/combinations.
			try {
				long handle = scm.openComPort(PORT, true, true, true);
				scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
				scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
				long handle1 = scm.openComPort(PORT1, true, true, true);
				scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
				scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);

				DataListener0 dataListener0 = new DataListener0();
				DataListener1 dataListener1 = new DataListener1();
				EventListener0 eventListener0 = new EventListener0();
				EventListener1 eventListener1 = new EventListener1();

				scm.registerDataListener(handle, dataListener0);
				scm.unregisterDataListener(handle, dataListener0);
				scm.registerLineEventListener(handle, eventListener0);
				scm.unregisterLineEventListener(handle, eventListener0);

				scm.registerDataListener(handle, dataListener0);
				scm.registerLineEventListener(handle, eventListener0);
				scm.unregisterDataListener(handle, dataListener0);
				scm.unregisterLineEventListener(handle, eventListener0);

				scm.registerLineEventListener(handle, eventListener0);
				scm.registerDataListener(handle, dataListener0);
				scm.unregisterDataListener(handle, dataListener0);
				scm.unregisterLineEventListener(handle, eventListener0);

				scm.registerLineEventListener(handle, eventListener0);
				scm.registerDataListener(handle, dataListener0);
				scm.unregisterLineEventListener(handle, eventListener0);
				scm.unregisterDataListener(handle, dataListener0);

				scm.registerDataListener(handle, dataListener0);
				scm.unregisterDataListener(handle, dataListener0);
				scm.registerLineEventListener(handle1, eventListener0);
				scm.unregisterLineEventListener(handle1, eventListener0);

				scm.registerDataListener(handle, dataListener0);
				scm.registerLineEventListener(handle1, eventListener1);
				scm.unregisterDataListener(handle, dataListener0);
				scm.unregisterLineEventListener(handle1, eventListener1);

				scm.registerLineEventListener(handle1, eventListener0);
				scm.registerDataListener(handle, dataListener0);
				scm.unregisterDataListener(handle, dataListener0);
				scm.unregisterLineEventListener(handle1, eventListener0);

				scm.registerLineEventListener(handle1, eventListener1);
				scm.registerDataListener(handle, dataListener0);
				scm.unregisterLineEventListener(handle1, eventListener1);
				scm.unregisterDataListener(handle, dataListener0);
				System.out.println("done 2!");
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
