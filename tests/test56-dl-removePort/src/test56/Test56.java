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

package test56;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.serial.ISerialComEventListener;
import com.serialpundit.usb.ISerialComUSBHotPlugListener;
import com.serialpundit.usb.SerialComUSB;
import com.serialpundit.serial.SerialComException;
import com.serialpundit.serial.SerialComLineEvent;
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;
import com.serialpundit.serial.ISerialComDataListener;

class Data extends Test56 implements ISerialComDataListener {

	@Override
	public void onNewSerialDataAvailable(byte[] arg0) {
		System.out.println("Read from serial port : " + new String(arg0));
		System.out.println("data length : " + arg0.length );
	}

	@Override
	public void onDataListenerError(int arg0) {
		System.out.println("onDataListenerError called " + arg0);
		try {
			scm.unregisterDataListener(handle, dataListener);
			scm.unregisterLineEventListener(handle, eventListener);
			scm.closeComPort(handle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class EventListener implements ISerialComEventListener{
	@Override
	public void onNewSerialEvent(SerialComLineEvent lineEvent) {
		System.out.println("eventCTS : " + lineEvent.getCTS());
		System.out.println("eventDSR : " + lineEvent.getDSR());
	}
}

class PortWatcher implements ISerialComUSBHotPlugListener {

	@Override
	public void onUSBHotPlugEvent(int arg0, int arg1, int arg2, String arg3) {
		// TODO Auto-generated method stub
	}
}

public class Test56 {

	protected static long handle = 0;
	protected static Data dataListener = null;
	protected static EventListener eventListener = null;
	protected static SerialComManager scm = null;

	public static void main(String[] args) {
		try {
			scm = new SerialComManager();

			String PORT = null;
			String PORT1 = null;
			SerialComPlatform scp = new SerialComPlatform(new SerialComSystemProperty());
			SerialComUSB scusb = new SerialComUSB(null, null);

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

			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);

			dataListener = new Data();
			eventListener = new EventListener();
			PortWatcher pw = new PortWatcher();

			scm.registerDataListener(handle, dataListener);
			scm.registerLineEventListener(handle, eventListener);
			scusb.registerUSBHotPlugEventListener(pw, 0x0403, 0x6001, null);

			System.out.println("ready");

			// remove usb-uart physically from system and see onDataListenerError(int arg0) will be called
			// where recovery policy will come into action.
			while(true);

		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
