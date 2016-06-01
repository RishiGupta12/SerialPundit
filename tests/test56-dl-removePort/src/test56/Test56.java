/*
 * This file is part of SerialPundit project and software.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package test56;

import com.embeddedunveiled.serial.ISerialComEventListener;
import com.embeddedunveiled.serial.ISerialComUSBHotPlugListener;
import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComLineEvent;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.ISerialComDataListener;

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
		} catch (SerialComException e) {
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

class portWatcher implements ISerialComUSBHotPlugListener {

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
			int osType = scm.getOSType();
			if(osType == SerialComManager.OS_LINUX) {
				PORT = "/dev/ttyUSB0";
				PORT1 = "/dev/ttyUSB1";
			}else if(osType == SerialComManager.OS_WINDOWS) {
				PORT = "COM51";
				PORT1 = "COM52";
			}else if(osType == SerialComManager.OS_MAC_OS_X) {
				PORT = "/dev/cu.usbserial-A70362A3";
				PORT1 = "/dev/cu.usbserial-A602RDCH";
			}else if(osType == SerialComManager.OS_SOLARIS) {
				PORT = null;
				PORT1 = null;
			}else{
			}

			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			dataListener = new Data();
			eventListener = new EventListener();
			portWatcher pw = new portWatcher();
			scm.registerDataListener(handle, dataListener);
			scm.registerLineEventListener(handle, eventListener);
			scm.registerUSBHotPlugEventListener(portWatcher, filterVID, filterPID, serialNumber);
			
			System.out.println("ready");
			
			// remove usb-uart physically from system and see onDataListenerError(int arg0) will be called
			// where recovery policy will come into action.
			while(true);

		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
